package fr.in2p3.jsaga.impl.buffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;

public class BufferTest {

    private static BufferFactoryImpl m_factory = new BufferFactoryImpl();
    private Buffer m_app_buffer;
    private Buffer m_imp_buffer;
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Before
    public void setUp() throws Exception {
        m_app_buffer = m_factory.doCreateBuffer("BUFFER_STRING".getBytes());
        m_imp_buffer = m_factory.doCreateBuffer(13);
    }
    
    @Test
    public void setSize() throws Exception {
        m_imp_buffer.setSize();
        exception.expect(IncorrectStateException.class);
        m_app_buffer.setSize();
    }

    @Test
    public void setSizeNotAllowed2() throws Exception {
        m_imp_buffer.setSize(4);
        exception.expect(IncorrectStateException.class);
        m_app_buffer.setSize(4);
    }
    
    @Test
    public void getSize() throws Exception {
        assertEquals(13, m_app_buffer.getSize());
        assertEquals(13, m_imp_buffer.getSize());
    }
    
    @Test
    public void setData() throws Exception {
        m_app_buffer.setData("NEW_STRING".getBytes());
        assertEquals(10, m_app_buffer.getSize());
        exception.expect(IncorrectStateException.class);
        m_imp_buffer.setData("NEW_STRING".getBytes());
    }
    
    @Test
    public void cloning() throws Exception {
        assertEquals(new String(((Buffer)m_app_buffer.clone()).getData()), new String(m_app_buffer.getData()));
        assertEquals(new String(((Buffer)m_imp_buffer.clone()).getData()), new String(m_imp_buffer.getData()));
    }
    
    @Test(expected=BadParameterException.class)
    public void closeAndSetSize() throws Exception {
        m_imp_buffer.close();
        m_imp_buffer.setSize();
    }

    @Test
    public void closeAndGetSize() throws Exception {
        m_imp_buffer.close();
        assertEquals(-1, m_imp_buffer.getSize());
        m_app_buffer.close();
        assertEquals(-1, m_app_buffer.getSize());
    }
    
    @Test(expected=DoesNotExistException.class)
    public void getDataAppBufferClosed()  throws Exception {
        this.closeAndGetData(m_app_buffer);
    }
    
    @Test(expected=DoesNotExistException.class)
    public void getDataImpBufferClosed()  throws Exception {
        this.closeAndGetData(m_imp_buffer);
    }
    
    private void closeAndGetData(Buffer buf) throws Exception {
        buf.close();
        buf.getData();
    }
}
