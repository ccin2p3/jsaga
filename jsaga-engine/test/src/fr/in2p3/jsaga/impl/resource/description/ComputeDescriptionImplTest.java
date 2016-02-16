package fr.in2p3.jsaga.impl.resource.description;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.resource.description.ComputeDescription;

public class ComputeDescriptionImplTest {


    private ComputeDescriptionImpl m_desc;
    
    @Before
    public void setUp() {
        m_desc = new ComputeDescriptionImpl();
    }
    
    @Test
    public void defaultComputeDescription() throws Exception {
        assertEquals(4, m_desc.listAttributes().length);
        assertEquals("ANY", m_desc.getAttribute(ComputeDescription.MACHINE_ARCH));
        assertEquals("ANY", m_desc.getAttribute(ComputeDescription.MACHINE_OS));
        assertEquals("1", m_desc.getAttribute(ComputeDescription.SIZE));
        assertEquals("root", m_desc.getAttribute(ComputeDescriptionImpl.ADMINUSER));
    }
    
}
