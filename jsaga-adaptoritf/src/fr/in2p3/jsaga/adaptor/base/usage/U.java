package fr.in2p3.jsaga.adaptor.base.usage;

import java.io.*;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class U implements Usage {
    protected String m_name;

    public U(String name) {
        m_name = name;
    }

    public final boolean containsName(String attributeName) {
        return attributeName.equals(m_name);
    }

    public void updateAttributes(Map attributes) throws Exception {
        Object value = attributes.get(m_name);
        if (value != null) {
            Object newValue = this.updateValue(value);
            if (!value.equals(newValue)) {
                attributes.put(m_name, newValue);
            }
        }
    }

    public final Usage getMissingValues(Map attributes) {
        try {
            this.throwExceptionIfInvalid(attributes.get(m_name));
            return null;
        } catch(Exception e) {
            return this;
        }
    }

    /**
     * To be overloaded if needed
     */
    public void promptForValues(Map attributes, String id) throws Exception {
        if (!attributes.containsKey(m_name)) {
            System.out.println("Enter value for attribute '"+m_name+"' of security context instance '"+id+"'");
            String value = this.getUserInput();
            try {
                this.throwExceptionIfInvalid(value);
                attributes.put(m_name, value);
            } catch(Exception e) {
                System.out.println("Error: "+e.getMessage());
                throw e;
            }
        }
    }

    /**
     * To be overloaded if needed
     */
    public String toString() {
        return m_name;
    }

    /**
     * To be overloaded if needed
     */
    protected String getUserInput() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = in.readLine().trim();
        if (line.length() > 0) {
            return line;
        } else {
            return null;
        }
    }

    /**
     * To be overloaded if needed
     */
    protected Object updateValue(Object value) throws Exception {
        return value;
    }

    /**
     * To be overloaded if needed
     */
    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        if (value == null) {
            throw new NullPointerException("Null value");
        }
        return value;
    }
}
