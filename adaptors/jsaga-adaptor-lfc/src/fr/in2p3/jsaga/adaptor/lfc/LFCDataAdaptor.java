package fr.in2p3.jsaga.adaptor.lfc;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.BaseURL;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.LFCReplica;
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.LfcError;
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.ReceiveException;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.url.URL;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * gLite Logical File Catalog (LFC) JSAGA Adaptor
 * 
 * @author Jerome Revillard
 */
public class LFCDataAdaptor implements LogicalReader, LogicalWriter, LinkAdaptor, DataAdaptor {
    private static final int LFC_PORT_DEFAULT = 5010;
    private GSSCredentialSecurityCredential m_vomscredential;
    private String m_vo;
    private LfcConnector m_lfcConnector;	
    
	public String getType() {
		return "lfn";
	}
	
	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
		try{
		m_lfcConnector = LfcConnector.getInstance(host, port, m_vo, m_vomscredential.getGSSCredential());
		}catch (IllegalArgumentException e) {
			throw new NoSuccessException(e.getMessage());
		}
	}
	
	public void disconnect() throws NoSuccessException {
		//TODO: When is it requested..? Should I modify LfcConnector to use only 1 connection?
		//m_lfcConnector.close()
	}

    public final Usage getUsage() {
        return null;
    }

	@SuppressWarnings("unchecked")
	public final Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public final Class<SecurityCredential>[] getSupportedSecurityCredentialClasses() {
        return new Class[]{GSSCredentialSecurityCredential.class};
    }

    public final void setSecurityCredential(SecurityCredential credential) {
    	m_vomscredential = (GSSCredentialSecurityCredential) credential;
        try {
			m_vo = m_vomscredential.getAttribute(Context.USERVO);
		} catch (Exception e) {
			//Should never be raised.
		}
    }

    public final BaseURL getBaseURL() throws IncorrectURLException {
        return new BaseURL(LFC_PORT_DEFAULT);
    }

	public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {
			return m_lfcConnector.exist(absolutePath);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(LfcError.OPERATION_NOT_PERMITTED.equals(e.getLFCError()) || LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(LfcError.OPERATION_NOT_PERMITTED.toString());
			}else{
				throw new NoSuccessException(e);
			}
		}
	}

	public void addLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
		try{
			//TODO: check if all the tests are needed or just see if we can just analyze the error
			//Test if the entry is already in the LFC
			String guid= null;
			if(!m_lfcConnector.exist(logicalEntry)){
				//No then create it
				org.ogf.saga.file.File replicaFile;
				try {
					replicaFile = FileFactory.createFile(replicaEntry);
				} catch (Exception e) {
					throw new NoSuccessException(e);
				}
				try {
					m_lfcConnector.create(replicaFile.getSize());
				} catch (NotImplementedException e) {
					throw new NoSuccessException("Unable to get the file size of "+replicaEntry, e);
				} catch (AuthenticationFailedException e) {
					throw new NoSuccessException("Unable to get the file size of "+replicaEntry, e);
				} catch (AuthorizationFailedException e) {
					throw new NoSuccessException("Unable to get the file size of "+replicaEntry, e);
				}
			}
			// add replica location (if it does not already exist)
			Collection<LFCReplica> replicas = m_lfcConnector.listReplicas(logicalEntry, null);
			for (Iterator<LFCReplica> iterator = replicas.iterator(); iterator.hasNext();) {
				LFCReplica lfcReplica = iterator.next();
				if(lfcReplica.toString().equals(lfcReplica.getSfn())){
					//The replica already exists... nothing to do
					return;
				}
			}
			if(guid == null){
				//retrieve the guid
				guid = m_lfcConnector.stat(logicalEntry, true).getGuid();
			}
			try {
				m_lfcConnector.addReplica(guid, new java.net.URI(replicaEntry.toString()));
			} catch (URISyntaxException e) {
				//Cannot happen
			}
		}catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(LfcError.OPERATION_NOT_PERMITTED.equals(e.getLFCError()) || LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(LfcError.OPERATION_NOT_PERMITTED.toString());
			}else{
				throw new NoSuccessException(e);
			}
		}
	}

	public void removeLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, BadParameterException {
		try{
			//Test if the entry is already in the LFC
			if(!m_lfcConnector.exist(logicalEntry)){
				throw new IncorrectStateException(logicalEntry+" does not exist");
			}
			if(m_lfcConnector.stat(logicalEntry, false).isDirectory()){
				throw new BadParameterException(logicalEntry+" is a directory");
			}
			
			throw new NoSuccessException(new NotImplementedException("LFC removeLocation not done yet!!"));
			//TODO: DEL REPLICA IS NOT YET IMPLEMENTED!!!!
			//m_lfcConnector.delreplica(replicaEntry)
		}catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(LfcError.OPERATION_NOT_PERMITTED.equals(e.getLFCError()) || LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(LfcError.OPERATION_NOT_PERMITTED.toString());
			}else{
				throw new NoSuccessException(e);
			}
		} catch (IncorrectStateException e) {
			throw e;
		} catch (BadParameterException e) {
			throw e;
		}

	}

	public String[] listLocations(String logicalEntry, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		Collection<LFCReplica> replicas;
		try {
			replicas = m_lfcConnector.listReplicas(logicalEntry, null);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(LfcError.OPERATION_NOT_PERMITTED.equals(e.getLFCError()) || LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(LfcError.OPERATION_NOT_PERMITTED.toString());
			}else{
				throw new NoSuccessException(e);
			}
		}
		String[] locations = new String[replicas.size()];
		int index = 0;
		for (Iterator<LFCReplica> iterator = replicas.iterator(); iterator.hasNext();) {
			LFCReplica lfcReplica = iterator.next();
			locations[index] = lfcReplica.getSfn();
		}
		//TODO: WHAT TO DO IF THERE IS NO REPLICA???
		return locations;
	}

	public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		//TODO
		throw new NoSuccessException(new NotImplementedException("NOT IMPLEMENTED"));
	}

	public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		//TODO
		throw new NoSuccessException(new NotImplementedException("NOT IMPLEMENTED"));
	}

	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
		try {
			m_lfcConnector.mkdir(parentAbsolutePath + (parentAbsolutePath.endsWith("/")?"":"/") + directoryName);
		} catch (IOException e) {
			new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(LfcError.OPERATION_NOT_PERMITTED.equals(e.getLFCError()) || LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(LfcError.OPERATION_NOT_PERMITTED.toString());
			}else if(LfcError.FILE_EXISTS.equals(e.getLFCError())){
				throw new AlreadyExistsException(e);
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new ParentDoesNotExist(e);
			}else if (LfcError.NOT_A_DIRECTORY.equals(e.getLFCError())) {
				throw new BadParameterException(e);
			}else {
				throw new NoSuccessException(e);
			}
		}
	}

	public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		try {
			m_lfcConnector.deleteDir(parentAbsolutePath + (parentAbsolutePath.endsWith("/")?"":"/") + directoryName);
		} catch (IOException e) {
			new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(LfcError.OPERATION_NOT_PERMITTED.equals(e.getLFCError()) || LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(LfcError.OPERATION_NOT_PERMITTED.toString());
			}else if(LfcError.NOT_A_DIRECTORY.equals(e.getLFCError())){
				throw new BadParameterException(e);
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else {
				throw new NoSuccessException(e);
			}
		}
	}

	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		try {
			m_lfcConnector.deleteFile(parentAbsolutePath + (parentAbsolutePath.endsWith("/")?"":"/") + fileName);
		} catch (IOException e) {
			new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(LfcError.OPERATION_NOT_PERMITTED.equals(e.getLFCError()) || LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(LfcError.OPERATION_NOT_PERMITTED.toString());
			}else if(LfcError.NOT_A_DIRECTORY.equals(e.getLFCError())){
				throw new BadParameterException(e);
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else {
				throw new NoSuccessException(e);
			}
		}
	}

	public boolean isLink(String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		try {
			if(m_lfcConnector.stat(absolutePath, false).isSymbolicLink()){
				return true;
			}else{
				return false;
			}
		}catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(LfcError.OPERATION_NOT_PERMITTED.equals(e.getLFCError()) || LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(LfcError.OPERATION_NOT_PERMITTED.toString());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
					throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		}
	}

	public String readLink(String absolutePath) throws NotLink, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		//TODO
		throw new NoSuccessException(new NotImplementedException("NOT IMPLEMENTED"));
	}

	public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite) throws PermissionDeniedException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
		//TODO
		throw new NoSuccessException(new NotImplementedException("NOT IMPLEMENTED"));
	}
}
