package fr.in2p3.jsaga.adaptor.resource;

import java.util.Properties;

/**
 * A class that represents a security context necessary to access a particular resource.
 * 
 * @author schwarz
 *
 */
public class SecuredResource extends Properties {

    private static final long serialVersionUID = 1L;
    private String m_id;
    private String m_type;

    /**
     * constructor
     * 
     * @param id the identifier of the resource
     * @param type the type of security context
     */
    public SecuredResource(String id, String type) {
        m_id = id;
        m_type = type;
    }

    public String getId() {
        return m_id;
    }

    public String getContextType() {
        return m_type;
    }

}
