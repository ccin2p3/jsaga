/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 * 
 * Authors: Luigi Zangrando (zangrando@pd.infn.it)
 */

package org.glite.ce.creamapi.jobmanagement;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;
import java.util.Enumeration;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.axis.MessageContext;

import org.apache.log4j.Logger;
import org.glite.ce.creamapi.jobmanagement.cmdexecutor.AbstractJobExecutor;

public class JobWrapper {
	private static final Logger logger = Logger.getLogger(JobWrapper.class.getName());
    private static final String COPY_RETRY_COUNT_VALUE_DEFAULT  = "5";
    private static final String COPY_RETRY_WAIT_VALUE           = "30"; //sec.
    private static final String COPY_PROXY_MIN_RETRY_WAIT_VALUE = "60"; //sec.
    private static final String DELEGATION_TIME_SLOT            = "3600"; //sec.	 
    
    private static final String WRAPPER_MPI_TEMPLATE_NAME = "jobwrapper-mpi.tpl";
    private static final String WRAPPER_TEMPLATE_NAME     = "jobwrapper.tpl";
    private static       Hashtable<String, String> wrapperTemplateHashTable = new Hashtable<String, String>(0);


    public static String filenameNorm(String str) {
        if (str == null || str.length() == 0)
            return "";

        StringBuffer buff = new StringBuffer();
        int start = 0;
        int end = str.length();

        if (str.charAt(0) == '"' && str.charAt(end - 1) == '"') {
            start++;
            end--;
        }

        for (int k = start; k < end; k++) {
            if (str.charAt(k) == ' ')
                buff.append("\\ ");
            else
                buff.append(str.charAt(k));
        }
        return buff.toString();
    }

    public static String stringNorm(String str) {
        StringBuffer buff = new StringBuffer("\"");
        for (int k = 0; k < str.length(); k++) {
            if (str.charAt(k) == '"')
                buff.append("\\\"");
            else if (str.charAt(k) == '$')
                buff.append("\\$");
            else
                buff.append(str.charAt(k));
        }
        buff.append("\"");
        return buff.toString();
    }

