package fr.in2p3.jsaga;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;

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
        int query = url.indexOf('?');
        int fragment = (query>-1 ? url.indexOf('#',query) : url.indexOf('#'));
        if (query > -1) {
            return encodePath(url.substring(0,query)) + url.substring(query);
        } else if (fragment > -1) {
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
            if (isIllegal(c)) {
                buffer.append('%');
                buffer.append(charToHex(c/16));
                buffer.append(charToHex(c%16));
            } else {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }
    private static boolean isIllegal(char c) {
        if (c>32 && c<128) {
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
        } else {
            return c <= 160;
        }
    }
    private static char charToHex(int c) {
        final char[] HEX = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        return HEX[c];
    }

    public static String decode(String url) {
        StringBuffer buffer = new StringBuffer();
        char[] array = url.toCharArray();
        for (int i=0; i<array.length; i++) {
            char c = array[i];
            if (c=='%' && i<array.length-2) {
                char c1 = array[i+1];
                char c2 = array[i+2];
                if (isHex(c1) && isHex(c2)) {
                    c = (char) (hexToChar(c1)*16 + hexToChar(c2));  // replace
                    i += 2; // jump
                }
            }
            buffer.append(c);
        }
        return buffer.toString();
    }
    private static boolean isHex(char c) {
        return (c>='0' && c<='9') || (c>='A' && c<='F') || (c>='a' && c<='f');
    }
    private static char hexToChar(char h) {
        if (h>='0' && h<='9') {
            return (char) (h - '0');
        } else if (h>='A' && h<='F') {
            return (char) (h - 'A' + 10);
        } else if (h>='a' && h<='f') {
            return (char) (h - 'a' + 10);
        } else {
            throw new RuntimeException("INTERNAL ERROR");
        }
    }
}
