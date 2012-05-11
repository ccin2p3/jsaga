package fr.in2p3.jsaga.adaptor.data;

import org.apache.axis.client.Stub;
import org.globus.axis.gsi.GSIConstants;
import org.ogf.saga.error.*;
import org.ogf.srm11.bean.FileMetaData;
import org.ogf.srm11.service.ISRM;
import org.ogf.srm11.service.SRMServerV1Locator;

import javax.xml.rpc.ServiceException;
import java.lang.Exception;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   SRM11DataAdaptor
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class SRM11DataAdaptor extends SRMDataAdaptorAbstract implements DataAdaptor {
    private static final String SERVICE_PROTOCOL = "httpg";
    private static final String SERVICE_PATH = "/srm/managerv1";
    private ISRM m_stub;

    public String getType() {
        return "srm-v1.1";
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        super.connect(userInfo, host, port, basePath, attributes);
        try {
            URL serviceUrl = new URL(SERVICE_PROTOCOL, host, port, SERVICE_PATH, new org.globus.net.protocol.httpg.Handler()); //workaround for using HTTPG in tomcat or OGSi containers
            SRMServerV1Locator service = new SRMServerV1Locator(s_provider);
            m_stub = service.getISRM(serviceUrl);
            // set security
            Stub stub = (Stub) m_stub;
            stub._setProperty(GSIConstants.GSI_CREDENTIALS, m_credential);
            stub.setTimeout(120 * 1000); //2 mins
//            stub._setProperty(GSIConstants.GSI_MODE, GSIConstants.GSI_MODE_FULL_DELEG);
        } catch (ServiceException e) {
            throw new NoSuccessException(e);
        } catch (MalformedURLException e) {
            throw new NoSuccessException(e);
        }
    }

    public void disconnect() throws NoSuccessException {
        // do nothing
    }

    protected void ping() throws NoSuccessException {
        try {
            if (m_stub.ping()) {
                throw new NoSuccessException("SRM service is not available");
            }
        } catch (RemoteException e) {
            throw new NoSuccessException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        SRM11DataAdaptor adaptor = new SRM11DataAdaptor();
        adaptor.connect(null, null, 0, null, null);
        String uri = "srm://ccsrm.in2p3.fr:8443//pnfs/in2p3.fr/data/dteam/sylvain";
        FileMetaData[] array = adaptor.m_stub.getFileMetaData(new String[]{uri});
        System.out.println("Web Service Response size : " + array[0].getSize());
    }
}
