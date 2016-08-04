package fr.in2p3.jsaga.impl.job.instance.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GetterInputStreamTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    

    @Test
    public void ok() throws Exception {
        byte[] b = new byte[4];
        GetterInputStream gis = new GetterInputStream(
                                    new ByteArrayInputStream("INPUT_STRING".getBytes())
                                );
        // otherwise read is done before data is ready
        Thread.sleep(500);
        assertTrue(gis.read() >= 0);
        assertEquals(4, gis.read(b, 0, 4));
        assertEquals(4, gis.read(b));
        gis.skip(1);
        gis.close();
    }
    
    @Test
    public void exceptions() throws Exception {
        GetterInputStream gis = new GetterInputStream(null);

        exception.expect(IOException.class);
        gis.read();
        gis.read(new byte[4]);
        gis.read(new byte[4], 0, 2);
        gis.close();
    }
}
