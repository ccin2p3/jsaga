package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.impl.namespace.EntryImplTestAbstract;
import org.jmock.Expectations;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.namespace.Flags;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileImplTest extends EntryImplTestAbstract<FileReader, FileWriter> {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void directory() throws Exception {
        DirectoryImpl dir = new DirectoryImpl(m_session, createURL(), createAdaptor_directory(), 0);
        assertEquals(1000+1000, dir.getSize());
        super.directory(dir);
    }
    
    @Test
    public void file() throws Exception {
        FileImpl file = new FileImpl(m_session, createURL(), createAdaptor_entry_read(FileReader.class), 0);
        assertEquals(1000, file.getSize());
        super.entry(file);

        // Cannot read if no READ flag
        final Buffer buffer = m_mockery.mock(Buffer.class, "buffer-file");
        m_mockery.checking(new Expectations() {{
            allowing(buffer).getSize(); will(returnValue(4));
        }});
        exception.expect(IncorrectStateException.class);
        exception.expectMessage("Reading file requires READ or READWRITE flags");
        assertEquals(4, file.read(buffer));
        exception.expectMessage("Writing file requires WRITE or READWRITE flags");
        assertEquals(4, file.write(buffer));
    }

    @Test
    public void fileRead() throws Exception {
        final FileReaderStreamFactory adaptor = m_mockery.mock(FileReaderStreamFactory.class, "adaptor-file-read");
        final Buffer buffer = m_mockery.mock(Buffer.class, "buffer-file-read");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).exists(with(any(String.class)), with(aNull(String.class))); will(returnValue(true));
            allowing(adaptor).disconnect(); will(returnValue(null));
            allowing(adaptor).getInputStream(with(any(String.class)), with(aNull(String.class))); 
                will(returnValue(new ByteArrayInputStream("INPUT_STRING".getBytes())));
            allowing(buffer).getSize(); will(returnValue(4));
            allowing(buffer).getData(); will(returnValue("INPUT_DATA".getBytes()));
        }});
        FileImpl file = new FileImpl(m_session, createURL(), adaptor, Flags.READ.getValue());
        assertEquals(4, file.read(buffer));
    }

    @Test
    public void fileWrite() throws Exception {
        final FileWriterStreamFactory adaptor = m_mockery.mock(FileWriterStreamFactory.class, "adaptor-file-write");
        final Buffer buffer = m_mockery.mock(Buffer.class, "buffer-file-write");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).disconnect(); will(returnValue(null));
            allowing(adaptor).getOutputStream(with(any(String.class)), with(any(String.class)), with(any(Boolean.class)), 
                                                with(any(Boolean.class)), with(aNull(String.class)));
                will(returnValue(new ByteArrayOutputStream()));
            allowing(buffer).getSize(); will(returnValue(4));
            allowing(buffer).getData(); will(returnValue("INPUT_DATA".getBytes()));
        }});
        FileImpl file = new FileImpl(m_session, createURL(), adaptor, Flags.WRITE.getValue());
        assertEquals(4, file.write(buffer));
    }

    @Test
    public void link() throws Exception {
        FileImpl file = new FileImpl(m_session, createURL(), createAdaptor_link(FileReader.class), 0);
        assertTrue(file.isLink());
        file.close();
    }
}
