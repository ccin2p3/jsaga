package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
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
import java.util.Map;

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
public class SRM22DataAdaptor extends SRMDataAdaptorAbstract implements DirectoryReader, DirectoryWriter, FileReader, FileWriter {
    private static final String SERVICE_PROTOCOL = "httpg";
    private static final String SERVICE_PATH = "/srm/managerv2";
    private ISRM m_stub;
    private String m_readToken;
    private String m_writeToken;

    public SRM22DataAdaptor() {
        m_stub = null;
        m_readToken = null;
        m_writeToken = null;
    }

    public String[] getSchemeAliases() {
        return new String[]{"srm"}; //todo: replace with srm22 when a generic plugin will be developed
    }

    public void connect(String userInfo, String host, int port, Map attributes) throws AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        super.connect(userInfo, host, port, attributes);
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
            if (m_readToken != null) {
                this.closeInputStream(null);
            }
            if (m_writeToken != null) {
                this.closeOutputStream(null);
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

    public boolean exists(String absolutePath) throws PermissionDenied, Timeout, NoSuccess {
        try {
            this.getMetaData(absolutePath);
            return true;
        } catch (DoesNotExist doesNotExist) {
            return false;
        }
    }

    public boolean isDirectory(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        TMetaDataPathDetail metadata = this.getMetaData(absolutePath);
        return metadata.getType().equals(TFileType.DIRECTORY);
    }

    public boolean isEntry(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        TMetaDataPathDetail metadata = this.getMetaData(absolutePath);
        return metadata.getType().equals(TFileType.FILE);
    }

    public long getSize(String absolutePath) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        TMetaDataPathDetail metadata = this.getMetaData(absolutePath);
        return metadata.getSize().longValue();
    }

    public InputStream getInputStream(String absolutePath) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        if (m_readToken != null) {
            throw new NoSuccess("Input stream already opened !");
        }
        org.apache.axis.types.URI logicalUri = toSrmURI(absolutePath);
        SrmPrepareToGetRequest request = new SrmPrepareToGetRequest();
        request.setArrayOfFileRequests(new ArrayOfTGetFileRequest(new TGetFileRequest[]{
                new TGetFileRequest(logicalUri, new TDirOption(false, Boolean.FALSE, new Integer(0)))}));
        request.setTransferParameters(new TTransferParameters(
                TAccessPattern.TRANSFER_MODE, TConnectionType.WAN, null, new ArrayOfString(m_transferProtocols)));
        java.net.URI transferUrl;
        try {
            SrmPrepareToGetResponse response = m_stub.srmPrepareToGet(request);
            // save request token
            m_readToken = response.getRequestToken();
            // wait for physical file to be staged
            TStatusCode status = response.getReturnStatus().getStatusCode();
            TGetRequestFileStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
            long period = 15000;    // 15 seconds
            SrmStatusOfGetRequestRequest request2 = new SrmStatusOfGetRequestRequest();
            request2.setRequestToken(m_readToken);
            request2.setArrayOfSourceSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{logicalUri}));
            while (status.equals(TStatusCode.SRM_REQUEST_QUEUED) || status.equals(TStatusCode.SRM_REQUEST_INPROGRESS)) {
                try {
                    Thread.currentThread().sleep(period);
                    period *= 2;
                } catch (InterruptedException e) {/*ignore*/}
                SrmStatusOfGetRequestResponse response2 = m_stub.srmStatusOfGetRequest(request2);
                status = response2.getReturnStatus().getStatusCode();
                fileStatus = response2.getArrayOfFileStatuses().getStatusArray(0);
            }
            status = fileStatus.getStatus().getStatusCode();
            if (status.equals(TStatusCode.SRM_FILE_PINNED)) {
                transferUrl = new java.net.URI(fileStatus.getTransferURL().toString());
            } else {
                String explanation = fileStatus.getStatus().getExplanation();
                if (status.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
                    throw new PermissionDenied(explanation);
                } else if (status.equals(TStatusCode.SRM_INVALID_PATH)) {
                    throw new DoesNotExist(explanation);
                } else if (status.equals(TStatusCode.SRM_FILE_LIFETIME_EXPIRED)) {
                    throw new Timeout(explanation);
                } else {
                    throw new NoSuccess(status.getValue()+": "+explanation);
                }
            }
        } catch (RemoteException e) {
            throw new Timeout(e);
        } catch (URISyntaxException e) {
            throw new NoSuccess(e);
        }
        // connect to transfer server
        SagaDataAdaptor adaptor = new SagaDataAdaptor(transferUrl, m_credential);
        return adaptor.getInputStream(transferUrl.getPath());
    }
    private void closeInputStream(String logicalEntry) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        //todo
//        org.apache.axis.types.URI logicalUri = toSrmURI(logicalEntry);
        SrmReleaseFilesRequest request = new SrmReleaseFilesRequest();
        request.setRequestToken(m_readToken);
