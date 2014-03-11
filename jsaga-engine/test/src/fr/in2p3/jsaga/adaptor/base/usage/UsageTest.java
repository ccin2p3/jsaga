package fr.in2p3.jsaga.adaptor.base.usage;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.impl.context.ContextImpl;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   UsageTest
 * Author: lionel.schwarz@in2p3.fr
 * Date:   11 mars 2014
 * ***************************************************
 * Description:                                      */

public class UsageTest {

    protected Usage m_adaptor_usage;
    protected static File m_tmpFile;
    protected Context m_context;
    
    @BeforeClass
    public static void createUsage() throws Exception {
        m_tmpFile = File.createTempFile("jsaga-usagetest", ".tmp");
    }
    
    @AfterClass
    public static void clean() throws Exception {
        m_tmpFile.delete();
    }
    
    @Before
    public void createContext() throws Exception {
        m_context = ContextFactory.createContext("");
    }
    
    @After
    public void dropContext() throws Exception {
        m_context = null;
    }
    
    @Test
    public void UAndToString() throws Exception {
        UAnd u = new UAnd.Builder()
                       .and(new U("A"))
                       .and(new U("B"))
                       .build();
        Assert.assertEquals("(A  B)", u.toString());
    }
    
    @Test
    public void UOrToString() throws Exception {
        UOr u = new UOr.Builder()
                       .or(new U("A"))
                       .or(new U("B"))
                       .build();
        Assert.assertEquals("(A | B)", u.toString());
    }
    
    @Test
    public void UAndGetFirstMatchingUsage() throws Exception {
        UAnd u = new UAnd.Builder()
                        .and(new U(1, "A"))
                        .and(new U(2, "B"))
                        .build();
        // File A does not exist -> usage = -1
        m_context.setAttribute("A", "a");
        m_context.setAttribute("B", "b");
        Assert.assertEquals(1, u.getFirstMatchingUsage(((ContextImpl)m_context)._getAttributesMap()));
    }
    
    @Test
    public void UAndGetFirstMatchingUsageFile() throws Exception {
        UAnd u = new UAnd.Builder()
                        .id(1)
                        .and(new UFile("A"))
                        .and(new UFile("B"))
                        .build();
        // File A does not exist -> usage = -1
        m_context.setAttribute("A", "/tmp/DoesNotExists");
        m_context.setAttribute("B", m_tmpFile.getAbsolutePath());
        Assert.assertEquals(-1, u.getFirstMatchingUsage(((ContextImpl)m_context)._getAttributesMap()));

        // File B does not exist -> usage = -1
        m_context.setAttribute("A", m_tmpFile.getAbsolutePath());
        m_context.setAttribute("B", "/tmp/DoesNotExists");
        Assert.assertEquals(-1, u.getFirstMatchingUsage(((ContextImpl)m_context)._getAttributesMap()));

        // Both attributes -> Usage = 1
        m_context.setAttribute("A", m_tmpFile.getAbsolutePath());
        m_context.setAttribute("B", m_tmpFile.getAbsolutePath());
        Assert.assertEquals(1, u.getFirstMatchingUsage(((ContextImpl)m_context)._getAttributesMap()));
    }
    
    @Test
    public void UAndGetFirstMatchingUsageOptional() throws Exception {
        UAnd u = new UAnd.Builder()
                        .id(1)
                        .and(new UFile("F"))
                        .and(new UOptional("A"))
                        .build();
        // No attribute A -> usage = 1
        m_context.setAttribute("F", m_tmpFile.getAbsolutePath());
        Assert.assertEquals(1, u.getFirstMatchingUsage(((ContextImpl)m_context)._getAttributesMap()));
        
        // File does not exist -> usage = -1
        m_context.setAttribute("F", "/tmp/DoesNotExists");
        m_context.setAttribute("A", "a");
        Assert.assertEquals(-1, u.getFirstMatchingUsage(((ContextImpl)m_context)._getAttributesMap()));

        // Both attributes -> Usage=1
        m_context.setAttribute("F", m_tmpFile.getAbsolutePath());
        m_context.setAttribute("A", "a");
        Assert.assertEquals(1, u.getFirstMatchingUsage(((ContextImpl)m_context)._getAttributesMap()));
    }
    
    @Test
    public void UOrGetFirstMatchingUsage() throws Exception {
        UOr u = new UOr.Builder()
                        .or(new UFile(1, "F"))
                        .or(new U(2, "A"))
                        .build();

        // File exist, no A -> usage = 1
        m_context.setAttribute("F", m_tmpFile.getAbsolutePath());
        Assert.assertEquals(1, u.getFirstMatchingUsage(((ContextImpl)m_context)._getAttributesMap()));

        // File does not exist -> usage = 2
        m_context.setAttribute("F", "/tmp/DoesNotExists");
        m_context.setAttribute("A", "a");
        Assert.assertEquals(2, u.getFirstMatchingUsage(((ContextImpl)m_context)._getAttributesMap()));

        // Both attributes -> Usage=1
        m_context.setAttribute("F", m_tmpFile.getAbsolutePath());
        m_context.setAttribute("A", "a");
        Assert.assertEquals(1, u.getFirstMatchingUsage(((ContextImpl)m_context)._getAttributesMap()));
    }
}
