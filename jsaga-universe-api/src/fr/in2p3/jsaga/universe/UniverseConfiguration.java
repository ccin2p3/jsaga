package fr.in2p3.jsaga.universe;

import fr.in2p3.jsaga.universe.schema.UNIVERSE;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.*;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UniverseConfiguration
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   6 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UniverseConfiguration {
    private File m_file;
    private UNIVERSE m_config;

    public UniverseConfiguration(File file) throws FileNotFoundException, ValidationException, MarshalException {
        m_file = file;
        this._load();
    }

    public UNIVERSE getConfig() {
        return m_config;
    }

    public void save() throws FileNotFoundException, ValidationException, MarshalException {
        this._save(new FileOutputStream(m_file));
    }

    public void dump() throws ValidationException, MarshalException {
        this._save(System.out);
    }

    private void _load() throws FileNotFoundException, ValidationException, MarshalException {
        Unmarshaller unmarshaller = new Unmarshaller(UNIVERSE.class);
        unmarshaller.setIgnoreExtraAttributes(false);
        unmarshaller.setValidation(true);
        Reader reader = new InputStreamReader(new FileInputStream(m_file));
        m_config = (UNIVERSE) unmarshaller.unmarshal(reader);
    }

    private void _save(OutputStream out) throws ValidationException, MarshalException {
        LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "true");
        Writer writer = new OutputStreamWriter(out);
        Marshaller.marshal(m_config, writer);
    }

    public static void main(String[] args) throws Exception {
        new UniverseConfiguration(new File("etc/jsaga-universe.xml")).dump();
    }
}
