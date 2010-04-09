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
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.LFCFile;
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.LFCReplica;
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.LfcError;
import fr.in2p3.jsaga.adaptor.lfc.LfcConnection.ReceiveException;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;

import org.apache.log4j.Logger;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.url.URL;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * gLite Logical File Catalog (LFC) JSAGA Adaptor
 * 
 * @author Jerome Revillard
 */
public class LFCDataAdaptor implements LogicalReader, LogicalWriter, LinkAdaptor, DataAdaptor {
	private static Logger logger = Logger.getLogger(LFCDataAdaptor.class);
    private static final int LFC_PORT_DEFAULT = 5010;
    private GSSCredentialSecurityCredential m_vomscredential;
    private String m_vo;
    private LfcConnector m_lfcConnector;	
    
	public String getType() {
		return "lfn";
	}
	
	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
		logger.debug("DOING: connect");
		try{
			m_lfcConnector = LfcConnector.getInstance(host, port, m_vo, m_vomscredential.getGSSCredential());
		}catch (IllegalArgumentException e) {
			throw new NoSuccessException(e.getMessage());
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else{
				throw new NoSuccessException(e);
			}
		}
		logger.debug("DONE: connect");
	}
	
	public void disconnect() throws NoSuccessException {
//		logger.debug("DOING: disconnect");
//		try {
//			m_lfcConnector.closeSession();
//		} catch (Exception e) {
//			throw new NoSuccessException(e);
//		}
//		logger.debug("DONE: disconnect");
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
			m_vo = "Unknown_VO";
		}
    }

    public final BaseURL getBaseURL() throws IncorrectURLException {
        return new BaseURL(LFC_PORT_DEFAULT);
    }

	public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {
			logger.debug("DOING: exists("+absolutePath+", "+additionalArgs+")");
			boolean exist = m_lfcConnector.exist(absolutePath);
			logger.debug("DONE: exists("+absolutePath+", "+additionalArgs+")");
			return exist;
		} catch (IOException e) {
			logger.error("ERROR: exists("+absolutePath+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: exists("+absolutePath+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: exists("+absolutePath+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
	}

	public void addLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
		try{
			logger.debug("DOING: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")");
			//Test if the replica exists
			Collection<LFCReplica> replicas = null;
			try{
				replicas = m_lfcConnector.listReplicas(logicalEntry, null);
			}catch (ReceiveException e) {
				if(!LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())){
					throw e;
				}
			}
			
			if(replicas == null){
				//No then create it
				org.ogf.saga.file.File replicaFile;
				try {
					replicaFile = FileFactory.createFile(replicaEntry);
				} catch (Exception e) {
					throw new NoSuccessException(e);
				}
				String guid = null;
				try {
					guid = m_lfcConnector.create(logicalEntry, UUID.randomUUID().toString(), replicaFile.getSize());
				} catch (NotImplementedException e) {
					throw new NoSuccessException("Unable to get the file size of "+replicaEntry, e);
				} catch (AuthenticationFailedException e) {
					throw new NoSuccessException("Unable to get the file size of "+replicaEntry, e);
				} catch (AuthorizationFailedException e) {
					throw new NoSuccessException("Unable to get the file size of "+replicaEntry, e);
				}
				try{
					m_lfcConnector.addReplica(guid, new URI(replicaEntry.getString()));
				} catch (IOException e) {
					try{
						m_lfcConnector.deleteGuid(guid,true);
					}catch (Exception e1) {}
					throw e;
				} catch (ReceiveException e) {
					try{
						m_lfcConnector.deleteGuid(guid,true);
					}catch (Exception e1) {}
					throw e;
				} catch (URISyntaxException e) {
					//impossible..
				}
			}else{
				// add replica location (if it does not already exist)
				for (Iterator<LFCReplica> iterator = replicas.iterator(); iterator.hasNext();) {
					LFCReplica lfcReplica = iterator.next();
					if(lfcReplica.getSfn().equals(replicaEntry.getString())){
						//The replica already exists... nothing to do
						return;
					}
				}
				String guid = m_lfcConnector.stat(logicalEntry, true, true).getGuid();
				try {
					m_lfcConnector.addReplica(guid, new java.net.URI(replicaEntry.getString()));
				} catch (URISyntaxException e) {
					//Cannot happen
				}
			}
			logger.debug("DONE: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")");
		}catch (IOException e) {
			logger.error("ERROR: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
	}

	public void removeLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, BadParameterException {
		logger.debug("DOING: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")");
		LFCFile lfcFile = null;
		try{
			lfcFile = m_lfcConnector.stat(logicalEntry, true, true);
		}catch (IOException e) {
			logger.error("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if(LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())){
				throw new IncorrectStateException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
		
		if(lfcFile.isDirectory()){
			BadParameterException e = new BadParameterException(logicalEntry+" is a directory");
			logger.error("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			throw e;
		}
		
		try{
			m_lfcConnector.deleteReplica(lfcFile.getGuid(), replicaEntry.getString());
		}catch (IOException e) {
			logger.error("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if(LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())){
				throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
		logger.debug("DONE: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")");
	}

	public String[] listLocations(String logicalEntry, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: listLocations("+logicalEntry+", "+additionalArgs+")");
		Collection<LFCReplica> replicas;
		try {
			replicas = m_lfcConnector.listReplicas(logicalEntry, null);
		} catch (IOException e) {
			logger.error("ERROR: listLocations("+logicalEntry+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: listLocations("+logicalEntry+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if(LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())){
				throw new DoesNotExistException(e.toString());
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: listLocations("+logicalEntry+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
		String[] locations = new String[replicas.size()];
		int index = 0;
		for (Iterator<LFCReplica> iterator = replicas.iterator(); iterator.hasNext();) {
			LFCReplica lfcReplica = iterator.next();
			locations[index] = lfcReplica.getSfn();
			index++;
		}
		logger.debug("DONE: listLocations("+logicalEntry+", "+additionalArgs+")");
		return locations;
	}

	public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException, BadParameterException {
		logger.debug("DOING: listAttributes("+absolutePath+", "+additionalArgs+")");
		Collection<LFCFile> lfcFiles = null;
		try {
			lfcFiles = m_lfcConnector.list(absolutePath, true);
		} catch (IOException e) {
			logger.error("ERROR: listAttributes("+absolutePath+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: listAttributes("+absolutePath+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else if (LfcError.NOT_A_DIRECTORY.equals(e.getLFCError())) {
				throw new BadParameterException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: listAttributes("+absolutePath+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
		FileAttributes[] fileAttributes = new FileAttributes[lfcFiles.size()];
		int index = 0;
		for (Iterator<LFCFile> iterator = lfcFiles.iterator(); iterator.hasNext();) {
			fileAttributes[index] =  new LFCFileAttributes(iterator.next());
			index++;
		}
		logger.debug("DONE: listAttributes("+absolutePath+", "+additionalArgs+")");
		return fileAttributes;
	}

	public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: getAttributes("+absolutePath+", "+additionalArgs+")");
		LFCFile lfcFile = null;
		try {
			lfcFile = m_lfcConnector.stat(absolutePath, false,false);
		} catch (IOException e) {
			logger.error("ERROR: getAttributes("+absolutePath+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: getAttributes("+absolutePath+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: getAttributes("+absolutePath+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
		logger.debug("DONE: getAttributes("+absolutePath+", "+additionalArgs+")");
		return new LFCFileAttributes(lfcFile);
	}

	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
		logger.debug("DOING: makeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")");
		try {
			m_lfcConnector.mkdir(parentAbsolutePath + (parentAbsolutePath.endsWith("/")?"":"/") + directoryName);
		} catch (IOException e) {
			logger.error("ERROR: makeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: makeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if(LfcError.FILE_EXISTS.equals(e.getLFCError())){
				throw new AlreadyExistsException(e);
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new ParentDoesNotExist(e);
			}else if (LfcError.NOT_A_DIRECTORY.equals(e.getLFCError())) {
				throw new BadParameterException(e);
			}else {
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: makeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
		logger.debug("DONE: makeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")");
	}

	public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")");
		try {
			m_lfcConnector.deleteDir(parentAbsolutePath + (parentAbsolutePath.endsWith("/")?"":"/") + directoryName);
		} catch (IOException e) {
			logger.error("ERROR: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if(LfcError.NOT_A_DIRECTORY.equals(e.getLFCError())){
				throw new BadParameterException(e);
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else {
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
		logger.debug("DONE: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")");
	}

	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")");
		String filePath = parentAbsolutePath + (parentAbsolutePath.endsWith("/")?"":"/") + fileName;
		//List replicas
		//TODO: Check if it's a directory.
		Collection<LFCReplica> replicas = null;
		try{
			replicas = m_lfcConnector.listReplicas(filePath,null);
		} catch (IOException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
		
		//Delete replicas information
		try{
			for (Iterator<LFCReplica> iterator = replicas.iterator(); iterator.hasNext();) {
				LFCReplica lfcReplica = iterator.next();
				m_lfcConnector.deleteReplica(lfcReplica.getGuid(), lfcReplica.getSfn());
			}
		} catch (IOException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
		
		//Remove the file from the LFC
		try {
			m_lfcConnector.unlink(filePath);
		} catch (IOException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if(LfcError.OPERATION_NOT_PERMITTED.equals(e.getLFCError())){
				throw new BadParameterException(e);
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else {
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			throw new TimeoutException(e.toString());
		}
		logger.debug("DONE: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")");
	}

	public boolean isLink(String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: isLink("+absolutePath+")");
		try {
			boolean isSymlink = m_lfcConnector.stat(absolutePath, false, false).isSymbolicLink();
			logger.debug("DONE: isLink("+absolutePath+")");
			if(isSymlink){
				return true;
			}else{
				return false;
			}
		}catch (IOException e) {
			logger.error("ERROR: isLink("+absolutePath+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: isLink("+absolutePath+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.toString());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
					throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: isLink("+absolutePath+")",e);
			throw new TimeoutException(e.toString());
		}
	}

	public String readLink(String absolutePath) throws NotLink, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: readLink("+absolutePath+")");
		//TODO
		throw new NoSuccessException(new NotImplementedException("NOT IMPLEMENTED"));
		//logger.debug("DONE: readLink("+absolutePath+")");
	}

	public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite) throws PermissionDeniedException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
		logger.debug("DOING: readLink("+sourceAbsolutePath+", "+linkAbsolutePath+", "+overwrite+")");
		//TODO
		throw new NoSuccessException(new NotImplementedException("NOT IMPLEMENTED"));
		//logger.debug("DONE: readLink("+sourceAbsolutePath+", "+linkAbsolutePath+", "+overwrite+")");
		
	}
}
