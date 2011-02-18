package fr.in2p3.jsaga.engine.session;

import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.generated.session.JsagaDefault;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.w3c.dom.Document;

import java.io.*;
import java.net.MalformedURLException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   EditableSessionConfiguration
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class EditableSessionConfiguration extends SessionConfiguration {
    private File m_file;

    public EditableSessionConfiguration(File file) throws ConfigurationException, MalformedURLException {
        super(file.toURI().toURL());
        m_file = file;
    }

    public void save() throws Exception {
        // serialize bean (needed for transforming config)
        ByteArrayOutputStream rawConfig = new ByteArrayOutputStream();
        Marshaller marshaller = new Marshaller(new OutputStreamWriter(rawConfig));
        marshaller.setValidation(true);
        marshaller.marshal(m_config);

        // transform config
        XSLTransformerFactory tFactory = XSLTransformerFactory.getInstance();
        Document merged = tFactory.create(MERGE).transformToDOM(rawConfig.toByteArray());
        Document doc = tFactory.create(XSL).transformToDOM(merged);

        // deserialize bean (needed for creating session objects)
        Unmarshaller unmarshaller = new Unmarshaller(JsagaDefault.class);
        unmarshaller.setIgnoreExtraAttributes(false);
        unmarshaller.setValidation(true);
        m_config = (JsagaDefault) unmarshaller.unmarshal(doc);

        // save to file
        Writer writer = new FileWriter(m_file);
        super.dump(writer);
    }

    public JsagaDefault getJsagaDefault() {
        return m_config;
    }
}
