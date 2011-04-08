package fr.in2p3.jsaga.impl.job.streaming;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOHandler;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobBatch;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GenericStreamableJobAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   7 avril 2011
* ***************************************************
* Description:                                      */

/**
 * This class is a wrapper to a JobControlAdaptor for emulating streaming via data staging
 */
public class GenericStreamableJobAdaptor implements StreamableJobBatch {

	private JobControlAdaptor m_adaptor;

	// TODO: restrict to adaptors that implement data staging
	public GenericStreamableJobAdaptor(JobControlAdaptor adaptor) {
		m_adaptor = adaptor;
	}
	
	public JobIOHandler submit(String jobDesc, boolean checkMatch,
			String uniqId, InputStream stdin) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		File input = LocalFileFactory.getLocalInputFile(uniqId);
        OutputStream out;
		try {
			out = new FileOutputStream(input);
			if (stdin != null) {
				int n;
				byte[] buffer = new byte[1024];
				while ((n = stdin.read(buffer)) != -1) {
					out.write(buffer, 0, n);
				}
			} else  {
				out.write(' ');	// if null input was provided, write a char as middleware may not support empty input files (e.g ARC)
			}
	        out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new NoSuccessException("Standard input could not be written to local file: " + input);
		}
		String jobId = m_adaptor.submit(jobDesc, checkMatch, uniqId);
		return new GenericJobIOHandler(jobId, uniqId);
	}

	//////////////////////////////////////
	// Just a wrapper to JobControlAdaptor
	//////////////////////////////////////
	
	public JobDescriptionTranslator getJobDescriptionTranslator()
			throws NoSuccessException {
		return m_adaptor.getJobDescriptionTranslator();
	}

	public JobMonitorAdaptor getDefaultJobMonitor() {
		return m_adaptor.getDefaultJobMonitor();
	}

	public String submit(String jobDesc, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException, BadResource {
		return m_adaptor.submit(jobDesc, checkMatch, uniqId);
	}

	public void cancel(String nativeJobId) throws PermissionDeniedException,
			TimeoutException, NoSuccessException {
		m_adaptor.cancel(nativeJobId);
	}

	public Class[] getSupportedSecurityCredentialClasses() {
		return m_adaptor.getSupportedSecurityCredentialClasses();
	}

	public void setSecurityCredential(SecurityCredential credential) {
		m_adaptor.setSecurityCredential(credential);
	}

	public int getDefaultPort() {
		return m_adaptor.getDefaultPort();
	}

	public void connect(String userInfo, String host, int port,
			String basePath, Map attributes) throws NotImplementedException,
			AuthenticationFailedException, AuthorizationFailedException,
			IncorrectURLException, BadParameterException, TimeoutException,
			NoSuccessException {
		m_adaptor.connect(userInfo, host, port, basePath, attributes);
	}

	public void disconnect() throws NoSuccessException {
		m_adaptor.disconnect();
	}

	public String getType() {
		return m_adaptor.getType();
	}

	public Usage getUsage() {
		return m_adaptor.getUsage();
	}

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
		return m_adaptor.getDefaults(attributes);
	}

}
