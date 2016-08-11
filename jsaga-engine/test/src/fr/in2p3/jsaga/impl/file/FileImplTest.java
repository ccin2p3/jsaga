package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import fr.in2p3.jsaga.impl.namespace.EntryImplTestAbstract;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Test;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.SagaException;
import org.ogf.saga.namespace.Flags;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileImplTest extends EntryImplTestAbstract<FileReader, FileWriter> {

    @Mock private FileReader reader;
    @Mock private FileWriter writer;
    @Mock private Buffer buffer;
    @Mock private FileReaderStreamFactory readerFactory;
    @Mock private FileWriterStreamFactory writerFactory;

    @Test
    public void directory() throws Exception {
        super.setDirectory(reader);
        DirectoryImpl dir = new DirectoryImpl(m_session, createURL(), reader, 0);
        assertEquals(1000+1000, dir.getSize());
        super.directory(dir);
    }
    
    @Test
    public void file() throws Exception {
        super.setEntry(reader, EntryType.FILE);
        FileImpl file = new FileImpl(m_session, createURL(), reader, 0);
        assertEquals(1000, file.getSize());
        super.entry(file);
    }

    @Test
    public void link() throws Exception {
        super.setEntry(reader, EntryType.LINK);
        FileImpl file = new FileImpl(m_session, createURL(), reader, 0);
        assertTrue(file.isLink());
        file.close();
    }

    @Test
    public void buffer_IncorrectStateException_reading() throws SagaException {
        super.setEntry(reader, EntryType.FILE);
        FileImpl file = new FileImpl(m_session, createURL(), reader, 0);
        context.checking(new Expectations() {{
            allowing(buffer).getSize(); will(returnValue(4));
        }});
        exception.expect(IncorrectStateException.class);
        exception.expectMessage("Writing file requires WRITE or READWRITE flags");
        file.write(buffer);
    }

    @Test
    public void buffer_IncorrectStateException_writing() throws SagaException {
        super.setEntry(reader, EntryType.FILE);
        FileImpl file = new FileImpl(m_session, createURL(), reader, 0);
        context.checking(new Expectations() {{
            allowing(buffer).getSize(); will(returnValue(4));
        }});
        exception.expect(IncorrectStateException.class);
        exception.expectMessage("Reading file requires READ or READWRITE flags");
        file.read(buffer);
    }

    @Test
    public void fileRead() throws Exception {
        context.checking(new Expectations() {{
            allowing(readerFactory).getType(); will(returnValue("adaptor"));
            allowing(readerFactory).exists(with(any(String.class)), with(aNull(String.class))); will(returnValue(true));
            allowing(readerFactory).disconnect(); will(returnValue(null));
            allowing(readerFactory).getInputStream(with(any(String.class)), with(aNull(String.class)));
                will(returnValue(new ByteArrayInputStream("INPUT_STRING".getBytes())));
            allowing(buffer).getSize(); will(returnValue(4));
            allowing(buffer).getData(); will(returnValue("INPUT_DATA".getBytes()));
        }});
        FileImpl file = new FileImpl(m_session, createURL(), readerFactory, Flags.READ.getValue());
        assertEquals(4, file.read(buffer));
    }

    @Test
    public void fileWrite() throws Exception {
        context.checking(new Expectations() {{
            allowing(writerFactory).getType(); will(returnValue("adaptor"));
            allowing(writerFactory).disconnect(); will(returnValue(null));
            allowing(writerFactory).getOutputStream(with(any(String.class)), with(any(String.class)), with(any(Boolean.class)),
                                                with(any(Boolean.class)), with(aNull(String.class)));
                will(returnValue(new ByteArrayOutputStream()));
            allowing(buffer).getSize(); will(returnValue(4));
            allowing(buffer).getData(); will(returnValue("INPUT_DATA".getBytes()));
        }});
        FileImpl file = new FileImpl(m_session, createURL(), writerFactory, Flags.WRITE.getValue());
        assertEquals(4, file.write(buffer));
    }
}
