package fr.in2p3.jsaga.adaptor.base.usage;

import java.text.ParseException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UDuration
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UDuration extends U {
    private static final Pattern REGEXP = Pattern.compile("-?P" +
            "(?:([0-9]+)Y)?" +
            "(?:([0-9]+)M)?" +
            "(?:([0-9]+)D)?" +
            "(?:T" +
                "(?:([0-9]+)H)?" +
                "(?:([0-9]+)M)?" +
                "(?:" +
                    "(?:([0-9]+)(?:.([0-9]*))?S)" +
                    "|" +
                    "(?:.([0-9]+)S)" +
                ")?" +
            ")?");
    private static final int YEARS = 1;
    private static final int MONTHS = 2;
    private static final int DAYS = 3;
    private static final int HOURS = 4;
    private static final int MINUTES = 5;
    private static final int SECONDS = 6;
    private static final int MILLISECONDS = 7;
    private static final int MILLISECONDS_BIS = 8;

    public UDuration(String name) {
        super(name);
    }

    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        return new Long(toLong((String) super.throwExceptionIfInvalid(value)));
    }

    public static int toInt(Object value) throws ParseException {
        return (int) (toLong((String) value) / 1000);
    }

    private static long toLong(String value) throws ParseException {
        Matcher matcher = REGEXP.matcher(value);
        if (!matcher.matches()) {
            throw new ParseException("Value is not a XSD duration: "+value, 0);
        }
        Calendar duration = Calendar.getInstance();
        duration.setTimeInMillis(0);
        if (matcher.group(YEARS) != null) {
            duration.add(Calendar.YEAR, Integer.parseInt(matcher.group(YEARS)));
        }
        if (matcher.group(MONTHS) != null) {
            duration.add(Calendar.MONTH, Integer.parseInt(matcher.group(MONTHS)));
        }
        if (matcher.group(DAYS) != null) {
            duration.add(Calendar.DAY_OF_YEAR, Integer.parseInt(matcher.group(DAYS)));
        }
        if (matcher.group(HOURS) != null) {
            duration.add(Calendar.HOUR, Integer.parseInt(matcher.group(HOURS)));
        }
        if (matcher.group(MINUTES) != null) {
            duration.add(Calendar.MINUTE, Integer.parseInt(matcher.group(MINUTES)));
        }
        if (matcher.group(SECONDS) != null) {
            duration.add(Calendar.SECOND, Integer.parseInt(matcher.group(SECONDS)));
        }
        if (matcher.group(MILLISECONDS) != null) {
            duration.add(Calendar.MILLISECOND, Integer.parseInt(matcher.group(MILLISECONDS)));
        } else if (matcher.group(MILLISECONDS_BIS) != null) {
            duration.add(Calendar.MILLISECOND, Integer.parseInt(matcher.group(MILLISECONDS_BIS)));
        }
        return duration.getTimeInMillis();
    }
}