    public static String buildWrapper(Job job) throws IOException {

        StringBuffer wrapper = new StringBuffer("#!/bin/sh -l\n");
        wrapper.append("__create_subdir=1\n");

        String gridJobId = job.getGridJobId();
        String creamJobId = job.getId();
        String executable = job.getExecutable();
        String[] arguments = job.getArguments();
        String stdi = job.getStandardInput();
        String stdo = job.getStandardOutput();
        String stde = job.getStandardError();
        String loggerDestURI = job.getLoggerDestURI();
        String tokenURL = job.getTokenURL();
        int nodes = job.getNodeNumber();
        String perusalListFileURI = job.getPerusalListFileURI();
        String perusalFilesDestURI = job.getPerusalFilesDestURI();
        int perusalTimeInterval = job.getPerusalTimeInterval();
        String prologue = job.getPrologue();
        String prologueArgs = job.getPrologueArguments();
        String epilogue = job.getEpilogue();
        String epilogueArgs = job.getEpilogueArguments();
        String delegationProxyCertSandboxPath = (String)job.getVolatilePropertyValue(AbstractJobExecutor.DELEGATION_PROXY_CERT_SANDBOX_URI);
        
        String copyRetryCountValue =  JobWrapper.COPY_RETRY_COUNT_VALUE_DEFAULT;
        /**
       	if (job.getVolatilePropertyValue(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_COUNT) != null) {
       		try{
       			Integer.parseInt((String)job.getVolatilePropertyValue(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_COUNT));
       			copyRetryCountValue = (String)job.getVolatilePropertyValue(AbstractJobExecutor.JOB_WRAPPER_COPY_RETRY_COUNT);
       		} catch (NumberFormatException nfe){
       			logger.warn("JOB_WRAPPER_COPY_RETRY_COUNT must be integer. So it'll be used the default value.");
       		}
       	}
        **/
       	String copyProxyMinRetryWaitValue =  JobWrapper.COPY_PROXY_MIN_RETRY_WAIT_VALUE;
       	if (job.getVolatilePropertyValue(AbstractJobExecutor.JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT) != null) {
       		try{
       			Integer.parseInt((String)job.getVolatilePropertyValue(AbstractJobExecutor.JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT));
       			copyProxyMinRetryWaitValue = (String)job.getVolatilePropertyValue(AbstractJobExecutor.JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT);
       		} catch (NumberFormatException nfe){
       			logger.warn("JOB_WRAPPER_COPY_PROXY_MIN_RETRY_WAIT must be integer. So it'll be used the default value.");
       		}
       	}

        String delegationTimeSlot =  null;
        String delegationProxyCertSandboxFileName = null;

        if ((delegationProxyCertSandboxPath == null) || ("".equals(delegationProxyCertSandboxPath))) {
        	//no ProxyRenewal
        	logger.debug("No PROXY_RENEW for jobid= " + job.getId());
        	delegationTimeSlot = "-1";
        } else {
        	delegationTimeSlot =  JobWrapper.DELEGATION_TIME_SLOT;
            if (job.getVolatilePropertyValue(AbstractJobExecutor. JOB_WRAPPER_DELEGATION_TIME_SLOT) != null) {
              try{
            	Integer.parseInt((String)job.getVolatilePropertyValue(AbstractJobExecutor.JOB_WRAPPER_DELEGATION_TIME_SLOT));
            	delegationTimeSlot = (String)job.getVolatilePropertyValue(AbstractJobExecutor. JOB_WRAPPER_DELEGATION_TIME_SLOT);
              } catch (NumberFormatException nfe){
            	logger.warn(" JOB_WRAPPER_DELEGATION_TIME_SLOT must be integer. So it'll be used the default value.");
              }
            }
            delegationProxyCertSandboxFileName = delegationProxyCertSandboxPath.substring(delegationProxyCertSandboxPath.lastIndexOf('/')) +
            		                             creamJobId.substring(5) + ".glexec";
            delegationProxyCertSandboxPath = delegationProxyCertSandboxPath + ".glexec";
        }
        
        if (creamJobId == null) {
            throw new IllegalArgumentException("Missing cream job id");
        }
        if (gridJobId == null || gridJobId.length() == 0){
            gridJobId = "\"\"";
        }
        if (executable == null || executable.equals("")) {
            throw new IllegalArgumentException("Missing executable");
        }
        if (nodes == 0 && job.isMpich()) {
            throw new IllegalArgumentException("Missing node number for mpich job");
        }
        if (!executable.startsWith("/") && !executable.startsWith("./")) {
            executable = "./" + executable;
        }
        if (loggerDestURI == null || loggerDestURI.length() == 0) {
            loggerDestURI = "\"\"";
        }
        String tokenHost = null;
        String tokenPath = null;
        if (tokenURL == null || tokenURL.length() == 0) {
            tokenURL = "\"\"";
            tokenHost = "\"\"";
            tokenPath = "\"\"";
        } else {
            try {
                URI tmpuri = new URI(tokenURL);
                tokenHost = tmpuri.getHost();
                if (tmpuri.getPort() > 0)
                    tokenHost = tokenHost + ":" + tmpuri.getPort();
                tokenPath = tmpuri.getPath();
            } catch (Exception ex) {
                throw new IOException("Wrong url format: " + tokenURL);
            }
        }
        
        wrapper.append("export __delegationProxyCertSandboxPath=").append(delegationProxyCertSandboxPath).append("\n");
        wrapper.append("export __delegationProxyCertSandboxPathTmp=").append("/tmp" + delegationProxyCertSandboxFileName).append("\n");
        wrapper.append("export __delegationTimeSlot=").append(delegationTimeSlot).append("\n");
        wrapper.append("__gridjobid=").append(gridJobId).append("\n");
        wrapper.append("__creamjobid=").append(creamJobId).append("\n");
        wrapper.append("__executable=").append(executable).append("\n");
        wrapper.append("__working_directory=");
        wrapper.append(creamJobId.substring(creamJobId.indexOf("CREAM")));
        wrapper.append("\n");

        wrapper.append("__ce_hostname=");
        try {
            wrapper.append(InetAddress.getLocalHost().getHostName()).append("\n");
        } catch (UnknownHostException uhEx) {
            wrapper.append("\n");
        }

        StringBuffer cmdLine = new StringBuffer();
        if (job.isInteractive()) {
            cmdLine.append("./glite-wms-job-agent $BYPASS_SHADOW_HOST $BYPASS_SHADOW_PORT \"");
            cmdLine.append(executable).append(" ");
            cmdLine.append(arguments).append(" $*\"");

        } else {
            cmdLine.append("\"").append(executable).append("\" ");
            if (arguments != null) {
                for(int i=0; i<arguments.length; i++) {
                    if(arguments[i] != null) {
                        cmdLine.append(arguments[i]);
                    }
                }
            }
            cmdLine.append("$* ");
            

            if (stdi != null && !stdi.equals("")) {
                cmdLine.append("< \"").append(stdi).append("\"");
            }

            if (stdo != null && !stdo.equals("")) {
                cmdLine.append(" > \"").append(stdo).append("\"");
                wrapper.append("__stdout_file=\"").append(stdo).append("\"\n");
            } else {
                cmdLine.append(" > /dev/null ");
            }

            if (stde != null && !stde.equals("")) {
                cmdLine.append(stde.equals(stdo) ? " 2>&1" : " 2> \"" + stde + "\"");
                wrapper.append("__stderr_file=\"").append(stde).append("\"\n");
            } else {
                cmdLine.append(" 2> /dev/null");
            }
        }

        wrapper.append("__cmd_line=").append(stringNorm(cmdLine.toString())).append("\n");
        wrapper.append("__logger_dest=").append(loggerDestURI).append("\n");
        wrapper.append("__token_file=").append(tokenURL).append("\n");
        wrapper.append("__token_hostname=").append(tokenHost).append("\n");
        wrapper.append("__token_fullpath=").append(tokenPath).append("\n");
        wrapper.append("__nodes=").append(nodes).append("\n");
        wrapper.append("export __copy_retry_count=").append(copyRetryCountValue).append("\n");
        wrapper.append("export __copy_proxy_min_retry_wait=").append(copyProxyMinRetryWaitValue).append("\n");
        wrapper.append("export __copy_retry_first_wait=").append(COPY_RETRY_WAIT_VALUE).append("\n");

        if (perusalFilesDestURI != null) {
            if (perusalFilesDestURI == null || perusalTimeInterval < 1)
                throw new IllegalArgumentException("Missing perusal parameters");

            wrapper.append("__perusal_filesdesturi=").append(perusalFilesDestURI).append("\n");
            wrapper.append("__perusal_listfileuri=").append(perusalListFileURI).append("\n");
            wrapper.append("__perusal_timeinterval=").append(perusalTimeInterval).append("\n");
        }

        if (prologue != null && prologue.length() > 0) {
            if (!prologue.startsWith("/"))
                prologue = "./" + prologue;
            wrapper.append("__prologue=\"").append(prologue).append("\"\n");
            if (prologueArgs == null)
                prologueArgs = "";
            wrapper.append("__prologue_arguments=\"").append(prologueArgs).append("\"\n");
        }

        if (epilogue != null && epilogue.length() > 0) {
            if (!epilogue.startsWith("/"))
                epilogue = "./" + epilogue;
            wrapper.append("__epilogue=\"").append(epilogue).append("\"\n");
            if (epilogueArgs == null)
                epilogueArgs = "";
            wrapper.append("__epilogue_arguments=\"").append(epilogueArgs).append("\"\n");
        }

        // environment
        wrapper.append("declare -a __environment\n\n");
        int counter = 0;

        if (job.getHlrLocation() != null) {
            wrapper.append("__environment[0]=\"HLR_LOCATION=");
            wrapper.append(job.getHlrLocation()).append("\"\n");
            counter++;
        }

        Hashtable<String, String> env = job.getEnvironment();
        if (env != null) {
            Enumeration<String> allKeys = env.keys();
            while (allKeys.hasMoreElements()) {
                String key = allKeys.nextElement();
                wrapper.append("__environment[" + counter + "]=");
                wrapper.append(stringNorm(key + "=" + env.get(key)));
                wrapper.append("\n");
                counter++;
            }
        }

        // isb
        String[] fileNames = job.getInputFiles();

        if (job.isInteractive()) {
            String[] tmparray = null;
            int idx = 0;
            if (fileNames != null && fileNames.length > 0) {
                tmparray = new String[fileNames.length + 4];
                System.arraycopy(fileNames, 0, tmparray, 0, fileNames.length);
                idx += fileNames.length;
            } else {
                tmparray = new String[4];
            }
            tmparray[idx] = "gsiftp://${__ce_hostname}/${GLITE_WMS_LOCATION}/bin/glite-wms-pipe-input";
            tmparray[idx + 1] = "gsiftp://${__ce_hostname}/${GLITE_WMS_LOCATION}/bin/glite-wms-pipe-output";
            tmparray[idx + 2] = "gsiftp://${__ce_hostname}/${GLITE_WMS_LOCATION}/bin/glite-wms-job-agent";
            tmparray[idx + 3] = "gsiftp://${__ce_hostname}/${GLITE_WMS_LOCATION}/lib/libglite-wms-grid-console-agent.so.0";
            fileNames = tmparray;
        }

        if (fileNames != null && fileNames.length > 0) {
            String prefix = job.getInputSandboxBaseURI();

            wrapper.append("declare -a __input_file_url\n");
            wrapper.append("declare -a __input_file_dest\n");
            wrapper.append("declare -a __input_transfer_cmd\n");

            if (prefix == null)
                prefix = "";
            else if (!prefix.endsWith("/"))
                prefix = prefix + "/";

            for (int k = 0; k < fileNames.length; k++) {

                String fName = filenameNorm(fileNames[k]);

                if (fName.indexOf("://") < 0) {
                    if (fName.startsWith("/") || prefix.length() == 0) {
                        fName = "file://" + fName;
                    } else {
                        fName = prefix + fName;
                    }
                }

                String pName = fName.substring(fName.lastIndexOf("/") + 1);

                if (fName.startsWith("file://")) {

                    String tmpURI = job.getCREAMInputSandboxURI();
                    if (tmpURI == null || tmpURI.equalsIgnoreCase("N/A")) {
                        throw new IllegalArgumentException("Missing CREAMInputSandboxURI");
                    }
                    fName = tmpURI + "/" + pName;

                }

                wrapper.append("__input_file_url[").append(k).append("]=");
                wrapper.append(stringNorm(fName)).append("\n");
                wrapper.append("__input_file_dest[").append(k).append("]=");
                wrapper.append(stringNorm(pName)).append("\n");

                if (fName.startsWith("gsiftp") || fName.startsWith("file")) {
                    wrapper.append("__input_transfer_cmd[").append(k).append("]=\"\\${globus_transfer_cmd}\"\n");
                } else if (fName.startsWith("https")) {
                    wrapper.append("__input_transfer_cmd[").append(k).append("]=\"\\${https_transfer_cmd}\"\n");
                } else {
                    throw new IllegalArgumentException("Unsupported protocol");
                }

            }

        }
        wrapper.append("\n");

        // osb
    	String maxOutputSandboxSize = (String)job.getVolatilePropertyValue(Job.MAX_OUTPUT_SANDBOX_SIZE);
    	logger.debug("maxOutputSandboxSize = " + maxOutputSandboxSize);
    	long maxOutputSandboxSizeLong = -1;
    	if ((maxOutputSandboxSize != null) && (maxOutputSandboxSize.length() > 0)){
    		if (maxOutputSandboxSize.startsWith("(")){
    			maxOutputSandboxSize = maxOutputSandboxSize.substring(1);
    		}
    		if (maxOutputSandboxSize.endsWith(")")){
    			maxOutputSandboxSize = maxOutputSandboxSize.substring(0, maxOutputSandboxSize.length()-1);
    		}
    		logger.debug("maxOutputSandboxSize without parentheses = " + maxOutputSandboxSize);
    		try{
    		  maxOutputSandboxSizeLong = Math.round(Double.parseDouble(maxOutputSandboxSize));
    		} catch (NumberFormatException nfe){
    		  logger.error(" Number mismatch for maxOutputSandboxSize = " + maxOutputSandboxSize);
    		  throw new IllegalArgumentException(" Number mismatch for maxOutputSandboxSize = " + maxOutputSandboxSize);
    		}
    	}
    	wrapper.append("__max_osb_size=").append("" + maxOutputSandboxSizeLong).append("\n");
        
        fileNames = job.getOutputFiles();
        if (fileNames != null && fileNames.length > 0) {

            wrapper.append("declare -a __output_file\n");
            wrapper.append("declare -a __output_transfer_cmd\n");
            wrapper.append("declare -a __output_file_dest\n\n");

            String sandboxBaseDestURI = job.getOutputSandboxBaseDestURI();
            String[] sandboxDestURI = job.getOutputSandboxDestURI();
            if (sandboxBaseDestURI != null) {
                if (!sandboxBaseDestURI.endsWith("/"))
                    sandboxBaseDestURI = sandboxBaseDestURI + "/";

                if (sandboxBaseDestURI.startsWith("gsiftp://"))
                    wrapper.append("__gsiftp_dest_uri=").append(sandboxBaseDestURI).append("\n");
                else if (sandboxBaseDestURI.startsWith("https://"))
                    wrapper.append("__https_dest_uri=").append(sandboxBaseDestURI).append("\n");
                else
                    sandboxBaseDestURI = null;
            }

            if (sandboxDestURI != null && sandboxDestURI.length != fileNames.length)
                throw new IllegalArgumentException("Number mismatch for OutputSandboxDestURI");

            if (!(sandboxBaseDestURI == null ^ sandboxDestURI == null))
                throw new IllegalArgumentException("Missing or duplicate sandboxBaseDestURI and sandboxDestURI");

            for (int k = 0; k < fileNames.length; k++) {

                String fName = filenameNorm(fileNames[k]);
                if (fName.indexOf('/') != 0) {
                    fName = "${workdir}/" + fName;
                }

                wrapper.append("__output_file[").append(k).append("]=");
                wrapper.append(stringNorm(fName)).append("\n");

                if (sandboxDestURI != null) {
                    wrapper.append("__output_file_dest[").append(k).append("]=");
                    wrapper.append(sandboxDestURI[k]).append("\n");

                    if (sandboxDestURI[k].startsWith("gsiftp")) {

                        wrapper.append("__output_transfer_cmd[").append(k).append("]=\"\\${globus_transfer_cmd}\"\n");

                    } else if (sandboxDestURI[k].startsWith("https")) {

                        wrapper.append("__output_transfer_cmd[").append(k).append("]=\"\\${https_transfer_cmd}\"\n");

                    } else {
                        throw new IllegalArgumentException("Unsupported protocol");
                    }
                }

            }
            wrapper.append("\n");
        }

        String templateName = (job.isMpich()) ? WRAPPER_MPI_TEMPLATE_NAME : WRAPPER_TEMPLATE_NAME;
        
        if (wrapperTemplateHashTable.get(templateName) == null) {
        	wrapperTemplateHashTable.put(templateName, getWrapperTemplate(templateName));
        }
        wrapper.append(wrapperTemplateHashTable.get(templateName));
        return wrapper.toString();
    }
    
