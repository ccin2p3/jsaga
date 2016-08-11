package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.LogicalReaderMetaDataExtended;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriterMetaData;
import fr.in2p3.jsaga.impl.namespace.EntryImplTestAbstract;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Test;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.url.URL;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogicalFileImplTest extends EntryImplTestAbstract<LogicalReaderMetaDataExtended, LogicalWriterMetaData> {

    @Mock private LogicalReaderMetaDataExtended reader;
    @Mock private LogicalWriterMetaData writer;

    @Test
    public void directory() throws Exception {
        super.setDirectory(reader);
        LogicalDirectoryImpl dir = new LogicalDirectoryImpl(m_session, createURL(), reader, 0);
        super.directory(dir);
    }

    @Test
    public void file() throws Exception {
        super.setEntry(reader, EntryType.FILE);
        LogicalFileImpl file = new LogicalFileImpl(m_session, createURL(), reader, 0);
        super.entry(file);
    }

    @Test
    public void link() throws Exception {
        super.setEntry(reader, EntryType.LINK);
        LogicalFileImpl file = new LogicalFileImpl(m_session, createURL(), reader, 0);
        assertTrue(file.isLink());
        file.close();
    }

    @Test
    public void listLocations() throws Exception {
        super.setEntry(reader, EntryType.FILE);
        context.checking(new Expectations() {{
            allowing(reader).listLocations(with(any(String.class)), with(aNull(String.class))); will(returnValue(new String[]{"location1","location2"}));
        }});
        LogicalFileImpl file = new LogicalFileImpl(m_session, createURL(), reader, 0);
        List<URL> list = file.listLocations();
        assertEquals(2, list.size());
        assertEquals("location1", list.get(0).getString());
    }

    @Test
    public void listMetaData() throws Exception {
        super.setEntry(reader, EntryType.FILE);
        context.checking(new Expectations() {{
            allowing(reader).listMetaData(with(any(String.class)), with(aNull(String.class))); will(returnValue(new HashMap<String, String[]>(){{put("foo",new String[]{"bar1","bar2"});}}));
        }});
        LogicalFileImpl file = new LogicalFileImpl(m_session, createURL(), reader, 0);
        String[] metadata = file.getVectorAttribute("foo");
        assertEquals(2, metadata.length);;
        assertEquals("bar1", metadata[0]);
    }

    @Test(expected = AlreadyExistsException.class)
    public void create_AlreadyExists_FAILURE() throws Exception {
        super.setEntry(writer);
        context.checking(new Expectations() {{
            allowing(writer).create(with(any(String.class)), with(aNull(String.class))); will(throwException(new AlreadyExistsException()));
        }});
        new LogicalFileImpl(m_session, createURL(), writer, Flags.CREATE.or(Flags.EXCL));
    }
    @Test
    public void create_AlreadyExists_SUCCESS() throws Exception {
        super.setEntry(writer);
        context.checking(new Expectations() {{
            allowing(writer).create(with(any(String.class)), with(aNull(String.class))); will(throwException(new AlreadyExistsException()));
        }});
        new LogicalFileImpl(m_session, createURL(), writer, Flags.CREATE.getValue());
    }

    @Test(expected = DoesNotExistException.class)
    public void create_DoesNotExist_FAILURE() throws Exception {
        super.setEntry(writer);
        context.checking(new Expectations() {{
            allowing(writer).create(with(any(String.class)), with(aNull(String.class))); will(throwException(new ParentDoesNotExist()));
        }});
        new LogicalFileImpl(m_session, createURL(), writer, Flags.CREATE.getValue());
    }
    @Test
    public void create_DoesNotExist_SUCCESS() throws Exception {
        super.setEntry(writer);
        context.checking(new Expectations() {{
            allowing(writer).create(with(any(String.class)), with(aNull(String.class))); will(
                    onConsecutiveCalls(throwException(new ParentDoesNotExist()), returnValue(null)));
            allowing(writer).makeDir(with(any(String.class)), with(any(String.class)), with(aNull(String.class)));
        }});
        new LogicalFileImpl(m_session, createURL(), writer, Flags.CREATE.or(Flags.CREATEPARENTS));
    }
}
