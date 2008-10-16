package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.GeneralFileSystem;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.*;

import java.util.*;
import java.lang.Exception;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   IrodsDataAdaptorAbstract
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class IrodsDataAdaptorAbstract implements DataReaderAdaptor, FileReaderStreamFactory, FileWriterStreamFactory {
	protected GeneralFileSystem fileSystem;
	protected final static String SEPARATOR = "/";
	protected final static String FILE = "file";
	protected final static String DIR = "dir";
	protected final static String DOT =  "\\.";
	protected final static String DEFAULTRESOURCE	= "defaultresource", DOMAIN="domain", ZONE="zone";
	protected String userName, passWord, mdasDomainName, mcatZone, defaultStorageResource;
	protected SecurityAdaptor securityAdaptor;
	protected GSSCredential cert;

	public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{UserPassSecurityAdaptor.class, GSSCredentialSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
		this.securityAdaptor = securityAdaptor;	
    }
	
	public boolean exists(String absolutePath, String additionalArgs) throws PermissionDenied, Timeout, NoSuccess {
        // TODO: check existence as efficiently as possible, else re-use getAttributes()
        return true;
	}

	void parseValue(Map attributes) throws NoSuccess {
	
		if (securityAdaptor instanceof UserPassSecurityAdaptor) {
            try {
                userName = securityAdaptor.getUserID();
            } catch (Exception e) {
                throw new NoSuccess(e);
            }
            passWord = ((UserPassSecurityAdaptor)securityAdaptor).getUserPass();
		} else if (securityAdaptor instanceof GSSCredentialSecurityAdaptor) {
			userName = null;
            passWord = null;
        }
		
		// Parsing for defaultResource
		Set set = attributes.entrySet();
		Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            String key = ((String)me.getKey()).toLowerCase();
            String value =(String)me.getValue();
			
            if (key.equals(DEFAULTRESOURCE)) {
                defaultStorageResource = value;
            } else if (key.equals(DOMAIN)) {
				mdasDomainName  = value;
			} else if (key.equals(ZONE)) {
				mcatZone  = value;
			}
			
        }
    }
}