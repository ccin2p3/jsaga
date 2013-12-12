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

    protected static Usage m_adaptor_usage;
    protected static File m_tmpFile;
    protected Context m_context;
    protected static String m_type;
    
    @BeforeClass
    public static void createUsage() throws Exception {
        SecurityAdaptor a = new VOMSSecurityAdaptor();
        m_adaptor_usage = a.getUsage();
        m_type = a.getType();
        m_tmpFile = File.createTempFile("jsaga-test-" + m_type, ".tmp");
    }
    
    @AfterClass
    public static void clean() throws Exception {
        m_tmpFile.delete();
    }
    
    @Before
    public void createContext() throws Exception {
        m_context = ContextFactory.createContext(m_type);
        initAttributes();
    }
    
    @After
    public void dropContext() throws Exception {
        m_context = null;
    }
    
    @Test
    public void userProxyObject() throws Exception {
        Assert.assertEquals(GlobusSecurityAdaptor.USAGE_MEMORY, getMatchingUsage());
    }
    
    @Test
    public void userProxyString() throws Exception {
        m_context.removeAttribute(GlobusContext.USERPROXYOBJECT);
        m_context.setAttribute(VOMSContext.USERPROXYSTRING, "-----This is a proxy value-----");
        Assert.assertEquals(GlobusSecurityAdaptor.USAGE_LOAD, getMatchingUsage());
    }
    
    @Test
    public void userProxyExists() throws Exception {
        m_context.removeAttribute(GlobusContext.USERPROXYOBJECT);
        Assert.assertEquals(GlobusSecurityAdaptor.USAGE_LOAD, getMatchingUsage());
    }
    
    @Test
    public void userProxyDoesNotExist() throws Exception {
        m_context.removeAttribute(GlobusContext.USERPROXYOBJECT);
        m_context.setAttribute(Context.USERPROXY, "/tmp/doesNotExist");
        Assert.assertEquals(VOMSSecurityAdaptor.USAGE_INIT_PROXY, getMatchingUsage());
    }
    
    @Test
    public void initialProxy() throws Exception {
        prepareContextForProxyInit();
//        m_context.removeAttribute(VOMSContext.USERPROXYOBJECT);
//        m_context.removeAttribute(Context.USERPROXY);
        Assert.assertEquals(VOMSSecurityAdaptor.USAGE_INIT_PROXY, getMatchingUsage());
    }
    
    @Test
    public void userCertPKCS12() throws Exception {
        prepareContextForProxyInit();
        // InitialProxy must not be set
        m_context.removeAttribute(VOMSContext.INITIALPROXY);
        Assert.assertEquals(GlobusSecurityAdaptor.USAGE_INIT_PKCS12, getMatchingUsage());
    }
    
    @Test
    public void userCertPEM() throws Exception {
        prepareContextForProxyInit();
        // InitialProxy must not be set
        m_context.removeAttribute(VOMSContext.INITIALPROXY);
        // USerCertKey must not be set
        m_context.removeAttribute(GlobusContext.USERCERTKEY);
        Assert.assertEquals(GlobusSecurityAdaptor.USAGE_INIT_PEM, getMatchingUsage());
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
        m_context.removeAttribute(GlobusContext.USERPROXYOBJECT);
        m_context.setAttribute(VOMSContext.USERPROXYSTRING, "invalid");
        getMatchingUsage();
    }
    
    @Test(expected=DoesNotExistException.class)
    public void missingVO() throws Exception {
        missing(Context.USERVO);
    }
    
    @Test(expected=DoesNotExistException.class)
    public void missingCertRepository() throws Exception {
        missing(Context.CERTREPOSITORY);
    }
    
    @Test(expected=DoesNotExistException.class)
    public void missingVomsDir() throws Exception {
        missing(VOMSContext.VOMSDIR);
    }
    
    ///////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////
    
    protected void initAttributes() throws Exception {
        String exists = m_tmpFile.getAbsolutePath();
        m_context.setAttribute(GlobusContext.USERPROXYOBJECT, "");
        m_context.setAttribute(Context.USERPROXY, exists);
        m_context.setAttribute(VOMSContext.INITIALPROXY, exists);
        m_context.setAttribute(Context.USERCERT, exists);
        m_context.setAttribute(Context.USERKEY, exists);
        m_context.setAttribute(GlobusContext.USERCERTKEY, exists);
        m_context.setAttribute(Context.USERPASS, "changeIt");
        m_context.setAttribute(Context.USERVO, "myVo");
        m_context.setAttribute(Context.CERTREPOSITORY, exists);
        m_context.setAttribute(VOMSContext.VOMSDIR, exists);
    }
    
    protected void prepareContextForProxyInit() throws Exception {
        // Prepare for PKCS12 and check all attributes for building proxy
        m_context.removeAttribute(GlobusContext.USERPROXYOBJECT);
        // UserProxy must not exist
        m_context.setAttribute(Context.USERPROXY, "/tmp/doesNotExist");
    }

    protected void missing(String attr) throws Exception {
        prepareContextForProxyInit();
        m_context.removeAttribute(attr);
        getMatchingUsage();
    }
    
    protected int getMatchingUsage() throws Exception {
        Map<String, String> attributes = ((ContextImpl)m_context)._getAttributesMap();
        return m_adaptor_usage.getFirstMatchingUsage(attributes);
    }
    
}
