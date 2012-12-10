package org.glite.security.authz.pdp;


/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://public.eu-egee.org/partners/ for details on the copyright
 * holders.For license conditions see the license file or http://www.eu-egee.org/license.html
 *
 */
/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 * Modified and redistributed under the terms of the Apache Public
 * License, found at http://www.apache.org/licenses/LICENSE-2.0
 *  
*/


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.apache.log4j.Logger;
import org.glite.security.authz.AuthorizationException;
import org.glite.security.authz.AuthzUtil;
import org.glite.security.authz.ChainConfig;
import org.glite.security.authz.CloseException;
import org.glite.security.authz.InitializeException;
import org.glite.security.authz.ServicePDP;

import com.sun.xacml.Indenter;
import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Subject;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.impl.CurrentEnvModule;
import com.sun.xacml.finder.impl.FilePolicyModule;
import com.sun.xacml.finder.impl.SelectorModule;
import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;

/**
 * A PDP that uses an XACML policy enging for determeing the authorization decision.
 * The policies are located in the folder polycies.
 * @author Håkon Sagehaug
 */

public class XACMLServicePDP implements ServicePDP {
	
	private static Logger logger = Logger.getLogger(XACMLServicePDP.class);
	public static String XACML_POLICY_FILE = "XACMLPolicy";
	public static final String[] CONFIG_LOCATIONS = {
        "", ".", "/etc", "/etc/grid-security/voms"
    };
	
	private PDP pdp = null;

	
	 /**
     * initializes the interceptor with configuration information that are
     * valid up until the point when close is called.
     * @param config holding interceptor specific configuration values, that
     *               may be obtained using the name paramter
     * @param name the name that should be used to access all the interceptor
     *             local configuration
     * @param id the id in common for all interceptors in a chain (it is valid
     *          up until close is called)
     *          if close is not called the interceptor may assume that the id
     *          still exists after a process restart
     * @throws InitializeException if vomspdp was not found
     */
	public void initialize(ChainConfig config, String name, String id)
	throws InitializeException {
		
		if (config == null) {
            throw new InitializeException(
                "no configuration object (ChainConfig)found");
        }
	
		//Here we set the policyFile
		
		//Setting the directory that contains the XAMCLPolicies
		String policyDir = "test/XACMLPolicy";
		File XACMLPolicyDir = new File(policyDir);
		File [] XACMLpolicies = XACMLPolicyDir.listFiles();
		// Create a PolicyFinderModule and initialize it...in this case,
		// we're using the sample FilePolicyModule that is pre-configured
		// with a set of policies from the filesystem
		FilePolicyModule filePolicyModule = new FilePolicyModule();
		
		for(int i = 0; i < XACMLpolicies.length; i++){
			if(!XACMLpolicies[i].isDirectory()){
				filePolicyModule.addPolicy(policyDir+"/"+XACMLpolicies[i].getName());				
			}				
		}			
		
		// next, setup the PolicyFinder that this PDP will use
		PolicyFinder policyFinder = new PolicyFinder();
		Set policyModules = new HashSet();
		policyModules.add(filePolicyModule);
		policyFinder.setModules(policyModules);
		
		// now setup attribute finder modules for the current date/time and
		// AttributeSelectors (selectors are optional, but this project does
		// support a basic implementation)
		CurrentEnvModule envAttributeModule = new CurrentEnvModule();
		SelectorModule selectorAttributeModule = new SelectorModule();

		// Setup the AttributeFinder just like we setup the PolicyFinder. Note
		// that unlike with the policy finder, the order matters here. See the
		// the javadocs for more details.
		AttributeFinder attributeFinder = new AttributeFinder();
		List attributeModules = new ArrayList();
		attributeModules.add(envAttributeModule);
		attributeModules.add(selectorAttributeModule);
		attributeFinder.setModules(attributeModules);
		
		// Try to load the time-in-range function, which is used by several
		// of the examples...see the documentation for this function to
		// understand why it's provided here instead of in the standard
		// code base.
		
		/*
		  
		 FunctionFactoryProxy proxy =
		    StandardFunctionFactory.getNewFactoryProxy();
		FunctionFactory factory = proxy.getConditionFactory();
		factory.addFunction(new TimeInRangeFunction());
		FunctionFactory.setDefaultFactory(proxy);
		*/
		// finally, initialize our pdp
		pdp = new PDP(new PDPConfig(attributeFinder, policyFinder, null));		
		
	}

