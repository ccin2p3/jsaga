package fr.in2p3.jsaga.engine.config.adaptor;

import fr.in2p3.jsaga.adaptor.Adaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.evaluator.Evaluator;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.language.LanguageAdaptor;
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
    private SecurityAdaptorDescriptor m_securityDesc;
    private DataAdaptorDescriptor m_dataDesc;
    private JobAdaptorDescriptor m_jobDesc;
    private LanguageAdaptorDescriptor m_languageDesc;
    private EvaluatorAdaptorDescriptor m_evaluatorDesc;
    private EffectiveConfig m_xml;

    public AdaptorDescriptors() throws ConfigurationException {
        AdaptorLoader loader = new AdaptorLoader();
        try {
            m_securityDesc = new SecurityAdaptorDescriptor(loader.getClasses(SecurityAdaptor.class));
            m_dataDesc = new DataAdaptorDescriptor(loader.getClasses(DataAdaptor.class), m_securityDesc);
            m_jobDesc = new JobAdaptorDescriptor(loader.getClasses(JobControlAdaptor.class), m_securityDesc);
            m_languageDesc = new LanguageAdaptorDescriptor(loader.getClasses(LanguageAdaptor.class));
            m_evaluatorDesc = new EvaluatorAdaptorDescriptor(loader.getClasses(Evaluator.class));
        } catch(Exception e) {
            throw new ConfigurationException(e);
        }
        m_xml = new EffectiveConfig();
        m_xml.setContext(m_securityDesc.m_xml);
        m_xml.setProtocol(m_dataDesc.m_xml);
        m_xml.setExecution(m_jobDesc.m_xml);
        m_xml.setLanguage(m_languageDesc.m_xml);
        m_xml.setEvaluatorImpl(m_evaluatorDesc.getClazz().getName());
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

    public LanguageAdaptorDescriptor getLanguageDesc() {
        return m_languageDesc;
    }

    public EvaluatorAdaptorDescriptor getEvaluatorDesc() {
        return m_evaluatorDesc;
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

    private static void setRootAttributes(EffectiveConfig xml) {
        String tmpPath = (System.getProperty("file.separator").equals("\\")
                ? "/"+System.getProperty("java.io.tmpdir").replaceAll("\\\\", "/")
                : System.getProperty("java.io.tmpdir"));
        xml.setLocalIntermediary(tmpPath);
    }
}
