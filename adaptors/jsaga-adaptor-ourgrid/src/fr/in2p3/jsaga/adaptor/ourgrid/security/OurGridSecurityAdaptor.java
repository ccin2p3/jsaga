package fr.in2p3.jsaga.adaptor.ourgrid.security;

import java.util.Map;

import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.ourgrid.job.OurGridConstants;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

/* ***************************************************
 * ***  Distributed Systems Lab(LSD)-UFCG) ***
 * ***   http://www.lsd.ufcg.edu.br        ***
 * ***************************************************
 * File:   OurGridDataAdaptor
 * Author: Patricia Alanis (patriciaam@lsd.ufcg.edu.br)
 * Date:   August 2012
 * ***************************************************/


public class OurGridSecurityAdaptor implements SecurityAdaptor {

    private final String USER_ID = "UserID";
    private final String USER_NAME = "user.name";
    private final String USER_PASS = "UserPass";

    /**
     *  Gets the defaults values for (some of the) attributes supported by this adaptor
     *  These values can be static or dynamically created from the information available on local host
     *  (environment variables, files, ...) and from the attributes map.
     *  @param attributes the attributes set by the user
     *  @return Returns an array of default values
     */
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {

        return new Default[]{new Default(USER_ID, System.getProperty(USER_NAME))};
    }

    /**
     * @return Returns the adaptor type
     */
    public String getType() {

        return OurGridConstants.TYPE_ADAPTOR;
    }

    /**
     * Gets a data structure that describes how to use this adaptor
     * This data structure contains attribute names with usage constraints 
     * (and/or, required/optional, hidden...)
     * @return Returns the usage data structure
     * */
    public Usage getUsage() {

        return new UAnd.Builder()
                        .and(new U(USER_ID))
                        .and(new U(USER_PASS))
                        .build();
    }
    
    /**
     * Creates a security credential and initialize it with the provided attributes
     * @param usage the identifier of the usage
     * @param attributes  the provided attributes
     * @param contextId the identifier of the context instance
     * @return Returns the security credential
     */
    public SecurityCredential createSecurityCredential(int usage, Map attributes,String contextId) 
            throws IncorrectStateException, TimeoutException,NoSuccessException {

        String userPass = null;

        if (attributes.containsKey(USER_PASS)) {

            userPass = (String)attributes.get(USER_PASS);
        }

        return new OurGridSecurityCredencial( (String)attributes.get(USER_ID),userPass);      
    }

    /**
     * Returns the security credential class supported by this adaptor
     */
    public Class getSecurityCredentialClass() {

        return OurGridSecurityCredencial.class;
    }


}