	 /**
     * this operation is called by the PDP Framework whenever the application
     * needs to call secured operations. The PDP should return true if the
     * local policy allows the subject to invoke the operation. If the PDP
     * has no local knowledge about whether the operation is allowed or not
     * it should return false to allow other PDPs and PIPs in the chain to
     * continue the evaluation. Obligations to be read by other PIPs or PDPs
     * may be set as attributes in the Subject credentials.
     * @param peerSubject authenticated client subject with credentials
     *                    and attributes
     * @param context holds properties of this XML message exchange
     * @param operation operation that the subject wants to invoke
     * @return true if user was found, otherwise false
     * @throws AuthorizationException if an exception occured during evaluation
     */
	
	public boolean isPermitted(javax.security.auth.Subject peerSubject, MessageContext context,
			QName op) throws AuthorizationException {
		logger.debug("Starting the authorization");
		
		String subject = AuthzUtil.getIdentity(peerSubject);
		String operation = op.getLocalPart();
		
		HashSet subjectSet = new HashSet();
		HashSet actionSet = new HashSet();
		HashSet resourceSet = new HashSet();
		HashSet environmentSet = new HashSet();
		
		FileOutputStream requestOut = null;
		BufferedOutputStream bufferedOut = null;
	    Set credSet = peerSubject.getPublicCredentials();
	 	    
	    Vector rolesVector = new Vector();
	        
	    //Composing certChain to be sent to VOMSValidator
	    X509Certificate[] certChain = null;
	    Set certChainSet = peerSubject.getPublicCredentials((new X509Certificate[0]).getClass());
	           
	    if( certChainSet.size()==1 ){
	    	certChain = (X509Certificate[])certChainSet.iterator().next();
	       	logger.debug("Using the complete cert chain");
	    }else if( certChainSet.size()>1 ) {
	    	throw new AuthorizationException("Cannot store multiple arrays in Subject");
	    }else{
	    	ArrayList tmpList = new ArrayList();
	    	Iterator iter = credSet.iterator();       
	    	for (int m = 0; m < credSet.size(); m++) {  
	    		Object tmpo = iter.next();
	    		if( tmpo instanceof X509Certificate )
	    			tmpList.add(tmpo);
	    	}
	    	certChain = new X509Certificate[tmpList.size()];
	    	tmpList.toArray(certChain);
	    }       
	     	
	    VOMSValidator vv = new VOMSValidator(certChain).validate();
	    rolesVector = (Vector) vv.getVOMSAttributes();
	
	    //Should not go any further. 
	    if (rolesVector == null ||  rolesVector.size() == 0) {
        	throw new AuthorizationException(
                "No valid VOMS attributes found in Subject's credentials");
        }else{
        	if (logger.isDebugEnabled()) {
        		for (int i = 0; i < rolesVector.size(); i++) {
        			logger.debug("\nRoles " + rolesVector.get(i));
                }
            }
        }
	    
		logger.debug("Subject "+subject +" wants to preform an "+operation+ " operation");
		try{
			//Create the subject			
			Attribute subjectAttribute = createAttribute("urn:oasis:names:tc:xacml:1.0:subject:subject-id",
					"http://www.w3.org/2001/XMLSchema#string",subject, null, null);
			HashSet attributes = new HashSet();
			attributes.add(subjectAttribute);
					
			for (int i = 0; i < rolesVector.size(); i++) {
                VOMSAttribute vomsAttr = (VOMSAttribute) rolesVector.get(i);
                Vector fqanList = (Vector) vomsAttr.getFullyQualifiedAttributes();
                
                for (int j = 0; j < fqanList.size(); j++) {
                    String fqan = (String) fqanList.get(j);
                    subjectAttribute = createAttribute("FQAN",
    						"http://www.w3.org/2001/XMLSchema#string",
    						fqan,null, null);
                    attributes.add(subjectAttribute);
                    
                }
                
            }
			
			Subject subjectXACML = new Subject(attributes);
			subjectSet.add(subjectXACML);
			logger.debug("Created the subject");
			//Create action
			Attribute actionAttribute = createAttribute("urn:oasis:names:tc:xacml:1.0:action:action-id", 
					"http://www.w3.org/2001/XMLSchema#string",operation, null, null);
			logger.debug("Created the action:"+operation);
			actionSet.add(actionAttribute);
			
			//create resource	
			Attribute resourceAttr = createAttribute("urn:oasis:names:tc:xacml:1.0:resource:resource-id", 
					"http://www.w3.org/2001/XMLSchema#string","http://bccs.uib.no/", null, null);
			/*
			AnyURIAttribute resourceAttr = new AnyURIAttribute(new URI("http://bccs.uib.no/"));
			*/
			resourceSet.add(resourceAttr);
		
			logger.debug("Created the resource");
			
			//Making a request context with subject,resource,action end environments
			RequestCtx request = new RequestCtx(subjectSet,resourceSet,actionSet,environmentSet);
			request.encode(System.out,new Indenter());
			//Write the request to file
			
			requestOut = new FileOutputStream("XACMLRequest.xml");
			bufferedOut = new BufferedOutputStream(requestOut);
				
			request.encode(bufferedOut,new Indenter());
			bufferedOut.flush();
				
			logger.debug("The request is written to file:XACMLRequest.xml");	
				
			//gets the response from the pdp
			logger.debug("The request is going to be evaluated");
			ResponseCtx response = pdp.evaluate(request);
			requestOut = new FileOutputStream("XACMLResponse.xml");
			bufferedOut = new BufferedOutputStream(requestOut);
			response.encode(bufferedOut);
						
			//Collectiong the reesult of the response
			Set resultSet = response.getResults();
			Iterator it = resultSet.iterator();
				
			Result result = (Result)it.next();
			
			logger.debug("Setting the response in the message context");
			context.setProperty("Response",response);
			
			//checking the result 	
			if(result.getDecision() == Result.DECISION_PERMIT){
				return true;	
			}			
			else if(result.getDecision() == Result.DECISION_NOT_APPLICABLE){
				logger.debug("DECISION_NOT_APPLICABLE");				
			}	
			else if(result.getDecision() == Result.DECISION_INDETERMINATE){
				logger.debug("DECISION_INDETERMINATE");
			}				
			else{
				logger.debug("DECISION_DENY");
			}
				
				
		}catch(ParsingException e){
			throw new AuthorizationException("",e);
		}catch(URISyntaxException e){
			throw new AuthorizationException("Wrong URI",e);			
		}catch(UnknownIdentifierException e){
			throw new AuthorizationException("Uknown identifier",e);			
		}catch(FileNotFoundException e){
			throw new AuthorizationException("Can't find file",e);
		}catch(IOException e){			
			throw new AuthorizationException("Wrong when flushing the stream",e);
		}
		finally{
			if(bufferedOut != null){
				try{
					bufferedOut.close();
				}catch(IOException e){
					throw new AuthorizationException("Could not close the stream",e);
				}
			}				
		}
		return false;
	}
	

	public void close() throws CloseException {
		// TODO Auto-generated method stub
	}
		
	private Attribute createAttribute(String id,String type,Object value, String issuer, DateTimeAttribute  issuerInstant)throws
	URISyntaxException, UnknownIdentifierException, ParsingException  {
		
		URI idURI = new URI(id);
		URI typeURI = new URI(type);
		
		AttributeFactory attrFactory = AttributeFactory.getInstance();
		AttributeValue attrValue = attrFactory.createValue(typeURI, value.toString());
		
		return new Attribute(idURI,issuer,issuerInstant,attrValue);
	}

}
