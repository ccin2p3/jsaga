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

import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;

import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.advanced.CleanableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.ssh3.job.SSHJobControlAdaptor;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.error.PermissionDeniedException;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import org.apache.commons.net.telnet.TelnetClient;

/* *********************************************
 * *** Istituto Nazionale di Fisica Nucleare ***
 * ***      Sezione di Catania (Italy)       ***
 * ***        http://www.ct.infn.it/         ***
 * *********************************************
 * File:    rOCCIJobControlAdaptor.java
 * Authors: Giuseppe LA ROCCA, Diego SCARDACI
 * Email:   <giuseppe.larocca, diego.scardaci>@ct.infn.it
 * Ver.:    1.0.5
 * Date:    04 November 2014
 * *********************************************/

public class rOCCIJobControlAdaptor extends rOCCIAdaptorCommon
                                    implements JobControlAdaptor, 
                                               StagingJobAdaptorTwoPhase, 
                                               CleanableJobAdaptor
{     
    
  protected static final String ATTRIBUTES_TITLE = "attributes_title";
  protected static final String MIXIN_OS_TPL = "mixin_os_tpl";
  protected static final String MIXIN_RESOURCE_TPL = "mixin_resource_tpl";
  protected static final String PREFIX = "prefix";  
    
  // MAX tentatives before to gave up to connect the VM server.
  private final int MAX_CONNECTIONS = 10;
  
  private static final Logger log = 
          Logger.getLogger(rOCCIJobControlAdaptor.class);
      
  private rOCCIJobMonitorAdaptor rOCCIJobMonitorAdaptor = 
            new rOCCIJobMonitorAdaptor();
    
  private SSHJobControlAdaptor sshControlAdaptor = 
            new SSHJobControlAdaptor();
  
  private String prefix = "";
  private String action = "";
  private String resource = "";  
  private String auth = "";
  private String attributes_title = "";
  private String mixin_os_tpl = "";
  private String mixin_resource_tpl = "";  
  private String Endpoint = "";
    
  enum ACTION_TYPE { list, delete, describe, create; }
    
  String[] IP = new String[2];
  
  @Override
  public Usage getUsage()
  {
    return new UAnd.Builder()
                    .and(super.getUsage())
                    .and(new U(ATTRIBUTES_TITLE))
                    .and(new U(MIXIN_OS_TPL))
                    .and(new U(MIXIN_RESOURCE_TPL))
                    .and(new UOptional(PREFIX))
                    .build();
  }
    
  public boolean testIpAddress(byte[] testAddress)
  {
    Inet4Address inet4Address;
    boolean result=false;

    try
    {
        inet4Address = (Inet4Address) InetAddress.getByAddress(testAddress);
        result = inet4Address.isSiteLocalAddress();
    }
    catch (UnknownHostException ex) { log.error(ex); }
    
    return result;
  }
      
  private List<String> run_OCCI (String action_type, String action)            
  {
      String line;
      //List<String> list_rOCCI = new ArrayList();
      List<String> list_rOCCI = new ArrayList<String>();
            
      try
      {            
        Process p = Runtime.getRuntime().exec(action);

        BufferedReader in = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));

        ACTION_TYPE type = ACTION_TYPE.valueOf(action_type);
             
        while ((line = in.readLine()) != null) 
        {         
            // Skip blank lines.
            if (line.trim().length() > 0) {
                    
                switch (type) {
                    case list:
                        list_rOCCI.add(line.trim());
                        break;

                    case create:
                        list_rOCCI.add(line.trim());
                        log.info("");
                        log.info("A new OCCI computeID has been created:");
                        break;

                    case describe:
                        list_rOCCI.add(line.trim());
                        break;
                
                    case delete:
                        break;
                } // end switch
            } // end if
        } // end while

        in.close();
             
        if (action_type.equals("describe") || 
            action_type.equals("list") ||
            action_type.equals("delete")) 
            log.info("\n");         
                             
        for (int i = 0; i < list_rOCCI.size(); i++)         
            log.info(list_rOCCI.get(i));
                                             
        } catch (IOException ex) { log.error(ex); }
        
        return list_rOCCI;
    }
                        
    @Override
    public void connect (String userInfo, String host, int port, String basePath, Map attributes) 
           throws NotImplementedException, 
                  AuthenticationFailedException, 
                  AuthorizationFailedException, 
                  IncorrectURLException, 
                  BadParameterException, 
                  TimeoutException, 
                  NoSuccessException 
    {      
        
       //List<String> results = new ArrayList();
        List<String> results = new ArrayList<String>();
                 
       log.info("");
       log.info("Trying to connect to the cloud host [ " + host + " ] ");
       
       prefix = (String) attributes.get(PREFIX);
       String resourceID = (String) attributes.get("resourceID");
       action = (String) attributes.get(ACTION);
       auth = (String) attributes.get(AUTH);
       resource = (String) attributes.get(RESOURCE);
       attributes_title = (String) attributes.get(ATTRIBUTES_TITLE);
       mixin_os_tpl = (String) attributes.get(MIXIN_OS_TPL);
       mixin_resource_tpl = (String) attributes.get(MIXIN_RESOURCE_TPL);
       
       // Check if OCCI path is set                
       if ((prefix != null) && (new File((prefix)).exists()))
            prefix += System.getProperty("file.separator");
       else prefix = "";              
            
       Endpoint = "https://" 
                  + host + ":" + port 
                  + System.getProperty("file.separator");
       
       log.info("");
       log.info("See below the details: ");
       log.info("");
       log.info("PREFIX    = " + prefix);
       log.info("ACTION    = " + action);
       log.info("RESOURCE  = " + resource);
       
       log.info("");
       log.info("AUTH       = " + auth);       
       log.info("PROXY_PATH = " + user_cred);       
       log.info("CA_PATH    = " + ca_path);
       
       log.info("");
       log.info("HOST        = " + host);
       log.info("PORT        = " + port);
       log.info("ENDPOINT    = " + Endpoint);
       log.info("PUBLIC KEY  = " + credential.getSSHCredential().getPublicKeyFile().getPath());
       log.info("PRIVATE KEY = " + credential.getSSHCredential().getPrivateKeyFile().getPath());
       
       log.info("");
       if  (action.equals("list")) 
       {
            if (resource.equals("compute"))
                log.info("Listing active OCCI computeID(s)");
                
            if (resource.equals("os_tpl"))
                log.info("Listing of available VMs on the server... ");
            
            if (resource.equals("resource_tpl"))
                log.info("Listing active OCCI flavours... ");                        
                        
            String Execute = prefix +
                             "occi --endpoint " + Endpoint +
                             " --action " + action +
                             " --resource " + resource +
                             " --auth " + auth +
                             " --user-cred " + user_cred +
                             " --voms --ca-path " + ca_path;
            
            log.info(Execute);
            
            try {
                results = run_OCCI("list", Execute);
                if (results.isEmpty())
                    throw new NoSuccessException(
                    "Some problems occurred while contacting the server. "
                    + "Please check your settings.");                
            } catch (Exception ex) { log.error(ex); }
       } // end listing
       
       if  (action.equals("describe")) 
       {           
           log.info("Describing the OCCI computeID");
           
           if (resourceID.trim().length() > 0)
                log.info("ResourceID = " + resourceID);
                
            String Execute = prefix +
                             "occi --endpoint " + Endpoint +
                             " --action " + action +
                             " --resource " + resource +
                             " --resource " + resourceID +
                             " --auth " + auth +
                             " --user-cred " + user_cred +
                             " --voms --ca-path " + ca_path;

            log.info(Execute);        

            try {
                results = run_OCCI("describe", Execute);
                if (results.isEmpty())
                    throw new NoSuccessException(
                    "Some problems occurred while contacting the server. "
                    + "Please check your settings.");                
            } catch (Exception ex) { log.error(ex); }
       } // end describing
       
       if  (action.equals("delete")) 
       {           
           log.info("Deleting the OCCI computeID");
            
           if (resourceID.trim().length() > 0)
                log.info("ResourceID = " + resourceID);

           String Execute = prefix +
                            "occi --endpoint " + Endpoint +
                            " --action " + action +
                            " --resource " + resource +
                            " --resource " + resourceID +
                            " --auth " + auth +
                            " --user-cred " + user_cred +
                            " --voms --ca-path " + ca_path;

           log.info(Execute);           

           try {
                results = run_OCCI("delete", Execute);                
           } catch (Exception ex) { log.error(ex); }
        } // end deleting 
       
        sshControlAdaptor.setSecurityCredential(credential.getSSHCredential());
    }
            
    @Override
    public void start(String nativeJobId) throws PermissionDeniedException, 
                                                 TimeoutException, 
                                                 NoSuccessException 
    {
        String _publicIP = 
                nativeJobId.substring(nativeJobId.indexOf("@")+1, 
                                      nativeJobId.indexOf("#"));
        
        String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
        
        try {                        
            sshControlAdaptor.connect(null, _publicIP, 22, null, new HashMap());            
            sshControlAdaptor.start(_nativeJobId);                         
            
        } catch (NotImplementedException ex) { throw new NoSuccessException(ex); } 
          catch (AuthenticationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (AuthorizationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (BadParameterException ex) { throw new NoSuccessException(ex); }
    }
    
    @Override
    public void cancel(String nativeJobId) throws PermissionDeniedException, 
                                                  TimeoutException, 
                                                  NoSuccessException 
    {   
        String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, 
                                                 nativeJobId.indexOf("#"));
        
        String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
        
        try {                        
            sshControlAdaptor.connect(null, _publicIP, 22, null, new HashMap());            
            sshControlAdaptor.cancel(_nativeJobId);
        } catch (NotImplementedException ex) { throw new NoSuccessException(ex); } 
          catch (AuthenticationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (AuthorizationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (BadParameterException ex) { throw new NoSuccessException(ex); }
        
        log.info("Calling the cancel() method");        
    }
    
    @Override
    public void clean (String nativeJobId) throws PermissionDeniedException, 
                                                  TimeoutException, 
                                                  NoSuccessException 
    {    
        //List<String> results = new ArrayList();
        List<String> results = new ArrayList<String>();
        
        String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, 
                                                 nativeJobId.indexOf("#"));
        
        String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
        String _resourceId = nativeJobId.substring(nativeJobId.indexOf("#")+1);
        
        String Execute = prefix +
                         "occi --endpoint " + Endpoint +
                         " --action " + "delete" +
                         " --resource " + "compute" +
                         " --resource " + _resourceId +
                         " --auth " + auth +
                         " --user-cred " + user_cred +                         
                         " --voms --ca-path " + ca_path;                
                
        log.info("");
        log.info("Stopping the VM [ " + _publicIP + " ] in progress...");
                
        log.info(Execute);        
        
        try {            
            sshControlAdaptor.connect(null, _publicIP, 22, null, new HashMap());            
            sshControlAdaptor.clean(_nativeJobId);
            
            // Stopping the VM Server
            results = run_OCCI("delete", Execute);            
            
        } catch (NotImplementedException ex) { throw new NoSuccessException(ex); } 
          catch (AuthenticationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (AuthorizationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (BadParameterException ex) { throw new NoSuccessException(ex); }
    }
    
    public String getIP (List<String> results)
    {
        String publicIP = null;
        String tmp  = "";
        int k=0;
        boolean check = false;
        
        // Extracting IPs                         
        for (int i = 0; i < results.size() && !check;  i++) 
        {            
            if ((results.get(i)).contains("\"address\"")) 
            {
                // Stripping quote and blank chars
                IP[k] = results.get(i).substring(12,results.get(i).length()-1);
                
                Pattern patternID = 
                    Pattern.compile("(\\d{1,3}.)(\\d{1,3}.)(\\d{1,3}.)(\\d{1,3}.)");
                                                                                                         
                tmp = IP[k];
                                   
                Matcher matcher = patternID.matcher(IP[k]);
                                   
                while (matcher.find()) 
                {
                    String _IP0 = 
                        matcher.group(1).replace(".","");
                                       
                    String _IP1 = 
                        matcher.group(2).replace(".","");
                                       
                    String _IP2 = 
                        matcher.group(3).replace(".","");
                                       
                    String _IP3 = 
                        matcher.group(4).replace(".","");
                                                                      
                     //CHECK if IP[k] is PRIVATE or PUBLIC
                     byte[] rawAddress = { 
                        (byte) Integer.parseInt(_IP0),
                        (byte) Integer.parseInt(_IP1),
                        (byte) Integer.parseInt(_IP2),
                        (byte) Integer.parseInt(_IP3)
                     };
                                   
                     if (!testIpAddress(rawAddress)) {
                        // Saving the public IP
                        publicIP = tmp;
                        check = true;
                     }
                                   
                     k++;
                } // while
            }  // if
        } // end for
        
        return publicIP;
    }
        
    public boolean isNullOrEmpty(String myString)
    {
         return myString == null || "".equals(myString);
    }
    
    @Override
    public String submit (String jobDesc, boolean checkMatch, String uniqId) 
                  throws PermissionDeniedException, 
                         TimeoutException, 
                         NoSuccessException, 
                         BadResource 
    {
        String resourceID = "";
        String publicIP = "";
        //List<String> results = new ArrayList();
        List<String> results = new ArrayList<String>();
                        
        if (action.equals("create")) {
                
                log.info("Creating a new OCCI computeID. Please wait! ");
                
                if (attributes_title.trim().length() > 0)
                    log.info("VM Title     = " + attributes_title);
                    
                if (mixin_os_tpl.trim().length() > 0)
                    log.info("OS           = " + mixin_os_tpl);
                    
                if (mixin_resource_tpl.trim().length() > 0)
                    log.info("Flavour      = " + mixin_resource_tpl);
                    
                String Execute = prefix +
                                 "occi --endpoint " + Endpoint +                                 
                                 " --action " + "create" +
                                 " --resource " + resource +
                                 " --attribute occi.core.title=" + attributes_title +
                                 " --mixin os_tpl#" + mixin_os_tpl +
                                 " --mixin resource_tpl#" + mixin_resource_tpl +
                                 " --auth " + auth +
                                 " --user-cred " + user_cred +
                                 " --voms --ca-path " + ca_path;
                
                log.info("");
                log.info(Execute);
                 
                try {                        
                        results = run_OCCI("create", Execute); 
                        if (results.isEmpty()) 
                            throw new NoSuccessException(
                            "Some problems occurred while executing the action create. "
                            + "Please check your settings.");
                                                                        
                } catch (Exception ex) { log.error(ex); }
                                                   
                // Getting info about the VM
                if (results.size()>0) 
                {                    
                    resourceID = results.get(0);                                        
                    
                    int k=0;
                    boolean check = false;
                    
                    try {
                            while (!check) {
                            log.info("");
                            log.info("See below the details of the VM ");
                            log.info("[ " + resourceID + " ]");
                            log.info("");
                            
                            Execute = prefix +
                                    "occi --endpoint " + Endpoint +
                                    " --action " + "describe" +
                                    " --resource " + resource +
                                    " --resource " + resourceID +
                                    " --auth " + auth +
                                    " --user-cred " + user_cred +
                                    " --voms --ca-path " + ca_path +
                                    " --output-format json_extended_pretty";
                            
                            log.info(Execute);
                           
                            results = run_OCCI("describe", Execute);
                            
                            publicIP = getIP (results);                                
                            if (!isNullOrEmpty(publicIP)) check=true;
                             else check=true;                           
                            } // end while
                    } catch (Exception ex) { log.error(ex); }                                                          
                   
                    sshControlAdaptor.setSecurityCredential(credential.getSSHCredential());
                    log.info("");
                    log.info("Starting VM [ " + publicIP + " ] in progress...");
                                        
                    Date date = new Date();
                    SimpleDateFormat ft = 
                       new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
           
                    log.info("");
                    log.info("Waiting the remote VM finishes the boot! Sleeping for a while... ");
                    log.info(ft.format(date));
                                                       
                    byte[] buff = new byte[1024];
                    int ret_read = 0;
                    boolean flag = true;                                        
                    int MAX = 0;
                    
                    TelnetClient tc = null;
                    
                    while ((flag) && (MAX < MAX_CONNECTIONS))
                    {                        
                        try
                        {
                            tc = new TelnetClient();
                            tc.connect(publicIP, 22);
                            InputStream instr = tc.getInputStream();
                                                    
                            ret_read = instr.read(buff);                            
                            if (ret_read > 0)
                            {
                                log.info("[ SUCCESS ] ");
                                tc.disconnect();
                                flag=false;
                            }
                        } catch (IOException e) {
                            
                            try {                                
                                Thread.sleep(60000);
                            } catch (InterruptedException ex) { }
                            
                            MAX++;
                        }
                    }
               
                    date = new Date();
                    log.info(ft.format(date));
                }        
            
                rOCCIJobMonitorAdaptor.setSSHHost(publicIP);
        
                try {            
                    sshControlAdaptor.connect(null, publicIP, 22, null, new HashMap());            
                } catch (NotImplementedException ex) { throw new NoSuccessException(ex); } 
                  catch (AuthenticationFailedException ex) { throw new PermissionDeniedException(ex); } 
                  catch (AuthorizationFailedException ex) { throw new PermissionDeniedException(ex); } 
                  catch (BadParameterException ex) { throw new NoSuccessException(ex); }
                
                return sshControlAdaptor.submit(jobDesc, checkMatch, uniqId) 
                    + "@" 
                    + publicIP
                    + "#"
                    + resourceID;
            } // end creating
        
        else return null;
    }
    
    @Override
    public StagingTransfer[] getInputStagingTransfer(String nativeJobId) 
                             throws PermissionDeniedException, 
                                    TimeoutException, 
                                    NoSuccessException 
    {        
        StagingTransfer[] result = null;
        String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, 
                                                 nativeJobId.indexOf("#"));
        
        String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
        
        try {            	
	    sshControlAdaptor.setSecurityCredential(credential.getSSHCredential());
            sshControlAdaptor.connect(null, _publicIP, 22, null, new HashMap());
            result = sshControlAdaptor.getInputStagingTransfer(_nativeJobId);
                        
        } catch (NotImplementedException ex) { throw new NoSuccessException(ex); } 
          catch (AuthenticationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (AuthorizationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (BadParameterException ex) { throw new NoSuccessException(ex); }
             
        // change URL sftp:// tp rocci://
        return sftp2rocci(result);
    }
    
    @Override
    public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) 
                             throws PermissionDeniedException, 
                                    TimeoutException, 
                                    NoSuccessException 
    {
        
        StagingTransfer[] result = null;
        String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, 
                                                 nativeJobId.indexOf("#"));
        
        String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
        
        try {            
            sshControlAdaptor.connect(null, _publicIP, 22, null, new HashMap());            
            result = sshControlAdaptor.getOutputStagingTransfer(_nativeJobId);
        } catch (NotImplementedException ex) { throw new NoSuccessException(ex); } 
          catch (AuthenticationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (AuthorizationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (BadParameterException ex) { throw new NoSuccessException(ex); }
                        
        // change URL sftp:// tp rocci://
        return sftp2rocci(result);
    }

    private StagingTransfer[] sftp2rocci(StagingTransfer[] transfers) 
    {
        int index=0;
        StagingTransfer[] newTransfers = new StagingTransfer[transfers.length];
        
        for (StagingTransfer tr: transfers) 
        {
            StagingTransfer newTr = 
                    new StagingTransfer(
                        tr.getFrom().replace("sftp://", "rocci://"),
                        tr.getTo().replace("sftp://", "rocci://"),
                        tr.isAppend());
                
            newTransfers[index++] = newTr;
        }
        
        return newTransfers;
    }
    
    @Override
    public String getStagingDirectory(String nativeJobId) 
                  throws PermissionDeniedException, 
                         TimeoutException, 
                         NoSuccessException 
    {               
        String result = null;
        String _publicIP = nativeJobId.substring(nativeJobId.indexOf("@")+1, 
                                                 nativeJobId.indexOf("#"));
        
        String _nativeJobId = nativeJobId.substring(0, nativeJobId.indexOf("@"));
        
        try {            
            sshControlAdaptor.connect(null, _publicIP, 22, null, new HashMap());            
            result = sshControlAdaptor.getStagingDirectory(_nativeJobId);
        } catch (NotImplementedException ex) { throw new NoSuccessException(ex); } 
          catch (AuthenticationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (AuthorizationFailedException ex) { throw new PermissionDeniedException(ex); } 
          catch (BadParameterException ex) { throw new NoSuccessException(ex); }
                
        return result;
    }
    
    @Override
    public JobMonitorAdaptor getDefaultJobMonitor() 
    {        
        return rOCCIJobMonitorAdaptor;
    }
    
    @Override
    public JobDescriptionTranslator getJobDescriptionTranslator() 
            throws NoSuccessException 
    {        
        return sshControlAdaptor.getJobDescriptionTranslator();        
    }        
}
