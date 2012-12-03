package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.Base;
import org.ogf.saga.error.NoSuccessException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   WMStoLB
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   2 juil. 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class WMStoLB {
    private static final File FILE = new File(Base.JSAGA_VAR, "WMStoLB.properties");

    private static WMStoLB _instance;

    private Properties m_prop;

    public synchronized static WMStoLB getInstance() throws NoSuccessException {
        if (_instance == null) {
            _instance = new WMStoLB();
        }
        return _instance;
    }
    private WMStoLB() throws NoSuccessException {
        m_prop = new Properties();
        if (FILE.exists()) {
            try {
                m_prop.load(new FileInputStream(FILE));
            } catch (IOException e) {
                throw new NoSuccessException("Failed to load properties: "+ FILE);
            }
        } else {
            try {
                m_prop.store(new FileOutputStream(FILE), "WMS to LB");
            } catch (IOException e) {
                throw new NoSuccessException("Failed to create properties: "+ FILE);
            }
        }
    }

    public synchronized void setLBHost(String wmsServerUrl, String jobId) throws NoSuccessException {
        // parse jobId
        String newLbHost;
        try {
            URL jobIdUrl = new URL(jobId);
            newLbHost = jobIdUrl.getHost();
        } catch (MalformedURLException e) {
            throw new NoSuccessException("Bad jobId: "+jobId);
        }

        // may store newLbHost
        String oldLbHost = m_prop.getProperty(wmsServerUrl);
        if (oldLbHost==null || !newLbHost.equals(oldLbHost)) {
            m_prop.setProperty(wmsServerUrl, newLbHost);
            try {
                m_prop.store(new FileOutputStream(FILE), "WMS to LB");
            } catch (IOException e) {
                throw new NoSuccessException("Failed to store properties: "+ FILE);
            }
        }
    }

    public synchronized String getLBHost(String wmsServerUrl) {
        String lbHost = m_prop.getProperty(wmsServerUrl);
        if (lbHost != null) {
            return lbHost;
        } else {
        	//TODO: check the BDII ?
            return null;
        }
    }
}
