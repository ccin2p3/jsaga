package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.impl.namespace.EntryImplTestAbstract;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.buffer.Buffer;

import static org.junit.Assert.assertTrue;

public class LogicalFileImplTest extends EntryImplTestAbstract {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void directory() throws Exception {
        LogicalDirectoryImpl dir = new LogicalDirectoryImpl(m_session, createURL(), createAdaptor_directory(), 0);
        super.directory(dir);
    }

    @Test
    public void file() throws Exception {
        final Buffer buffer = m_mockery.mock(Buffer.class, "buffer-file");
        LogicalFileImpl file = new LogicalFileImpl(m_session, createURL(), createAdaptor_entry(buffer), 0);
        super.entry(file);
    }

    @Test
    public void link() throws Exception {
        LogicalFileImpl file = new LogicalFileImpl(m_session, createURL(), createAdaptor_link(), 0);
        assertTrue(file.isLink());
        file.close();
    }
}
