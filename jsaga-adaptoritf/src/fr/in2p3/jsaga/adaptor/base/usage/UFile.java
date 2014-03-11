package fr.in2p3.jsaga.adaptor.base.usage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UFile
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UFile extends UFilePath {
    public UFile(String name) {
        super(name);
    }

    public UFile(int id, String name) {
        super(id, name);
    }

    public String toString() {
        return "<"+m_name+">";
    }

    @Override
    public int getFirstMatchingUsage(Map attributes) throws DoesNotExistException, BadParameterException, FileNotFoundException {
        if (!attributes.containsKey(m_name)) {
            throw new DoesNotExistException("Attribute not found: "+m_name);
        }
        try {
            this.throwExceptionIfInvalid(attributes.get(m_name));
            return m_id;
        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return -1;
        }
    }


    @Override
    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        File file = (File) super.throwExceptionIfInvalid(value);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: "+file.getAbsolutePath());
        }
        return file;
    }
}
