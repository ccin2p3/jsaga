package fr.in2p3.jsaga.adaptor.bes.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.ogf.saga.error.NoSuccessException;
import org.xml.sax.SAXException;

import fr.in2p3.jsaga.adaptor.bes.BesUtils;
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.EndpointReferenceType;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJob
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   26 Nov 2010
* ***************************************************/

public class BesJob {

	private final static String m_root = System.getProperty("user.home") + "/.jsaga/var/adaptor/bes/";
	
	protected String m_nativeJobId;
	
	/**
	 * set activity identifier as an EndpointReferenceType
	 * 
	 * This methods stores a fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.EndpointReferenceType object
	 * like:
	 *  <ns2:EndpointReferenceType xsi:type="ns2:EndpointReferenceType" xmlns:ns2="http://www.w3.org/2005/08/addressing">
	 *    	<ns2:Address xsi:type="ns2:AttributedURIType">https://localhost6.localdomain6:8080/DEMO-SITE/services/BESActivity?res=70cc4add-1787-46d7-ad4c-7bc378883294</ns2:Address>
  	 * 		<ns2:ReferenceParameters xsi:type="ns2:ReferenceParametersType">
   	 * 			<unic:ResourceId xmlns:unic="http://www.unicore.eu/unicore6">70cc4add-1787-46d7-ad4c-7bc378883294</unic:ResourceId>
  	 *		</ns2:ReferenceParameters>
  	 *		<ns2:Metadata>
  	 *		...
  	 *		</ns2:Metadata>
 	 * 	</ns2:EndpointReferenceType>
 	 * in a XML file in the user's home
	 * @param epr is the the activity identifier
	 * @throws NoSuccessException 
	 */
	public void setActivityId(EndpointReferenceType epr) throws NoSuccessException {
		// compute hash of EPR and store the EPR as String in a file named hash.xml
		try {
			// Create root directory if not exists
			File rdir = new File(m_root);
			if (! rdir.exists())
				rdir.mkdirs();
			
			// Serialize ActivityIdentifier
			byte[] serialized = BesUtils.serialize(EndpointReferenceType.getTypeDesc().getXmlType(), epr).getBytes();
			
			m_nativeJobId = getMD5sum(serialized);
			
			// Store in file
	        OutputStream out = new FileOutputStream(getXmlJob());
	        out.write(serialized);
	        out.close();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	/**
	 * set activity identifier as a String
	 * 
	 * @param jobId is the native jobId
	 */
	public void setNativeId(String jobId) {
		m_nativeJobId = jobId;
	}
	
	/**
	 * returns the native job identifier
	 * @return String the native job identifier
	 */
	public String getNativeId() throws NoSuccessException {
		return m_nativeJobId;
	}
	
	/**
	 * returns the BES activity identifier (EndpointReferenceType)
	 * @return EndpointReferenceType the BES activity identifier
	 * 
	 * The activity identifier is stored as serialized EPR in a XML file in the user HOME
	 */
	public EndpointReferenceType getActivityIdentifier() throws NoSuccessException {
		// Read XML file
		File refFile = getXmlJob();
		try {
			EndpointReferenceType _job_endpoint;
	        if (refFile.exists()) {
	            byte[] ref = new byte[(int)refFile.length()];
	            InputStream in = new FileInputStream(refFile);
	            if (in.read(ref) > -1) {
	            	_job_endpoint = (EndpointReferenceType) BesUtils.deserialize(new String(ref), EndpointReferenceType.class);
	            } else {
	            	throw new NoSuccessException("Could not read file " + refFile.getAbsolutePath());
	            }
	            in.close();
	        } else {
	        	throw new NoSuccessException("File " + refFile.getAbsolutePath() + " does not exist");
	        }
	        return _job_endpoint;
		} catch (IOException e) {
        	throw new NoSuccessException("Could not read file " + refFile.getAbsolutePath(), e);
		} catch (SAXException e) {
        	throw new NoSuccessException("Could not deserialize file " + refFile.getAbsolutePath(), e);
		}
	}

	/**
	 * returns the name of the XML file that contains the serialization of the activity identifier (EPR)
	 * @return the name of the XML file that contains the serialization of the activity identifier (EPR)
	 * @throws NoSuccessException if the job id was not set
	 */
	public File getXmlJob() throws NoSuccessException {
		if (m_nativeJobId == null)
			throw new NoSuccessException("No nativeJobId yet");
		return getXmlJob(m_nativeJobId);
	}
	
	/**
	 * returns the name of the XML file of a given job ID
	 * @param nativeJobId the job ID
	 * @return the name of the XML file ("$HOME/.jsaga/var/adaptor/bes/<jobID>.xml")
	 */
	public static File getXmlJob(String nativeJobId) {
		return new File(m_root, nativeJobId + ".xml");
	}
	
	private static String getMD5sum(byte[] bytes) throws NoSuchAlgorithmException {
		BigInteger bigInt = new BigInteger(1, MessageDigest.getInstance("MD5").digest(bytes));
		return bigInt.toString(16);
	}
}
