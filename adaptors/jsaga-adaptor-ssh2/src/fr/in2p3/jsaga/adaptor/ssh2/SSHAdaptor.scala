/*
 * Copyright (C) 2011 reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.in2p3.jsaga.adaptor.ssh2

import ch.ethz.ssh2.Connection
import ch.ethz.ssh2.KnownHosts
import fr.in2p3.jsaga.adaptor.ClientAdaptor
import fr.in2p3.jsaga.adaptor.base.defaults.Default
import fr.in2p3.jsaga.adaptor.base.usage.UAnd
import fr.in2p3.jsaga.adaptor.base.usage.UDuration
import fr.in2p3.jsaga.adaptor.base.usage.UOptional
import fr.in2p3.jsaga.adaptor.base.usage.Usage
import fr.in2p3.jsaga.adaptor.security.SecurityCredential
import fr.in2p3.jsaga.adaptor.security.impl.SSHSecurityCredential
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential
import fr.in2p3.jsaga.adaptor.security.impl.UserPassStoreSecurityCredential
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import org.ogf.saga.error.AuthenticationFailedException
import org.ogf.saga.error.BadParameterException
import collection.JavaConversions._
import scala.actors.threadpool.locks.ReentrantReadWriteLock
import scala.collection.mutable.ConcurrentMap
import scala.collection.mutable.HashMap

object SSHAdaptor {
  val COMPRESSION_LEVEL = "CompressionLevel"
  val KNOWN_HOSTS = "KnownHosts"
  val IGNORE_KNOWN_HOSTS = "IgnoreKnownHosts"
  
  val connectionKeepAlive = UDuration.toInt("PT2M") * 1000
  val connectionCache = new ConnectionCache(connectionKeepAlive)
}

abstract class SSHAdaptor extends ClientAdaptor { adaptor =>
  
  Logger.getLogger("ch.ethz.ssh2").setLevel(Level.INFO)

  import SSHAdaptor._
  
  private var credential: SecurityCredential = _
  private var knownHosts: KnownHosts = _
  
  var user: String = _
  var host: String = _
  var port: Int = _
  
  def withConnection[T](f: Connection => T) = {
    val connection = connectionCache.cached(this)
    try f(connection)
    finally connectionCache.release(this)
  }
  
  def openConnection = {
    val c = new Connection(host, port)
    c.connect
    authenticate(c)
    c
  }
  
  private def authenticate(connection: Connection) = 
    try{
      credential match {
        case credential: UserPassSecurityCredential =>
          user = credential.getUserID
          val password = credential.getUserPass
          if(!connection.authenticateWithPassword(user, password))
            throw new AuthenticationFailedException("Authentication failed.")
        case credential: UserPassStoreSecurityCredential =>
          try {
            val password = credential.getUserPass(host)
            if(connection.authenticateWithPassword(user, password))
              throw new AuthenticationFailedException("Authentication failed.")
          } catch {
            case e => throw new AuthenticationFailedException(e);
          }
        case credential: SSHSecurityCredential =>
          val passPhrase = credential.getUserPass
          val key = credential.getPrivateKeyFile

          if (!connection.authenticateWithPublicKey(user, key, passPhrase))
            throw new AuthenticationFailedException("Authentication failed.")
        case _ => throw new AuthenticationFailedException("Invalid security instance.")
      }

      //homeDir = withSFTP(connection, _.canonicalPath("."))
    } catch {
      case ex: IOException => throw new AuthenticationFailedException(ex)
    } 
  
	
  override def getSupportedSecurityCredentialClasses = Array(classOf[UserPassSecurityCredential], classOf[UserPassStoreSecurityCredential], classOf[SSHSecurityCredential])
   
  override def setSecurityCredential(credential: SecurityCredential) = this.credential = credential;

  override def getDefaultPort = 22
 	
  override def getUsage = new UAnd(Array[Usage](
      new UOptional(KNOWN_HOSTS),
      new UOptional(IGNORE_KNOWN_HOSTS),
      new UOptional(COMPRESSION_LEVEL)))


  override def getDefaults(map: java.util.Map[_,_]) = 
    Array[Default](
      new Default(KNOWN_HOSTS, Array[File](new File(System.getProperty("user.home")+"/.ssh/known_hosts"))), 
      new Default(IGNORE_KNOWN_HOSTS, "false")
    )


  override def connect(userInfo: String, host: String, port: Int, basePath: String, attributes: java.util.Map[_, _]) = {
    this.host = host
    this.port = port
    
    this.knownHosts = new KnownHosts
    
    val attributesMap = mapAsScalaMap(attributes).asInstanceOf[collection.mutable.Map[String, String]]
    
    // Load known_hosts file into in-memory KnownHosts
    attributesMap.get(KNOWN_HOSTS) match {
      case Some(knownHostPath) => 
        val knownHost = new File(knownHostPath)
        if (!knownHost.exists) throw new BadParameterException("Unable to find the selected known host file.")
        this.knownHosts.addHostkeys(knownHost)
      case None =>
    }   
    
    user = credential.getUserID
  }

  override def disconnect = {}

}
