package fr.in2p3.jsaga.adaptor.base.usage;

import fr.in2p3.jsaga.Base;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.NoSuccess;

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
    public String correctValue(String attributeName, String attributeValue) throws DoesNotExist, NoSuccess {
        String filename = super.correctValue(attributeName, attributeValue);
        if (filename != null) {
            File file = new File(filename);
            if (!file.isAbsolute()) {
                file = new File(Base.JSAGA_HOME, file.getPath());
            }
            try {
                return file.getCanonicalPath();
            } catch (IOException e) {
                throw new NoSuccess(e);
            }
        } else {
            return attributeValue;
        }
    }

    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        File file = new File((String) super.throwExceptionIfInvalid(value));
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: "+file.getAbsolutePath());
        }
        return file;
    }
}
