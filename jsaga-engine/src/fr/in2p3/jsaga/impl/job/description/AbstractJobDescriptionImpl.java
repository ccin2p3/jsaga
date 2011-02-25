package fr.in2p3.jsaga.impl.job.description;

import fr.in2p3.jsaga.adaptor.language.SAGALanguageAdaptor;
import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.Collection;

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
    private Collection<String> m_scalarAttributes;
    private Collection<String> m_vectorAttributes;

    /** constructor */
    public AbstractJobDescriptionImpl() {
        super(null, true);  //isExtensible=true
        m_scalarAttributes = Arrays.asList(SAGALanguageAdaptor.OPTIONAL_PROPERTY_NAMES);
        m_vectorAttributes = Arrays.asList(SAGALanguageAdaptor.OPTIONAL_VECTOR_PROPERTY_NAMES);
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

    @Override
    public void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_vectorAttributes.contains(key)) {
            throw new IncorrectStateException("Attribute is a vector attribute: "+key);
        }
        super.setAttribute(key, value);
    }

    @Override
    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_vectorAttributes.contains(key)) {
            throw new IncorrectStateException("Attribute is a vector attribute: "+key);
        }
        return super.getAttribute(key);
    }

    @Override
    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_scalarAttributes.contains(key)) {
            throw new IncorrectStateException("Attribute is a scalar attribute: "+key);
        }
        super.setVectorAttribute(key, values);
    }

    @Override
    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_scalarAttributes.contains(key)) {
            throw new IncorrectStateException("Attribute is a scalar attribute: "+key);
        }
        return super.getVectorAttribute(key);
    }
}
