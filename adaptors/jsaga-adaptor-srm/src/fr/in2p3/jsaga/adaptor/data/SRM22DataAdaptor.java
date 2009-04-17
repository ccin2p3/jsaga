package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import org.apache.axis.client.Stub;
import org.globus.axis.gsi.GSIConstants;
import org.ogf.saga.error.*;
import org.ogf.srm22.*;

import javax.xml.rpc.ServiceException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.*;

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
public class SRM22DataAdaptor extends SRMDataAdaptorAbstract implements FileReaderStreamFactory, FileWriterStreamFactory {
    private static final String SERVICE_PROTOCOL = "httpg";
    private static final String SERVICE_PATH = "/srm/managerv2";
    private ISRM m_stub;
    private List m_readFiles;
    private List m_writenFiles;

    public SRM22DataAdaptor() {
        m_stub = null;
        m_readFiles = new ArrayList();
        m_writenFiles = new ArrayList();
    }

    public String getType() {
        return "srm";   //todo: replace with srm-v2.2 when a generic plugin will be developed
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        super.connect(userInfo, host, port, basePath, attributes);
        try {
            java.net.URL serviceUrl = new java.net.URL(SERVICE_PROTOCOL, host, port, SERVICE_PATH);
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
        try {
            for (Iterator it=m_readFiles.iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                String token = (String) entry.getKey();
                String absolutePath = (String) entry.getValue();
                this.closeInputStream(token, absolutePath);
            }
            for (Iterator it=m_writenFiles.iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                String token = (String) entry.getKey();
                String absolutePath = (String) entry.getValue();
                this.closeOutputStream(token, absolutePath);
            }
        } catch (PermissionDeniedException e) {
            throw new NoSuccessException(e);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException(e);
        } catch (TimeoutException e) {
            throw new NoSuccessException(e);
        }
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

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI logicalUri = this.toSrmURI(absolutePath);
        SrmPrepareToGetRequest request = new SrmPrepareToGetRequest();
        // dirOption is not supported by DPM
        TDirOption dirOption = null;    //new TDirOption(false, Boolean.FALSE, new Integer(0));
        request.setArrayOfFileRequests(new ArrayOfTGetFileRequest(new TGetFileRequest[]{
                new TGetFileRequest(logicalUri, dirOption)}));
        request.setTransferParameters(new TTransferParameters(
                TAccessPattern.TRANSFER_MODE, TConnectionType.WAN, null, new ArrayOfString(m_transferProtocols)));
        java.net.URI transferUrl;
        try {
            // send request
            SrmPrepareToGetResponse response = m_stub.srmPrepareToGet(request);

            // save request token
            String token = response.getRequestToken();

            // wait for physical file to be staged
            TReturnStatus status = response.getReturnStatus();
            TGetRequestFileStatus fileStatus = null;
            if (response.getArrayOfFileStatuses()!=null && response.getArrayOfFileStatuses().getStatusArray().length>0) {
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
                    Thread.currentThread().sleep(period);
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
                    org.apache.axis.types.URI tUri = fileStatus.getTransferURL();
                    try {
                        //todo: remove this workaround when the bug will be fixed in DPM
                        if (tUri.getPath()!=null && tUri.getPath().indexOf(':')>-1) {
                            tUri.setPath(tUri.getPath().substring(tUri.getPath().indexOf(':')+1));
                        }
                        if (tUri.getPort() == -1) {
                            tUri.setPort(2811);
                        }
                    } catch (org.apache.axis.types.URI.MalformedURIException e) {
                        throw new NoSuccessException("INTERNAL ERROR: failed to correct transfer URI: "+tUri);
                    }
                    transferUrl = new java.net.URI(fileStatus.getTransferURL().toString());
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
        } catch (URISyntaxException e) {
            throw new NoSuccessException(e);
        }
        // connect to transfer server
        SagaDataAdaptor adaptor = new SagaDataAdaptor(transferUrl, m_credential);
        return adaptor.getInputStream(transferUrl.getPath(), null);
    }
    private void closeInputStream(String token, String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
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
        if (response.getArrayOfFileStatuses()!=null && response.getArrayOfFileStatuses().getStatusArray().length>0) {
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

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI logicalUri = this.toSrmURI(parentAbsolutePath+"/"+fileName);
        SrmPrepareToPutRequest request = new SrmPrepareToPutRequest();
        request.setArrayOfFileRequests(new ArrayOfTPutFileRequest(new TPutFileRequest[]{new TPutFileRequest(logicalUri, null)}));
        request.setTransferParameters(new TTransferParameters(
                TAccessPattern.TRANSFER_MODE, TConnectionType.WAN, null, new ArrayOfString(m_transferProtocols)));
        java.net.URI transferUrl;
        try {
            // send request
            SrmPrepareToPutResponse response = m_stub.srmPrepareToPut(request);

            // save request token
            String token = response.getRequestToken();

            // wait for physical space to be ready
            TReturnStatus status = response.getReturnStatus();
            TPutRequestFileStatus fileStatus = null;
            if (response.getArrayOfFileStatuses()!=null && response.getArrayOfFileStatuses().getStatusArray().length>0) {
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
                    Thread.currentThread().sleep(period);
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
                    transferUrl = new java.net.URI(fileStatus.getTransferURL().toString());
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
                    throw new NoSuccessException(e2);
                }
                throw new NoSuccessException("INTERNAL ERROR: an exception should have been raised");
            }
        } catch (RemoteException e) {
            throw new TimeoutException(e);
        } catch (URISyntaxException e) {
            throw new NoSuccessException(e);
        }
        // connect to transfer server
        SagaDataAdaptor adaptor;
        try {
            adaptor = new SagaDataAdaptor(transferUrl, m_credential);
        } catch (DoesNotExistException e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        int pos = transferUrl.getPath().lastIndexOf('/');
        String transferParentPath = (pos>0 ? transferUrl.getPath().substring(0, pos) : "/");
        String transferFileName = (pos>-1 ? transferUrl.getPath().substring(pos+1) : transferUrl.getPath());
        return adaptor.getOutputStream(transferParentPath, transferFileName, exclusive, append, null);
    }
    private void closeOutputStream(String token, String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
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
        if (response.getArrayOfFileStatuses()!=null && response.getArrayOfFileStatuses().getStatusArray().length>0) {
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
        TMetaDataPathDetail[] list = metadata.getArrayOfSubPaths().getPathDetailArray();
        if (list != null) {
            FileAttributes[] files = new FileAttributes[list.length];
            for (int i=0; list!=null && i<list.length; i++) {
                files[i] = new SRM22FileAttributes(list[i]);
            }
            return files;
        } else {
            return new FileAttributes[0];
        }
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
        if (response.getArrayOfFileStatuses()!=null && response.getArrayOfFileStatuses().getStatusArray().length>0) {
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

    //////////////////////////////////////// private methods ////////////////////////////////////////

    private TMetaDataPathDetail getMetaData(String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        org.apache.axis.types.URI uri = this.toSrmURI(absolutePath);
        SrmLsRequest request = new SrmLsRequest();
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
        request.setAllLevelRecursive(Boolean.FALSE);
        SrmLsResponse response;
        try {
            // send request
            response = m_stub.srmLs(request);
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
            return new org.apache.axis.types.URI("srm", null, m_host, m_port, SERVICE_PATH, "SFN="+absolutePath, null);
        } catch (org.apache.axis.types.URI.MalformedURIException e) {
            throw new NoSuccessException(e);
        }
    }

    //todo: convert missing parent directory error to ParentDoesNotExist exception
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