//        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{logicalUri}));
        request.setDoRemove(Boolean.TRUE);
        SrmReleaseFilesResponse response;
        try {
            response = m_stub.srmReleaseFiles(request);
        } catch (RemoteException e) {
            throw new Timeout(e);
        }
        TSURLReturnStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
        TStatusCode status = fileStatus.getStatus().getStatusCode();
        if (! status.equals(TStatusCode.SRM_SUCCESS)) {
            String explanation = fileStatus.getStatus().getExplanation();
            if (status.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
                throw new PermissionDenied(explanation);
            } else if (status.equals(TStatusCode.SRM_INVALID_PATH)) {
                throw new DoesNotExist(explanation);
            } else if (status.equals(TStatusCode.SRM_FILE_LIFETIME_EXPIRED)) {
                throw new Timeout(explanation);
            } else {
                throw new NoSuccess(status.getValue()+": "+explanation);
            }
        }
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append) throws PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (m_writeToken != null) {
            throw new NoSuccess("Output stream already opened !");
        }
        org.apache.axis.types.URI uri = toSrmURI(parentAbsolutePath+"/"+fileName);
        SrmPrepareToPutRequest request = new SrmPrepareToPutRequest();
        request.setArrayOfFileRequests(new ArrayOfTPutFileRequest(new TPutFileRequest[]{new TPutFileRequest(uri, null)}));
        request.setTransferParameters(new TTransferParameters(
                TAccessPattern.TRANSFER_MODE, TConnectionType.WAN, null, new ArrayOfString(m_transferProtocols)));
        java.net.URI transferUrl;
        try {
            SrmPrepareToPutResponse response = m_stub.srmPrepareToPut(request);
            // save request token
            m_writeToken = response.getRequestToken();
            // wait for physical space to be ready
            TStatusCode status = response.getReturnStatus().getStatusCode();
            TPutRequestFileStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
            long period = 15000;    // 15 seconds
            SrmStatusOfPutRequestRequest request2 = new SrmStatusOfPutRequestRequest();
            request2.setRequestToken(m_writeToken);
            request2.setArrayOfTargetSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
            while (status.equals(TStatusCode.SRM_REQUEST_QUEUED) || status.equals(TStatusCode.SRM_REQUEST_INPROGRESS)) {
                try {
                    Thread.currentThread().sleep(period);
                    period *= 2;
                } catch (InterruptedException e) {/*ignore*/}
                SrmStatusOfPutRequestResponse response2 = m_stub.srmStatusOfPutRequest(request2);
                status = response2.getReturnStatus().getStatusCode();
                fileStatus = response2.getArrayOfFileStatuses().getStatusArray(0);
            }
            status = fileStatus.getStatus().getStatusCode();
            if (status.equals(TStatusCode.SRM_SPACE_AVAILABLE)) {
                transferUrl = new java.net.URI(fileStatus.getTransferURL().toString());
                // OK
            } else {
                String explanation = fileStatus.getStatus().getExplanation();
                if (status.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
                    throw new PermissionDenied(explanation);
                } else if (status.equals(TStatusCode.SRM_INVALID_PATH) || status.equals(TStatusCode.SRM_DUPLICATION_ERROR)) {
                    throw new DoesNotExist(explanation);
                } else if (status.equals(TStatusCode.SRM_SPACE_LIFETIME_EXPIRED)) {
                    throw new Timeout(explanation);
                } else {
                    throw new NoSuccess(status.getValue()+": "+explanation);
                }
            }
        } catch (RemoteException e) {
            throw new Timeout(e);
        } catch (URISyntaxException e) {
            throw new NoSuccess(e);
        }
        // connect to transfer server
        SagaDataAdaptor adaptor = new SagaDataAdaptor(transferUrl, m_credential);
        int pos = transferUrl.getPath().lastIndexOf('/');
        String transferParentPath = (pos>0 ? transferUrl.getPath().substring(0, pos) : "/");
        String transferFileName = (pos>-1 ? transferUrl.getPath().substring(pos+1) : transferUrl.getPath());
        return adaptor.getOutputStream(transferParentPath, transferFileName, exclusive, append);
    }
    private void closeOutputStream(String logicalEntry) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        //todo
//        org.apache.axis.types.URI logicalUri = toSrmURI(logicalEntry);
        SrmPutDoneRequest request = new SrmPutDoneRequest();
        request.setRequestToken(m_writeToken);
