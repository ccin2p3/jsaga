package org.glite.security.authz;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.apache.log4j.Logger;

import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;

/**
 * A class for collecting attributes from a VOMS proxy 
 * certificate, and stored in the peerSubject
 */

public class VomsServicePIP implements ServicePIP {
	
	private static Logger logger =
		Logger.getLogger(VomsServicePIP.class.getName());

	
	/**
     * collects attributes and populates the subject with
     * public or private credentials to be checked by subsequent
     * PDPs in the same interceptor chain.
     * @param peerSubject authenticated subject for which attributes
     *                    should be collected
     * @param context holds properties of this XML message exchange
     * @param operation operation that the subject wants to invoke
     * @throws AttributeException if an exception occurred while getting
     *                            the attributes
     */
	public void collectAttributes(Subject peerSubject, MessageContext context,
			QName operation) throws AttributeException {
		logger.debug("Starting to collect VomsAttributes");
		
		String id = AuthzUtil.getIdentity(peerSubject);
		Set credSet = peerSubject.getPublicCredentials();
 	    
        Vector rolesVector = new Vector();
        
       //Composing certChain to be sent to VOMSValidator
        X509Certificate[] certChain = null;
        Set certChainSet = peerSubject.getPublicCredentials((new X509Certificate[0]).getClass());
       
        if( certChainSet.size()==1 ){
        	certChain = (X509Certificate[])certChainSet.iterator().next();
        	logger.debug("Using the complete cert chain");
        }else if( certChainSet.size()>1 ) {
        	throw new AttributeException("Cannot store multiple arrays in Subject");
        }else{
            ArrayList tmpList = new ArrayList();
            Iterator iter = credSet.iterator();       
            for (int i = 0; i < credSet.size(); i++) {  
            	Object tmpo = iter.next();
            	if( tmpo instanceof X509Certificate )
            		tmpList.add(tmpo);
            }
            certChain = new X509Certificate[tmpList.size()];
            tmpList.toArray(certChain);
        }        
        
        VOMSValidator vv = new VOMSValidator(certChain).validate();
        rolesVector = (Vector) vv.getVOMSAttributes();
        if (rolesVector == null || rolesVector.size() == 0) {
            throw new AttributeException("No roles in Subject credentials found");
        } else {
        
	        logger.debug("Finding the FQAN for "+id);
	        
	        for(int i = 0;i < rolesVector.size(); i++){
	        	VOMSAttribute attr = (VOMSAttribute)rolesVector.get(i);
	        	Vector fqanList = (Vector) attr.getFullyQualifiedAttributes();
	        	
	        	for (int j = 0; j < fqanList.size(); j++) {
	                String attrString = (String) fqanList.get(j);
	                logger.debug("Got FQAN "+attrString);
	        	}
	        	
	        	PIPAttribute vomsAttribute = new PIPAttribute("VOMS_FQAN",attr);        	
	        	vomsAttribute.addPublic(peerSubject);
	        }
        }
    }

	public void close() throws CloseException {
		// TODO Auto-generated method stub

	}

	public void initialize(ChainConfig config, String name, String id)
			throws InitializeException {
		// TODO Auto-generated method stub

	}

}
