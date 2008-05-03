package fr.in2p3.jsaga;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;

import java.net.URI;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JSagaURL
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JSagaURL extends URL {
    private FileAttributes m_attributes;

    /** constructor for absolutePath */
    public JSagaURL(FileAttributes attributes, String absolutePath) throws NotImplemented, BadParameter, NoSuccess {
        super(absolutePath);
        m_attributes = attributes;
    }

    /** constructor for relativePath */
    public JSagaURL(FileAttributes attributes) throws NotImplemented, BadParameter, NoSuccess {
        super(encodePath(attributes.getName()));
        m_attributes = attributes;
    }

    public FileAttributes getAttributes() {
        return m_attributes;
    }

    public static String encodeUrl(String url) {
        int query, fragment;
        if ((query=url.lastIndexOf('?')) > -1) {
            return encodePath(url.substring(0,query)) + url.substring(query);
        } else if ((fragment=url.lastIndexOf('#')) > -1) {
            return encodePath(url.substring(0,fragment)) + url.substring(fragment);
        } else {
            return encodePath(url);
        }
    }
    public static String encodePath(String path) {
        StringBuffer buffer = new StringBuffer();
        char[] array = path.toCharArray();
        for (int i=0; i<array.length; i++) {
            char c = array[i];
            if (c < 128) {      // ASCII
                if (isIllegalASCII(c)) {
                    appendHex(buffer, c);
                } else {
                    buffer.append(c);
                }
            } else {            // non-ASCII (must be converted to UTF-8 before encoding)
                if (c <= 160) { // isIllegal
                    appendHex(buffer, 0xC0 | (c >> 6));
                    appendHex(buffer, 0x80 | (c & 0x3F));
                } else {
                    buffer.append(c);
                }
            }
        }
        return buffer.toString();
    }
    private static boolean isIllegalASCII(char c) {
        if (c <= 32) {
            return true;
        } else {
            switch(c) {
                // illegal characters
                case '"': case '%': case '<': case '>': case '[': case '\\': case ']':
                case '^': case '`': case '{': case '|': case '}': case 127:
                    return true;
                // reserved characters
                case '#': case '?':
                    return true;
                // other characters
                default:
                    return false;
            }
        }
    }
    private static void appendHex(StringBuffer buffer, int c) {
        final char[] HEX = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        buffer.append('%');
        buffer.append(HEX[c/16]);
        buffer.append(HEX[c%16]);
    }

    // [scheme://][[user-info@]host[:port]][path][?query][#fragment]
    public static String decode(URL url) throws NotImplemented {
        StringBuffer buffer = new StringBuffer();
        if (url.getScheme() != null) {
            buffer.append(url.getScheme());
            buffer.append("://");
        }
        if (url.getHost() != null) {
            if (url.getUserInfo() != null) {
                buffer.append(url.getUserInfo());
                buffer.append('@');
            }
            buffer.append(url.getHost());
            if (url.getPort() != -1) {
                buffer.append(':');
                buffer.append(url.getPort());
            }
        }
        if (url.getPath() != null) {
            buffer.append(url.getPath());
        }
        if (url.getQuery() != null) {
            buffer.append('?');
            buffer.append(url.getQuery());
        }
        if (url.getFragment() != null) {
            buffer.append('#');
            buffer.append(url.getFragment());
        }
        return buffer.toString();
    }

    public static String decode(URI url) throws NotImplemented {
        StringBuffer buffer = new StringBuffer();
        if (url.getScheme() != null) {
            buffer.append(url.getScheme());
            buffer.append("://");
        }
        if (url.getHost() != null) {
            if (url.getUserInfo() != null) {
                buffer.append(url.getUserInfo());
                buffer.append('@');
            }
            buffer.append(url.getHost());
            if (url.getPort() != -1) {
                buffer.append(':');
                buffer.append(url.getPort());
            }
        }
        if (url.getPath() != null) {
            buffer.append(url.getPath());
        }
        if (url.getQuery() != null) {
            buffer.append('?');
            buffer.append(url.getQuery());
        }
        if (url.getFragment() != null) {
            buffer.append('#');
            buffer.append(url.getFragment());
        }
        return buffer.toString();
    }
}
