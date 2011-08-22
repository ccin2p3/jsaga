package fr.in2p3.jsaga.adaptor.unicore.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.adaptor.unicore.UnicoreAbstract;

import org.apache.log4j.Logger;
import org.ogf.saga.error.*;
import org.unigrids.services.atomic.types.GridFileType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.client.SByteIOClient;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.StorageFactoryClient;
import de.fzj.unicore.wsrflite.xmlbeans.BaseFault;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreDataAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   22 aout 2011
* ***************************************************/
/**
 *
 */
public class UnicoreDataAdaptor extends UnicoreAbstract implements FileWriterPutter, FileReaderGetter {
    private static Logger s_logger = Logger.getLogger(UnicoreDataAdaptor.class);
	private String m_serverFileSeparator ;
	private StorageClient m_client;
    private String rootDirectory = ".";
    public String getType() {
        return "unicore";
    }

    public Usage getUsage() {
    	return new UAnd(new Usage[]{new U(TARGET),
    								new U(SERVICE_NAME), 
    								new U(RES),
    								});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default(TARGET, "DEMO-SITE"),
    			new Default(SERVICE_NAME, "StorageManagement"), 
    			new Default(RES, "default_storage"),
    			};
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
    	
    	try {
			m_client = new StorageClient(m_epr,m_uassecprop);

			// TODO customize this
			m_serverFileSeparator = "/";
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
    }

    public void disconnect() throws NoSuccessException {
    	m_client = null;
    	super.disconnect();
    }
    
    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	// prepare path
    	//absolutePath = getEntryPath(absolutePath);
		// if absolutePath is the root
		if(absolutePath.equals(rootDirectory)) {
			return true;
		}
		try {
			GridFileType gft = m_client.listProperties(absolutePath);
		} catch (BaseFault e) {
			e.printStackTrace();
			throw new NoSuccessException(e);
		} catch (FileNotFoundException e) {
			return false;
		}
    	return true;
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        //prepare path and connection
        //absolutePath = getEntryPath(absolutePath);
		try {
			GridFileType gft = m_client.listProperties(absolutePath);
			return new UnicoreFileAttributes(gft, m_serverFileSeparator);
		} catch (BaseFault e) {
			e.printStackTrace();
			throw new NoSuccessException(e);
		} catch (FileNotFoundException e) {
			throw  new DoesNotExistException(e);
		}
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
    	// prepare path
    	//absolutePath = getEntryPath(absolutePath);
    	try {
    		GridFileType[] _list = m_client.listDirectory(absolutePath);
        	FileAttributes[] attrs = new FileAttributes[_list.length];
        	int i=0;
			for (GridFileType gft : _list) {
				attrs[i++] = new UnicoreFileAttributes(gft, m_serverFileSeparator);
			}
			return attrs;
		} catch (BaseFault e) {
			throw new NoSuccessException(e);
		}
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
		try {
			if (!exists(parentAbsolutePath, additionalArgs)) throw new ParentDoesNotExist("Not found: " + parentAbsolutePath);
			if (exists(parentAbsolutePath + directoryName, additionalArgs)) throw new AlreadyExistsException("Already exists: " + parentAbsolutePath + directoryName);
			m_client.createDirectory(parentAbsolutePath + directoryName);
		} catch (BaseFault e) {
			e.printStackTrace();
			throw new NoSuccessException(e);
		}
    }

    private void delete(String path) throws NoSuccessException{
		try {
			m_client.delete(path);
		} catch (BaseFault e) {
			e.printStackTrace();
			throw new NoSuccessException(e);
		}
    }
    
    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
    	// prepare path
		delete(parentAbsolutePath + directoryName) ;
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
    	//prepare path
		delete(parentAbsolutePath + fileName);
    }

	public void putFromStream(String absolutePath, boolean append, String additionalArgs,
			InputStream stream) throws PermissionDeniedException,
            BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
		if(append) {
			throw new NoSuccessException("Append not supported.");
		}
		try {
			SByteIOClient io_client = m_client.getWriteStream(absolutePath);
			io_client.writeAllData(stream);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public void getToStream(String absolutePath, String additionalArgs,
			OutputStream stream) throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
		try {
			SByteIOClient io_client = m_client.getReadStream(absolutePath);
			io_client.readAllData(stream);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
	}

}
