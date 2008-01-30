package fr.in2p3.jsaga.adaptor.base.usage;

import fr.in2p3.jsaga.Base;
import org.ogf.saga.error.DoesNotExist;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UFile
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UFile extends U {
    public UFile(String name) {
        super(name);
    }

    public String toString() {
        return "<"+m_name+">";
    }

    /** override U.correctValue() */
    public String correctValue(String attributeName, String attributeValue, int attributeWeight) throws DoesNotExist {
        if (m_name.equals(attributeName)) {
            try {
                File file = (File) this.throwExceptionIfInvalid(attributeValue);
                m_weight = attributeWeight;
                // returns corrected path
                try {
                    return file.getCanonicalPath();
                } catch (IOException e) {
                    return file.getAbsolutePath();
                }
            } catch (Exception e) {
                m_weight = -1;
                return null;
            }
        } else {
            throw new DoesNotExist("Attribute not found: "+attributeName);
        }
    }

    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        File file = new File((String) super.throwExceptionIfInvalid(value));
        if (!file.isAbsolute()) {
            file = new File(Base.JSAGA_HOME, file.getPath());
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: "+file.getAbsolutePath());
        }
        return file;
    }
}
