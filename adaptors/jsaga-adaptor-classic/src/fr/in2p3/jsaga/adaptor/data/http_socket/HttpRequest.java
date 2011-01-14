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
    public static final String TYPE_PUT = "PUT";
    private String m_type;
    
    private static final String STATUS = "null";
    
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String LAST_MODIFIED = "Last-Modified";
    private static final SimpleDateFormat DF = new SimpleDateFormat("EEE, d MMM yyyy KK:mm:ss zzz", Locale.ENGLISH);
    
    protected Properties m_prop;
    private InputStream m_inputStream;
    private ByteArrayOutputStream m_outputStream;

    private String m_path;
    private Socket m_socket;
    private String m_version = "1.1";
    
    /**
     * Create a HTTP request and send it
     * 
     * @param	type 	The type of request. Supported requests are "GET", "HEAD", "PUT"
     * @param	path	The path of the file/directory on the HTTP server
     * @param	socket	The socket to use
     * @throws	IOException
     */
    public HttpRequest(String type, String path, Socket socket) throws IOException {
    	this(type, path, socket, true);
    }
    	
    /**
     * Create a HTTP request and eventually send it
     * 
     * @param	type 	The type of request. Supported requests are "GET", "HEAD", "PUT"
     * @param	path	The path of the file/directory on the HTTP server
     * @param	socket	The socket to use
     * @param	send	Send the request or not. If not, the send() method should be used later
     * @throws	IOException
     */
    public HttpRequest(String type, String path, Socket socket, boolean send) throws IOException {
    	m_type = type;
    	m_path = path;
    	m_socket = socket;
    	if (TYPE_PUT.equals(m_type)) {
    		m_outputStream = new ByteArrayOutputStream();
    	}
    	if (send) {
    		send();
    	}
    }
    
	public void write(int b) throws IOException {
        try {
        	m_outputStream.write(b);
        } catch (NullPointerException e) {
        }
	}
	
	public void write(byte[] b, int off, int len) throws IOException {
        try {
        	m_outputStream.write(b,off,len);
        } catch (NullPointerException e) {
        }
	}
	
    public void setVersion(String version) {
    	m_version = version;
    }
    
    public 	void send() throws IOException {
        // send request
        BufferedWriter request = new BufferedWriter(new OutputStreamWriter(m_socket.getOutputStream()));
        request.write(m_type + " " + m_path + " HTTP/" + m_version);
        request.newLine();
        System.out.println(m_type + " " + m_path + " HTTP/" + m_version);
        if (m_type.equals(TYPE_PUT)) {
            request.write("Accept: */*");
            request.newLine();
            System.out.println("Accept: */*");
            request.write("Content-Length: " + String.valueOf(m_outputStream.size()));
            request.newLine();
            System.out.println("Content-Length: " + String.valueOf(m_outputStream.size()));
        }
        request.newLine();
        if (m_type.equals(TYPE_PUT)) {
        	request.write(m_outputStream.toString());
            System.out.println(m_outputStream.toString());
        }
        request.flush();

        // get response
        InputStream response = m_socket.getInputStream();

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

        if (m_type.equals(TYPE_GET)) {
            // set input stream (with rest of response stream)
            m_inputStream = response;
        } else if (m_type.equals(TYPE_HEAD) || m_type.equals(TYPE_PUT)) {
            // close response stream
            response.close();
        } else {
            throw new IOException("[INTERNAL ERROR] Bad request type: "+m_type);
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

    protected static String readLine(InputStream in) throws IOException {
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
