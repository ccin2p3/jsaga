package fr.in2p3.jsaga.adaptor.base.usage;

/* 
* ***************************************************
* File:   UProxyValue
* Author: Jérôme Revillard (jrevillard@maatg.fr)
* Date:   28 mars 2012
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UProxyValue extends U {
    public UProxyValue(String name) {
        super(name);
    }

    public UProxyValue(int id, String name) {
        super(id, name);
    }

    public String toString() {
        return "@"+m_name+"@";
    }

    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        String proxyValue = (String) super.throwExceptionIfInvalid(value);
        if (!proxyValue.startsWith("-----") && proxyValue.endsWith("-----")) {
            throw new Exception("Not a proxy content value: "+ proxyValue);
        }
        return value;
    }
}
