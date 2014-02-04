package integration;

import org.junit.Test;
import org.ogf.saga.AbstractTest_JUNIT4;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

public class DiracOSBTest extends AbstractTest_JUNIT4 {

    protected Session m_session;
    protected NSEntry m_file;
    
	public DiracOSBTest() throws Exception {
		super();
        // configure
        URL jobOSBUrl = URLFactory.createURL(getRequiredProperty("dirac-osb", "url"));
        m_session = SessionFactory.createSession(true);
        m_file = NSFactory.createNSEntry(m_session, jobOSBUrl);
        if (!(m_file instanceof File)) {
        	throw new Exception("Not an instance of class: File");
        }
	}

    @Test
	public void test_OSB_OK() throws Exception {
        Buffer buffer = BufferFactory.createBuffer(1024);
        File reader = (File) NSFactory.createNSEntry(m_session, m_file.getURL(), Flags.READ.getValue());
        int len = reader.read(buffer);
        System.out.println("Content=\"" + new String(buffer.getData()).trim() + "\"");
        reader.close();
	}

    @Test
	public void test_OSB_empty() throws Exception {
        Buffer buffer = BufferFactory.createBuffer(1024);
        // modify filename: add "dummy" at the end
        URL ghostOSBUrl = URLFactory.createURL(getRequiredProperty("dirac-osb", "url").replace("?", "dummy?"));
        NSEntry ghost = NSFactory.createNSEntry(m_session, ghostOSBUrl);
        File reader = (File) NSFactory.createNSEntry(m_session, ghost.getURL(), Flags.READ.getValue());
        int len = reader.read(buffer);
        assertEquals("", new String(buffer.getData()).trim());
        reader.close();
	}
}
