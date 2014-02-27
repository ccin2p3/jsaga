package integration;

import org.junit.Test;
import org.ogf.saga.namespace.EntryTest;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.LinkTest;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.permissions.PermTest;
import org.ogf.saga.url.URL;

import fr.in2p3.jsaga.adaptor.data.LinuxDataAdaptor;
import fr.in2p3.jsaga.impl.url.AbstractURLImpl;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LinuxIntegrationTestSuite
* Author:
* Date:
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LinuxIntegrationTestSuite {

    /** test cases */
    public static class LinuxNSEntryTest extends EntryTest {
        public LinuxNSEntryTest() throws Exception {super("linux");}
    }
    
    public static class LinuxLinkTest extends LinkTest {
        public LinuxLinkTest() throws Exception {super("linux");}
        
        @Test
        public void test_link_dir() throws Exception {
            URL m_linkDirUrl = createURL(m_dirUrl, "subDir.lnk");
            m_dir.link(m_linkDirUrl, Flags.NONE.getValue());
            NSEntry m_linkDir = NSFactory.createNSEntry(m_session, m_linkDirUrl, Flags.NONE.getValue());
            assertEquals(
                    m_dirUrl.toString(),
                    m_linkDir.readLink().toString());
        }
    }
    
    public static class LinuxPermissionsTest extends PermTest {
        public LinuxPermissionsTest() throws Exception {super("linux");    }

        @Test
        public void test_getOwner() throws Exception {
            assertEquals(System.getProperty("user.name"), m_file.getOwner());
        }

        @Test
        public void test_getGroups() throws Exception {
            LinuxDataAdaptor lda = new LinuxDataAdaptor();
            lda.connect(null, null, 0, null, null);
            assertTrue(lda.getGroupsOf(System.getProperty("user.name")).length > 0);
            lda.disconnect();
        }

        @Test
        public void test_chgrp() throws Exception {
            LinuxDataAdaptor lda = new LinuxDataAdaptor();
            lda.connect(null, null, 0, null, null);
            String[] groups = lda.getGroupsOf(System.getProperty("user.name"));
            org.junit.Assume.assumeTrue("Could not test because user is in one group only", groups.length>1);
            //String initial_group = m_file.getGroup();
            String new_group = groups[1];
            URL thisUrl = m_file.getURL();
            lda.setGroup(thisUrl.getPath(), new_group);
            ((AbstractURLImpl)thisUrl).setCache(null);
            assertEquals(new_group, m_file.getGroup());
            // back to initial group owner
            //lda.setGroup(m_file.getURL().getPath(), initial_group);
            lda.disconnect();        
        }
    }
}
