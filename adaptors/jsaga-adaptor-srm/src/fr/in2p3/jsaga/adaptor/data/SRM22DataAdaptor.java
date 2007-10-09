package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.*;
import fr.in2p3.jsaga.adaptor.data.write.DirectoryWriter;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriterAsync;
import org.apache.axis.client.Stub;
import org.globus.axis.gsi.GSIConstants;
//import org.gridforum.jgss.ExtendedGSSCredential;
//import org.gridforum.jgss.ExtendedGSSManager;
//import org.ietf.jgss.GSSCredential;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.srm22.*;

import javax.xml.rpc.ServiceException;
//import java.io.File;
//import java.io.FileInputStream;
import java.lang.Exception;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
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
public class SRM22DataAdaptor extends SRMDataAdaptorAbstract implements DirectoryReader {//, DirectoryWriter, LogicalReaderAsync, LogicalWriterAsync {
    private static final String SERVICE_PROTOCOL = "httpg";
    private static final String SERVICE_PATH = "/srm/managerv2";
    private ISRM m_stub;
    private Map m_tokenForGetRequest;
    private Map m_tokenForPutRequest;

    public static void main(String[] args) throws Exception {
        SRM22DataAdaptor adaptor = new SRM22DataAdaptor();

        // load credential
/*
        File proxyFile = new File("E:\\User Settings\\Bureau\\x509up_u_sylvain reynaud");
        byte [] proxyBytes = new byte[(int) proxyFile.length()];
        FileInputStream in = new FileInputStream(proxyFile);
        in.read(proxyBytes);
        in.close();
        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        adaptor.m_credential = manager.createCredential(proxyBytes, ExtendedGSSCredential.IMPEXP_OPAQUE, GSSCredential.DEFAULT_LIFETIME, null, GSSCredential.INITIATE_AND_ACCEPT);
*/

        // test
        adaptor.connect(null, "ccsrmtestv2.in2p3.fr", 8443, null);
        TMetaDataPathDetail metadata = adaptor.getMetaData("/pnfs/in2p3.fr/data/dteam");
        TMetaDataPathDetail[] list = metadata.getArrayOfSubPaths().getPathDetailArray();
        for (int i=0; list!=null && i<list.length; i++) {
            System.out.println(list[i].getPath());
        }
    }

    public SRM22DataAdaptor() {
        super();
        m_tokenForGetRequest = new HashMap();
        m_tokenForPutRequest = new HashMap();
    }

    public String[] getSchemeAliases() {
        return new String[]{"srm"}; //todo: replace with srm22 when a generic plugin will be developed
    }

