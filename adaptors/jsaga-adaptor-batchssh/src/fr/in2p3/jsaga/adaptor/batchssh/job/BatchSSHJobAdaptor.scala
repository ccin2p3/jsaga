package fr.in2p3.jsaga.adaptor.batchssh.job

import ch.ethz.ssh2.Connection
import ch.ethz.ssh2.SFTPException
import ch.ethz.ssh2.SFTPv3Client
import ch.ethz.ssh2.SFTPv3FileHandle
import ch.ethz.ssh2.Session
import ch.ethz.ssh2.StreamGobbler
import fr.in2p3.jsaga.adaptor.base.usage.UAnd
import fr.in2p3.jsaga.adaptor.base.usage.UOptional
import fr.in2p3.jsaga.adaptor.job.BadResource
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor
import fr.in2p3.jsaga.adaptor.job.control.advanced.HoldableJobAdaptor
import fr.in2p3.jsaga.adaptor.job.control.advanced.SuspendableJobAdaptor


import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor
import fr.in2p3.jsaga.adaptor.security.impl.SSHSecurityCredential
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential
import fr.in2p3.jsaga.adaptor.security.impl.UserPassStoreSecurityCredential

import fr.in2p3.jsaga.adaptor.ssh2.data.SFTPDataAdaptor
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.util.ArrayList
import java.util.Map
import java.util.NoSuchElementException
import java.util.Scanner

import java.util.logging.Level
import java.util.logging.Logger

import BatchSSHAdaptorAbstract._

import collection.JavaConversions._

import org.ogf.saga.error._
import fr.in2p3.jsaga.adaptor.job.control.description.{JobDescriptionTranslatorJSDL, JobDescriptionTranslator, JobDescriptionTranslatorXSLT}
import xml.XML


