package fr.in2p3.jsaga.adaptor.security;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.impl.context.ContextImpl;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   VOMSContextUsage
 * Author: lionel.schwarz@in2p3.fr
 * Date:   6 dec 2013
 * ***************************************************
 * Description:                                      */

public class VOMSContextUsage {

    private static Usage m_adaptor_usage;
    private static File m_tmpFile;
    private Context m_context;
    
    @BeforeClass
    public static void createUsage() throws Exception {
        m_adaptor_usage = new VOMSSecurityAdaptor().getUsage();
        m_tmpFile = File.createTempFile("jsaga-VOMS-test", ".tmp");
    }
    
    @AfterClass
    public static void clean() throws Exception {
        m_tmpFile.delete();
    }
    
    @Before
    public void createContext() throws Exception {
        m_context = ContextFactory.createContext("VOMS");
        initAttributes();
    }
    
    @After
    public void dropContext() throws Exception {
        m_context = null;
    }
    
    @Test
    public void userProxyObject() throws Exception {
        Assert.assertEquals(VOMSSecurityAdaptor.USAGE_MEMORY, getMatchingUsage());
    }
    
    @Test
    public void userProxyString() throws Exception {
        m_context.removeAttribute(VOMSContext.USERPROXYOBJECT);
        m_context.setAttribute(VOMSContext.USERPROXYSTRING, "-----This is a proxy value-----");
        Assert.assertEquals(VOMSSecurityAdaptor.USAGE_LOAD, getMatchingUsage());
    }
    
    @Test
    public void userProxyExists() throws Exception {
        m_context.removeAttribute(VOMSContext.USERPROXYOBJECT);
        Assert.assertEquals(VOMSSecurityAdaptor.USAGE_LOAD, getMatchingUsage());
    }
    
    @Test
    public void userProxyDoesNotExist() throws Exception {
        m_context.removeAttribute(VOMSContext.USERPROXYOBJECT);
        m_context.setAttribute(Context.USERPROXY, "/tmp/doesNotExist");
        Assert.assertEquals(VOMSSecurityAdaptor.USAGE_INIT_PROXY, getMatchingUsage());
    }
    
    @Test
    public void initialProxy() throws Exception {
        m_context.removeAttribute(VOMSContext.USERPROXYOBJECT);
        m_context.removeAttribute(Context.USERPROXY);
        Assert.assertEquals(VOMSSecurityAdaptor.USAGE_INIT_PROXY, getMatchingUsage());
    }
    
    @Test
    public void userCertPKCS12() throws Exception {
        prepareContextForProxyInit();
        Assert.assertEquals(VOMSSecurityAdaptor.USAGE_INIT_PKCS12, getMatchingUsage());
    }
    
    @Test
    public void userCertPEM() throws Exception {
        prepareContextForProxyInit();
        // USerCertKey must not be set
        m_context.removeAttribute(VOMSContext.USERCERTKEY);
        Assert.assertEquals(VOMSSecurityAdaptor.USAGE_INIT_PEM, getMatchingUsage());
    }
    
    @Test(expected=BadParameterException.class)
    public void invalidDelegation() throws Exception {
        prepareContextForProxyInit();
        m_context.setAttribute(VOMSContext.DELEGATION, "invalid");
        getMatchingUsage();
    }
    
    @Test(expected=BadParameterException.class)
    public void invalidProxyType() throws Exception {
        prepareContextForProxyInit();
        m_context.setAttribute(VOMSContext.PROXYTYPE, "invalid");
        getMatchingUsage();
    }
    
    @Test(expected=BadParameterException.class)
    public void invalidProxyString() throws Exception {
        m_context.removeAttribute(VOMSContext.USERPROXYOBJECT);
        m_context.setAttribute(VOMSContext.USERPROXYSTRING, "invalid");
        getMatchingUsage();
    }
    
    @Test(expected=DoesNotExistException.class)
    public void missingVO() throws Exception {
        prepareContextForProxyInit();
        m_context.removeAttribute(Context.USERVO);
        getMatchingUsage();
    }
    
    @Test(expected=DoesNotExistException.class)
    public void missingPassword() throws Exception {
        prepareContextForProxyInit();
        m_context.removeAttribute(Context.USERPASS);
        getMatchingUsage();
    }
    
    @Test(expected=DoesNotExistException.class)
    public void missingCertRepository() throws Exception {
        m_context.removeAttribute(Context.CERTREPOSITORY);
        getMatchingUsage();
    }
    
    @Test(expected=DoesNotExistException.class)
    public void missingVomsDir() throws Exception {
        m_context.removeAttribute(VOMSContext.VOMSDIR);
        getMatchingUsage();
    }
    
    ///////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////
    
    private void initAttributes() throws Exception {
        String exists = m_tmpFile.getAbsolutePath();
        m_context.setAttribute(VOMSContext.USERPROXYOBJECT, "");
        m_context.setAttribute(Context.USERPROXY, exists);
        m_context.setAttribute(VOMSContext.INITIALPROXY, exists);
        m_context.setAttribute(Context.USERCERT, exists);
        m_context.setAttribute(Context.USERKEY, exists);
        m_context.setAttribute(VOMSContext.USERCERTKEY, exists);
        m_context.setAttribute(Context.USERPASS, "changeIt");
        m_context.setAttribute(Context.USERVO, "myVo");
        m_context.setAttribute(Context.CERTREPOSITORY, exists);
        m_context.setAttribute(VOMSContext.VOMSDIR, exists);
    }
    
    private void prepareContextForProxyInit() throws Exception {
        // Prepare for PKCS12 and check all attributes for building proxy
        m_context.removeAttribute(VOMSContext.USERPROXYOBJECT);
        m_context.removeAttribute(VOMSContext.INITIALPROXY);
        // UserProxy must not exist
        m_context.setAttribute(Context.USERPROXY, "/tmp/doesNotExist");
    }

    private int getMatchingUsage() throws Exception {
        Map<String, String> attributes = ((ContextImpl)m_context)._getAttributesMap();
        return m_adaptor_usage.getFirstMatchingUsage(attributes);
    }
    
}
