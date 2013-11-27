package fr.in2p3.jsaga.adaptor.security;

import org.ogf.saga.error.IncorrectStateException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   VomsesFile
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
@Deprecated
public class VomsesFile {
    private Matcher m_vomses;

    public VomsesFile() throws IncorrectStateException {
        File vomses = new File(System.getProperty("user.home"), ".vomses");
        if (vomses.exists()) {
            byte[] buffer = new byte[(int) vomses.length()];
            try {
                DataInputStream in = new DataInputStream(new FileInputStream(vomses));
                in.readFully(buffer);
            } catch (IOException e) {
                throw new IncorrectStateException(e);
            }
            String line = new String(buffer).trim();
            Pattern regexp = Pattern.compile("\"(.+)\" +\"(.+)\" +\"(.+)\" +\"(.+)\" +\"(.+)\"");
            m_vomses = regexp.matcher(line);
            if (!m_vomses.matches() || m_vomses.groupCount()!=5) {
                throw new IncorrectStateException("Failed to parse vomses file: "+vomses);
            }
        } else {
            m_vomses = null;
        }
    }

    public String getDefaultServer() {
        if (m_vomses != null) {
            StringBuffer server = new StringBuffer();
            server.append("voms://");
            server.append(m_vomses.group(2));
            server.append(":");
            server.append(m_vomses.group(3));
            server.append(m_vomses.group(4));
            return server.toString();
        } else {
            return null;
        }
    }

    public String getDefaultVO() {
        if (m_vomses != null) {
            return m_vomses.group(1);
        } else {
            return null;
        }
    }
}
