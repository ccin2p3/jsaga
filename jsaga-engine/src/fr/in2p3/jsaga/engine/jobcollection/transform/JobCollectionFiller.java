package fr.in2p3.jsaga.engine.jobcollection.transform;

import fr.in2p3.jsaga.JSagaURL;
import fr.in2p3.jsaga.engine.data.FilledURL;
import fr.in2p3.jsaga.engine.jobcollection.preprocess.XJSDLNamespaceContext;
import org.ogf.saga.error.*;
import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.util.UUID;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionFiller
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionFiller {
    private XPath m_selector;
    private String m_collectionName;
    private Document m_effectiveJobCollection;

    public JobCollectionFiller(Document jobCollecDesc) throws NotImplemented, BadParameter, NoSuccess {
        // set xpath selector
        m_selector = XPathFactory.newInstance().newXPath();
        m_selector.setNamespaceContext(new XJSDLNamespaceContext());

        // set job collection
        m_effectiveJobCollection = jobCollecDesc;

        // set job collection name
        m_collectionName = this.getValue("/ext:JobCollection/ext:JobCollectionDescription/ext:JobCollectionIdentification/ext:JobCollectionName/text()");
        if (m_collectionName == null) {
            m_collectionName = this.getValue("/ext:JobCollection/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName/text()");
            if (m_collectionName == null) {
                m_collectionName = UUID.randomUUID().toString();
            }
        }

        // update URI
        NodeList list = this.getNodes("/ext:JobCollection/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging/jsdl:*/jsdl:URI/text()");
        for (int i=0; i<list.getLength(); i++) {
            Text node = (Text) list.item(i);
            String url = node.getData();
            String filledUrl = (node.getData().contains("@{")
                    ? JSagaURL.decode(new FilledURL(JSagaURL.encodeUrl(url)).getURI())
                    : new FilledURL(url).getURI().toString());
            node.setData(filledUrl);
        }
    }

    public String getCollectionName() {
        return m_collectionName;
    }

    public Document getEfectiveJobCollection() {
         return m_effectiveJobCollection;
    }

    ////////////////////////////////////////// XPath selection //////////////////////////////////////////

    private NodeList getNodes(String xpath) throws NoSuccess {
        try {
            return (NodeList) m_selector.evaluate(xpath, m_effectiveJobCollection, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
    }

    private String getValue(String xpath) throws NoSuccess {
        try {
            String result = (String) m_selector.evaluate(xpath, m_effectiveJobCollection, XPathConstants.STRING);
            if (!result.equals("")) {
                return result;
            } else {
                return null;
            }
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
    }

    private boolean exists(String xpath) throws NoSuccess {
        try {
            Boolean result = (Boolean) m_selector.evaluate(xpath, m_effectiveJobCollection, XPathConstants.BOOLEAN);
            return result.booleanValue();
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
    }
}
