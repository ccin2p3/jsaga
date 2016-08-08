package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.SagaException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import static org.junit.Assert.*;

public class EntryImplTestAbstract<R extends DataReaderAdaptor, W extends DataWriterAdaptor> {
    protected static final FileAttributes DIRECTORY = new FileAttributes() {
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
    protected static final FileAttributes FILE = new FileAttributes() {
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
    protected static final FileAttributes LINK = new FileAttributes() {
        @Override public String getName() { return "file2"; }
        @Override public int getType() { return FileAttributes.TYPE_LINK; }
        @Override public long getSize() { return 1000; }
        @Override public PermissionBytes getUserPermission() { return null; }
        @Override public PermissionBytes getGroupPermission() { return null; }
        @Override public PermissionBytes getAnyPermission() { return null; }
        @Override public String getOwner() { return "owner"; }
        @Override public String getGroup() { return "group"; }
        @Override public long getLastModified() { return 2000; }
    };

    @Rule
    public final TestName name = new TestName();

    protected static Mockery m_mockery;
    protected static Session m_session;

    @BeforeClass
    public static void init() throws SagaException {
        m_mockery = new Mockery();
        m_session = SessionFactory.createSession(false);
    }

    protected DataReaderAdaptor createAdaptor_directory() throws SagaException {
        final DataReaderAdaptor adaptor = m_mockery.mock(DataReaderAdaptor.class, "adaptor-"+name.getMethodName());
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).exists(with(any(String.class)), with(aNull(String.class))); will(returnValue(true));
            allowing(adaptor).listAttributes(with(any(String.class)), with(aNull(String.class))); will(returnValue(new FileAttributes[]{FILE, LINK}));
            allowing(adaptor).getAttributes(with(any(String.class)), with(aNull(String.class))); will(returnValue(DIRECTORY));
            allowing(adaptor).disconnect(); will(returnValue(null));
        }});
        return adaptor;
    }
    protected R createAdaptor_entry_read(Class<R> clazz) throws SagaException {
        final R adaptor = m_mockery.mock(clazz, "adaptor-"+name.getMethodName());
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).exists(with(any(String.class)), with(aNull(String.class))); will(returnValue(true));
            allowing(adaptor).getAttributes(with(any(String.class)), with(aNull(String.class))); will(returnValue(FILE));
            allowing(adaptor).disconnect(); will(returnValue(null));
        }});
        return adaptor;
    }
    protected W createAdaptor_entry_write(Class<W> clazz) throws SagaException {
        final W adaptor = m_mockery.mock(clazz, "adaptor-"+name.getMethodName());
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).disconnect(); will(returnValue(null));
        }});
        return adaptor;
    }
    protected R createAdaptor_link(Class<R> clazz) throws SagaException {
        final R adaptor = m_mockery.mock(clazz, "adaptor-"+name.getMethodName());
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).exists(with(any(String.class)), with(aNull(String.class))); will(returnValue(true));
            allowing(adaptor).getAttributes(with(any(String.class)), with(aNull(String.class))); will(returnValue(LINK));
            allowing(adaptor).disconnect(); will(returnValue(null));
        }});
        return adaptor;
    }

    protected static URL createURL() throws BadParameterException, NoSuccessException {
        return URLFactory.createURL("adaptor://host/path/to/entry");
    }

    protected void directory(AbstractNSDirectoryImpl dir) throws SagaException {
        assertTrue(dir.isDir());
        assertFalse(dir.isLink());
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
    protected void entry(AbstractNSEntryImpl entry) throws SagaException {
        assertTrue(entry.exists());
        assertFalse(entry.isDir());
        assertFalse(entry.isLink());
        assertEquals(2000, entry.getMTime());
        assertEquals("group", entry.getGroup());
        assertEquals("owner", entry.getOwner());
        assertEquals("entry", entry.getName().getString());
        assertEquals("adaptor://host/path/to/entry", entry.getURL().getString());
        assertEquals("adaptor://host/path/to/", entry.getCWD().getString());
        assertEquals("entry", entry.getName().getString());
        entry.close();
    }
}
