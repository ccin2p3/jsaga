package fr.in2p3.jsaga.adaptor.base.defaults;

import org.ogf.saga.error.IncorrectState;

import java.io.*;
import java.util.Properties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EnvironmentVariables
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EnvironmentVariables {
    private static EnvironmentVariables _instance;
    private Properties m_env = new Properties();

    public static EnvironmentVariables getInstance() throws IncorrectState {
        if (_instance == null) {
            try {
                _instance = new EnvironmentVariables();
            } catch(IOException e) {
                throw new IncorrectState(e);
            }
        }
        return _instance;
    }
    private EnvironmentVariables() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime r = Runtime.getRuntime();
        Process p;
        if (os.indexOf("windows")!=-1) {
            if (os.indexOf("95")!=-1 || os.indexOf("98")!=-1 || os.indexOf("Me")!=-1) {
                p = r.exec("command.com /c set");
            } else {
                p = r.exec("cmd.exe /c set");
            }
        } else {
            p = r.exec("/usr/bin/env");
        }
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line=reader.readLine()) != null) {
            int pos = line.indexOf('=');
            String key = line.substring(0, pos);
            String value = line.substring(pos + 1);
            m_env.setProperty(key, value);
        }
    }

    public String getProperty(String key) {
        return m_env.getProperty(key);
    }
}
