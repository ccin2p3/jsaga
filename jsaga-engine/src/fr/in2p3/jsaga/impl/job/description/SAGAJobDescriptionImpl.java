package fr.in2p3.jsaga.impl.job.description;

import fr.in2p3.jsaga.adaptor.language.SAGALanguageAdaptor;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformer;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SAGAJobDescriptionImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SAGAJobDescriptionImpl extends AbstractJobDescriptionImpl implements JobDescription {
    private SAGALanguageAdaptor m_adaptor;
    private Document m_jobDescDOM;
    private Element m_root;

    /** constructor */
    public SAGAJobDescriptionImpl() throws Exception {
        super(null);
        m_adaptor = new SAGALanguageAdaptor();
        m_adaptor.initParser();
        m_jobDescDOM = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        m_root = m_jobDescDOM.createElement("attributes");
        m_jobDescDOM.appendChild(m_root);
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        SAGAJobDescriptionImpl clone = (SAGAJobDescriptionImpl) super.clone();
        clone.m_adaptor = m_adaptor;
        clone.m_jobDescDOM = (Document) m_jobDescDOM.cloneNode(true);
        clone.m_root = (Element) clone.m_jobDescDOM.getFirstChild();
        return clone;
    }

    /** override super.setAttribute() */
    public void setAttribute(String key, String value) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor.isProperty(key)) {
            Element attribute = this.createAttribute(key);
            attribute.setAttribute("value", value);
            super.setAttribute(key, value);
        } else if (m_adaptor.isVectoryProperty(key)) {
            throw new IncorrectState("Attempt to set a vector attribute with method setAttribute: "+key);
        } else {
            throw new BadParameter("Unexpected attribute name: "+key);
        }
    }

    /** override super.setVectorAttribute() */
    public void setVectorAttribute(String key, String[] values) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor.isVectoryProperty(key)) {
            Element vectorAttribute = this.createAttribute(key);
            for (int i=0; i<values.length; i++) {
                Element item = m_jobDescDOM.createElement("value");
                item.appendChild(m_jobDescDOM.createTextNode(values[i]));
                vectorAttribute.appendChild(item);
            }
            super.setVectorAttribute(key, values);
        } else if (m_adaptor.isProperty(key)) {
            throw new IncorrectState("Attempt to set a scalar attribute with method setVectorAttribute: "+key);
        } else {
            throw new BadParameter("Unexpected attribute name: "+key);
        }
    }

    public Element getJSDL() throws NoSuccess {
        String stylesheet = m_adaptor.getTranslator();
        if (stylesheet == null) {
            throw new NoSuccess("INTERNAL ERROR: stylesheet is null");
        }
        try {
            XSLTransformer sagaToJSDL = XSLTransformerFactory.getInstance().getCached(stylesheet);
            Document jsdlDOM = sagaToJSDL.transformToDOM(m_jobDescDOM);
            return (Element) jsdlDOM.getFirstChild();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    private Element createAttribute(String key) throws NoSuccess {
        Element attribute;
        NodeList list = m_root.getElementsByTagName(key);
        switch(list.getLength()) {
            case 0:
                attribute = m_jobDescDOM.createElement(key);
                m_root.appendChild(attribute);
                return attribute;
            case 1:
                attribute = m_jobDescDOM.createElement(key);
                m_root.replaceChild(attribute, list.item(0));
                return attribute;
            default:
                throw new NoSuccess("INTERNAL ERROR: unexpected exception", this);
        }
    }
}
