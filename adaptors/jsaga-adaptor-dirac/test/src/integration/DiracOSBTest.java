package integration;

import org.ogf.saga.AbstractTest;
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

public class DiracOSBTest extends AbstractTest {

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

	public void test_OSB_OK() throws Exception {
        Buffer buffer = BufferFactory.createBuffer(1024);
        File reader = (File) NSFactory.createNSEntry(m_session, m_file.getURL(), Flags.READ.getValue());
        int len = reader.read(buffer);
        System.out.println(new String(buffer.getData()));
        reader.close();
	}

}