    public void connect(String userInfo, String host, int port, Map attributes) throws AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        try {
            URL serviceUri = new URL(SERVICE_PROTOCOL, host, port, SERVICE_PATH);
            SRMServiceLocator service = new SRMServiceLocator(s_provider);
            m_stub = service.getsrm(serviceUri);
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

    public String[] listLocations(String logicalEntry) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = toSrmURI(logicalEntry);
        SrmPrepareToGetRequest request = new SrmPrepareToGetRequest();
        request.setArrayOfFileRequests(new ArrayOfTGetFileRequest(new TGetFileRequest[]{
                new TGetFileRequest(uri, new TDirOption(false, Boolean.FALSE, new Integer(0)))}));
        SrmPrepareToGetResponse response;
        try {
            response = m_stub.srmPrepareToGet(request);
        } catch (RemoteException e) {
            throw new Timeout(e);
        }
        // save request token
        String token = response.getRequestToken();
        m_tokenForGetRequest.put(logicalEntry, token);
        // wait for physical file to be staged
        TStatusCode status = response.getReturnStatus().getStatusCode();
        TGetRequestFileStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
        try {
            long period = 15000;    // 15 seconds
            SrmStatusOfGetRequestRequest request2 = new SrmStatusOfGetRequestRequest();
            request2.setRequestToken(token);
            request2.setArrayOfSourceSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
            while (status.equals(TStatusCode.SRM_REQUEST_QUEUED) || status.equals(TStatusCode.SRM_REQUEST_INPROGRESS)) {
                try {
                    Thread.currentThread().sleep(period);
                    period *= 2;
                } catch (InterruptedException e) {/*ignore*/}
                SrmStatusOfGetRequestResponse response2 = m_stub.srmStatusOfGetRequest(request2);
                status = response2.getReturnStatus().getStatusCode();
                fileStatus = response2.getArrayOfFileStatuses().getStatusArray(0);
            }
        } catch (RemoteException e) {
            throw new Timeout(e);
        }
        status = fileStatus.getStatus().getStatusCode();
        if (status.equals(TStatusCode.SRM_FILE_PINNED)) {
            return new String[]{fileStatus.getTransferURL().toString()};
        } else {
            throwException_for_listLocations(fileStatus.getStatus());
            throw new NoSuccess("unexpected exception: should never occur");
        }
    }
    public void listLocationsDone(String logicalEntry) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = toSrmURI(logicalEntry);
        // get request token
        String token = (String) m_tokenForGetRequest.remove(logicalEntry);
        if (token == null) {
            throw new NoSuccess("No request token found for entry: "+logicalEntry);
        }
        // notify server
        SrmReleaseFilesRequest request = new SrmReleaseFilesRequest();
        request.setRequestToken(token);
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
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
            throwException_for_listLocations(fileStatus.getStatus());
        }
    }
    private void throwException_for_listLocations(TReturnStatus s) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        TStatusCode status = s.getStatusCode();
        String explanation = s.getExplanation();
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

    /**
     * @param logicalEntry the SURL
     * @param replicaEntry is ignored
     */
    public void addLocation(String logicalEntry, URI replicaEntry) throws PermissionDenied, IncorrectState, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = toSrmURI(logicalEntry);
        SrmPrepareToPutRequest request = new SrmPrepareToPutRequest();
        request.setArrayOfFileRequests(new ArrayOfTPutFileRequest(new TPutFileRequest[]{new TPutFileRequest(uri, null)}));
        SrmPrepareToPutResponse response;
        try {
            response = m_stub.srmPrepareToPut(request);
        } catch (RemoteException e) {
            throw new Timeout(e);
        }
        // save request token
        String token = response.getRequestToken();
        m_tokenForPutRequest.put(logicalEntry, token);
        // wait for physical space to be ready
        TStatusCode status = response.getReturnStatus().getStatusCode();
        TPutRequestFileStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
        try {
            long period = 15000;    // 15 seconds
            SrmStatusOfPutRequestRequest request2 = new SrmStatusOfPutRequestRequest();
            request2.setRequestToken(token);
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
        } catch (RemoteException e) {
            throw new Timeout(e);
        }
        status = fileStatus.getStatus().getStatusCode();
        if (status.equals(TStatusCode.SRM_SPACE_AVAILABLE)) {
            // OK
        } else {
            throwException_for_addLocation(fileStatus.getStatus());
            throw new NoSuccess("unexpected exception: should never occur");
        }
    }
    public void addLocationDone(String logicalEntry, URI replicaEntry) throws PermissionDenied, IncorrectState, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = toSrmURI(logicalEntry);
        // get request token
        String token = (String) m_tokenForPutRequest.remove(logicalEntry);
        if (token == null) {
            throw new NoSuccess("No request token found for entry: "+logicalEntry);
        }
        // notify server
        SrmPutDoneRequest request = new SrmPutDoneRequest();
        request.setRequestToken(token);
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
        SrmPutDoneResponse response;
        try {
            response = m_stub.srmPutDone(request);
        } catch (RemoteException e) {
            throw new Timeout(e);
        }
        TSURLReturnStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
        TStatusCode status = fileStatus.getStatus().getStatusCode();
        if (! status.equals(TStatusCode.SRM_SUCCESS)) {
            throwException_for_addLocation(fileStatus.getStatus());
        }
    }
    private void throwException_for_addLocation(TReturnStatus s) throws PermissionDenied, IncorrectState, Timeout, NoSuccess {
        TStatusCode status = s.getStatusCode();
        String explanation = s.getExplanation();
        if (status.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
            throw new PermissionDenied(explanation);
        } else if (status.equals(TStatusCode.SRM_INVALID_PATH) || status.equals(TStatusCode.SRM_DUPLICATION_ERROR)) {
            throw new IncorrectState(explanation);
        } else if (status.equals(TStatusCode.SRM_SPACE_LIFETIME_EXPIRED)) {
            throw new Timeout(explanation);
        } else {
            throw new NoSuccess(status.getValue()+": "+explanation);
        }
    }

    public void removeLocation(String logicalEntry, URI replicaEntry) throws PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        org.apache.axis.types.URI uri = toSrmURI(logicalEntry);
        SrmPurgeFromSpaceRequest request = new SrmPurgeFromSpaceRequest();
        request.setArrayOfSURLs(new ArrayOfAnyURI(new org.apache.axis.types.URI[]{uri}));
        SrmPurgeFromSpaceResponse response;
        try {
            response = m_stub.srmPurgeFromSpace(request);
        } catch (RemoteException e) {
            throw new NoSuccess(e);
        }
        if (! response.getReturnStatus().getStatusCode().equals(TStatusCode.SRM_SUCCESS)) {
            TSURLReturnStatus fileStatus = response.getArrayOfFileStatuses().getStatusArray(0);
            TStatusCode status = fileStatus.getStatus().getStatusCode();
            String explanation = fileStatus.getStatus().getExplanation();
            if (status.equals(TStatusCode.SRM_AUTHORIZATION_FAILURE)) {
                throw new PermissionDenied(explanation);
            } else if (status.equals(TStatusCode.SRM_INVALID_PATH)) {
                throw new IncorrectState(explanation);
            } else if (status.equals(TStatusCode.SRM_FILE_LOST)) {
                throw new DoesNotExist(explanation);
            } else if (status.equals(TStatusCode.SRM_LAST_COPY)) {
                // ignore
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
