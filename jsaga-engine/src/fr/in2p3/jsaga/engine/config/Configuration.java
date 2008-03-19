package fr.in2p3.jsaga.engine.config;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.engine.config.adaptor.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.config.attributes.*;
import fr.in2p3.jsaga.engine.config.bean.EngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.EffectiveConfig;
import fr.in2p3.jsaga.helpers.MD5Digester;
import fr.in2p3.jsaga.helpers.XMLFileParser;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.apache.log4j.PropertyConfigurator;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Configuration
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class Configuration {
    public static final String XSD_CONFIG = "schema/jsaga-config.xsd.xml";
    public static final File XML_MERGED_CONFIG = new File(Base.JSAGA_VAR, "jsaga-merged-config.xml");
    private static boolean ALWAYS_RELOAD_CONFIG = false;    //false is twice faster than true

    private static final String ADAPTOR_DESCRIPTORS = "adaptor-descriptors";
    private static final String XI_RAW_CONFIG = "raw-config.xi";
    private static final String XSL_1_DEAMBIGUISED_CONFIG = "xsl/config/1-deambiguised-config.xsl";
    private static final String XSL_2_EXPANDED_CONFIG = "xsl/config/2-expanded-config.xsl";
    private static final String XSL_3_FLATTEN_CONFIG = "xsl/config/3-flatten-config.xsl";
    private static final String XSL_4_MERGED_CONFIG = "xsl/config/4-merged-config.xsl";
    private static final String XSL_5_ENGINE_CONFIG = "xsl/config/5-engine-config.xsl";

    private static Configuration _instance = null;

    private AdaptorDescriptors m_descriptors;
    private EngineConfiguration m_configurations;

    public static synchronized Configuration getInstance() throws ConfigurationException {
        if (_instance == null) {
            try {
                _instance = new Configuration();
            } catch (Exception e) {
                throw new ConfigurationException(e);
            }
        }
        return _instance;
    }
    private Configuration() throws Exception {
        if (!Base.JSAGA_HOME.exists()) {
            throw new FileNotFoundException("JSAGA_HOME does not exist: "+Base.JSAGA_HOME.getAbsolutePath());
        }
        File baseDir = new File(Base.JSAGA_VAR, "jsaga-config");
        if(!baseDir.exists()) baseDir.mkdir();

        // configure log4j
        File log4jConfig = EngineProperties.getFile(EngineProperties.LOG4J_CONFIGURATION);
        if (log4jConfig.exists()) {
            PropertyConfigurator.configure(log4jConfig.getAbsolutePath());
        }

        // load adaptors
        m_descriptors = new AdaptorDescriptors();
        byte[] descBytes = m_descriptors.toByteArray();
        boolean sameDescMD5 = MD5Digester.isSame(new File(baseDir, ADAPTOR_DESCRIPTORS +".md5"), descBytes);

        // xinclude
        File jsagaConfig = EngineProperties.getRequiredFile(EngineProperties.JSAGA_CONFIGURATION);
        byte[] data = new XMLFileParser(null).xinclude(jsagaConfig);
        boolean sameConfigMD5 = MD5Digester.isSame(new File(baseDir, XI_RAW_CONFIG+".md5"), data);

        // load/generate merged config
        EffectiveConfig mergedConfig;
        Unmarshaller unmarshaller = new Unmarshaller(EffectiveConfig.class);
        unmarshaller.setIgnoreExtraAttributes(false);
        unmarshaller.setValidation(true);
        if (sameDescMD5 && sameConfigMD5 && XML_MERGED_CONFIG.exists() && !ALWAYS_RELOAD_CONFIG) {
            // *** load merged config from file ***
            Reader reader = new InputStreamReader(new FileInputStream(XML_MERGED_CONFIG));
            mergedConfig = (EffectiveConfig) unmarshaller.unmarshal(reader);
        } else {
            // *** generate merged config ***

            // parse config
            XMLFileParser parser = new XMLFileParser(new String[]{XSD_CONFIG});
            Document rawConfig = parser.parse(new ByteArrayInputStream(data), new File(baseDir, XI_RAW_CONFIG));

            // parse adaptor descriptors document (Note: ignored by stylesheet if marshalled from EffectiveConfig)
//            final String DEBUG = "xsl/config/descriptors.xml";
//            Source desc = new javax.xml.transform.stream.StreamSource(Configuration.class.getClassLoader().getResourceAsStream(DEBUG));
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            Source desc = new DOMSource(f.newDocumentBuilder().parse(new ByteArrayInputStream(descBytes)));

            // transform config
            XSLTransformerFactory tFactory = XSLTransformerFactory.getInstance();
            data = tFactory.create(XSL_1_DEAMBIGUISED_CONFIG).transform(rawConfig);
            data = tFactory.create(XSL_2_EXPANDED_CONFIG).transform(data);
            data = tFactory.create(XSL_3_FLATTEN_CONFIG).transform(data);
            data = tFactory.create(XSL_4_MERGED_CONFIG, new ConfigurationURIResolver(desc)).transform(data);
            Document doc = tFactory.create(XSL_5_ENGINE_CONFIG).transformToDOM(data);

            // parse merged config
            mergedConfig = (EffectiveConfig) unmarshaller.unmarshal(doc);

            // update attributes with system properties and adaptor usages
            new SystemPropertiesAttributesParser(mergedConfig).updateAttributes();
            new AdaptorUsageAttributesParser(mergedConfig, m_descriptors).updateAttributes();

            // save merged config to file
            LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "true");
            Writer writer = new OutputStreamWriter(new FileOutputStream(XML_MERGED_CONFIG));
            Marshaller.marshal(mergedConfig, writer);
        }

        // update attributes with user properties file and command line arguments
        new FilePropertiesAttributesParser(mergedConfig).updateAttributes();
        new CommandLineAttributesParser(mergedConfig).updateAttributes();

        // serialize config
        m_configurations = new EngineConfiguration(mergedConfig);

        // generate job structure
        File jobStructure = new File(Base.JSAGA_VAR, "jsaga-job-structure.xml");
        if (!jobStructure.exists()) {
            Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(
                    Configuration.class.getClassLoader().getResourceAsStream("xsl/jsaga-job-structure.xsl")));
            t.setURIResolver(new URIResolver(){
                public Source resolve(String href, String base) throws TransformerException {
                    return new StreamSource(Configuration.class.getClassLoader().getResourceAsStream(href));
                }
            });
            Source source = new StreamSource(new ByteArrayInputStream("<dummy/>".getBytes()));
            //bugfix: JAXB modify the file path if it is not encapsulated in an OutputStream
            Result result = new StreamResult(new FileOutputStream(jobStructure));
            t.transform(source, result);
        }
    }

    public AdaptorDescriptors getDescriptors() {
        return m_descriptors;
    }

    public EngineConfiguration getConfigurations() {
        return m_configurations;
    }
}
