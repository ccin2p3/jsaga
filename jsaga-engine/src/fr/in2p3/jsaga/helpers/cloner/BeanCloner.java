package fr.in2p3.jsaga.helpers.cloner;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Serializable;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BeanCloner
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BeanCloner {
    /**
     * deeply copy a castor bean
     * @param source the source castor bean
     * @return the target castor bean
     */
    public Serializable cloneBean(Serializable source) throws CloneNotSupportedException {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Marshaller.marshal(source, doc);
            return (Serializable) Unmarshaller.unmarshal(source.getClass(), doc);
        } catch (Exception e) {
            throw new CloneNotSupportedException(e.getMessage());
        }
    }
}
