package fr.in2p3.jsaga.impl.url;

import java.net.URI;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   URLEncoder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class URLEncoder {
	
	/* this method is called on a full URL 
	 * Only the part before ? (query separator) or # (fragment separator) is encoded
	 * 
	 */
    static String encode(String url) {
        int query, fragment;
        if ((query=url.lastIndexOf('?')) > -1) {
            return encodeUrlNoQueryNoFragment(url.substring(0,query)) + url.substring(query);
        } else if ((fragment=url.lastIndexOf('#')) > -1) {
            return encodeUrlNoQueryNoFragment(url.substring(0,fragment)) + url.substring(fragment);
        } else {
            return encodeUrlNoQueryNoFragment(url);
        }
    }

    /* This method encodes a URL with no query and no fragment
     * Thus ? and # are encoded
     */
    static String encodeUrlNoQueryNoFragment(String url) {
        StringBuffer buffer = new StringBuffer();
        char[] array = url.toCharArray();
        for (int i=0; i<array.length; i++) {
            char c = array[i];
            if (c < 128) {      // ASCII
                if (c=='%' && (isEncodedQuestionMark(array,i) || isEncodedSharp(array,i))) {
                    buffer.append(c);   // allow already encoded '?' or '#'
                } else if (isIllegalASCII(c)) {
                    appendHex(buffer, c);
                } else {
                    buffer.append(c);
                }
            } else {            // non-ASCII (must be converted to UTF-8 before encoding)
            	// FIXME: french chars are not encoded
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

    /**
     * This is specific for filenames where we can support additional charachers as ':' ...
     * @param path
     * @return
     */
    public static String encodePathOnly(String path) {
        StringBuffer buffer = new StringBuffer();
        char[] array = path.toCharArray();
        for (int i=0; i<array.length; i++) {
            char c = array[i];
            if (c < 128) {      // ASCII
                if (c=='%' && (isEncodedQuestionMark(array,i) || isEncodedSharp(array,i))) {
                    buffer.append(c);   // allow already encoded '?' or '#'
                } else if (isReservedASCII(c)) {
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
    
    /* same as isIllegalASCII but additional characters are encoded for filenames as ':'
     * 
     */
    private static boolean isReservedASCII(char c) {
    	if (isIllegalASCII(c)) return true;
        switch(c) {
            // additional reserved characters for filenames
            case ':':
                return true;
            // other characters
            default:
                return isIllegalASCII(c);
        }
    }
    
    private static boolean isEncodedQuestionMark(char[] array, int pos) {
        return pos+2<array.length && array[pos+1]=='3' && array[pos+2]=='F';
    }
    private static boolean isEncodedSharp(char[] array, int pos) {
        return pos+2<array.length && array[pos+1]=='2' && array[pos+2]=='3';
    }
    private static void appendHex(StringBuffer buffer, int c) {
        final char[] HEX = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        buffer.append('%');
        buffer.append(HEX[c/16]);
        buffer.append(HEX[c%16]);
    }

    // [scheme://][[user-info@]host[:port]][path][?query][#fragment]
    static String decode(URI url, boolean mustRemoveSlash) {
        StringBuffer buffer = new StringBuffer();
        if (url.getScheme() != null) {
            buffer.append(url.getScheme());
            buffer.append(":");
        }
        if (url.getHost() != null) {
            if (url.getScheme() != null) buffer.append("//");
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
            if (mustRemoveSlash) {
                buffer.append(url.getPath().substring(1));
            } else {
                buffer.append(url.getPath());
            }
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
