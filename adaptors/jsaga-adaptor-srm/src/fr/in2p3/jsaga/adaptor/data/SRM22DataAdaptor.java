package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptorBasic;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Stub;
import org.glite.voms.VOMSAttribute;
import org.glite.voms.VOMSValidator;
import org.globus.axis.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.*;
import org.ogf.saga.permissions.Permission;
import org.ogf.srm22.*;

import javax.xml.rpc.ServiceException;
import java.io.*;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SRM22DataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 sept. 2007
* ***************************************************
* Description:                                      */
/**
 * TODO: implement DataCopy when it will be supported also by DPM
 */
public class SRM22DataAdaptor extends SRMDataAdaptorAbstract implements FileReaderStreamFactory, FileWriterStreamFactory, StreamCallback, PermissionAdaptorBasic
//todo: uncomment this when mixed adaptors (i.e. both physical and logical) will be supported by engine
//        , LogicalReader
{
    private static final String SERVICE_PROTOCOL = "httpg";
    private static final String SERVICE_PATH = "/srm/managerv2";
    private ISRM m_stub;

    public SRM22DataAdaptor() {
        m_stub = null;
    }

    public String getType() {
        return "srm";   //todo: replace with srm-v2.2 when a generic plugin will be developed
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        super.connect(userInfo, host, port, basePath, attributes);
        try {
            java.net.URL serviceUrl = new java.net.URL(SERVICE_PROTOCOL, host, port, SERVICE_PATH);
//                    , new org.globus.net.protocol.httpg.Handler()); //workaround for using HTTPG in tomcat or OGSi containers
            SRMServiceLocator service = new SRMServiceLocator(s_provider);
            m_stub = service.getsrm(serviceUrl);
            // set security
            Stub stub = (Stub) m_stub;
            stub._setProperty(GSIConstants.GSI_CREDENTIALS, m_credential);
//            stub._setProperty(GSIConstants.GSI_MODE, GSIConstants.GSI_MODE_FULL_DELEG);
        } catch (MalformedURLException e) {
            throw new NoSuccessException("unexpected exception", e);
        } catch (ServiceException e) {
            throw new NoSuccessException(e);
        }
    }

    public void disconnect() throws NoSuccessException {
        // do nothing
    }

    protected void ping() throws BadParameterException, NoSuccessException {
        try {
            SrmPingResponse response = m_stub.srmPing(new SrmPingRequest());
            if (response.getVersionInfo() == null) {
                throw new NoSuccessException("Unknown version");
            }
        } catch (RemoteException e) {
            throw new BadParameterException(e);
        }
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            this.getMetaData(absolutePath);
            return true;
        } catch (DoesNotExistException doesNotExist) {
            return false;
        }
    }

    private SRMResponse srmPrepareToGet(String absolutePath) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI logicalUri = this.toSrmURI(absolutePath);
        SrmPrepareToGetRequest request = new SrmPrepareToGetRequest();
        // dirOption is not supported by DPM
        TDirOption dirOption = null;    //new TDirOption(false, Boolean.FALSE, new Integer(0));
        request.setArrayOfFileRequests(new ArrayOfTGetFileRequest(new TGetFileRequest[]{
                new TGetFileRequest(logicalUri, dirOption)}));
        request.setTransferParameters(new TTransferParameters(
                TAccessPattern.TRANSFER_MODE, TConnectionType.WAN, null, new ArrayOfString(m_transferProtocols)));
        try {
            // send request
            SrmPrepareToGetResponse response = m_stub.srmPrepareToGet(request);

            // save request token
            String token = response.getRequestToken();

            // wait for physical file to be staged
            TReturnStatus status = response.getReturnStatus();
            TGetRequestFileStatus fileStatus = null;
            if (response.getArrayOfFileStatuses()!=null &&
                response.getArrayOfFileStatuses().getStatusArray()!=null &&
                response.getArrayOfFileStatuses().getStatusArray().length>0)
            {
                fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
            }
            long period = 1000;     // 1 second
            SrmStatusOfGetRequestRequest request2 = new SrmStatusOfGetRequestRequest();
            request2.setRequestToken(token);
            request2.setArrayOfSourceSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{logicalUri}));
            while (status.getStatusCode().equals(TStatusCode.SRM_REQUEST_QUEUED) ||
                   status.getStatusCode().equals(TStatusCode.SRM_REQUEST_INPROGRESS))
            {
                try {
                    Thread.sleep(period);
                    period *= 2;
                } catch (InterruptedException e) {/*ignore*/}
                SrmStatusOfGetRequestResponse response2 = m_stub.srmStatusOfGetRequest(request2);
                status = response2.getReturnStatus();
                if (response2.getArrayOfFileStatuses()!=null && response2.getArrayOfFileStatuses().getStatusArray().length>0) {
                    fileStatus = response2.getArrayOfFileStatuses().getStatusArray(0);
                }
            }

            // returns
            TReturnStatus detailedStatus = (fileStatus!=null ? fileStatus.getStatus() : null);
            if (status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
                if (detailedStatus!=null && detailedStatus.getStatusCode().equals(TStatusCode.SRM_FILE_PINNED)) {
                    return new SRMResponse(token, fileStatus.getTransferURL());
                } else {
                    throw new NoSuccessException("Request successful but file is not pinned");
                }
            } else {
                try {
                    if (detailedStatus != null) {
                        rethrowException(detailedStatus);
                    } else {
                        rethrowException(status);
                    }
                    throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
                } catch (AlreadyExistsException e) {
                    throw new NoSuccessException("INTERNAL ERROR: unexpected exception", e);
                }
            }
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }
    }
    public String[] listLocations(String logicalEntry, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // send srmPrepareToGet message
        SRMResponse srmResponse = this.srmPrepareToGet(logicalEntry);
        java.net.URI transferUrl = srmResponse.getTransferUrl();

        // returns
        return new String[]{transferUrl.toString()};
    }
    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // send srmPrepareToPGet message
        SRMResponse srmResponse = this.srmPrepareToGet(absolutePath);
        String token = srmResponse.getToken();
        java.net.URI transferUrl = srmResponse.getTransferUrl();

        // connect to transfer server
        SagaDataAdaptor adaptor = new SagaDataAdaptor(transferUrl, m_credential, m_certRepository, token, absolutePath, this);
        return adaptor.getInputStream(transferUrl.getPath(), null);
    }
    public void freeInputStream(String token, String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI logicalUri = toSrmURI(absolutePath);
        SrmReleaseFilesRequest request = new SrmReleaseFilesRequest();
        request.setRequestToken(token);
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{logicalUri}));
        request.setDoRemove(Boolean.TRUE);
        SrmReleaseFilesResponse response;
        try {
            // send request
            response = m_stub.srmReleaseFiles(request);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }
        TSURLReturnStatus fileStatus = null;
        if (response.getArrayOfFileStatuses()!=null &&
            response.getArrayOfFileStatuses().getStatusArray()!=null &&
            response.getArrayOfFileStatuses().getStatusArray().length>0)
        {
            fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
        }

        // returns
        TReturnStatus status = response.getReturnStatus();
        TReturnStatus detailedStatus = (fileStatus!=null ? fileStatus.getStatus() : null);
        if (! status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            try {
                if (detailedStatus != null) {
                    rethrowException(detailedStatus);
                } else {
                    rethrowException(status);
                }
                throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
            } catch (BadParameterException e) {
                throw new NoSuccessException("INTERNAL ERROR: unexpected exception", e);
            } catch (AlreadyExistsException e) {
                throw new NoSuccessException("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }

    private SRMResponse srmPrepareToPut(String absolutePath) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI logicalUri = this.toSrmURI(absolutePath);
        SrmPrepareToPutRequest request = new SrmPrepareToPutRequest();
        request.setArrayOfFileRequests(new ArrayOfTPutFileRequest(new TPutFileRequest[]{new TPutFileRequest(logicalUri, null)}));
        request.setTransferParameters(new TTransferParameters(
                TAccessPattern.TRANSFER_MODE, TConnectionType.WAN, null, new ArrayOfString(m_transferProtocols)));
        try {
            // send request
            SrmPrepareToPutResponse response = m_stub.srmPrepareToPut(request);

            // save request token
            String token = response.getRequestToken();

            // wait for physical space to be ready
            TReturnStatus status = response.getReturnStatus();
            TPutRequestFileStatus fileStatus = null;
            if (response.getArrayOfFileStatuses()!=null &&
                response.getArrayOfFileStatuses().getStatusArray()!=null &&
                response.getArrayOfFileStatuses().getStatusArray().length>0)
            {
                fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
            }
            long period = 1000;     // 1 second
            SrmStatusOfPutRequestRequest request2 = new SrmStatusOfPutRequestRequest();
            request2.setRequestToken(token);
            request2.setArrayOfTargetSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{logicalUri}));
            while (status.getStatusCode().equals(TStatusCode.SRM_REQUEST_QUEUED) ||
                   status.getStatusCode().equals(TStatusCode.SRM_REQUEST_INPROGRESS))
            {
                try {
                    Thread.sleep(period);
                    period *= 2;
                } catch (InterruptedException e) {/*ignore*/}
                SrmStatusOfPutRequestResponse response2 = m_stub.srmStatusOfPutRequest(request2);
                status = response2.getReturnStatus();
                if (response2.getArrayOfFileStatuses()!=null && response2.getArrayOfFileStatuses().getStatusArray().length>0) {
                    fileStatus = response2.getArrayOfFileStatuses().getStatusArray(0);
                }
            }

            // returns
            TReturnStatus detailedStatus = (fileStatus!=null ? fileStatus.getStatus() : null);
            if (status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
                if (detailedStatus!=null && detailedStatus.getStatusCode().equals(TStatusCode.SRM_SPACE_AVAILABLE)) {
                    return new SRMResponse(token, fileStatus.getTransferURL());
                } else {
                    throw new NoSuccessException("Request successful but space is not available");
                }
            } else {
                try {
                    if (detailedStatus != null) {
                        rethrowException(detailedStatus);
                    } else {
                        rethrowException(status);
                    }
                } catch (DoesNotExistException e2) {
                    throw new ParentDoesNotExist(e2);
                }
                throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
            }
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }
    }
    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        // send srmPrepareToPut message
        String absolutePath = parentAbsolutePath+"/"+fileName;
        SRMResponse srmResponse = this.srmPrepareToPut(absolutePath);
        String token = srmResponse.getToken();
        java.net.URI transferUrl = srmResponse.getTransferUrl();
        
        // connect to transfer server
        SagaDataAdaptor adaptor;
        try {
            adaptor = new SagaDataAdaptor(transferUrl, m_credential, m_certRepository, token, absolutePath, this);
        } catch (DoesNotExistException e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        int pos = transferUrl.getPath().lastIndexOf('/');
        String transferParentPath = (pos>0 ? transferUrl.getPath().substring(0, pos) : "/");
        String transferFileName = (pos>-1 ? transferUrl.getPath().substring(pos+1) : transferUrl.getPath());
        return adaptor.getOutputStream(transferParentPath, transferFileName, exclusive, append, null);
    }
    public void freeOutputStream(String token, String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI logicalUri = toSrmURI(absolutePath);
        SrmPutDoneRequest request = new SrmPutDoneRequest();
        request.setRequestToken(token);
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{logicalUri}));
        SrmPutDoneResponse response;
        try {
            // send request
            response = m_stub.srmPutDone(request);
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }
        TSURLReturnStatus fileStatus = null;
        if (response.getArrayOfFileStatuses()!=null &&
            response.getArrayOfFileStatuses().getStatusArray()!=null &&
            response.getArrayOfFileStatuses().getStatusArray().length>0)
        {
            fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
        }

        // returns
        TReturnStatus status = response.getReturnStatus();
        TReturnStatus detailedStatus = (fileStatus!=null ? fileStatus.getStatus() : null);
        if (! status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            try {
                if (detailedStatus != null) {
                    rethrowException(detailedStatus);
                } else {
                    rethrowException(status);
                }
                throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
            } catch (BadParameterException e) {
                throw new NoSuccessException("INTERNAL ERROR: unexpected exception", e);
            } catch (AlreadyExistsException e) {
                throw new NoSuccessException("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        TMetaDataPathDetail metadata = this.getMetaData(absolutePath);
        return new SRM22FileAttributes(metadata);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        TMetaDataPathDetail metadata = this.getMetaData(absolutePath);
        ArrayOfTMetaDataPathDetail detail = metadata.getArrayOfSubPaths();
        if (detail != null) {   // check for DPM
            TMetaDataPathDetail[] list = detail.getPathDetailArray();
            if (list != null) { // check for dCache
                FileAttributes[] files = new FileAttributes[list.length];
                for (int i=0; list!=null && i<list.length; i++) {
                    files[i] = new SRM22FileAttributes(list[i]);
                }
                return files;
            }
        }
        return new FileAttributes[0];
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI uri = this.toSrmURI(parentAbsolutePath+"/"+directoryName);
        SrmMkdirRequest request = new SrmMkdirRequest();
        request.setSURL(uri);
        SrmMkdirResponse response;
        try {
            // send request
            response = m_stub.srmMkdir(request);
        } catch (RemoteException e) {
            throw new NoSuccessException(e);
        }
        // returns
        TReturnStatus status = response.getReturnStatus();
        if (! status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            try {
                rethrowException(status);
            } catch (DoesNotExistException e) {
                throw new NoSuccessException(e);
            }
            throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
        }
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI uri = this.toSrmURI(parentAbsolutePath+"/"+directoryName);
        SrmRmdirRequest request = new SrmRmdirRequest();
        request.setSURL(uri);
        request.setRecursive(Boolean.FALSE);
        SrmRmdirResponse response;
        try {
            // send request
            response = m_stub.srmRmdir(request);
        } catch (RemoteException e) {
            throw new NoSuccessException(e);
        }
        // returns
        TReturnStatus status = response.getReturnStatus();
        if (! status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            try {
                rethrowException(status);
                throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
            } catch (AlreadyExistsException e) {
                throw new NoSuccessException("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI uri = this.toSrmURI(parentAbsolutePath+"/"+fileName);
        SrmRmRequest request = new SrmRmRequest();
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
        SrmRmResponse response;
        try {
            // send request
            response = m_stub.srmRm(request);
        } catch (RemoteException e) {
            throw new NoSuccessException(e);
        }
        TSURLReturnStatus fileStatus = null;
        if (response.getArrayOfFileStatuses()!=null &&
            response.getArrayOfFileStatuses().getStatusArray()!=null &&
            response.getArrayOfFileStatuses().getStatusArray().length>0)
        {
            fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
        }

        // returns
        TReturnStatus status = response.getReturnStatus();
        TReturnStatus detailedStatus = (fileStatus!=null ? fileStatus.getStatus() : null);
        if (! status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            try {
                if (detailedStatus != null) {
                    rethrowException(detailedStatus);
                } else {
                    rethrowException(status);
                }
                throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
            } catch (AlreadyExistsException e) {
                throw new NoSuccessException("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }
    
    public int[] getSupportedScopes() {
		return new int[]{SCOPE_USER,SCOPE_GROUP,SCOPE_ANY};
	}

	public void permissionsAllow(String absolutePath, int scope, PermissionBytes permissions) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		FileAttributes srmFileAttributes;
		try {
			srmFileAttributes = getAttributes(absolutePath, null);
		} catch (DoesNotExistException e) {
			throw new NoSuccessException(e);
		}
		
		SrmSetPermissionRequest setPermissionRequest = new SrmSetPermissionRequest();
		setPermissionRequest.setSURL(toSrmURI(absolutePath));
		setPermissionRequest.setPermissionType(TPermissionType.CHANGE);
		setPermissionRequest.setArrayOfUserPermissions(null);
		switch (scope) {
			case SCOPE_USER:
					if(srmFileAttributes.getUserPermission().containsAll(permissions.getValue())){
						return;
					}else{
						setPermissionRequest.setOwnerPermission(getSRMPermissions(srmFileAttributes.getUserPermission().or(permissions)));
					}
				break;
			case SCOPE_GROUP:
					if(srmFileAttributes.getGroupPermission().containsAll(permissions.getValue())){
						return;
					}else{
						setPermissionRequest.setArrayOfGroupPermissions(new ArrayOfTGroupPermission(new TGroupPermission[]{new TGroupPermission(srmFileAttributes.getGroup(), getSRMPermissions(srmFileAttributes.getGroupPermission().or(permissions)))}));
					}
				break;
			case SCOPE_ANY:
					if(srmFileAttributes.getAnyPermission().containsAll(permissions.getValue())){
						return;
					}else{
						setPermissionRequest.setOtherPermission(getSRMPermissions(srmFileAttributes.getAnyPermission().or(permissions)));
					}
				break;
			default:
				throw new RuntimeException("Unkown scope: "+scope);
		}
		
		SrmSetPermissionResponse response = null;
		try{
			response = m_stub.srmSetPermission(setPermissionRequest);
		} catch (RemoteException e) {
	        throw new NoSuccessException(e);
	    }
		
        TReturnStatus status = response.getReturnStatus();
        if (! status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            try {
                rethrowException(status);
            } catch (DoesNotExistException e) {
                throw new NoSuccessException(e);
            } catch (AlreadyExistsException e) {
            	throw new NoSuccessException(e);
			} catch (BadParameterException e) {
				throw new NoSuccessException(e);
			}
            throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
        }
	}

//Will be needed when full permissions will be implemented.
/*
	public boolean permissionsCheck(String absolutePath, int scope, PermissionBytes permissions, String id) throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException {
		if(id == null){
			//TODO: Check it!
		}
		
		if(permissions.contains(Permission.QUERY)){
			throw new BadParameterException("The QUERY permission is not supported by the SRM");
		}
		
		if(permissions.contains(Permission.OWNER)){
			throw new BadParameterException("The OWNER permission is not yet supported by the SRM adaptor");
		}
		
		FileAttributes srmFileAttributes;
		try {
			srmFileAttributes = getAttributes(absolutePath, null);
		} catch (DoesNotExistException e) {
			throw new NoSuccessException(e);
		}
		
		PermissionBytes actualScopePermissions;
		switch (scope) {
			case SCOPE_USER:
				actualScopePermissions = srmFileAttributes.getUserPermission();
				break;
			case SCOPE_GROUP:
				actualScopePermissions = srmFileAttributes.getGroupPermission();
				break;
			case SCOPE_ANY:
				actualScopePermissions = srmFileAttributes.getAnyPermission();
				break;
			default:
				throw new RuntimeException("Unkown scope: "+scope);
		}
		
		if(actualScopePermissions.containsAll(permissions.getValue())){
			return true;
		}else{
			return false;
		}
	}
*/

	public void permissionsDeny(String absolutePath, int scope, PermissionBytes permissions) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		FileAttributes srmFileAttributes;
		try {
			srmFileAttributes = getAttributes(absolutePath, null);
		} catch (DoesNotExistException e) {
			throw new NoSuccessException(e);
		}
		
		SrmSetPermissionRequest setPermissionRequest = new SrmSetPermissionRequest();
		setPermissionRequest.setSURL(toSrmURI(absolutePath));
		setPermissionRequest.setPermissionType(TPermissionType.CHANGE);
		setPermissionRequest.setArrayOfUserPermissions(null);
		switch (scope) {
			case SCOPE_USER:
				PermissionBytes userPermissions = srmFileAttributes.getUserPermission();
				PermissionBytes newUserPermissions = removePermissions(userPermissions, permissions);
				if(userPermissions == newUserPermissions){
					return;
				}else{
					setPermissionRequest.setOwnerPermission(getSRMPermissions(newUserPermissions));
				}
				break;
			case SCOPE_GROUP:
				PermissionBytes groupPermissions = srmFileAttributes.getGroupPermission();
				PermissionBytes newGroupPermissions = removePermissions(groupPermissions, permissions);
				if(groupPermissions == newGroupPermissions){
					return;
				}else{
					setPermissionRequest.setArrayOfGroupPermissions(new ArrayOfTGroupPermission(new TGroupPermission[]{new TGroupPermission(srmFileAttributes.getGroup(), getSRMPermissions(newGroupPermissions))}));
				}
				break;
			case SCOPE_ANY:
				PermissionBytes anyPermissions = srmFileAttributes.getAnyPermission();
				PermissionBytes newAnyPermissions = removePermissions(anyPermissions, permissions);
				if(anyPermissions == newAnyPermissions){
					return;
				}else{
					setPermissionRequest.setOtherPermission(getSRMPermissions(srmFileAttributes.getAnyPermission().or(permissions)));
				}
				break;
			default:
				throw new RuntimeException("Unkown scope: "+scope);
		}
		
		SrmSetPermissionResponse response = null;
		try{
			response = m_stub.srmSetPermission(setPermissionRequest);
		} catch (RemoteException e) {
	        throw new NoSuccessException(e);
	    }
		
        TReturnStatus status = response.getReturnStatus();
        if (! status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            try {
                rethrowException(status);
            } catch (DoesNotExistException e) {
                throw new NoSuccessException(e);
            } catch (AlreadyExistsException e) {
            	throw new NoSuccessException(e);
			} catch (BadParameterException e) {
				throw new NoSuccessException(e);
			}
            throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
        }
	}

	public void setGroup(String id) throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException {
		throw new PermissionDeniedException("Only root can do that on SRM.");
	}

	public void setOwner(String id) throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException {
		throw new PermissionDeniedException("Only root can do that on SRM.");
	}

	public String[] getGroupsOf(String id) throws BadParameterException, NoSuccessException {
		String userId = null;
		try {
			userId = m_credential.getName().toString();
		} catch (GSSException e) {
			throw new BadParameterException("Unable to extract the user ID from the certificate");
		}
		if(!userId.equals(id)){
			throw new BadParameterException("The id is not the actual user");
		}
		GlobusCredential globusCred = null;
		if (m_credential instanceof GlobusGSSCredentialImpl) {
			globusCred = ((GlobusGSSCredentialImpl)m_credential).getGlobusCredential();
		} else {
			throw new BadParameterException("Not a globus proxy");
		}

		Vector v = VOMSValidator.parse(globusCred.getCertificateChain());
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

    //////////////////////////////////////// private methods ////////////////////////////////////////
	
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
	
    private static TPermissionMode getSRMPermissions(PermissionBytes permissions) {
    	String perms = "";
    	if(permissions.contains(Permission.READ)){
			perms += "R";
		}
		if(permissions.contains(Permission.WRITE)){
			perms += "W";
		}
		if(permissions.contains(Permission.EXEC)){
			perms += "X";
		}
		
		if(perms.equals("")){
			perms = "NONE";
		}
		
		return TPermissionMode.fromValue(perms);
    }
	
    private TMetaDataPathDetail getMetaData(String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI uri = this.toSrmURI(absolutePath);
        SrmLsRequest request = new SrmLsRequest();
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
        request.setAllLevelRecursive(Boolean.FALSE);
        request.setFullDetailedList(Boolean.TRUE);
        SrmLsResponse response;
        try {
            // send request
            response = m_stub.srmLs(request);
        } catch (AxisFault e) {
            if (EOFException.class.getName().equals(e.getFaultString())) {
                throw new PermissionDeniedException(e.getCause());
            } else {
                throw new TimeoutException(e);
            }
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        }
        TMetaDataPathDetail metadata = null;
        if (response.getDetails()!=null && response.getDetails().getPathDetailArray().length>0) {
            metadata = response.getDetails().getPathDetailArray(0);
        }

        // returns
        TReturnStatus status = response.getReturnStatus();
        TReturnStatus detailedStatus = (metadata!=null ? metadata.getStatus() : null);
        if (status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            if (detailedStatus!=null && detailedStatus.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
                return metadata;
            } else {
                throw new NoSuccessException("Request successful but metadata not received");
            }
        } else {
            try {
                if (detailedStatus != null) {
                    rethrowException(detailedStatus);
                } else {
                    rethrowException(status);
                }
                throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
            } catch (BadParameterException e) {
                throw new NoSuccessException("INTERNAL ERROR: unexpected exception", e);
            } catch (AlreadyExistsException e) {
                throw new NoSuccessException("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }

    private org.apache.axis.types.URI toSrmURI(String absolutePath) throws NoSuccessException {
        try {
            String encodedPath = PathEncoder.encode(absolutePath);
            return new org.apache.axis.types.URI("srm", null, m_host, m_port, SERVICE_PATH, "SFN="+encodedPath, null);
        } catch (org.apache.axis.types.URI.MalformedURIException e) {
            throw new NoSuccessException(e);
        }
    }

    private void rethrowException(TReturnStatus status) throws PermissionDeniedException, BadParameterException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        TStatusCode code = status.getStatusCode();
        String explanation = status.getExplanation();
        if (code.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
            throw new PermissionDeniedException(explanation);
        } else if (code.equals(TStatusCode.SRM_NON_EMPTY_DIRECTORY)) {
            throw new NoSuccessException(explanation);
        } else if (code.equals(TStatusCode.SRM_INVALID_PATH)
                || code.equals(TStatusCode.SRM_FILE_LOST)) {
            throw new DoesNotExistException(explanation);
        } else if (code.equals(TStatusCode.SRM_DUPLICATION_ERROR)) {
            throw new AlreadyExistsException(explanation);
        } else if (code.equals(TStatusCode.SRM_REQUEST_TIMED_OUT)
                || code.equals(TStatusCode.SRM_FILE_LIFETIME_EXPIRED)
                || code.equals(TStatusCode.SRM_SPACE_LIFETIME_EXPIRED)) {
            throw new TimeoutException(explanation);
        } else {
            throw new NoSuccessException(code.getValue()+": "+explanation);
        }
    }
}
