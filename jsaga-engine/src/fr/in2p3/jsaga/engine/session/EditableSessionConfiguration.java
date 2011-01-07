package fr.in2p3.jsaga.engine.session;

import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.generated.session.JsagaDefault;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

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

    public void save() throws MarshalException, ValidationException, IOException {
        Writer writer = new FileWriter(m_file);
        super.dump(writer);
    }

    public JsagaDefault getJsagaDefault() {
        return m_config;
    }
}
