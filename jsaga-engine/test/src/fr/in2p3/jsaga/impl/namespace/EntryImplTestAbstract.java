package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
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

    @Rule public final JUnitRuleMockery context = new JUnitRuleMockery();
    @Rule public final ExpectedException exception = ExpectedException.none();

    protected static Session m_session;

    @BeforeClass
    public static void init() throws SagaException {
        m_session = SessionFactory.createSession(false);
    }

    protected void setDirectory(final R adaptor) throws SagaException {
        this.setEntry(adaptor, DIRECTORY);
        context.checking(new Expectations() {{
            allowing(adaptor).listAttributes(with(any(String.class)), with(aNull(String.class))); will(returnValue(new FileAttributes[]{FILE, LINK}));
        }});
    }
    protected void setEntry(final R adaptor, final FileAttributes attributes) throws SagaException {
        context.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).exists(with(any(String.class)), with(aNull(String.class))); will(returnValue(true));
            allowing(adaptor).getAttributes(with(any(String.class)), with(aNull(String.class))); will(returnValue(attributes));
            allowing(adaptor).disconnect(); will(returnValue(null));
        }});
    }
    protected void setEntry(final W adaptor) throws SagaException {
        context.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).disconnect(); will(returnValue(null));
        }});
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
