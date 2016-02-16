package fr.in2p3.jsaga.adaptor.resource;

import java.util.Properties;

public class SecuredResource extends Properties {

    /**
     * TODO JAVADOC
     */
    private static final long serialVersionUID = 1L;
    private String m_id;
    private String m_type;

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
