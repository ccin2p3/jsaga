package fr.in2p3.jsaga.adaptor.base.usage;

import fr.in2p3.jsaga.Base;
import org.ogf.saga.error.DoesNotExist;

import java.io.File;
import java.io.IOException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UFilePath
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   1 févr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UFilePath extends U {
    public UFilePath(String name) {
        super(name);
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

    /**
     * To be overloaded if needed
     */
    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        File file = new File((String) super.throwExceptionIfInvalid(value));
        if (!file.isAbsolute()) {
            file = new File(Base.JSAGA_HOME, file.getPath());
        }
        return file;
    }
}
