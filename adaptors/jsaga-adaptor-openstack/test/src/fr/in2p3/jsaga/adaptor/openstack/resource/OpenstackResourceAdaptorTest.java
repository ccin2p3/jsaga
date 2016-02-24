package fr.in2p3.jsaga.adaptor.openstack.resource;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.resource.description.ComputeDescription;
import org.openstack4j.model.compute.Flavor;

public class OpenstackResourceAdaptorTest {

    private Mockery m_mockery = null;
    private OpenstackResourceAdaptor m_adaptor;
    private List<Flavor> m_availableFlavors;
    private Properties m_constraints;
    
    public OpenstackResourceAdaptorTest() {
        m_adaptor = new OpenstackResourceAdaptor();
        m_constraints = new Properties();
        m_availableFlavors = new ArrayList<Flavor>();
        m_mockery = new Mockery();
        m_availableFlavors.add(this.buildFlavor("512_1_private", false, false, 512, 1));
        m_mockery = new Mockery();
        m_availableFlavors.add(this.buildFlavor("512_1_disabled", true, true, 512, 1));
        m_mockery = new Mockery();
        m_availableFlavors.add(this.buildFlavor("1024_1", false, true, 1024, 1));
        m_mockery = new Mockery();
        m_availableFlavors.add(this.buildFlavor("1024_2", false, true, 1024, 2));
        m_mockery = new Mockery();
        m_availableFlavors.add(this.buildFlavor("2048_1", false, true, 2048, 1));
        m_mockery = new Mockery();
        m_availableFlavors.add(this.buildFlavor("2048_2", false, true, 2048, 2));
    }

    @Before
    public void setUp() {
        m_constraints.clear();
    }
    
    @Test
    public void noConstraints() throws Exception {
        Flavor f = m_adaptor.getMostAppropriateFlavorInList(m_availableFlavors, m_constraints);
        assertEquals("1024_1", f.getName());
    }
    
    @Test
    public void noRam() throws Exception {
        m_constraints.setProperty(ComputeDescription.SIZE, "2");
        Flavor f = m_adaptor.getMostAppropriateFlavorInList(m_availableFlavors, m_constraints);
        assertEquals("1024_2", f.getName());
    }
    
    @Test
    public void noCPU() throws Exception {
        m_constraints.setProperty(ComputeDescription.MEMORY, "2000");
        Flavor f = m_adaptor.getMostAppropriateFlavorInList(m_availableFlavors, m_constraints);
        assertEquals("2048_1", f.getName());
    }
    
    @Test
    public void get1024_1() throws Exception {
        m_constraints.setProperty(ComputeDescription.MEMORY, "512");
        m_constraints.setProperty(ComputeDescription.SIZE, "1");
        Flavor f = m_adaptor.getMostAppropriateFlavorInList(m_availableFlavors, m_constraints);
        assertEquals("1024_1", f.getName());
    }
    
    @Test(expected = DoesNotExistException.class)
    public void tooMuchMemory() throws Exception {
        m_constraints.setProperty(ComputeDescription.MEMORY, "4096");
        Flavor f = m_adaptor.getMostAppropriateFlavorInList(m_availableFlavors, m_constraints);
    }
    
    @Test(expected = DoesNotExistException.class)
    public void tooMuchCPU() throws Exception {
        m_constraints.setProperty(ComputeDescription.SIZE, "3");
        Flavor f = m_adaptor.getMostAppropriateFlavorInList(m_availableFlavors, m_constraints);
    }
    
    private Flavor buildFlavor(final String name, final Boolean disabled, final Boolean pub, final int ram, final int cpus) {
        final Flavor flavor = m_mockery.mock(Flavor.class);
        m_mockery.checking(new Expectations() {{
          allowing(flavor).getName(); will(returnValue(name));
          allowing(flavor).isDisabled(); will(returnValue(disabled));
          allowing(flavor).isPublic(); will(returnValue(pub));
          allowing(flavor).getRam(); will(returnValue(ram));
          allowing(flavor).getVcpus(); will(returnValue(cpus));
        }});
        return flavor;
    }
}