class BatchSSHJobAdaptor extends BatchSSHAdaptorAbstract 
                            with JobControlAdaptor
                            with CleanableJobAdaptor
                            with SuspendableJobAdaptor
                            with HoldableJobAdaptor
                            with StagingJobAdaptorOnePhase {

  var host: String = _
  var port: Int = _
  
  def getDefaultJobMonitor = new BatchSSHMonitorAdaptor

  override def getJobDescriptionTranslator = new JobDescriptionTranslatorJSDL

  /*
   * override to dynamically mkdir staging directory
   */
  override def connect(
    userInfo: String,
    host: String,
    port: Int, 
    basePath: String,
    _attributes: java.util.Map[_, _]) = {

    this.host = host
    this.port = port
    
    val attributes = _attributes.asInstanceOf[java.util.Map[String, String]]
    super.connect(userInfo, host, port, basePath, _attributes)
    // create SFTP connection
    val sftp = connection.openSFTPClient
    
    try {        
      val stagingDir = 
        attributes.getOrElse(STAGING_DIRECTORY, DEFAULT_STAGING_DIRECTORY)
    
      val dir = 
        if(basePath.isEmpty) stagingDir
      else basePath + "/" + stagingDir
      
      dir.split("/").foldLeft("") {
        (base, d) => 
        if(!d.isEmpty) {
          val dir = if(base.isEmpty) d else base + "/" + d
          sftp.tryCreateDir(dir, 0700)
          dir
        } else base
      }
           
      m_stagingDirectory = 
        try sftp.canonicalPath(dir)
      catch {
        case e: IOException => throw new NoSuccessException("Unable to build staging root directory", e)
      }
      
      sftp.tryCreateDir(idDir, 0700)
      
    } finally sftp.close
  }
  
  
  
  def submit(jobDesc: String, checkMatch: Boolean, uniqId: String) = {
    
    val jobScript = new StringBuilder("#!/bin/bash\n")
    val x = XML.loadString(jobDesc)

    val application = x \ "JobDescription" \ "Application" \ "POSIXApplication"
    
    if(!(application \ "Output" isEmpty)) {
      jobScript append ("#PBS -o " + (application \ "Output" text))
      jobScript append "\n"
    }
    
    if(!(application \ "Error" isEmpty)) {     
      jobScript append ("#PBS -e " + (application \ "Error" text))
      jobScript append "\n"
    }
    
    if(!(application \ "WorkingDirectory" isEmpty)) {
      val workDir = (application \ "WorkingDirectory" text)
      jobScript append ("mkdir -p " + workDir + '\n')
      jobScript append ("cd " + workDir)
    } else jobScript append ("cd $PBS_O_WORKDIR")
    
    jobScript append "\n"

    val executable =  (application \ "Executable" text) + " " + ((application \ "Argument" map(_.text)).mkString(" "))
    jobScript append executable
    
    jobScript append "\n"
    
    val stagingDir = stagingDirectoryFile(uniqId)
    val scriptFileName = stagingDir + "/" + scriptName

    val sftp = connection.openSFTPClient
    try {
      
      try {
        sftp.tryCreateDir(stagingDir, 0755)
        
        try {
          val script = sftp.createFile(scriptFileName)
          try sftp.write(script, 0, jobScript.toString.getBytes, 0, jobScript.length)
          finally sftp.closeFile(script)
        } catch {
          case e: IOException => throw new NoSuccessException("Unable to send script via SFTP", e)
        }
      } catch {
        case e =>
          sftp.tryRmDir(stagingDir)
          throw e
      }

      sftp.tryCreateDir(idDir, 0755)
      sftp.tryCreateDir(descriptionDir, 0755)
      
      try {
        val session = connection.openSession
        try {
          sendCommand("cd " + stagingDir + " ; qsub " + scriptFileName, session)
          // Retrieving the standard output
          val stdout = new StreamGobbler(session.getStdout)
          val br = new BufferedReader(new InputStreamReader(stdout))
          val jobId = try br.readLine finally br.close
          if (jobId == null) throw new IOException("qsub did not return a JobID")
          
          try {
            val idFile = sftp.createFile(uniqIdFile(jobId))
            try sftp.write(idFile, 0, uniqId.getBytes, 0, uniqId.length)
            finally sftp.closeFile(idFile)
  
            val descFile = sftp.createFile(descriptionFile(jobId))
            try sftp.write(descFile, 0, jobDesc.getBytes, 0, jobDesc.length)
            finally sftp.closeFile(descFile)
          } catch {
            case e => 
              clean(jobId)
              throw e
          }

          jobId
        } finally session.close
      } catch {
        case ex: IOException => throw new NoSuccessException("Unable to submit job", ex)
        case e: BatchSSHCommandFailedException =>
          if (e.isErrorTypeOfBadResource) throw new BadResource("Error in Job description", e)
          throw new NoSuccessException("Unable to submit job", e)
        case e => throw e
      } 
    } finally  sftp.close
    
  }

  def clean(nativeJobId: String) {
    val sftp = connection.openSFTPClient
    try {
      sftp.tryRm(descriptionFile(nativeJobId))
      sftp.tryRm(uniqIdFile(nativeJobId))
      //sftp.tryRmDir(stagingDirectoryFile(uniqId(nativeJobId, sftp)))
    } finally sftp.close
  }

  def cancel(nativeJobId: String) {
    val session = connection.openSession

    try sendCommand("qdel " + nativeJobId, session)
    catch {
      case ex: IOException => throw new NoSuccessException("Unable to cancel job", ex);
      case e: BatchSSHCommandFailedException => throw new NoSuccessException("Unable to cancel job", e);
    } finally session.close
  }

  def suspend(nativeJobId: String) = {
    val session = connection.openSession

    try {
      sendCommand("qhold " + nativeJobId, session)
      true
    } catch {
      case ex: IOException => throw new NoSuccessException("Unable to suspend/hold job", ex);
      case ex: BatchSSHCommandFailedException => 
        if (ex.getErrno == BatchSSHCommandFailedException.PBS_QHOLD_E_JOB_INVALID_STATE) false
        else throw new NoSuccessException("Unable to suspend/hold job", ex)
    } finally session.close
  }

  def resume(nativeJobId: String) = {
    val session = connection.openSession
    
    try {
      sendCommand("qrls " + nativeJobId, session)
      true
    } catch {
      case ex: IOException => throw new NoSuccessException("Unable to resume/release job", ex)
      case ex: BatchSSHCommandFailedException =>
        if (ex.getErrno == BatchSSHCommandFailedException.PBS_QHOLD_E_JOB_INVALID_STATE) false
        else throw new NoSuccessException("Unable to resume/release job", ex)
    } finally session.close
  }

  def hold(nativeJobId: String) = suspend(nativeJobId)

  def release(nativeJobId: String) = resume(nativeJobId)

  
  def stagingDirectoryFile(uniqId: String) = m_stagingDirectory + "/" + uniqId

  override def getStagingDirectory(nativeJobDescription: String, uniqId: String): String =  "sftp2://" + host + ":" + port + stagingDirectoryFile(uniqId)
  
  override def getInputStagingTransfer(nativeJobDescription: String, uniqId: String): Array[StagingTransfer] = {  
    val x = XML.loadString(nativeJobDescription)
    
    val url = "sftp2://" + host + ":" + port + stagingDirectoryFile(uniqId) + "/"

    val dataStaging = x \ "JobDescription" \ "DataStaging"
    dataStaging.filterNot (d => d \ "Source" isEmpty ).map {
      d => 
      new StagingTransfer(
        (d \ "Source" \ "URI"  text),
        url + (d \ "FileName" text),
        false
      )
    }.toArray
  } 
  
  
  def getOutputStagingTransfer(nativeJobDescription: String, uniqId: String): Array[StagingTransfer] = {
    val x = XML.loadString(nativeJobDescription)

    val url = "sftp2://" + host + ":" + port + stagingDirectoryFile(uniqId) + "/"
    
    val dataStaging = x \ "JobDescription" \ "DataStaging"
    dataStaging.filterNot (d => d \ "Target" isEmpty ).map {
      d => 
      new StagingTransfer(
        url + (d \ "FileName" text),
        (d \ "Target" \ "URI" text),
        false
      )
    }.toArray
  } 
  

  override def getStagingDirectory(nativeJobId: String):  String = {
    val sftp = connection.openSFTPClient
    try "sftp2://" + host + ":" + port + stagingDirectoryFile(uniqId(nativeJobId, sftp))
    finally sftp.close
  }
  
  override def getInputStagingTransfer(nativeJobId: String): Array[StagingTransfer] = {
    val sftp = connection.openSFTPClient
    try { 
      val desc = description(nativeJobId, sftp)
      getInputStagingTransfer(desc, uniqId(nativeJobId, sftp))
    } finally sftp.close
  }
    
  override def getOutputStagingTransfer(nativeJobId: String): Array[StagingTransfer] = {
    val sftp = connection.openSFTPClient
    try { 
      val desc = description(nativeJobId, sftp)
      getOutputStagingTransfer(desc, uniqId(nativeJobId, sftp))
    } finally sftp.close
  }
    
}
