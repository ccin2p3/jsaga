package fr.in2p3.jsaga.engine.jobcollection.transform;

import fr.in2p3.jsaga.engine.data.FilledURL;
import fr.in2p3.jsaga.helpers.xpath.XJSDLXPathSelector;
import org.ogf.saga.error.*;
import org.w3c.dom.*;

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
    private Document m_effectiveJobCollection;

    public JobCollectionFiller(Document jobCollecDesc) throws NotImplemented, BadParameter, NoSuccess {
        // modify the <URI> elements of job collection
        XJSDLXPathSelector selector = new XJSDLXPathSelector(jobCollecDesc);
        NodeList list = selector.getNodes("/ext:JobCollection/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging/jsdl:*/jsdl:URI/text()");
        for (int i=0; i<list.getLength(); i++) {
            Text node = (Text) list.item(i);
            String url = node.getData();
            // getString() decodes the URL
            String filledUrl = new FilledURL(url).getString();
            node.setData(filledUrl);
        }

        // set effective job collection with modified document
        m_effectiveJobCollection = jobCollecDesc;
    }

    public Document getEfectiveJobCollection() {
         return m_effectiveJobCollection;
    }
}
