package fr.in2p3.jsaga.adaptor.security.usage;

import java.io.File;
import java.io.FileNotFoundException;

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

    protected void throwExceptionIfInvalid(Object value) throws Exception {
        super.throwExceptionIfInvalid(value);
        File file = new File((String)value);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: "+file.getAbsolutePath());
        }
    }
}
