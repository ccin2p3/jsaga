package fr.in2p3.jsaga.impl.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

public class EntryImplTest {

    private static Mockery m_mockery;
    private static Session m_session;
    private static URL m_url;
    private static FileAttributes m_attrs;
    private static FileAttributes m_attrs1;
    private static FileAttributes m_attrs2;
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @BeforeClass
    public static void init() throws Exception {
        m_mockery = new Mockery() {{
            // multithread support for run()
            setThreadingPolicy(new Synchroniser());
        }};
        m_session = SessionFactory.createSession(false);
        m_url = URLFactory.createURL("adaptor://host/path/to/entry");
        m_attrs = new FileAttributes() {
            @Override public String getName() { return "dir"; }
            @Override public int getType() { return FileAttributes.TYPE_DIRECTORY; }
            @Override public long getSize() { return 1000; }
            @Override public PermissionBytes getUserPermission() { return null; }
            @Override public PermissionBytes getGroupPermission() { return null; }
            @Override public PermissionBytes getAnyPermission() { return null; }
            @Override public String getOwner() { return "owner"; }
            @Override public String getGroup() { return "group"; }
            @Override public long getLastModified() { return 2000; }
        };
        m_attrs1 = new FileAttributes() {
            @Override public String getName() { return "file1"; }
            @Override public int getType() { return FileAttributes.TYPE_FILE; }
            @Override public long getSize() { return 1000; }
            @Override public PermissionBytes getUserPermission() { return null; }
            @Override public PermissionBytes getGroupPermission() { return null; }
            @Override public PermissionBytes getAnyPermission() { return null; }
            @Override public String getOwner() { return "owner"; }
            @Override public String getGroup() { return "group"; }
            @Override public long getLastModified() { return 2000; }
        };
        m_attrs2 = new FileAttributes() {
            @Override public String getName() { return "file2"; }
            @Override public int getType() { return FileAttributes.TYPE_FILE; }
            @Override public long getSize() { return 1000; }
            @Override public PermissionBytes getUserPermission() { return null; }
            @Override public PermissionBytes getGroupPermission() { return null; }
            @Override public PermissionBytes getAnyPermission() { return null; }
            @Override public String getOwner() { return "owner"; }
            @Override public String getGroup() { return "group"; }
            @Override public long getLastModified() { return 2000; }
        };
    }

    @Test
    public void directory() throws Exception {
        final DataReaderAdaptor adaptor = m_mockery.mock(DataReaderAdaptor.class, "adaptor-directory");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).exists(with(any(String.class)), with(aNull(String.class))); will(returnValue(true));
            allowing(adaptor).listAttributes(with(any(String.class)), with(aNull(String.class))); will(returnValue(new FileAttributes[]{m_attrs1, m_attrs2}));
            allowing(adaptor).getAttributes(with(any(String.class)), with(aNull(String.class))); will(returnValue(m_attrs));
            allowing(adaptor).disconnect(); will(returnValue(null));
        }});
        DirectoryImpl dir = new DirectoryImpl(m_session, m_url, adaptor, 0);
        assertEquals(1000+1000, dir.getSize());
        assertEquals(2000, dir.getMTime());
        assertEquals("group", dir.getGroup());
        assertEquals("owner", dir.getOwner());
        assertEquals("entry", dir.getName().getString());
        assertEquals("adaptor://host/path/to/entry/", dir.getURL().getString());
        assertEquals("adaptor://host/path/to/entry/", dir.getCWD().getString());
        assertEquals("entry", dir.getName().getString());
        assertEquals(2, dir.getNumEntries());
        assertEquals("file1", dir.getEntry(0).getString());
        assertTrue(dir.find("pattern").isEmpty());
        assertEquals(2, dir.find("file*").size());
        assertEquals(1, dir.find("*2").size());
        dir.close();
    }
    
    @Test
    public void file() throws Exception {
        final DataReaderAdaptor adaptor = m_mockery.mock(DataReaderAdaptor.class, "adaptor-file");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).exists(with(any(String.class)), with(aNull(String.class))); will(returnValue(true));
            allowing(adaptor).getAttributes(with(any(String.class)), with(aNull(String.class))); will(returnValue(m_attrs1));
            allowing(adaptor).disconnect(); will(returnValue(null));
        }});
        FileImpl file = new FileImpl(m_session, m_url, adaptor, 0);
        assertTrue(file.exists());
        assertEquals(1000, file.getSize());
        assertEquals(2000, file.getMTime());
        assertEquals("group", file.getGroup());
        assertEquals("owner", file.getOwner());
        assertEquals("entry", file.getName().getString());
        assertEquals("adaptor://host/path/to/entry", file.getURL().getString());
        assertEquals("adaptor://host/path/to/", file.getCWD().getString());
        assertEquals("entry", file.getName().getString());
        file.close();
    }
}
