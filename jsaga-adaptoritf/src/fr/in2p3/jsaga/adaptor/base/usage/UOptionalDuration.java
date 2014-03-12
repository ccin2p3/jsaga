package fr.in2p3.jsaga.adaptor.base.usage;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

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
public class UOptionalDuration extends UDuration {

    public UOptionalDuration(String name) {
        super(name);
    }

    @Override
    public int getFirstMatchingUsage(Map attributes) throws DoesNotExistException, BadParameterException {
        try {
            super.getFirstMatchingUsage(attributes);
        } catch (DoesNotExistException e) {
        } catch (FileNotFoundException e) {
        }
        return m_id;
    }
    
    @Override
    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        if (value == null) return value;
        return super.throwExceptionIfInvalid(value);
    }

    public String toString() {
        return "["+m_name+"]";
    }

}
