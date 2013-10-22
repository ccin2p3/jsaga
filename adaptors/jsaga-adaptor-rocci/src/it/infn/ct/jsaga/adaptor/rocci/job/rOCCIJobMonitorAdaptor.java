/*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package it.infn.ct.jsaga.adaptor.rocci.job;

import it.infn.ct.jsaga.adaptor.rocci.rOCCIAdaptorCommon;

import fr.in2p3.jsaga.adaptor.job.control.manage.ListableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import fr.in2p3.jsaga.adaptor.security.impl.SSHSecurityCredential;
import fr.in2p3.jsaga.adaptor.orionssh.job.SSHJobMonitorAdaptor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import org.ogf.saga.error.*;

/* *********************************************
 * *** Istituto Nazionale di Fisica Nucleare ***
 * ***      Sezione di Catania (Italy)       ***
 * ***        http://www.ct.infn.it/         ***
 * *********************************************
 * File:    rOCCIJobControlAdaptor.java
 * Authors: Giuseppe LA ROCCA, Diego SCARDACI
 * Email:   <giuseppe.larocca, diego.scardaci>@ct.infn.it
 * Ver.:    1.0.3
 * Date:    27 September 2013
 * *********************************************/

public class rOCCIJobMonitorAdaptor extends rOCCIAdaptorCommon 
                                      implements QueryIndividualJob, 
                                                 ListableJobAdaptor, 
                                                 JobInfoAdaptor
{      
  
  private SSHJobMonitorAdaptor sshMonitorAdaptor = 
          new SSHJobMonitorAdaptor();
  
  private static final Logger log = 
          Logger.getLogger(rOCCIJobMonitorAdaptor.class);
  
  @Override
  public void connect(String userInfo, String host, int port, 
                      String basePath, Map attributes) 
              throws NotImplementedException, 
                     AuthenticationFailedException, 
                     AuthorizationFailedException, 
                     IncorrectURLException, 
                     BadParameterException, 
                     TimeoutException, 
                     NoSuccessException 
  {
      
    super.connect(userInfo, host, port, basePath, attributes);    
    
    	sshMonitorAdaptor.setSecurityCredential(credential.getSSHCredential());
  }
    
  @Override
  public String getType() {
    return "rocci";
  }
  
  public void setSSHHost(String host) {
      //this.sshHost = host;    
  }

  public JobStatus getStatus(String nativeJobId) 
                   throws TimeoutException, NoSuccessException 
  {        
    JobStatus result = null;
    String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, nativeJobId.indexOf("#"));
    String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
    
    try {                    
          sshMonitorAdaptor.connect(null, _publicIP, 22, null, new HashMap());
          result = sshMonitorAdaptor.getStatus(_nativeJobId);
      } catch (Exception ex) { 
          ex.printStackTrace(System.out);
      }
       
    log.info("");
    log.info("Calling the getStatus() method");    
    
    return result;    
  }
  
  public String[] list() throws PermissionDeniedException, TimeoutException, NoSuccessException 
  {
    return sshMonitorAdaptor.list();
  }       
  
  public Date getCreated(String nativeJobId) 
              throws NotImplementedException, NoSuccessException 
  {    
    Date result = null;
    String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, nativeJobId.indexOf("#"));
    String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
    
    try {
        sshMonitorAdaptor.connect(null, _publicIP, 22, null, new HashMap());
        result = sshMonitorAdaptor.getCreated(_nativeJobId);
      } catch (Exception ex) { 
          ex.printStackTrace(System.out);         
      }
       
    log.info("Calling the getCreated() method");
    
    return result;
  }
  
  public Date getStarted(String nativeJobId) 
              throws NotImplementedException, NoSuccessException 
  {     
    Date result = null;
    String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, nativeJobId.indexOf("#"));
    String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
    
    try {
        sshMonitorAdaptor.connect(null, _publicIP, 22, null, new HashMap());
        result = sshMonitorAdaptor.getStarted(_nativeJobId);
      } catch (Exception ex) { 
          ex.printStackTrace(System.out);        
      }
            
    log.info("Calling the getStarted() method");
    
    return result;
  }
  
  public Date getFinished(String nativeJobId) 
              throws NotImplementedException, NoSuccessException 
  {    
    Date result = null;
    String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, nativeJobId.indexOf("#"));
    String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
    
    try {
        sshMonitorAdaptor.connect(null, _publicIP, 22, null, new HashMap());
        result = sshMonitorAdaptor.getFinished(_nativeJobId);
      } catch (Exception ex) { 
          ex.printStackTrace(System.out);
      }
   
    log.info("Calling the getFinished() method");
    
    return result;
  }

  public Integer getExitCode(String nativeJobId) 
                 throws NotImplementedException, NoSuccessException 
  {        
    Integer result = null;
    String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, nativeJobId.indexOf("#"));
    String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
    
    try {
        sshMonitorAdaptor.connect(null, _publicIP, 22, null, new HashMap());
        result = sshMonitorAdaptor.getExitCode(_nativeJobId);
      } catch (Exception ex) {           
          ex.printStackTrace(System.out);
      }
    
    log.info("Calling the getExitCode() method");
    
    return result;
  }
  
  public String[] getExecutionHosts(String nativeJobId) 
                  throws NotImplementedException, NoSuccessException 
  {        
    String[] result = null;
    String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, nativeJobId.indexOf("#"));
    String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
    
    try {
        sshMonitorAdaptor.connect(null, _publicIP, 22, null, new HashMap());
        result = sshMonitorAdaptor.getExecutionHosts(_nativeJobId);
      } catch (Exception ex) {           
          ex.printStackTrace(System.out);
      }        
        
    return result;
  }
}