    private static String getWrapperTemplate(String templateName) throws IOException {
    	MessageContext currentContext = MessageContext.getCurrentContext();
        String jwPath = (String)currentContext.getProperty("configPath"); 

        FileReader templateFileReader = null;
       
        try{
    	  templateFileReader = new FileReader(jwPath + "/" + templateName);
        } catch(FileNotFoundException fnf){
          throw new IOException("Cannot find jobwrapper template");
        }
        StringBuffer wrapperTemplate = new StringBuffer();
        BufferedReader in = new BufferedReader(templateFileReader);
        String line = in.readLine();
        while (line != null) {
        	wrapperTemplate.append(line + "\n");
            line = in.readLine();
        }
        try{
          in.close();
          templateFileReader.close();
        } catch (IOException ioe){
        	//nothing.
        }
        return wrapperTemplate.toString();
    }

    public static void main(String[] args) throws java.net.MalformedURLException, Exception {

        if (args.length != 1) {
            System.out.println("Bad parameter");
            System.exit(1);
        }

        StringBuffer jdl = new StringBuffer();
        BufferedReader jdlReader = null;

        try {
            jdlReader = new BufferedReader(new FileReader(args[0]));
            String tmps = jdlReader.readLine();
            while (tmps != null) {
                jdl.append(tmps);
                tmps = jdlReader.readLine();
            }
            jdlReader.close();
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            if (jdlReader != null)
                jdlReader.close();
            System.exit(1);
        }

        Job tmpJob = new Job(jdl.toString());
        // dummy CE gridftp site
        tmpJob.setCREAMInputSandboxURI("file:///tmp");
        tmpJob.setId("https://lxgianelle.pd.infn.it:9000/CREAM-542526256534");
        tmpJob.setGridJobId("https://lxgianelle.pd.infn.it:9000/GRID-542526256534");
        tmpJob.setHlrLocation("hlr.location.net");

        System.out.println(buildWrapper(tmpJob));
    }

}
