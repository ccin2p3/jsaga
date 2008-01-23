package fr.in2p3.jsaga.engine.config;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.engine.config.adaptor.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.config.bean.EngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.EffectiveConfig;
import fr.in2p3.jsaga.helpers.MD5Digester;
import fr.in2p3.jsaga.helpers.XMLFileParser;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
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
    private static final String XSL_FLATTEN_CONFIG_1 = "xsl/config/flatten-config-1.xsl";
    private static final String XSL_FLATTEN_CONFIG_2 = "xsl/config/flatten-config-2.xsl";
    private static final String XSL_MERGED_CONFIG = "xsl/config/merged-config.xsl";

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
        if (!Base.JSAGA_CONFIG.exists()) {
            throw new FileNotFoundException("Configuration file does not exist: "+Base.JSAGA_CONFIG.getAbsolutePath());
        }
        File baseDir = new File(Base.JSAGA_VAR, "jsaga-config");
        if(!baseDir.exists()) baseDir.mkdir();

        // load adaptors
        m_descriptors = new AdaptorDescriptors();
        byte[] descBytes = m_descriptors.toByteArray();
        boolean sameDescMD5 = MD5Digester.isSame(new File(baseDir, ADAPTOR_DESCRIPTORS +".md5"), descBytes);

        // xinclude
        byte[] data = new XMLFileParser(null).xinclude(Base.JSAGA_CONFIG);
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

            // transform config
            XSLTransformerFactory tFactory = XSLTransformerFactory.getInstance();
            data = tFactory.create(XSL_FLATTEN_CONFIG_1).transform(rawConfig);
            data = tFactory.create(XSL_FLATTEN_CONFIG_2).transform(data);

            // parse adaptor descriptors document (Note: ignored by stylesheet if marshalled from EffectiveConfig)
//            final String DEBUG = "xsl/config/descriptors.xml";
//            Source desc = new javax.xml.transform.stream.StreamSource(Configuration.class.getClassLoader().getResourceAsStream(DEBUG));
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            Source desc = new DOMSource(f.newDocumentBuilder().parse(new ByteArrayInputStream(descBytes)));

            // merge config and adaptor descriptors
            Document doc = tFactory.create(XSL_MERGED_CONFIG, new ConfigurationURIResolver(desc)).transformToDOM(data);
            mergedConfig = (EffectiveConfig) unmarshaller.unmarshal(doc);

            // save merged config to file
            LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "true");
            Writer writer = new OutputStreamWriter(new FileOutputStream(XML_MERGED_CONFIG));
            Marshaller.marshal(mergedConfig, writer);
        }

        // serialize config
        m_configurations = new EngineConfiguration(mergedConfig, m_descriptors);
    }

    public AdaptorDescriptors getDescriptors() {
        return m_descriptors;
    }

    public EngineConfiguration getConfigurations() {
        return m_configurations;
    }
}
