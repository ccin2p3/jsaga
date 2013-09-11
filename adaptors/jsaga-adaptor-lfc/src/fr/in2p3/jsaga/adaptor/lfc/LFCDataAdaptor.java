package fr.in2p3.jsaga.adaptor.lfc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.file.FileFactory;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.optimise.DataRename;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptorBasic;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityCredential;
import fr.in2p3.jsaga.helpers.URLEncoder;
import fr.maatg.glite.dm.CNSConnector;
import fr.maatg.glite.dm.connection.DMError;
import fr.maatg.glite.dm.connection.ReceiveException;
import fr.maatg.glite.dm.ns.CNSConnection;
import fr.maatg.glite.dm.ns.CNSConnections;
import fr.maatg.glite.dm.ns.CNSFileReaddir;
import fr.maatg.glite.dm.ns.CNSFileStat;
import fr.maatg.glite.dm.ns.CNSPermissions;
import fr.maatg.glite.dm.ns.CNSReplica;

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
    private CNSConnector m_lfcConnector;
    private CNSConnection connection;
    private Session m_session = null;
    
	public String getType() {
		return "lfn";
	}
	
	@SuppressWarnings("rawtypes")
	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
		logger.debug("DOING: connect");
		try{
			m_lfcConnector = CNSConnector.getInstance(host, port, m_vo, m_globuscredential.getGSSCredential());
		}catch (IllegalArgumentException e) {
			throw new NoSuccessException(e.getMessage());
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
		}
		
		try {
			connection = m_lfcConnector.getNewConnection();
			m_lfcConnector.startSession(connection, null);
			connection = CNSConnections.getCNSConnectionTimeout(connection);
		} catch (IOException e) {
			logger.debug("ERROR: connect("+userInfo+", "+host+", "+port+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: connect("+userInfo+", "+host+", "+port+"): "+e.getMessage());
			if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
		}
		
		logger.debug("DONE: connect");
	}
	
	public void disconnect() throws NoSuccessException {
		logger.debug("DOING: disconnect");
		try {
			m_lfcConnector.close(connection);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
		logger.debug("DONE: disconnect");
	}

    public final Usage getUsage() {
        return null;
    }

	@SuppressWarnings("rawtypes")
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

    public final int getDefaultPort() {
        return LFC_PORT_DEFAULT;
    }

	public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {
			logger.debug("DOING: exists("+absolutePath+", "+additionalArgs+")");
			boolean exist = m_lfcConnector.exist(connection, absolutePath);
			System.err.println();
			logger.debug("DONE: exists("+absolutePath+", "+additionalArgs+")");
			return exist;
		} catch (IOException e) {
			logger.debug("ERROR: exists("+absolutePath+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: exists("+absolutePath+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
		}
	}

    public void create(String logicalEntry, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
    	try {
			logger.debug("DOING: create("+logicalEntry+", "+additionalArgs+")");
			// 755
			int permission = CNSPermissions.S_IRUSR | CNSPermissions.S_IWUSR | CNSPermissions.S_IXUSR | CNSPermissions.S_IRGRP | CNSPermissions.S_IXGRP | CNSPermissions.S_IROTH | CNSPermissions.S_IXOTH;
			m_lfcConnector.create(connection, logicalEntry, UUID.randomUUID().toString(), 0L, permission);
			logger.debug("DONE: create("+logicalEntry+", "+additionalArgs+")");
		} catch (IOException e) {
			logger.debug("ERROR: create("+logicalEntry+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: create("+logicalEntry+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.EISDIR.equals(e.getDMError())){
				throw new BadParameterException(e.toString());
			}else if(DMError.EEXIST.equals(e.getDMError())){
				throw new AlreadyExistsException(e.toString());
			}else if(DMError.ENOENT.equals(e.getDMError())){
				throw new ParentDoesNotExist(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
		}
    }

    public void addLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
		try{
			logger.debug("DOING: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")");
			//Test if the replica exists
			Collection<CNSReplica> replicas = null;
			try{
				replicas = m_lfcConnector.listReplicas(connection, logicalEntry, null);
			}catch (ReceiveException e) {
				logger.debug("ERROR: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")");
				if(DMError.EACCES.equals(e.getDMError())){
					throw new PermissionDeniedException(e.toString());
				}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
					throw new TimeoutException(e.getMessage());
				}else if(DMError.ENOENT.equals(e.getDMError())){
					throw new IncorrectStateException(e);
				}else{
					throw new NoSuccessException(e);
				}
			}

			// add replica location (if it does not already exist)
			for (Iterator<CNSReplica> iterator = replicas.iterator(); iterator.hasNext();) {
				CNSReplica lfcReplica = iterator.next();
				if(lfcReplica.getSfn().equals(replicaEntry.getString())){
					//The replica already exists... nothing to do
					return;
				}
			}
			
			//It does not exists...so create it
			org.ogf.saga.file.File replicaFile;
			try {
				if(m_session == null){
					Context context = ContextFactory.createContext(JSAGA_FACTORY, "InMemoryProxy");
			        context.setAttribute(Context.USERPROXY, InMemoryProxySecurityCredential.toBase64(m_globuscredential.getGSSCredential()));
			        context.setAttribute(Context.CERTREPOSITORY, m_globuscredential.getCertRepository().getAbsolutePath());
			        m_session = SessionFactory.createSession(JSAGA_FACTORY, false);
			        m_session.addContext(context);
				}
				URL new_replicaEntry = URLFactory.createURL(JSAGA_FACTORY, replicaEntry.getString());
				new_replicaEntry.setFragment("InMemoryProxy");
				replicaFile = FileFactory.createFile(JSAGA_FACTORY, m_session, new_replicaEntry);
			} catch (Exception e) {
				throw new NoSuccessException(e);
			}
			
			long fileSize;
			try {
				fileSize = replicaFile.getSize();
			} catch (NotImplementedException e) {
				throw new NoSuccessException("Unable to get the file size of "+replicaEntry, e);
			} catch (AuthenticationFailedException e) {
				throw new NoSuccessException("Unable to get the file size of "+replicaEntry, e);
			} catch (AuthorizationFailedException e) {
				throw new NoSuccessException("Unable to get the file size of "+replicaEntry, e);
			}
			
			String guid = m_lfcConnector.statg(connection, logicalEntry, null).getGuid();
			m_lfcConnector.startTransaction(connection,null);
			boolean done = false;
			try {
				//m_lfcConnector.addReplica(connection, guid, new java.net.URI(URLEncoder.encode(replicaEntry.getString())));
				m_lfcConnector.addReplica(connection, guid, new java.net.URI(replicaEntry.toString()));
				m_lfcConnector.setFileSize(connection, logicalEntry, 0L, fileSize, null, null);
				m_lfcConnector.endTransaction(connection);
				done = true;
			} catch (URISyntaxException e) {
				e.printStackTrace();
				//Cannot happen
			}finally{
				if(!done){
					m_lfcConnector.abortTransaction(connection);
				}
			}
			logger.debug("DONE: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")");
		}catch (IOException e) {
			logger.debug("ERROR: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: addLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
		}
	}

	public void removeLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, BadParameterException {
		logger.debug("DOING: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")");
		CNSFileStat lfcFile = null;
		try{
			lfcFile = m_lfcConnector.stat(connection, logicalEntry, 0L,false);
		}catch (IOException e) {
			logger.debug("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if(DMError.ENOENT.equals(e.getDMError())){
				throw new IncorrectStateException(e);
			}else{
				throw new NoSuccessException(e);
			}
		}
		
		if(lfcFile.isDirectory()){
			BadParameterException e = new BadParameterException(logicalEntry+" is a directory");
			logger.debug("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+"): "+e.getMessage());
			throw e;
		}
		
		try{
			m_lfcConnector.deleteReplica(connection, lfcFile.getFileId(), null, replicaEntry.getString());
		}catch (IOException e) {
			logger.debug("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if(DMError.ENOENT.equals(e.getDMError())){
				throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		}
		logger.debug("DONE: removeLocation("+logicalEntry+", "+replicaEntry+", "+additionalArgs+")");
	}

	public String[] listLocations(String logicalEntry, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: listLocations("+logicalEntry+", "+additionalArgs+")");
		Collection<CNSReplica> replicas;
		try {
			replicas = m_lfcConnector.listReplicas(connection, logicalEntry, null);
		} catch (IOException e) {
			logger.debug("ERROR: listLocations("+logicalEntry+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: listLocations("+logicalEntry+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if(DMError.ENOENT.equals(e.getDMError())){
				throw new DoesNotExistException(e.toString());
			}else{
				throw new NoSuccessException(e);
			}
		}
		String[] locations = new String[replicas.size()];
		int index = 0;
		for (Iterator<CNSReplica> iterator = replicas.iterator(); iterator.hasNext();) {
			CNSReplica lfcReplica = iterator.next();
			locations[index] = lfcReplica.getSfn();
			index++;
		}
		logger.debug("DONE: listLocations("+logicalEntry+", "+additionalArgs+")");
		return locations;
	}

	public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException, BadParameterException {
		logger.debug("DOING: listAttributes("+absolutePath+", "+additionalArgs+")");
		Collection<CNSFileReaddir> lfcFiles = null;
		try {
			lfcFiles = m_lfcConnector.list(connection, absolutePath, false);
		} catch (IOException e) {
			logger.debug("ERROR: listAttributes("+absolutePath+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: listAttributes("+absolutePath+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if (DMError.ENOENT.equals(e.getDMError())) {
				throw new DoesNotExistException(e);
			}else if (DMError.ENOTDIR.equals(e.getDMError())) {
				throw new BadParameterException(e);
			}else{
				throw new NoSuccessException(e);
			}
		}
		FileAttributes[] fileAttributes = new FileAttributes[lfcFiles.size()];
		int index = 0;
		for (Iterator<CNSFileReaddir> iterator = lfcFiles.iterator(); iterator.hasNext();) {
			fileAttributes[index] =  new NSFileAttributes(iterator.next(),connection);
			index++;
		}
		logger.debug("DONE: listAttributes("+absolutePath+", "+additionalArgs+")");
		return fileAttributes;
	}

	public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: getAttributes("+absolutePath+", "+additionalArgs+")");
		CNSFileStat lfcFile = null;
		try {
			lfcFile = m_lfcConnector.stat(connection, absolutePath, 0L, false);
		} catch (IOException e) {
			logger.debug("ERROR: getAttributes("+absolutePath+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: getAttributes("+absolutePath+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if (DMError.ENOENT.equals(e.getDMError())) {
				throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		}
		logger.debug("DONE: getAttributes("+absolutePath+", "+additionalArgs+")");
		return new NSFileAttributes(lfcFile, connection);
	}

	public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
		logger.debug("DOING: makeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")");
		try {
			// 755
			int permission = CNSPermissions.S_IRUSR | CNSPermissions.S_IWUSR | CNSPermissions.S_IXUSR | CNSPermissions.S_IRGRP | CNSPermissions.S_IXGRP | CNSPermissions.S_IROTH | CNSPermissions.S_IXOTH;
			m_lfcConnector.mkdir(connection, parentAbsolutePath + (parentAbsolutePath.endsWith("/")?"":"/") + directoryName, permission);
		} catch (IOException e) {
			logger.debug("ERROR: makeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: makeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if(DMError.EEXIST.equals(e.getDMError())){
				throw new AlreadyExistsException(e);
			}else if (DMError.ENOENT.equals(e.getDMError())) {
				throw new ParentDoesNotExist(e);
			}else if (DMError.ENOTDIR.equals(e.getDMError())) {
				throw new BadParameterException(e);
			}else {
				throw new NoSuccessException(e);
			}
		}
		logger.debug("DONE: makeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")");
	}

	public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")");
		try {
			m_lfcConnector.deleteDir(connection, parentAbsolutePath + (parentAbsolutePath.endsWith("/")?"":"/") + directoryName);
		} catch (IOException e) {
			logger.debug("ERROR: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if(DMError.ENOTDIR.equals(e.getDMError())){
				throw new BadParameterException(e);
			}else if (DMError.ENOENT.equals(e.getDMError())) {
				throw new DoesNotExistException(e);
			}else {
				throw new NoSuccessException(e);
			}
		}
		logger.debug("DONE: removeDir("+parentAbsolutePath+", "+directoryName+", "+additionalArgs+")");
	}

	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")");
		String filePath = parentAbsolutePath + (parentAbsolutePath.endsWith("/")?"":"/") + fileName;
		CNSFileStat lfcFile = null;
		try{
			lfcFile = m_lfcConnector.stat(connection, filePath, 0L, false);
		} catch (IOException e) {
			logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if (DMError.ENOENT.equals(e.getDMError())) {
				throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		}
		
		if(lfcFile.isDirectory()){
			throw new BadParameterException(filePath+" is a directory.");
		}else if(lfcFile.isRegularFile()){
//	TODO: It seems that doing it induce a problem in the links tests tearDone. I don't know yet why.
//			try {
//				m_lfcConnector.deleteFilesByNames(connection, new String[]{filePath}, true);
//			} catch (IOException e) {
//				logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
//				throw new NoSuccessException(e);
//			} catch (ReceiveException e) {
//				logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
//				if(DMError.EACCES.equals(e.getDMError())){
//					throw new PermissionDeniedException(e.toString());
//				}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
//					throw new TimeoutException(e.getMessage());
//				}else if (DMError.ENOENT.equals(e.getDMError())) {
//					throw new DoesNotExistException(e);
//				}else{
//					throw new NoSuccessException(e);
//				}
//			} catch (CNSStatusesException e) {
//				logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
//				if(DMError.EACCES.equals(e.getCNSErrors()[0])){
//					throw new PermissionDeniedException(e.toString());
//				}else if(DMError.SETIMEDOUT.equals(e.getCNSErrors()[0])){
//					throw new TimeoutException(e.getMessage());
//				}else if (DMError.ENOENT.equals(e.getCNSErrors()[0])) {
//					throw new DoesNotExistException(e);
//				}else{
//					throw new NoSuccessException(e);
//				}
//			}
//		}
			
			Collection<CNSReplica> replicas;
			try{
				replicas = m_lfcConnector.listReplicas(connection, filePath,null);
			} catch (IOException e) {
				logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
				throw new NoSuccessException(e);
			} catch (ReceiveException e) {
				logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
				if(DMError.EACCES.equals(e.getDMError())){
					throw new PermissionDeniedException(e.toString());
				}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
					throw new TimeoutException(e.getMessage());
				}else if (DMError.ENOENT.equals(e.getDMError())) {
					throw new DoesNotExistException(e);
				}else{
					throw new NoSuccessException(e);
				}
			}
			//Delete replicas information
			try{
				for (Iterator<CNSReplica> iterator = replicas.iterator(); iterator.hasNext();) {
					CNSReplica lfcReplica = iterator.next();
					m_lfcConnector.deleteReplica(connection, 0L, lfcReplica.getGuid(), lfcReplica.getSfn());
				}
			} catch (IOException e) {
				logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
				throw new NoSuccessException(e);
			} catch (ReceiveException e) {
				logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
				if(DMError.EACCES.equals(e.getDMError())){
					throw new PermissionDeniedException(e.toString());
				}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
					throw new TimeoutException(e.getMessage());
				}else if (DMError.ENOENT.equals(e.getDMError())) {
					throw new DoesNotExistException(e);
				}else{
					throw new NoSuccessException(e);
				}
			}
		}
		
		//Remove the entry from the LFC
		try {
			m_lfcConnector.delete(connection, filePath);
		} catch (IOException e) {
			logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if (DMError.ENOENT.equals(e.getDMError())) {
				throw new DoesNotExistException(e);
			}else {
				throw new NoSuccessException(e);
			}
		}
		logger.debug("DONE: removeFile("+parentAbsolutePath+", "+fileName+", "+additionalArgs+")");
	}

	public String readLink(String absolutePath) throws NotLink, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
		logger.debug("DOING: readLink("+absolutePath+")");
		String path = null;
		try {
			path = m_lfcConnector.readlink(connection, absolutePath);
		}catch (IOException e) {
			logger.debug("ERROR: readLink("+absolutePath+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: readLink("+absolutePath+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if (DMError.ENOENT.equals(e.getDMError())) {
					throw new DoesNotExistException(e);
			}else{
				throw new NoSuccessException(e);
			}
		}
		logger.debug("DONE: readLink("+absolutePath+")");
		return path;
	}

	public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite) throws PermissionDeniedException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
		logger.debug("DOING: link("+sourceAbsolutePath+", "+linkAbsolutePath+", "+overwrite+")");
		try {
			m_lfcConnector.symbLink(connection, sourceAbsolutePath, linkAbsolutePath, overwrite);
		}catch (IOException e) {
			logger.debug("ERROR: link("+sourceAbsolutePath+", "+linkAbsolutePath+", "+overwrite+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: link("+sourceAbsolutePath+", "+linkAbsolutePath+", "+overwrite+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if (DMError.ENOENT.equals(e.getDMError())) {
					throw new DoesNotExistException(e);
			}else if (DMError.EEXIST.equals(e.getDMError())) {
				throw new AlreadyExistsException(e);
			}else{
				throw new NoSuccessException(e);
			}
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
			m_lfcConnector.chmod(connection, absolutePath,permission);
		}catch (IOException e) {
			logger.debug("ERROR: permissionsAllow("+absolutePath+", "+scope+", "+permissions.getValue()+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: permissionsAllow("+absolutePath+", "+scope+", "+permissions.getValue()+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
		}
		logger.debug("DONE: permissionsAllow("+absolutePath+", "+scope+", "+permissions.getValue()+")");
	}
	
	private int generateLFCPermissions(int scope, PermissionBytes permissions){
		int perms = 0;
        switch (scope) {
            case SCOPE_USER:
            	if(permissions.contains(Permission.READ)){
        			perms |= CNSPermissions.S_IRUSR;
        		}
        		if(permissions.contains(Permission.WRITE)){
        			perms |= CNSPermissions.S_IWUSR;
        		}
        		if(permissions.contains(Permission.EXEC)){
        			perms |= CNSPermissions.S_IXUSR;
        		}
            	break;
            case SCOPE_GROUP:
            	if(permissions.contains(Permission.READ)){
        			perms |= CNSPermissions.S_IRGRP;
        		}
        		if(permissions.contains(Permission.WRITE)){
        			perms |= CNSPermissions.S_IWGRP;
        		}
        		if(permissions.contains(Permission.EXEC)){
        			perms |= CNSPermissions.S_IXGRP;
        		}
            	break;
            case SCOPE_ANY:
            	if(permissions.contains(Permission.READ)){
        			perms |= CNSPermissions.S_IROTH;
        		}
        		if(permissions.contains(Permission.WRITE)){
        			perms |= CNSPermissions.S_IWOTH;
        		}
        		if(permissions.contains(Permission.EXEC)){
        			perms |= CNSPermissions.S_IXOTH;
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
			m_lfcConnector.chmod(connection, absolutePath,permission);
		}catch (IOException e) {
			logger.debug("ERROR: permissionsDeny("+absolutePath+", "+scope+", "+permissions.getValue()+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: permissionsDeny("+absolutePath+", "+scope+", "+permissions.getValue()+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else{
				throw new NoSuccessException(e);
			}
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
	
	public void setGroup(String absolutePath, String id) throws DoesNotExistException, PermissionDeniedException,
			TimeoutException, BadParameterException, NoSuccessException {
		// TODO Auto-generated method stub
		throw new NoSuccessException("Not implemented");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
			m_lfcConnector.rename(connection, sourceAbsolutePath, targetAbsolutePath);
		} catch (IOException e) {
			logger.debug("ERROR: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+"): "+e.getMessage());
			throw new NoSuccessException(e);
		} catch (ReceiveException e) {
			logger.debug("ERROR: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+"): "+e.getMessage());
			if(DMError.EACCES.equals(e.getDMError())){
				throw new PermissionDeniedException(e.toString());
			}else if(DMError.SETIMEDOUT.equals(e.getDMError())){
				throw new TimeoutException(e.getMessage());
			}else if (DMError.ENOENT.equals(e.getDMError())) {
				throw new DoesNotExistException(e);
			}else if (DMError.EEXIST.equals(e.getDMError())) {
				if(overwrite == true){
					try{
						CNSFileStat file = m_lfcConnector.stat(connection, targetAbsolutePath, 0L, false);
						if(file.isDirectory()){
							m_lfcConnector.deleteDir(connection, targetAbsolutePath);
						}else{
							String formattedTargetAbsolutePath = null;
							if(targetAbsolutePath.endsWith("/")){
								formattedTargetAbsolutePath = targetAbsolutePath.substring(0, targetAbsolutePath.length()-1);
							}
							this.removeFile(formattedTargetAbsolutePath.substring(0, formattedTargetAbsolutePath.lastIndexOf("/")-1), formattedTargetAbsolutePath.substring(formattedTargetAbsolutePath.lastIndexOf("/")+1),null);
						}
					} catch (IOException e1) {
						logger.debug("ERROR: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+"): "+e1.getMessage());
						throw new NoSuccessException(e);
					} catch (ReceiveException e1) {
						logger.debug("ERROR: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+"): "+e1.getMessage());
						if(DMError.EACCES.equals(e1.getDMError())){
							throw new PermissionDeniedException(e1.toString());
						}else if(DMError.SETIMEDOUT.equals(e1.getDMError())){
							throw new TimeoutException(e1.getMessage());
						}else{
							throw new NoSuccessException(e1);
						}
					}
					this.rename(sourceAbsolutePath, targetAbsolutePath, false, additionalArgs);
				}
				throw new AlreadyExistsException(e);
			}else{
				throw new NoSuccessException(e);
			}
		}
		logger.debug("DONE: rename("+sourceAbsolutePath+", "+targetAbsolutePath+", "+overwrite+", "+additionalArgs+")");
	}
}
