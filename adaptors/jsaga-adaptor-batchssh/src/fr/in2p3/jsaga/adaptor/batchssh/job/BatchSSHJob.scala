/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.in2p3.jsaga.adaptor.batchssh.job

import org.ogf.saga.error.NoSuccessException
import ch.ethz.ssh2.Session
import ch.ethz.ssh2.StreamGobbler
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import BatchSSHAdaptorAbstract._
import org.ogf.saga.task.State

object BatchSSHJob {
  val ATTR_EXIT_STATUS = "EXIT_STATUS"
  val ATTR_JOB_STATE = "JOB_STATE"
  val ATTR_CREATE_TIME = "CTIME"
  val ATTR_START_TIME = "START_TIME"
  val ATTR_END_TIME = "MTIME"
  val ATTR_EXEC_HOST = "EXEC_HOST"
  val ATTR_OUTPUT = "OUTPUT_PATH"
  val ATTR_ERROR = "ERROR_PATH"
  val ATTR_STAGEOUT = "STAGEOUT"
  val ATTR_SERVER = "SERVER"
  val ATTR_VARS = "VARIABLE_LIST"
  val ATTR_VAR_WORKDIR = "PBS_O_WORKDIR"
  
  def status(nativeId: String, session: Session) = {
    val command = "qstat -f -1 " + nativeId

    try {
      val ret = sendCommand(command, session)
      
      if(ret == 153) new BatchSSHJobStatus(nativeId, "C", ret)
      else {
        
        val br = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout)))
        try {
          val lines = Iterator.continually(br.readLine).takeWhile(_ != null).map(_.trim)

          val state = lines.filter(_.matches(".*=.*")).map {
            prop => 
            val splited = prop.split('=')
            splited(0).trim.toUpperCase -> splited(1).trim
          }.toMap.getOrElse(ATTR_JOB_STATE, throw throw new NoSuccessException("State not found in qstat output."))
          
          new BatchSSHJobStatus(nativeId, state ,ret)   
        } finally br.close
      }
    } catch {
      case e => throw new NoSuccessException("Unable to query job status", e)
    } finally session.close

  }
  
}
