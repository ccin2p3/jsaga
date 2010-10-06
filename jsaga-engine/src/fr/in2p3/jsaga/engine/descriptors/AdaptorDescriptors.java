package fr.in2p3.jsaga.engine.descriptors;

import fr.in2p3.jsaga.adaptor.Adaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.schema.config.*;
import fr.in2p3.jsaga.engine.schema.config.types.AttributeSourceType;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.*;
import org.ogf.saga.error.IncorrectStateException;

import java.io.*;
import java.util.*;

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
    private static AdaptorDescriptors _instance;

    private SecurityAdaptorDescriptor m_securityDesc;
    private DataAdaptorDescriptor m_dataDesc;
    private JobAdaptorDescriptor m_jobDesc;
    private Adaptors m_xml;

    public synchronized static AdaptorDescriptors getInstance() throws ConfigurationException {
        if (_instance == null) {
            _instance = new AdaptorDescriptors();
        }
        return _instance;
    }
    private AdaptorDescriptors() throws ConfigurationException {
        AdaptorLoader loader = new AdaptorLoader();
        try {
            m_securityDesc = new SecurityAdaptorDescriptor(loader.getClasses(SecurityAdaptor.class));
            m_dataDesc = new DataAdaptorDescriptor(loader.getClasses(DataAdaptor.class), m_securityDesc);
            m_jobDesc = new JobAdaptorDescriptor(loader.getClasses(JobControlAdaptor.class), m_securityDesc);
        } catch(Exception e) {
            throw new ConfigurationException(e);
        }
        m_xml = new Adaptors();
        m_xml.setContext(m_securityDesc.m_xml);
        m_xml.setProtocol(m_dataDesc.m_xml);
        m_xml.setExecution(m_jobDesc.m_xml);
        setRootAttributes(m_xml);
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
        m.setValidation(false); //no validation because some required attributes can not be set from adaptors
        m.marshal(m_xml);

        // build DOM
        return xmlStream.toByteArray();
    }

    protected static Map getDefaultsMap(Adaptor adaptor) {
        Map<String,String> map = new HashMap<String,String>();
        Default[] defaults;
        try {
            defaults = adaptor.getDefaults(new HashMap());
        } catch (IncorrectStateException e) {
            defaults = null;
        }
        if (defaults != null) {
            for (Default d : defaults) {
                if (d.getValue() != null) {
                    map.put(d.getName(), d.getValue());
                }
            }
        }
        return map;
    }

    protected static void setDefaults(ObjectType bean, Adaptor adaptor) {
        Default[] defaults;
        try {
            defaults = adaptor.getDefaults(new HashMap());
        } catch (IncorrectStateException e) {
            defaults = null;
        }
        if (defaults != null) {
            List list = new ArrayList();
            for (int d=0; d<defaults.length; d++) {
                if (defaults[d].getValue() != null) {
                    Attribute attr = new Attribute();
                    attr.setName(defaults[d].getName());
                    attr.setValue(defaults[d].getValue());
                    attr.setSource(AttributeSourceType.ADAPTORDEFAULTS);
                    list.add(attr);
                }
            }
            bean.setAttribute((Attribute[]) list.toArray(new Attribute[list.size()]));
        }
    }

    private static void setRootAttributes(Adaptors xml) {
        String tmpPath = (System.getProperty("file.separator").equals("\\")
                ? "/"+System.getProperty("java.io.tmpdir").replaceAll("\\\\", "/")
                : System.getProperty("java.io.tmpdir"));
        xml.setLocalIntermediary(tmpPath);
    }
}