//        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{logicalUri}));
        SrmPutDoneResponse response;
        try {
            response = m_stub.srmPutDone(request);
        } catch (RemoteException e) {
            throw new Timeout(e);
        }
        TSURLReturnStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
        TStatusCode status = fileStatus.getStatus().getStatusCode();
        if (! status.equals(TStatusCode.SRM_SUCCESS)) {
            String explanation = fileStatus.getStatus().getExplanation();
            if (status.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
                throw new PermissionDenied(explanation);
            } else if (status.equals(TStatusCode.SRM_INVALID_PATH) || status.equals(TStatusCode.SRM_DUPLICATION_ERROR)) {
                throw new DoesNotExist(explanation);
            } else if (status.equals(TStatusCode.SRM_SPACE_LIFETIME_EXPIRED)) {
                throw new Timeout(explanation);
            } else {
                throw new NoSuccess(status.getValue()+": "+explanation);
            }
        }
    }

    public FileAttributes[] listAttributes(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        TMetaDataPathDetail metadata = this.getMetaData(absolutePath);
        TMetaDataPathDetail[] list = metadata.getArrayOfSubPaths().getPathDetailArray();
        FileAttributes[] files = new FileAttributes[list.length];
        for (int i=0; list!=null && i<list.length; i++) {
            files[i] = new SRM22FileAttributes(list[i]);
        }
        return files;
    }

    public void makeDir(String parentAbsolutePath, String directoryName) throws PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = toSrmURI(parentAbsolutePath+"/"+directoryName);
        SrmMkdirRequest request = new SrmMkdirRequest();
        request.setSURL(uri);
        SrmMkdirResponse response;
        try {
            response = m_stub.srmMkdir(request);
        } catch (RemoteException e) {
            throw new NoSuccess(e);
        }
        TStatusCode status = response.getReturnStatus().getStatusCode();
        String explanation = response.getReturnStatus().getExplanation();
        if (! status.equals(TStatusCode.SRM_SUCCESS)) {
            if (status.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
                throw new PermissionDenied(explanation);
            } else if (status.equals(TStatusCode.SRM_DUPLICATION_ERROR)) {
                throw new AlreadyExists(explanation);
            } else if (status.equals(TStatusCode.SRM_INVALID_PATH)) {
                throw new DoesNotExist(explanation);
            } else {
                throw new NoSuccess(status.getValue()+": "+explanation);
            }
        }
    }

    public void removeDir(String parentAbsolutePath, String directoryName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = toSrmURI(parentAbsolutePath+"/"+directoryName);
        SrmRmdirRequest request = new SrmRmdirRequest();
        request.setSURL(uri);
        request.setRecursive(Boolean.FALSE);
        SrmRmdirResponse response;
        try {
            response = m_stub.srmRmdir(request);
        } catch (RemoteException e) {
            throw new NoSuccess(e);
        }
        TStatusCode status = response.getReturnStatus().getStatusCode();
        String explanation = response.getReturnStatus().getExplanation();
        if (! status.equals(TStatusCode.SRM_SUCCESS)) {
            if (status.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
                throw new PermissionDenied(explanation);
            } else if (status.equals(TStatusCode.SRM_NON_EMPTY_DIRECTORY)) {
                throw new BadParameter(explanation);
            } else if (status.equals(TStatusCode.SRM_INVALID_PATH)) {
                throw new DoesNotExist(explanation);
            } else {
                throw new NoSuccess(status.getValue()+": "+explanation);
            }
        }
    }

    public void removeFile(String parentAbsolutePath, String fileName) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = toSrmURI(parentAbsolutePath+"/"+fileName);
        SrmRmRequest request = new SrmRmRequest();
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
        SrmRmResponse response;
        try {
            response = m_stub.srmRm(request);
        } catch (RemoteException e) {
            throw new NoSuccess(e);
        }
        if (! response.getReturnStatus().getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            TSURLReturnStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
            TStatusCode status = fileStatus.getStatus().getStatusCode();
            String explanation = fileStatus.getStatus().getExplanation();
            if (status.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
                throw new PermissionDenied(explanation);
            } else if (status.equals(TStatusCode.SRM_INVALID_PATH) || status.equals(TStatusCode.SRM_FILE_LOST)) {
                throw new DoesNotExist(explanation);
            } else {
                throw new NoSuccess(status.getValue()+": "+explanation);
            }
        }
    }

    //////////////////////////////////////// private methods ////////////////////////////////////////

    private TMetaDataPathDetail getMetaData(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = toSrmURI(absolutePath);
        SrmLsRequest request = new SrmLsRequest();
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
        request.setAllLevelRecursive(Boolean.FALSE);
        SrmLsResponse response;
        try {
            response = m_stub.srmLs(request);
        } catch (RemoteException e) {
            throw new Timeout(e);
        }
        // analyse response
        TMetaDataPathDetail metadata = response.getDetails().getPathDetailArray(0);
        if (response.getReturnStatus().getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            return metadata;
        } else {
            TStatusCode status = metadata.getStatus().getStatusCode();
            String explanation = metadata.getStatus().getExplanation();
            if (status.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
                throw new PermissionDenied(explanation);
            } else if (status.equals(TStatusCode.SRM_INVALID_PATH)) {
                throw new DoesNotExist(explanation);
            } else if (status.equals(TStatusCode.SRM_REQUEST_TIMED_OUT)) {
                throw new Timeout(explanation);
            } else {
                throw new NoSuccess(status.getValue()+": "+explanation);
            }
        }
    }

    private static org.apache.axis.types.URI toSrmURI(String absolutePath) throws NoSuccess {
        try {
            return new org.apache.axis.types.URI("srm", absolutePath);
        } catch (org.apache.axis.types.URI.MalformedURIException e) {
            throw new NoSuccess(e);
        }
    }
}
