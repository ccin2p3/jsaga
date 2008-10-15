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

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
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
            throw new NoSuccess("unexpected exception", e);
        } catch (ServiceException e) {
            throw new NoSuccess(e);
        }
    }

    public void disconnect() throws NoSuccess {
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
        } catch (PermissionDenied e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess(e);
        } catch (Timeout e) {
            throw new NoSuccess(e);
        }
    }

    protected void ping() throws BadParameter, NoSuccess {
        try {
            SrmPingResponse response = m_stub.srmPing(new SrmPingRequest());
            if (response.getVersionInfo() == null) {
                throw new NoSuccess("Unknown version");
            }
        } catch (RemoteException e) {
            throw new BadParameter(e);
        }
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDenied, Timeout, NoSuccess {
        try {
            this.getMetaData(absolutePath);
            return true;
        } catch (DoesNotExist doesNotExist) {
            return false;
        }
    }

    public boolean isDirectory(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        TMetaDataPathDetail metadata = this.getMetaData(absolutePath);
        return metadata.getType().equals(TFileType.DIRECTORY);
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI logicalUri = this.toSrmURI(absolutePath);
        SrmPrepareToGetRequest request = new SrmPrepareToGetRequest();
        request.setArrayOfFileRequests(new ArrayOfTGetFileRequest(new TGetFileRequest[]{
                new TGetFileRequest(logicalUri, new TDirOption(false, Boolean.FALSE, new Integer(0)))}));
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
                    transferUrl = new java.net.URI(fileStatus.getTransferURL().toString());
                } else {
                    throw new NoSuccess("Request successful but file is not pinned");
                }
            } else {
                try {
                    if (detailedStatus != null) {
                        rethrowException(detailedStatus);
                    } else {
                        rethrowException(status);
                    }
                    throw new NoSuccess("INTERNAL ERROR: an exception should have been raised");
                } catch (AlreadyExists e) {
                    throw new NoSuccess("INTERNAL ERROR: unexpected exception", e);
                }
            }
        } catch (RemoteException e) {
            throw new Timeout(e);
        } catch (URISyntaxException e) {
            throw new NoSuccess(e);
        }
        // connect to transfer server
        SagaDataAdaptor adaptor = new SagaDataAdaptor(transferUrl, m_credential);
        return adaptor.getInputStream(transferUrl.getPath(), null);
    }
    private void closeInputStream(String token, String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
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
            throw new Timeout(e);
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
                throw new NoSuccess("INTERNAL ERROR: an exception should have been raised");
            } catch (BadParameter e) {
                throw new NoSuccess("INTERNAL ERROR: unexpected exception", e);
            } catch (AlreadyExists e) {
                throw new NoSuccess("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
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
                    throw new NoSuccess("Request successful but space is not available");
                }
            } else {
                try {
                    if (detailedStatus != null) {
                        rethrowException(detailedStatus);
                    } else {
                        rethrowException(status);
                    }
                } catch (DoesNotExist e2) {
                    throw new NoSuccess(e2);
                }
                throw new NoSuccess("INTERNAL ERROR: an exception should have been raised");
            }
        } catch (RemoteException e) {
            throw new Timeout(e);
        } catch (URISyntaxException e) {
            throw new NoSuccess(e);
        }
        // connect to transfer server
        SagaDataAdaptor adaptor;
        try {
            adaptor = new SagaDataAdaptor(transferUrl, m_credential);
        } catch (DoesNotExist e) {
            throw new ParentDoesNotExist("Parent directory does not exist");
        }
        int pos = transferUrl.getPath().lastIndexOf('/');
        String transferParentPath = (pos>0 ? transferUrl.getPath().substring(0, pos) : "/");
        String transferFileName = (pos>-1 ? transferUrl.getPath().substring(pos+1) : transferUrl.getPath());
        return adaptor.getOutputStream(transferParentPath, transferFileName, exclusive, append, null);
    }
    private void closeOutputStream(String token, String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI logicalUri = toSrmURI(absolutePath);
        SrmPutDoneRequest request = new SrmPutDoneRequest();
        request.setRequestToken(token);
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{logicalUri}));
        SrmPutDoneResponse response;
        try {
            // send request
            response = m_stub.srmPutDone(request);
        } catch (RemoteException e) {
            throw new Timeout(e);
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
                throw new NoSuccess("INTERNAL ERROR: an exception should have been raised");
            } catch (BadParameter e) {
                throw new NoSuccess("INTERNAL ERROR: unexpected exception", e);
            } catch (AlreadyExists e) {
                throw new NoSuccess("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        TMetaDataPathDetail metadata = this.getMetaData(absolutePath);
        return new SRM22FileAttributes(metadata);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
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

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = this.toSrmURI(parentAbsolutePath+"/"+directoryName);
        SrmMkdirRequest request = new SrmMkdirRequest();
        request.setSURL(uri);
        SrmMkdirResponse response;
        try {
            // send request
            response = m_stub.srmMkdir(request);
        } catch (RemoteException e) {
            throw new NoSuccess(e);
        }
        // returns
        TReturnStatus status = response.getReturnStatus();
        if (! status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            try {
                rethrowException(status);
            } catch (DoesNotExist e) {
                throw new NoSuccess(e);
            }
            throw new NoSuccess("INTERNAL ERROR: an exception should have been raised");
        }
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = this.toSrmURI(parentAbsolutePath+"/"+directoryName);
        SrmRmdirRequest request = new SrmRmdirRequest();
        request.setSURL(uri);
        request.setRecursive(Boolean.FALSE);
        SrmRmdirResponse response;
        try {
            // send request
            response = m_stub.srmRmdir(request);
        } catch (RemoteException e) {
            throw new NoSuccess(e);
        }
        // returns
        TReturnStatus status = response.getReturnStatus();
        if (! status.getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            try {
                rethrowException(status);
                throw new NoSuccess("INTERNAL ERROR: an exception should have been raised");
            } catch (AlreadyExists e) {
                throw new NoSuccess("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = this.toSrmURI(parentAbsolutePath+"/"+fileName);
        SrmRmRequest request = new SrmRmRequest();
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
        SrmRmResponse response;
        try {
            // send request
            response = m_stub.srmRm(request);
        } catch (RemoteException e) {
            throw new NoSuccess(e);
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
                throw new NoSuccess("INTERNAL ERROR: an exception should have been raised");
            } catch (AlreadyExists e) {
                throw new NoSuccess("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }

    //////////////////////////////////////// private methods ////////////////////////////////////////

    private TMetaDataPathDetail getMetaData(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = this.toSrmURI(absolutePath);
        SrmLsRequest request = new SrmLsRequest();
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
        request.setAllLevelRecursive(Boolean.FALSE);
        SrmLsResponse response;
        try {
            // send request
            response = m_stub.srmLs(request);
        } catch (RemoteException e) {
            throw new Timeout(e);
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
                throw new NoSuccess("Request successful but metadata not received");
            }
        } else {
            try {
                if (detailedStatus != null) {
                    rethrowException(detailedStatus);
                } else {
                    rethrowException(status);
                }
                throw new NoSuccess("INTERNAL ERROR: an exception should have been raised");
            } catch (BadParameter e) {
                throw new NoSuccess("INTERNAL ERROR: unexpected exception", e);
            } catch (AlreadyExists e) {
                throw new NoSuccess("INTERNAL ERROR: unexpected exception", e);
            }
        }
    }

    private org.apache.axis.types.URI toSrmURI(String absolutePath) throws NoSuccess {
        try {
            return new org.apache.axis.types.URI("srm", null, m_host, m_port, absolutePath, null, null);
        } catch (org.apache.axis.types.URI.MalformedURIException e) {
            throw new NoSuccess(e);
        }
    }

    //todo: convert missing parent directory error to ParentDoesNotExist exception
    private void rethrowException(TReturnStatus status) throws PermissionDenied, BadParameter, DoesNotExist, AlreadyExists, Timeout, NoSuccess {
        TStatusCode code = status.getStatusCode();
        String explanation = status.getExplanation();
        if (code.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
            throw new PermissionDenied(explanation);
        } else if (code.equals(TStatusCode.SRM_NON_EMPTY_DIRECTORY)) {
            throw new NoSuccess(explanation);
        } else if (code.equals(TStatusCode.SRM_INVALID_PATH)
                || code.equals(TStatusCode.SRM_FILE_LOST)) {
            throw new DoesNotExist(explanation);
        } else if (code.equals(TStatusCode.SRM_DUPLICATION_ERROR)) {
            throw new AlreadyExists(explanation);
        } else if (code.equals(TStatusCode.SRM_REQUEST_TIMED_OUT)
                || code.equals(TStatusCode.SRM_FILE_LIFETIME_EXPIRED)
                || code.equals(TStatusCode.SRM_SPACE_LIFETIME_EXPIRED)) {
            throw new Timeout(explanation);
        } else {
            throw new NoSuccess(code.getValue()+": "+explanation);
        }
    }
}
