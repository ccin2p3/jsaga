/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.in2p3.jsaga.adaptor.batchssh.job

import fr.in2p3.jsaga.adaptor.ClientAdaptor
import fr.in2p3.jsaga.adaptor.base.defaults.Default
import fr.in2p3.jsaga.adaptor.base.usage.UAnd
import fr.in2p3.jsaga.adaptor.base.usage.UOptional
import fr.in2p3.jsaga.adaptor.security.SecurityCredential
import fr.in2p3.jsaga.adaptor.security.impl.SSHSecurityCredential
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential
import fr.in2p3.jsaga.adaptor.security.impl.UserPassStoreSecurityCredential
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.logging.Level
import java.util.logging.Logger
import org.ogf.saga.context.Context
import org.ogf.saga.error.AuthenticationFailedException
import org.ogf.saga.error.BadParameterException
import collection.JavaConversions._
import org.ogf.saga.error.NoSuccessException
import fr.in2p3.jsaga.adaptor.ssh2.data.SFTPDataAdaptor._
import ch.ethz.ssh2._

object BatchSSHAdaptorAbstract {
  val KNOWN_HOSTS = "KnownHosts"

  val STAGING_DIRECTORY = "StagingDirectory"
  
  val DEFAULT_STAGING_DIRECTORY = ".jsaga/var/adaptor/" + getType
  
  val BUFFER_SIZE = 32768
  /*val DIRECTORY_CREATION_MODE = "DirectoryCreationMode"
   val DEFAULT_DIRECTORY_CREATION_MODE = 0700.toString*/
  
  val scriptName = "job.pbs"
  
  implicit def connectionDecorator(connection: Connection) = new {
    def openSFTPClient =
      try new SFTPv3Client(connection)
    catch  {
      case e: IOException => throw new NoSuccessException("Unable to create SFTP connection", e)
    }
  }
  
  def withSFTP[T](connection: Connection, f: SFTPv3Client => T) = {
    val client = connection.openSFTPClient
    try f(client)
    finally client.close
  }

  def getType = "pbs-ssh"
  
  implicit def sftpClientDecorator(sftp: SFTPv3Client) = new {
    def tryCreateDir(dir: String, mode: Int = 0700) = 
      try {
        sftp.mkdir(dir, mode)
        true
      } catch {
        case e: SFTPException => 
          Logger.getLogger(classOf[BatchSSHAdaptorAbstract].getName).log(Level.FINE, "Directory creation failed", e)
          false
      }
    
    def tryRmDir(dir: String) = 
      try {
        sftp.rmdir(dir)
        true
      } catch {
        case e: SFTPException =>
          Logger.getLogger(classOf[BatchSSHAdaptorAbstract].getName).log(Level.FINE, "Directory remove failed", e)
          false
      }
    
    def tryRm(file: String) = 
      try {
        sftp.rm(file)
        true
      } catch {
        case e: SFTPException =>
          Logger.getLogger(classOf[BatchSSHAdaptorAbstract].getName).log(Level.FINE, "File remove failed", e)
          false
      }
    
  }
  
  
  def sendCommandCheckReturn(command: String, session: Session) = {
    val exitStatus = sendCommand(command, session)
    if (exitStatus != 0) {
      // try to read stderr
      try {
        val br = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStderr)))
        val msg = br.readLine.split("MSG=")(1)
        throw new BatchSSHCommandFailedException(command, exitStatus, msg)
      } catch {
        case e => throw new BatchSSHCommandFailedException(command, exitStatus, "Unable to get error message")
      }
    }
  }
  
  def sendCommand(command: String, session: Session): Int = {
    session.execCommand(command)

    // waiting for the qsub command to end
    session.waitForCondition(ChannelCondition.EXIT_STATUS, 0)

    session.getExitStatus
  }

  
}

import BatchSSHAdaptorAbstract._

abstract class BatchSSHAdaptorAbstract extends ClientAdaptor {

  def getType = BatchSSHAdaptorAbstract.getType
    
  def getDefaultPort = 22
    
  val knownHosts = new KnownHosts
 
