package fr.in2p3.jsaga.impl.job.description;

import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.JobDescription;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractJobDescriptionImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractJobDescriptionImpl extends AbstractAttributesImpl implements JobDescription {
    /** constructor */
    public AbstractJobDescriptionImpl() {
        super(null, true);  //isExtensible=true
    }

    public abstract Document getAsDocument() throws NoSuccessException;

    public Document getJSDL() throws NoSuccessException {
        Document jobDesc = this.getAsDocument();

        // check it is JSDL
        Element root = jobDesc.getDocumentElement();
        if ("http://schemas.ggf.org/jsdl/2005/11/jsdl".equals(root.getNamespaceURI()) &&
            "JobDefinition".equals(root.getLocalName()))
        {
            return jobDesc;
        } else {
            throw new NoSuccessException("[INTERNAL ERROR] Job description is not a JSDL document: "+root.getLocalName());
        }
    }
}
