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

	private String jobId ;
	private WMProxyAPI m_client;
	private GSSCredential m_credential;
	private String stdoutFile, stderrFile;

	public WMSJobIOHandler(String jobId, 
			WMProxyAPI m_client,
			GSSCredential m_credential,
			String stdoutFile,
			String stderrFile) {
		this.jobId = jobId;
		this.m_client = m_client;
		this.m_credential = m_credential;
		this.stdoutFile = stdoutFile;
		this.stderrFile = stderrFile;
	}

	public String getJobId() {
		return jobId;
	}

	public InputStream getStderr() throws PermissionDenied, Timeout, NoSuccess {
		try {
			getSandboxFile(stderrFile);
			return new FileInputStream(stderrFile);
		} catch (FileNotFoundException e) {
			throw new NoSuccess(e);
		}
	}

	public InputStream getStdout() throws PermissionDenied, Timeout, NoSuccess {
		try {
			getSandboxFile(stdoutFile);
			return new FileInputStream(stdoutFile);
		} catch (FileNotFoundException e) {
			throw new NoSuccess(e);
		}
	}
	
	private void getSandboxFile(String file) throws NoSuccess, PermissionDenied {
		
		try {
			//Use the "gsiftp" transfer protocols to retrieve the list of files produced by the jobs.
	        StringAndLongList result = m_client.getOutputFileList(jobId, "gsiftp");
	        if ( result != null ) 
	        {
	            // list of files+their size
	            StringAndLongType[] list = (StringAndLongType[]) result.getFile();            
	            if (list != null) {
	                for (int i=0; i<list.length ; i++){
	                	System.out.println("get:"+list[i].getName()+" > "+new File(file).getName());
	                	if(list[i].getName().endsWith(new File(file).getName())) {
		                    // Creation of the "fromURL" link from where download the file(s).
		                    String port = list[i].getName().substring(list[i].getName().lastIndexOf(":")+1,list[i].getName().length());
		                    port = port.substring(0, port.indexOf("/"));                        
		                    int pos = (list[i].getName()).indexOf(port);
		                    int length = (list[i].getName()).length();                      
		                    String front = (list[i].getName()).substring(0 , pos);
		                    String rear = (list[i].getName()).substring(pos + 4 , length);                      
		                    String fromURL = front + port + "/" + rear;
		                    
		                    String toURL = "file:///" + file;	                        
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
		}
	}
}