  /**
   * the staging root directory, either absolute path defined in the
   * configuration or ".jsaga/var/adaptor/pbs-ssh" under the remote $HOME
   * directory The staging directory for the job is the concat of the root
   * directory and the unique job id. It is passed to PBS as: #PBS -v dir It
   * can be retrieved in job attributes (with qstat -f) in the variable called
   * PBS_O_WORKDIR
   */
  protected var m_stagingDirectory: String = _
  protected var connection: Connection = _
  protected var homeDir: String = _
  private var credential: SecurityCredential = _

  def getUsage =
    new UAnd(
      Array(
        new UOptional(KNOWN_HOSTS),
        new UOptional(STAGING_DIRECTORY)
        //new UOptional(DIRECTORY_CREATION_MODE)
      )
    )
    

  def getDefaults(attributes: java.util.Map[_, _]) =
    Array (
      new Default(KNOWN_HOSTS, Array(new File(System.getProperty("user.home") + "/.ssh/known_hosts"))),
      new Default(Context.USERID, System.getProperty("user.name")),
      new Default(STAGING_DIRECTORY, DEFAULT_STAGING_DIRECTORY)
      //new Default(DIRECTORY_CREATION_MODE, DEFAULT_DIRECTORY_CREATION_MODE)
    )

  def getSupportedSecurityCredentialClasses = 
    Array(classOf[UserPassSecurityCredential], classOf[UserPassStoreSecurityCredential], classOf[SSHSecurityCredential])
    

  def setSecurityCredential(credential: SecurityCredential) {
    this.credential = credential
  }

  override def connect(  
    userInfo: String,
    host: String,
    port: Int,
    basePath: String,
    attributes: java.util.Map[_, _]) = {

    try {
      // Creating a connection instance
      connection = new Connection(host, port)
      // Disable some INFO message from ganymed
      java.util.logging.Logger.getLogger("ch.ethz.ssh2").setLevel(java.util.logging.Level.WARNING)
      // Now connect
      connection.connect

      // Load known_hosts file into in-memory KnownHosts
      mapAsScalaMap(attributes).asInstanceOf[collection.mutable.Map[String, String]].get(KNOWN_HOSTS) match {
        case Some(knownHostPath) => 
          val knownHost = new File(knownHostPath)
          if (!knownHost.exists) throw new BadParameterException("Unable to find the selected known host file.")
          this.knownHosts.addHostkeys(knownHost)
        case None =>
      }

      credential match {
        case credential: UserPassSecurityCredential =>
          val userId = credential.getUserID
          val password = credential.getUserPass
          if(!connection.authenticateWithPassword(userId, password))
            throw new AuthenticationFailedException("Authentication failed.")
        case credential: UserPassStoreSecurityCredential =>
          try {
            val userId = credential.getUserID(host)
            val password = credential.getUserPass(host)
            if(connection.authenticateWithPassword(userId, password))
              throw new AuthenticationFailedException("Authentication failed.")
          } catch {
            case e => throw new AuthenticationFailedException(e);
          }
        case credential: SSHSecurityCredential =>
          val userId = credential.getUserID
          val passPhrase = credential.getUserPass
          val key = credential.getPrivateKeyFile

          if (!connection.authenticateWithPublicKey(userId, key, passPhrase))
            throw new AuthenticationFailedException("Authentication failed.")
        case _ => throw new AuthenticationFailedException("Invalid security instance.")
      }

      homeDir = withSFTP(connection, _.canonicalPath("."))
    } catch {
      case ex: IOException => throw new AuthenticationFailedException(ex)
    }
  }

  def disconnect { connection.close }
  

  protected def idDir = m_stagingDirectory + "/id"
  
  protected def uniqIdFile(nativeId: String) = idDir + "/" + nativeId + ".id"
  
  protected def uniqId(nativeId: String, sftp: SFTPv3Client) = {
    val stream = new StringBuilderOutputStream
    getToStream(sftp, uniqIdFile(nativeId), "", stream)
    stream.toString
  }
  
  protected def descriptionDir = m_stagingDirectory + "/description"
  
  protected def descriptionFile(nativeId: String) = descriptionDir + "/" + nativeId + ".xml"
  
  protected def description(nativeId: String, sftp: SFTPv3Client) = {
    val stream = new StringBuilderOutputStream
    getToStream(sftp, descriptionFile(nativeId), "", stream)
    stream.toString
  }
  
}

