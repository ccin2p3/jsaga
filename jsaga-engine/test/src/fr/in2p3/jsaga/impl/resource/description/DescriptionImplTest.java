package fr.in2p3.jsaga.impl.resource.description;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.resource.description.ComputeDescription;

public class DescriptionImplTest {

    @Test
    public void createEmptyComputeDescription() throws Exception {
        ComputeDescriptionImpl desc = new ComputeDescriptionImpl();
        // Check default attributes
        assertEquals(4, desc.listAttributes().length);
        assertEquals("ANY", desc.getAttribute(ComputeDescription.MACHINE_ARCH));
        assertEquals("ANY", desc.getAttribute(ComputeDescription.MACHINE_OS));
        assertEquals("1", desc.getAttribute(ComputeDescription.SIZE));
        assertEquals("root", desc.getAttribute(ComputeDescriptionImpl.ADMINUSER));
        
        // modify attribute
        desc.setAttribute(ComputeDescription.SIZE, "2");
        assertEquals("2", desc.getAttribute(ComputeDescription.SIZE));
        desc.setVectorAttribute(ComputeDescription.HOST_NAMES, new String[]{"server1", "server2"});
        assertEquals(2, desc.getVectorAttribute(ComputeDescription.HOST_NAMES).length);
    }
    
    @Test
    public void createComputeDescriptionFromProperties() throws Exception {
        Properties p = new Properties();
        p.put(ComputeDescription.SIZE, "2");
        p.put(ComputeDescription.HOST_NAMES, new String[]{"server1", "server2"});
        ComputeDescriptionImpl desc = new ComputeDescriptionImpl(p);
        assertEquals("2", desc.getAttribute(ComputeDescription.SIZE));
        assertEquals(2, desc.getVectorAttribute(ComputeDescription.HOST_NAMES).length);
    }
    
    @Test
    public void badAttribute() throws Exception {
        AbstractResourceDescriptionImpl desc;
        desc = new ComputeDescriptionImpl();
        try {
            desc.setAttribute(ComputeDescription.HOST_NAMES, "host");
            fail("expected IncorrectStateException");
        } catch (IncorrectStateException ise) {
            // OK
        }
        try {
            desc.setVectorAttribute(ComputeDescription.SIZE, new String[]{"1", "2"});
            fail("expected IncorrectStateException");
        } catch (IncorrectStateException ise) {
            // OK
        }
        try {
            desc.getAttribute(ComputeDescription.HOST_NAMES);
            fail("expected IncorrectStateException");
        } catch (IncorrectStateException ise) {
            // OK
        }
        try {
            desc.getVectorAttribute(ComputeDescription.SIZE);
            fail("expected IncorrectStateException");
        } catch (IncorrectStateException ise) {
            // OK
        }
    }
    
    @Test
    public void extensible() throws Exception {
        AbstractResourceDescriptionImpl desc;
        desc = new ComputeDescriptionImpl();
        desc.setAttribute("NewAttribute", "2");
        desc = new StorageDescriptionImpl();
        desc.setAttribute("NewAttribute", "2");
        desc = new NetworkDescriptionImpl();
        desc.setAttribute("NewAttribute", "2");
    }
}
