package fr.in2p3.jsaga.adaptor.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   PathEncoder
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   2 oct. 2009
 * ***************************************************
 * Description:                                      */
/**
 * This is an extract of class fr.in2p3.jsaga.impl.url.URLEncoder in module jsaga-engine
 */
public class PathEncoder {
    static String encode(String path) throws UnsupportedEncodingException {
        StringBuffer buffer = new StringBuffer();
        char[] array = path.toCharArray();
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
                if (c <= 160) { // isIllegal
                    appendHex(buffer, 0xC0 | (c >> 6));
                    appendHex(buffer, 0x80 | (c & 0x3F));
                } else {
                    buffer.append(URLEncoder.encode(Character.toString(c), "UTF-8"));
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
}
