package fr.in2p3.jsaga.adaptor.data;

import org.ogf.saga.error.*;
import org.ogf.srm11.bean.FileMetaData;
import org.ogf.srm11.service.ISRM;
import org.ogf.srm11.service.SRMServerV1Locator;

import javax.xml.rpc.ServiceException;
import java.lang.Exception;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.rmi.RemoteException;

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
    private ISRM m_stub;

    public String[] getSchemeAliases() {
        return new String[]{"srm11"};
    }

    public void connect(String userInfo, String host, int port, Map attributes) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        //todo: support security context at non-default location!
        String wsdl = "httpg://ccsrm.in2p3.fr:8443/srm/managerv1";
        SRMServerV1Locator service = new SRMServerV1Locator(s_provider);
        try {
            m_stub = service.getISRM(new URL(wsdl));
        } catch (ServiceException e) {
            throw new NoSuccess(e);
        } catch (MalformedURLException e) {
            throw new NoSuccess(e);
        }
    }

    protected void ping() throws NoSuccess {
        try {
            if (m_stub.ping()) {
                throw new NoSuccess("SRM service is not available");
            }
        } catch (RemoteException e) {
            throw new NoSuccess(e);
        }
    }

    public static void main(String[] args) throws Exception {
        SRM11DataAdaptor adaptor = new SRM11DataAdaptor();
        adaptor.connect(null, null, 0, null);
        String uri = "srm://ccsrm.in2p3.fr:8443//pnfs/in2p3.fr/data/dteam/sylvain";
        FileMetaData[] array = adaptor.m_stub.getFileMetaData(new String[]{uri});
        System.out.println("Web Service Response size : " + array[0].getSize());
    }
}
