package fr.in2p3.jsaga.helpers.xpath;

import org.ogf.saga.error.NoSuccess;
import org.w3c.dom.*;

import javax.xml.xpath.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   XJSDLXPathSelector
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class XJSDLXPathSelector {
    private XPath m_selector;
    private Document m_jcDesc;

    public XJSDLXPathSelector(Document jcDesc) {
        m_selector = XPathFactory.newInstance().newXPath();
        m_selector.setNamespaceContext(new XJSDLNamespaceContext());
        m_jcDesc = jcDesc;
    }

    public boolean exists(String xpath) throws NoSuccess {
        return this.exists(m_jcDesc, xpath);
    }
    public boolean exists(Node domTree, String xpath) throws NoSuccess {
        try {
            Boolean result = (Boolean) m_selector.evaluate(xpath, domTree, XPathConstants.BOOLEAN);
            return result.booleanValue();
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
    }

    public String getString(String xpath) throws NoSuccess {
        return this.getString(m_jcDesc, xpath);
    }
    public String getString(Node domTree, String xpath) throws NoSuccess {
        try {
            String result = (String) m_selector.evaluate(xpath, domTree, XPathConstants.STRING);
            if (!result.equals("")) {
                return result;
            } else {
                return null;
            }
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
    }

    public Node getNode(String xpath) throws NoSuccess {
        return this.getNode(m_jcDesc, xpath);
    }
    public Node getNode(Node domTree, String xpath) throws NoSuccess {
        try {
            return (Node) m_selector.evaluate(xpath, domTree, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
    }

    public NodeList getNodes(String xpath) throws NoSuccess {
        return this.getNodes(m_jcDesc, xpath);
    }
    public NodeList getNodes(Node domTree, String xpath) throws NoSuccess {
        try {
            return (NodeList) m_selector.evaluate(xpath, domTree, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
    }
}
