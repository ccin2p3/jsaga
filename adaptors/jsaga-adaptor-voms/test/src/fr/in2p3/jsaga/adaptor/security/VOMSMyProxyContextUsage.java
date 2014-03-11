package fr.in2p3.jsaga.adaptor.security;

import java.io.File;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   VOMSMyProxyContextUsage
 * Author: lionel.schwarz@in2p3.fr
 * Date:   6 dec 2013
 * ***************************************************
 * Description:                                      */

public class VOMSMyProxyContextUsage extends VOMSContextUsage {

    @BeforeClass
    public static void createUsage() throws Exception {
        SecurityAdaptor a = new VOMSMyProxySecurityAdaptor();
        m_adaptor_usage = a.getUsage();
        m_type = a.getType();
        m_tmpFile = File.createTempFile("jsaga-test-" + m_type, ".tmp");
    }
    
    @Test @Override
    public void userProxyExists() throws Exception {
        m_context.removeAttribute(GlobusContext.USERPROXYOBJECT);
        Assert.assertEquals(GlobusSecurityAdaptor.USAGE_LOAD, getMatchingUsage());
    }
    
    @Test @Override
    public void initialProxy() throws Exception {
        prepareContextForProxyInit();
        m_context.setAttribute(GlobusContext.DELEGATIONLIFETIME, "PT12H");
        Assert.assertEquals(VOMSSecurityAdaptor.USAGE_INIT_PROXY, getMatchingUsage());
    }
    
    @Test
    public void getDelegatedFromMemory() throws Exception {
        m_context.setAttribute(GlobusContext.DELEGATIONLIFETIME, "PT12H");
        Assert.assertEquals(VOMSMyProxySecurityAdaptor.USAGE_GET_DELEGATED_MEMORY, getMatchingUsage());
    }
    
    @Test
    public void getDelegatedFromFile() throws Exception {
        m_context.setAttribute(GlobusContext.DELEGATIONLIFETIME, "PT12H");
        m_context.removeAttribute(GlobusContext.USERPROXYOBJECT);
        Assert.assertEquals(VOMSMyProxySecurityAdaptor.USAGE_GET_DELEGATED_LOAD, getMatchingUsage());
    }
    
    @Test(expected=BadParameterException.class)
    public void invalidDelegationLifetime() throws Exception {
//        prepareContextForProxyInit();
        m_context.setAttribute(GlobusContext.DELEGATIONLIFETIME, "invalid");
        getMatchingUsage();
    }
    
    @Test(expected=DoesNotExistException.class)
    public void missingMyProxyServer() throws Exception {
        missing(VOMSContext.MYPROXYSERVER);
    }
    

    ///////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////
    
    protected void initAttributes() throws Exception {
        super.initAttributes();
        m_context.setAttribute(VOMSContext.MYPROXYSERVER, "MyProxyServer");
    }
    
    protected void prepareContextForProxyInit() throws Exception {
        super.prepareContextForProxyInit();
    }

}
