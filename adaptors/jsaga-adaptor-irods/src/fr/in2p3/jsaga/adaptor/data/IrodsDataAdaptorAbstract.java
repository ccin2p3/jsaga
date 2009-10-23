package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.*;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.*;

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
public abstract class IrodsDataAdaptorAbstract implements DataReaderAdaptor {
	protected GeneralFileSystem fileSystem;
	protected final static String SEPARATOR = "/";
	protected final static String FILE = "file";
	protected final static String DIR = "dir";
	protected final static String DOT =  "\\.";
	protected final static String DEFAULTRESOURCE	= "defaultresource", DOMAIN="domain", ZONE="zone", METADATAVALUE="metadatavalue";
	protected String srbHost, srbPort, userName, passWord, mdasDomainName, mcatZone, defaultStorageResource, metadataValue;
	protected SecurityAdaptor securityAdaptor;
	protected GSSCredential cert;

	public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{UserPassSecurityAdaptor.class, GSSCredentialSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
		this.securityAdaptor = securityAdaptor;	
    }
	
	public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		GeneralFile generalFile =  FileFactory.newFile(fileSystem, absolutePath);
        return generalFile.exists();
	}

	public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		GeneralFile generalFile =  FileFactory.newFile(fileSystem, absolutePath);
		return new GeneralFileAttributes(generalFile);
    }

	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
		GeneralFile parentFile =  FileFactory.newFile(fileSystem, parentAbsolutePath);
		if (!parentFile.exists()) {throw new ParentDoesNotExist(parentAbsolutePath);}
	
		GeneralFile generalFile =  FileFactory.newFile(fileSystem, parentAbsolutePath +SEPARATOR + directoryName);
		if (generalFile.exists()) {throw new AlreadyExistsException(parentAbsolutePath+SEPARATOR + directoryName);}

		generalFile.mkdir();
	}
}