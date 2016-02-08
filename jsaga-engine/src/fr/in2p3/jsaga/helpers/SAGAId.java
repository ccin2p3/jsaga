package fr.in2p3.jsaga.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.url.URL;

public class SAGAId {

    /**
     * extract nativeId from a SAGA ID
     * SAGA ID is something like [URL]-[nativeId]
     * 
     * @param sagaId
     * @return
     * @throws BadParameterException
     */
    public static String idFromSagaId(String sagaId) throws BadParameterException {
        Pattern p = Pattern.compile("(\\[.*]-\\[)(.+)(])");
        Matcher m = p.matcher(sagaId);
        if (m.find()) {
            return m.group(2);
        }
        throw new BadParameterException();
    }

    public static String idToSagaId(URL url, String nativeId) {
        return "[" + url.getString() + "]-[" + nativeId + "]";
    }
}
