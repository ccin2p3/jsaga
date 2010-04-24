package fr.in2p3.jsaga.adaptor.lfc;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.BaseURL;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptorBasic;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
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
import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.permissions.Permission;
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
public class LFCDataAdaptor implements LogicalReader, LogicalWriter, LinkAdaptor, DataRename, PermissionAdaptorBasic {
	private static Logger logger = Logger.getLogger(LFCDataAdaptor.class);
    private static final int LFC_PORT_DEFAULT = 5010;
    private GSSCredentialSecurityCredential m_globuscredential;
    private String m_vo;
    private LfcConnector m_lfcConnector;	
    
	public String getType() {
		return "lfn";
	}
	
	@SuppressWarnings("unchecked")
	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
		logger.debug("DOING: connect");
		try{
			m_lfcConnector = LfcConnector.getInstance(host, port, m_vo, m_globuscredential.getGSSCredential());
		}catch (IllegalArgumentException e) {
			throw new NoSuccessException(e.getMessage());
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.getMessage());
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

    @SuppressWarnings("unchecked")
	public final Class<SecurityCredential>[] getSupportedSecurityCredentialClasses() {
        return new Class[]{GSSCredentialSecurityCredential.class};
    }

    public final void setSecurityCredential(SecurityCredential credential) {
    	m_globuscredential = (GSSCredentialSecurityCredential) credential;
        try {
			m_vo = m_globuscredential.getAttribute(Context.USERVO);
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
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: exists("+absolutePath+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
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
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
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
				throw new TimeoutException(e.getMessage());
			}else if(LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())){
				throw new IncorrectStateException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
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
				throw new TimeoutException(e.getMessage());
			}else if(LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())){
				throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
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
				throw new TimeoutException(e.getMessage());
			}else if(LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())){
				throw new DoesNotExistException(e.toString());
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: listLocations("+logicalEntry+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
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
				throw new TimeoutException(e.getMessage());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else if (LfcError.NOT_A_DIRECTORY.equals(e.getLFCError())) {
				throw new BadParameterException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: listAttributes("+absolutePath+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
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
				throw new TimeoutException(e.getMessage());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: getAttributes("+absolutePath+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
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
				throw new TimeoutException(e.getMessage());
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
			throw new TimeoutException(e.getMessage());
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
				throw new TimeoutException(e.getMessage());
			}else if(LfcError.NOT_A_DIRECTORY.equals(e.getLFCError())){
				throw new BadParameterException(e);
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else {
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
		}
		logger.debug("DONE: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")");
	}

	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")");
		String filePath = parentAbsolutePath + (parentAbsolutePath.endsWith("/")?"":"/") + fileName;
		LFCFile lfcFile = null;
		try{
			lfcFile = m_lfcConnector.stat(filePath, false, false);
		} catch (IOException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.getMessage());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
		}
		
		if(lfcFile.isDirectory()){
			throw new BadParameterException(filePath+" is a directory.");
		}else if(lfcFile.isRegularFile()){
			Collection<LFCReplica> replicas;
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
					throw new TimeoutException(e.getMessage());
				}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
					throw new DoesNotExistException(e);
				}else{
					throw new NoSuccessException(e);
				}
			} catch (java.util.concurrent.TimeoutException e) {
				logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
				throw new TimeoutException(e.getMessage());
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
					throw new TimeoutException(e.getMessage());
				}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
					throw new DoesNotExistException(e);
				}else{
					throw new NoSuccessException(e);
				}
			} catch (java.util.concurrent.TimeoutException e) {
				logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
				throw new TimeoutException(e.getMessage());
			}
		}
		
		//Remove the entry from the LFC
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
				throw new TimeoutException(e.getMessage());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else {
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
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
				throw new TimeoutException(e.getMessage());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
					throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: isLink("+absolutePath+")",e);
			throw new TimeoutException(e.getMessage());
		}
	}

	public String readLink(String absolutePath) throws NotLink, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: readLink("+absolutePath+")");
		String path = null;
		try {
			path = m_lfcConnector.readlink(absolutePath);
		}catch (IOException e) {
			logger.error("ERROR: readLink("+absolutePath+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: readLink("+absolutePath+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.getMessage());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
					throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: readLink("+absolutePath+")",e);
			throw new TimeoutException(e.getMessage());
		}
		logger.debug("DONE: readLink("+absolutePath+")");
		return path;
	}

	public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite) throws PermissionDeniedException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
		logger.debug("DOING: link("+sourceAbsolutePath+", "+linkAbsolutePath+", "+overwrite+")");
		try {
			m_lfcConnector.symbLink(sourceAbsolutePath, linkAbsolutePath, overwrite);
		}catch (IOException e) {
			logger.error("ERROR: link("+sourceAbsolutePath+", "+linkAbsolutePath+", "+overwrite+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: link("+sourceAbsolutePath+", "+linkAbsolutePath+", "+overwrite+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.getMessage());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
					throw new DoesNotExistException(e);
			}else if (LfcError.FILE_EXISTS.equals(e.getLFCError())) {
				throw new AlreadyExistsException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: link("+sourceAbsolutePath+", "+linkAbsolutePath+", "+overwrite+")",e);
			throw new TimeoutException(e.getMessage());
		}
		logger.debug("DONE: link("+sourceAbsolutePath+", "+linkAbsolutePath+", "+overwrite+")");
	}

	public int[] getSupportedScopes() {
		return new int[]{SCOPE_USER,SCOPE_GROUP,SCOPE_ANY};
	}

	public void permissionsAllow(String absolutePath, int scope, PermissionBytes permissions) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		logger.debug("DOING: permissionsAllow("+absolutePath+", "+scope+", "+permissions.getValue()+")");
		
		FileAttributes lfcFileAttributes;
		try {
			lfcFileAttributes = getAttributes(absolutePath, null);
		} catch (DoesNotExistException e) {
			throw new NoSuccessException(e);
		}
		
		int permission = -1;
		switch (scope) {
			case SCOPE_USER:
				if(lfcFileAttributes.getUserPermission().containsAll(permissions.getValue())){
					return;
				}else{
					PermissionBytes ownerPermissions = lfcFileAttributes.getUserPermission().or(permissions);
					permission = generateLFCPermissions(SCOPE_USER,ownerPermissions) | generateLFCPermissions(SCOPE_GROUP,lfcFileAttributes.getGroupPermission()) | generateLFCPermissions(SCOPE_ANY,lfcFileAttributes.getAnyPermission());
				}
				break;
			case SCOPE_GROUP:
				if(lfcFileAttributes.getGroupPermission().containsAll(permissions.getValue())){
					return;
				}else{
					PermissionBytes groupPermissions = lfcFileAttributes.getGroupPermission().or(permissions);
					permission = generateLFCPermissions(SCOPE_USER,lfcFileAttributes.getUserPermission()) | generateLFCPermissions(SCOPE_GROUP,groupPermissions) | generateLFCPermissions(SCOPE_ANY,lfcFileAttributes.getAnyPermission());
				}
				break;
			case SCOPE_ANY:
				if(lfcFileAttributes.getAnyPermission().containsAll(permissions.getValue())){
					return;
				}else{
					PermissionBytes anyPermissions = lfcFileAttributes.getAnyPermission().or(permissions);
					permission = generateLFCPermissions(SCOPE_USER,lfcFileAttributes.getUserPermission()) | generateLFCPermissions(SCOPE_GROUP,lfcFileAttributes.getGroupPermission()) | generateLFCPermissions(SCOPE_ANY,anyPermissions);
				}
				break;
			default:
				throw new RuntimeException("Unkown scope: "+scope);
		}
		
		try {
			m_lfcConnector.chmod(absolutePath,permission);
		}catch (IOException e) {
			logger.error("ERROR: permissionsAllow("+absolutePath+", "+scope+", "+permissions.getValue()+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: permissionsAllow("+absolutePath+", "+scope+", "+permissions.getValue()+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: permissionsAllow("+absolutePath+", "+scope+", "+permissions.getValue()+")",e);
			throw new TimeoutException(e.getMessage());
		}
		logger.debug("DONE: permissionsAllow("+absolutePath+", "+scope+", "+permissions.getValue()+")");
	}
	
	private int generateLFCPermissions(int scope, PermissionBytes permissions){
		int perms = 0;
        switch (scope) {
            case SCOPE_USER:
            	if(permissions.contains(Permission.READ)){
        			perms |= LfcConnection.S_IRUSR;
        		}
        		if(permissions.contains(Permission.WRITE)){
        			perms |= LfcConnection.S_IWUSR;
        		}
        		if(permissions.contains(Permission.EXEC)){
        			perms |= LfcConnection.S_IXUSR;
        		}
            	break;
            case SCOPE_GROUP:
            	if(permissions.contains(Permission.READ)){
        			perms |= LfcConnection.S_IRGRP;
        		}
        		if(permissions.contains(Permission.WRITE)){
        			perms |= LfcConnection.S_IWGRP;
        		}
        		if(permissions.contains(Permission.EXEC)){
        			perms |= LfcConnection.S_IXGRP;
        		}
            	break;
            case SCOPE_ANY:
            	if(permissions.contains(Permission.READ)){
        			perms |= LfcConnection.S_IROTH;
        		}
        		if(permissions.contains(Permission.WRITE)){
        			perms |= LfcConnection.S_IWOTH;
        		}
        		if(permissions.contains(Permission.EXEC)){
        			perms |= LfcConnection.S_IXOTH;
        		}
            	break;
        }		
		return perms;
	}

// Will be needed for full permissions management
/*
	public boolean permissionsCheck(String absolutePath, int scope, PermissionBytes permissions) throws PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
		logger.debug("DOING: permissionsCheck("+absolutePath+", "+scope+", "+permissions.getValue()+")");
		
		if(permissions.contains(Permission.QUERY)){
			throw new BadParameterException("The QUERY permission is not supported by the LFC");
		}
		
		if(permissions.contains(Permission.OWNER)){
			throw new BadParameterException("The OWNER permission is not yet supported by the LFC adaptor");
		}
		
		FileAttributes lfcFileAttributes;
		try {
			lfcFileAttributes = getAttributes(absolutePath, null);
		} catch (DoesNotExistException e) {
			throw new NoSuccessException(e);
		}
		
		PermissionBytes actualScopePermissions;
		switch (scope) {
			case SCOPE_USER:
				actualScopePermissions = lfcFileAttributes.getUserPermission();
				break;
			case SCOPE_GROUP:
				actualScopePermissions = lfcFileAttributes.getGroupPermission();
				break;
			case SCOPE_ANY:
				actualScopePermissions = lfcFileAttributes.getAnyPermission();
				break;
			default:
				throw new RuntimeException("Unkown scope: "+scope);
		}
		
		if(actualScopePermissions.containsAll(permissions.getValue())){
			logger.debug("DONE: permissionsCheck("+absolutePath+", "+scope+", "+permissions.getValue()+")");
			return true;
		}else{
			logger.debug("DONE: permissionsCheck("+absolutePath+", "+scope+", "+permissions.getValue()+")");
			return false;
		}
	}
*/	

	public void permissionsDeny(String absolutePath, int scope, PermissionBytes permissions) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		logger.debug("DOING: permissionsDeny("+absolutePath+", "+scope+", "+permissions.getValue()+")");
		
		FileAttributes lfcFileAttributes;
		try {
			lfcFileAttributes = getAttributes(absolutePath, null);
		} catch (DoesNotExistException e) {
			throw new NoSuccessException(e);
		}
		
		int permission = -1;
		switch (scope) {
			case SCOPE_USER:
				PermissionBytes ownerPermissions = lfcFileAttributes.getUserPermission();
				PermissionBytes newOwnerPermissions = removePermissions(ownerPermissions, permissions);
				if(ownerPermissions == newOwnerPermissions){
					return;
				}else{
					//ownerPermissions was modified during the "removePermissions" operation.
					permission = generateLFCPermissions(SCOPE_USER,newOwnerPermissions) | generateLFCPermissions(SCOPE_GROUP,lfcFileAttributes.getGroupPermission()) | generateLFCPermissions(SCOPE_ANY,lfcFileAttributes.getAnyPermission());
				}
				break;
			case SCOPE_GROUP:
				PermissionBytes groupPermissions = lfcFileAttributes.getGroupPermission();
				PermissionBytes newGroupPermissions = removePermissions(groupPermissions, permissions);
				if(groupPermissions == newGroupPermissions){
					return;
				}else{
					//groupPermissions was modified during the "removePermissions" operation.
					permission = generateLFCPermissions(SCOPE_USER,lfcFileAttributes.getUserPermission()) | generateLFCPermissions(SCOPE_GROUP,newGroupPermissions) | generateLFCPermissions(SCOPE_ANY,lfcFileAttributes.getAnyPermission());
				}
				break;
			case SCOPE_ANY:
				PermissionBytes anyPermissions = lfcFileAttributes.getAnyPermission();
				PermissionBytes newAnyPermissions = removePermissions(anyPermissions, permissions);
				if(anyPermissions == newAnyPermissions){
					return;
				}else{
					//anyPermissions was modified during the "removePermissions" operation.
					permission = generateLFCPermissions(SCOPE_USER,lfcFileAttributes.getUserPermission()) | generateLFCPermissions(SCOPE_GROUP,lfcFileAttributes.getGroupPermission()) | generateLFCPermissions(SCOPE_ANY,newAnyPermissions);
				}
				break;
			default:
				throw new RuntimeException("Unkown scope: "+scope);
		}
		
		try {
			m_lfcConnector.chmod(absolutePath,permission);
		}catch (IOException e) {
			logger.error("ERROR: permissionsDeny("+absolutePath+", "+scope+", "+permissions.getValue()+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: permissionsDeny("+absolutePath+", "+scope+", "+permissions.getValue()+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: permissionsDeny("+absolutePath+", "+scope+", "+permissions.getValue()+")",e);
			throw new TimeoutException(e.getMessage());
		}
		
		logger.debug("DONE: permissionsDeny("+absolutePath+", "+scope+", "+permissions.getValue()+")");

	}
	
	
	/**
	 * @param actualPermissions The permissions to potentially modify.
	 * @param permissionsToRemove The permissions that have to be removed
	 * @return The new permissions 
	 */
	private static PermissionBytes removePermissions(PermissionBytes actualPermissions, PermissionBytes permissionsToRemove){
		PermissionBytes newPermissionBytes = actualPermissions;
		if(permissionsToRemove.contains(Permission.EXEC) && actualPermissions.contains(Permission.EXEC)){
			newPermissionBytes = new PermissionBytes(newPermissionBytes.getValue() - Permission.EXEC.getValue());
		}
		if(permissionsToRemove.contains(Permission.READ) && actualPermissions.contains(Permission.READ)){
			newPermissionBytes = new PermissionBytes(newPermissionBytes.getValue() - Permission.READ.getValue());
		}
		if(permissionsToRemove.contains(Permission.WRITE) && actualPermissions.contains(Permission.WRITE)){
			newPermissionBytes = new PermissionBytes(newPermissionBytes.getValue() - Permission.WRITE.getValue());
		}
		return newPermissionBytes;
	}
	

	public void setGroup(String id) throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException {
		// TODO Auto-generated method stub
		throw new NoSuccessException("Not implemented");
	}

	
	public void setOwner(String id) throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException {
		// TODO Auto-generated method stub
		throw new NoSuccessException("Not implemented");
	}
	
	@SuppressWarnings("unchecked")
	public String[] getGroupsOf(String id) throws BadParameterException, NoSuccessException {
		String userId = null;
		try {
			userId = m_globuscredential.getGSSCredential().getName().toString();
		} catch (GSSException e) {
			throw new BadParameterException("Unable to extract the user ID from the certificate");
		}
		if(!userId.equals(id)){
			throw new BadParameterException("The id is not the actual user");
		}
		GlobusCredential globusCred = null;
		if (m_globuscredential.getGSSCredential() instanceof GlobusGSSCredentialImpl) {
			globusCred = ((GlobusGSSCredentialImpl)m_globuscredential.getGSSCredential()).getGlobusCredential();
		} else {
			throw new BadParameterException("Not a globus proxy");
		}

		Vector<VOMSAttribute> v = VOMSValidator.parse(globusCred.getCertificateChain());
		for (int i=0; i<v.size(); i++) {
			VOMSAttribute attr = (VOMSAttribute) v.elementAt(i);
			if(m_vo.equals(attr.getVO())){
				String[] groups = new String[attr.getFullyQualifiedAttributes().size()];
				int index = 0;
				for (Iterator it=attr.getFullyQualifiedAttributes().iterator(); it.hasNext(); ) {
					groups[index] = (String) it.next();
					if(groups[index].startsWith("/")){
						groups[index] = groups[index].substring(1);
					}
					index++;
				}
				return groups;
			}
		}
		return new String[0];
	}

	public void rename(String sourceAbsolutePath, String targetAbsolutePath, boolean overwrite, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
		logger.debug("DOING: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+")");
		try{
			m_lfcConnector.rename(sourceAbsolutePath, targetAbsolutePath);
		} catch (IOException e) {
			logger.error("ERROR: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+")",e);
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.error("ERROR: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+")",e);
			if(LfcError.PERMISSION_DENIED.equals(e.getLFCError())){
				throw new PermissionDeniedException(e.toString());
			}else if(LfcError.TIMED_OUT.equals(e.getLFCError())){
				throw new TimeoutException(e.getMessage());
			}else if (LfcError.NO_SUCH_FILE_OR_DIRECTORY.equals(e.getLFCError())) {
				throw new DoesNotExistException(e);
			}else if (LfcError.FILE_EXISTS.equals(e.getLFCError())) {
				if(overwrite == true){
					try{
						LFCFile file = m_lfcConnector.stat(targetAbsolutePath, false, false);
						if(file.isDirectory()){
							m_lfcConnector.deleteDir(targetAbsolutePath);
						}else{
							String formattedTargetAbsolutePath = null;
							if(targetAbsolutePath.endsWith("/")){
								formattedTargetAbsolutePath = targetAbsolutePath.substring(0, targetAbsolutePath.length()-1);
							}
							this.removeFile(formattedTargetAbsolutePath.substring(0, formattedTargetAbsolutePath.lastIndexOf("/")-1), formattedTargetAbsolutePath.substring(formattedTargetAbsolutePath.lastIndexOf("/")+1),null);
						}
					} catch (IOException e1) {
						logger.error("ERROR: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+")",e1);
						throw new NoSuccessException(e);
					} catch (ReceiveException e1) {
						logger.error("ERROR: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+")",e1);
						if(LfcError.PERMISSION_DENIED.equals(e1.getLFCError())){
							throw new PermissionDeniedException(e1.toString());
						}else if(LfcError.TIMED_OUT.equals(e1.getLFCError())){
							throw new TimeoutException(e1.getMessage());
						}else{
							throw new NoSuccessException(e1);
						}
					} catch (java.util.concurrent.TimeoutException e1) {
						logger.error("ERROR: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+")",e1);
						throw new TimeoutException(e1.getMessage());
					}
					this.rename(sourceAbsolutePath, targetAbsolutePath, false, additionalArgs);
				}
				throw new AlreadyExistsException(e);
			}else{
				throw new NoSuccessException(e);
			}
		} catch (java.util.concurrent.TimeoutException e) {
			logger.error("ERROR: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+")",e);
			throw new TimeoutException(e.getMessage());
		}
		logger.debug("DONE: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+")");
	}
}
