package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterPseudo;

import org.glite.wms.wmproxy.AuthenticationFaultException;
import org.glite.wms.wmproxy.AuthorizationFaultException;
import org.glite.wms.wmproxy.InvalidArgumentFaultException;
import org.glite.wms.wmproxy.JobUnknownFaultException;
import org.glite.wms.wmproxy.OperationNotAllowedFaultException;
import org.glite.wms.wmproxy.ServiceException;
import org.glite.wms.wmproxy.StringAndLongList;
import org.glite.wms.wmproxy.StringAndLongType;
import org.glite.wms.wmproxy.WMProxyAPI;
import org.globus.io.urlcopy.UrlCopy;
import org.globus.io.urlcopy.UrlCopyException;
import org.globus.util.GlobusURL;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobIOHandler
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   13 mai 2008
* ***************************************************/

public class WMSJobIOHandler implements JobIOGetterPseudo {

	private String jobId, stdoutFileName, stderrFileName;
	private WMProxyAPI m_client;
	protected GSSCredential m_credential;

	public WMSJobIOHandler(String jobId, 
			WMProxyAPI m_client,
			GSSCredential m_credential,
			String stdoutFileName,
			String stderrFileName) {
		this.jobId = jobId;
		this.m_client = m_client;
		this.m_credential = m_credential;
		this.stdoutFileName = stdoutFileName;
		this.stderrFileName = stderrFileName; 
	}

	public String getJobId() {
		return jobId;
	}

	public InputStream getStderr() throws PermissionDenied, Timeout, NoSuccess {
		try {
			return new FileInputStream(getSandboxFile(stderrFileName));
		} catch (FileNotFoundException e) {
			throw new NoSuccess(e);
		}
	}

	public InputStream getStdout() throws PermissionDenied, Timeout, NoSuccess {
		try {
			return new FileInputStream(getSandboxFile(stdoutFileName));
		} catch (FileNotFoundException e) {
			throw new NoSuccess(e);
		}
	}
	
	private File getSandboxFile(String filename) throws NoSuccess, PermissionDenied {
		
		try {
			// create tmp file
			File resultFile = File.createTempFile("sandbox",".txt");
			resultFile.deleteOnExit();
			
			//Use the "gsiftp" transfer protocols to retrieve the list of files produced by the jobs.
	        StringAndLongList result = m_client.getOutputFileList(jobId, "gsiftp");
	        if ( result != null ) 
	        {
	            // list of files+their size
	            StringAndLongType[] list = (StringAndLongType[]) result.getFile();            
	            if (list != null) {
	                for (int i=0; i<list.length ; i++){
	                	if(list[i].getName().endsWith(filename)) {
		                    // Creation of the "fromURL" link from where download the file(s).
		                    String port = list[i].getName().substring(list[i].getName().lastIndexOf(":")+1,list[i].getName().length());
		                    port = port.substring(0, port.indexOf("/"));                        
		                    int pos = (list[i].getName()).indexOf(port);
		                    int length = (list[i].getName()).length();                      
		                    String front = (list[i].getName()).substring(0 , pos);
		                    String rear = (list[i].getName()).substring(pos + 4 , length);                      
		                    String fromURL = front + port + "/" + rear;
		                    
		                    String toURL = "file:///" + resultFile.getAbsolutePath();	                        
	                        //Retrieve the file(s) from the WMProxy Server.
	                        GlobusURL from = new GlobusURL(fromURL);
	                        GlobusURL to = new GlobusURL(toURL);
	                        
	                        UrlCopy uCopy = new UrlCopy();
	                        uCopy.setDestinationCredentials(m_credential);
	                        uCopy.setSourceCredentials(m_credential);
	                        uCopy.setDestinationUrl(to);
	                        uCopy.setSourceUrl(from);
	                        uCopy.setUseThirdPartyCopy(true);
	                        uCopy.copy();
	                    }
	                }
	            }
	        }
	        return resultFile;
		} catch( UrlCopyException e) {
			throw new NoSuccess(e);
		} catch (MalformedURLException e) {
			throw new NoSuccess(e);
		} catch (AuthorizationFaultException e) {
			throw new PermissionDenied(e);
		} catch (AuthenticationFaultException e) {
			throw new PermissionDenied(e);
		} catch (OperationNotAllowedFaultException e) {
			throw new PermissionDenied(e);
		} catch (InvalidArgumentFaultException e) {
			throw new NoSuccess(e);
		} catch (JobUnknownFaultException e) {
			throw new NoSuccess(e);
		} catch (ServiceException e) {
			throw new NoSuccess(e);
		} catch (IOException e) {
			throw new NoSuccess(e);
		}
	}
}