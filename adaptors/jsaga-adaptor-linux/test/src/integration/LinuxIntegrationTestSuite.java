package integration;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ogf.saga.file.DirectoryListTest;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSEntryTest;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.namespace.NSLinkTest;
import org.ogf.saga.permissions.PermissionsTest;
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
public class LinuxIntegrationTestSuite extends TestSuite {
    /** create test suite */
    public static Test suite() throws Exception {return new LinuxIntegrationTestSuite();}
    /** index of test cases */
    public static class index extends IndexTest {public index(){super(LinuxIntegrationTestSuite.class);}}

    /** test cases */
    public static class LinuxNSEntryTest extends NSEntryTest {
    	public LinuxNSEntryTest() throws Exception {super("linux");}
    }
    
    public static class LinuxLinkTest extends NSLinkTest {
    	public LinuxLinkTest() throws Exception {super("linux");}
    	public void test_link_dir() throws Exception {
    		URL m_linkDirUrl = createURL(m_dirUrl, "subDir.lnk");
    		m_dir.link(m_linkDirUrl, Flags.NONE.getValue());
    		NSEntry m_linkDir = NSFactory.createNSEntry(m_session, m_linkDirUrl, Flags.NONE.getValue());
            assertEquals(
                    m_dirUrl.toString(),
                    m_linkDir.readLink().toString());
    	}
    }
    public static class LinuxDirectoryListTest extends DirectoryListTest {
        public LinuxDirectoryListTest() throws Exception {super("linux");}
    }
    
    public static class LinuxPermissionsTest extends PermissionsTest {
		public LinuxPermissionsTest() throws Exception {super("linux");	}
        public void test_getOwner() throws Exception {
        	assertEquals(System.getProperty("user.name"), m_file.getOwner());
        }
        // TODO: check group
        /*public void test_group() throws Exception {
        	//com.sun.security.auth.module.UnixSystem thisSystem = new com.sun.security.auth.module.UnixSystem();
        	//assertEquals(thisSystem.getGroups()[0], m_file.getGroup());
        	assertEquals(System.getProperty("user.name"), m_file.getGroup());
        }*/
        public void test_chgrp() throws Exception {
        	LinuxDataAdaptor lda = new LinuxDataAdaptor();
        	lda.connect(null, null, 0, null, null);
        	String[] groups = lda.getGroupsOf(System.getProperty("user.name"));
        	if (groups.length < 2) {
        		super.ignore("Could not test because user is in one group only");
        	}
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
