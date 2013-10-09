package fr.in2p3.jsaga.adaptor.dirac.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.dirac.DiracAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.dirac.util.DiracConstants;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DiracOSBDataAdaptor
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   8 oct 2013
 * ***************************************************/

public class DiracOSBDataAdaptor extends DiracAdaptorAbstract implements FileReaderStreamFactory {

	public boolean exists(String absolutePath, String additionalArgs)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		// not used
		return true;
	}

	public FileAttributes getAttributes(String absolutePath,
			String additionalArgs) throws PermissionDeniedException,
			DoesNotExistException, TimeoutException, NoSuccessException {
		// not used
		return null;
	}

	public FileAttributes[] listAttributes(String absolutePath,
			String additionalArgs) throws PermissionDeniedException,
			BadParameterException, DoesNotExistException, TimeoutException,
			NoSuccessException {
		// not used
		return null;
	}

	public void connect(String userInfo, String host, int port,
			String basePath, Map attributes) throws NotImplementedException,
			AuthenticationFailedException, AuthorizationFailedException,
			IncorrectURLException, BadParameterException, TimeoutException,
			NoSuccessException {
		super.connect(userInfo, host, port, basePath, attributes);
        // Get the access_token
		if (attributes.containsKey(DiracConstants.DIRAC_GET_PARAM_ACCESS_TOKEN)) {
			this.m_accessToken = (String)attributes.get(DiracConstants.DIRAC_GET_PARAM_ACCESS_TOKEN);
			m_logger.debug("Token="+this.m_accessToken);
		} else {
			throw new AuthenticationFailedException("Missing " + DiracConstants.DIRAC_GET_PARAM_ACCESS_TOKEN);
		}
	}

	public String getType() {
		return "dirac-osb";
	}

	public Usage getUsage() {
		return new U(DiracConstants.DIRAC_GET_PARAM_ACCESS_TOKEN);
	}

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
		return null;
	}

	public InputStream getInputStream(String absolutePath, String additionalArgs)
			throws PermissionDeniedException, BadParameterException,
			DoesNotExistException, TimeoutException, NoSuccessException {
		return this.getOSBInputStream(absolutePath);
	}

	private InputStream getOSBInputStream(String absolutePath) throws DoesNotExistException, 
							PermissionDeniedException, NoSuccessException, BadParameterException {
		// Separate filename and path
		String filename = absolutePath.substring(absolutePath.lastIndexOf("/")+1);
		String path = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
		
		// Get all files in OSB in a bzip2 TAR
		DiracRESTClient client = new DiracRESTClient();
		InputStream tarbzInputFile;
		try {
			tarbzInputFile = client.getStream(new URL(m_url, path), "GET");
		} catch (UnrecoverableKeyException e) {
			throw new PermissionDeniedException(e);
		} catch (KeyManagementException e) {
			throw new PermissionDeniedException(e);
		} catch (KeyStoreException e) {
			throw new PermissionDeniedException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new PermissionDeniedException(e);
		} catch (CertificateException e) {
			throw new PermissionDeniedException(e);
		} catch (IncorrectURLException e) {
			throw new NoSuccessException(e);
		} catch (MalformedURLException e) {
			throw new BadParameterException(e);
		} catch (IOException e) {
			throw new DoesNotExistException(e);
		}
		
		// unzip
		BZip2CompressorInputStream uncompressedInputStream;
		try {
			uncompressedInputStream = new BZip2CompressorInputStream(tarbzInputFile);
		} catch (IOException e) {
			try {
				tarbzInputFile.close();
			} catch (IOException e1) {
			}
			throw new NoSuccessException(e);
		}
		
		// extract a particular file
		TarArchiveInputStream tarInputStream;
		tarInputStream = new TarArchiveInputStream(uncompressedInputStream);
		try {
			TarArchiveEntry entry = tarInputStream.getNextTarEntry();
			while (entry != null) {
				// If file found, return inputstream
				m_logger.debug("Entry: " + entry.getName());
				if (entry.getName().equals(filename)) {
					return tarInputStream;
				}
				entry = tarInputStream.getNextTarEntry();
			}
		} catch (IOException e) {
			try {
				tarbzInputFile.close();
				uncompressedInputStream.close();
				tarInputStream.close();
			} catch (IOException e1) {
			}
		}
		try {
			tarbzInputFile.close();
			uncompressedInputStream.close();
			tarInputStream.close();
		} catch (IOException e1) {
		}
		throw new DoesNotExistException(filename);
	}
}
