package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.LogicalReaderMetaDataExtended;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReaderMetaData;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriterMetaData;
import fr.in2p3.jsaga.impl.namespace.EntryImplTestAbstract;
import org.jmock.Expectations;
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

    @Test
    public void directory() throws Exception {
        LogicalDirectoryImpl dir = new LogicalDirectoryImpl(m_session, createURL(), createAdaptor_directory(), 0);
        super.directory(dir);
    }

    @Test
    public void file() throws Exception {
        LogicalFileImpl file = new LogicalFileImpl(m_session, createURL(), createAdaptor_entry_read(LogicalReaderMetaDataExtended.class), 0);
        super.entry(file);
    }

    @Test
    public void link() throws Exception {
        LogicalFileImpl file = new LogicalFileImpl(m_session, createURL(), createAdaptor_link(LogicalReaderMetaDataExtended.class), 0);
        assertTrue(file.isLink());
        file.close();
    }

    @Test
    public void listLocations() throws Exception {
        final LogicalReaderMetaData adaptor = createAdaptor_entry_read(LogicalReaderMetaDataExtended.class);
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).listLocations(with(any(String.class)), with(aNull(String.class))); will(returnValue(new String[]{"location1","location2"}));
        }});
        LogicalFileImpl file = new LogicalFileImpl(m_session, createURL(), adaptor, 0);
        List<URL> list = file.listLocations();
        assertEquals(2, list.size());
        assertEquals("location1", list.get(0).getString());
    }

    @Test
    public void listMetaData() throws Exception {
        final LogicalReaderMetaData adaptor = createAdaptor_entry_read(LogicalReaderMetaDataExtended.class);
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).listMetaData(with(any(String.class)), with(aNull(String.class))); will(returnValue(new HashMap<String, String[]>(){{put("foo",new String[]{"bar1","bar2"});}}));
        }});
        LogicalFileImpl file = new LogicalFileImpl(m_session, createURL(), adaptor, 0);
        String[] metadata = file.getVectorAttribute("foo");
        assertEquals(2, metadata.length);;
        assertEquals("bar1", metadata[0]);
    }

    @Test(expected = AlreadyExistsException.class)
    public void create_AlreadyExists_FAILURE() throws Exception {
        final LogicalWriterMetaData adaptor = createAdaptor_entry_write(LogicalWriterMetaData.class);
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).create(with(any(String.class)), with(aNull(String.class))); will(throwException(new AlreadyExistsException()));
        }});
        new LogicalFileImpl(m_session, createURL(), adaptor, Flags.CREATE.or(Flags.EXCL));
    }
    @Test
    public void create_AlreadyExists_SUCCESS() throws Exception {
        final LogicalWriterMetaData adaptor = createAdaptor_entry_write(LogicalWriterMetaData.class);
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).create(with(any(String.class)), with(aNull(String.class))); will(throwException(new AlreadyExistsException()));
        }});
        new LogicalFileImpl(m_session, createURL(), adaptor, Flags.CREATE.getValue());
    }

    @Test(expected = DoesNotExistException.class)
    public void create_DoesNotExist_FAILURE() throws Exception {
        final LogicalWriterMetaData adaptor = createAdaptor_entry_write(LogicalWriterMetaData.class);
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).create(with(any(String.class)), with(aNull(String.class))); will(throwException(new ParentDoesNotExist()));
        }});
        new LogicalFileImpl(m_session, createURL(), adaptor, Flags.CREATE.getValue());
    }
    @Test
    public void create_DoesNotExist_SUCCESS() throws Exception {
        final LogicalWriterMetaData adaptor = createAdaptor_entry_write(LogicalWriterMetaData.class);
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).create(with(any(String.class)), with(aNull(String.class))); will(
                    onConsecutiveCalls(throwException(new ParentDoesNotExist()), returnValue(null)));
            allowing(adaptor).makeDir(with(any(String.class)), with(any(String.class)), with(aNull(String.class)));
        }});
        new LogicalFileImpl(m_session, createURL(), adaptor, Flags.CREATE.or(Flags.CREATEPARENTS));
    }
}
