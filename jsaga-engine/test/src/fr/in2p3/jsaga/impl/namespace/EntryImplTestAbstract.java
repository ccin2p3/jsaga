package fr.in2p3.jsaga.impl.namespace;

import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
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

public abstract class EntryImplTestAbstract<R extends DataReaderAdaptor, W extends DataWriterAdaptor> {
    protected enum EntryType {
        DIRECTORY(FileAttributes.TYPE_DIRECTORY, "dir"),
        FILE(FileAttributes.TYPE_FILE, "file1"),
        LINK(FileAttributes.TYPE_LINK, "file2");
        int flag; String name;
        EntryType(int flag,String name){this.flag=flag; this.name=name;}
    }
    @Mock private FileAttributes entryAttributes;
    @Mock private FileAttributes file1Attributes;
    @Mock private FileAttributes file2Attributes;

    @Rule public final JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    @Rule public final ExpectedException exception = ExpectedException.none();

    protected static Session m_session;

    @BeforeClass
    public static void init() throws SagaException {
        m_session = SessionFactory.createSession(false);
    }

    protected void setDirectory(final R adaptor) throws SagaException {
        this.setEntry(adaptor, EntryType.DIRECTORY);
        this.setFileAttributes(file1Attributes, EntryType.FILE);
        this.setFileAttributes(file2Attributes, EntryType.LINK);
        context.checking(new Expectations() {{
            allowing(adaptor).listAttributes(with(any(String.class)), with(aNull(String.class))); will(
                    returnValue(new FileAttributes[]{file1Attributes, file2Attributes}));
        }});
    }
    protected void setEntry(final R adaptor, final EntryType type) throws SagaException {
        this.setFileAttributes(entryAttributes, type);
        context.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).exists(with(any(String.class)), with(aNull(String.class))); will(returnValue(true));
            allowing(adaptor).getAttributes(with(any(String.class)), with(aNull(String.class))); will(returnValue(entryAttributes));
            allowing(adaptor).disconnect(); will(returnValue(null));
        }});
    }
    protected void setEntry(final W adaptor) throws SagaException {
        context.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).disconnect(); will(returnValue(null));
        }});
    }
    private void setFileAttributes(final FileAttributes attributes, final EntryType type) {
        context.checking(new Expectations() {{
            allowing(attributes).getName(); will(returnValue(type.name));
            allowing(attributes).getType(); will(returnValue(type.flag));
            allowing(attributes).getSize(); will(returnValue(1000L));
            allowing(attributes).getOwner(); will(returnValue("owner"));
            allowing(attributes).getGroup(); will(returnValue("group"));
            allowing(attributes).getLastModified(); will(returnValue(2000L));
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
