package fr.in2p3.jsaga.engine.config.adaptor;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.job.JobAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptorBuilder;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.schema.config.EffectiveConfig;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.*;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AdaptorDescriptors
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AdaptorDescriptors {
    private SecurityAdaptorDescriptor m_securityDesc;
    private DataAdaptorDescriptor m_dataDesc;
    private JobAdaptorDescriptor m_jobDesc;
    private EffectiveConfig m_xml;

    public AdaptorDescriptors() throws ConfigurationException {
        AdaptorLoader loader = new AdaptorLoader();
        try {
            m_securityDesc = new SecurityAdaptorDescriptor(loader.getClasses(SecurityAdaptorBuilder.class));
            m_dataDesc = new DataAdaptorDescriptor(loader.getClasses(DataAdaptor.class));
            m_jobDesc = new JobAdaptorDescriptor(loader.getClasses(JobAdaptor.class));
        } catch(Exception e) {
            throw new ConfigurationException(e);
        }
        m_xml = new EffectiveConfig();
        m_xml.setContextInstance(m_securityDesc.m_xml);
        m_xml.setProtocol(m_dataDesc.m_xml);
        m_xml.setJobservice(m_jobDesc.m_xml);
    }

    public SecurityAdaptorDescriptor getSecurityDesc() {
        return m_securityDesc;
    }

    public DataAdaptorDescriptor getDataDesc() {
        return m_dataDesc;
    }

    public JobAdaptorDescriptor getJobDesc() {
        return m_jobDesc;
    }

    public byte[] toByteArray() throws IOException, ValidationException, MarshalException {
        ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();

        // marshall
        LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "true");
        Marshaller m = new Marshaller(new OutputStreamWriter(xmlStream));
        m.setNamespaceMapping(null, "http://www.in2p3.fr/jsaga");
        m.setSuppressNamespaces(true);
        m.marshal(m_xml);

        // build DOM
        return xmlStream.toByteArray();
    }
}
