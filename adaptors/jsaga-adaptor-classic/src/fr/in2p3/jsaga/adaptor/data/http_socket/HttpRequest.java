package fr.in2p3.jsaga.adaptor.data.http_socket;

import org.ogf.saga.error.NoSuccessException;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpRequest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class HttpRequest {
    public static final String TYPE_GET = "GET";
    public static final String TYPE_HEAD = "HEAD";
    private static final String STATUS = "null";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String LAST_MODIFIED = "Last-Modified";
    private static final SimpleDateFormat DF = new SimpleDateFormat("EEE, d MMM yyyy KK:mm:ss zzz", Locale.ENGLISH);
    private Properties m_prop;
    private InputStream m_inputStream;

    public HttpRequest(String type, String path, Socket socket) throws IOException {
        // send request
        BufferedWriter request = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        request.write(type+" "+path+" HTTP/1.0");
        request.newLine();
        request.newLine(); // end of HTTP request
        request.flush();

        // get response
        InputStream response = socket.getInputStream();

        // set properties
        m_prop = new Properties();
        String line;
        while ( (line=readLine(response)).length() > 0 ) {
            int pos = line.indexOf(':');
            if (pos > -1) {
                m_prop.setProperty(line.substring(0,pos), line.substring(pos+2));
            } else {
                m_prop.setProperty(STATUS, line);
            }
        }

        if (type.equals(TYPE_GET)) {
            // set input stream (with rest of response stream)
            m_inputStream = response;
        } else if (type.equals(TYPE_HEAD)) {
            // close response stream
            response.close();
        } else {
            throw new IOException("[INTERNAL ERROR] Bad request type: "+type);
        }
    }

    public InputStream getInputStream() {
        return m_inputStream;
    }

    public String getStatus() {
        return m_prop.getProperty(STATUS);
    }

    public long getContentLength() {
        String value = m_prop.getProperty(CONTENT_LENGTH);
        if (value != null) {
            return Long.parseLong(value);
        } else {
            return -1;
        }
    }

    public Date getLastModified() throws NoSuccessException {
        String value = m_prop.getProperty(LAST_MODIFIED);
        if (value != null) {
            try {
                return DF.parse(value);
            } catch (ParseException e) {
                throw new NoSuccessException(e);
            }
        } else {
            return null;
        }
    }

    public boolean isDir() {
        return !m_prop.contains(LAST_MODIFIED);
    }

    private static String readLine(InputStream in) throws IOException {
        byte b;
        StringBuffer buf = new StringBuffer();
        while ((b=(byte) in.read())>0 && b!='\n') {
            if (b!='\r') {
                buf.append(new String(new byte[]{b}));
            }
        }
        return buf.toString();
    }
}
