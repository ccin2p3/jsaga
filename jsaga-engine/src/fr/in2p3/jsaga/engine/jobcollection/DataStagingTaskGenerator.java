package fr.in2p3.jsaga.engine.jobcollection;

import fr.in2p3.jsaga.engine.jobcollection.preprocess.XJSDLNamespaceContext;
import fr.in2p3.jsaga.workflow.Workflow;
import org.ogf.saga.error.*;
import org.w3c.dom.*;

import javax.xml.xpath.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataStagingTaskGenerator
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DataStagingTaskGenerator {
    private XPath m_selector;
    private Document m_jobCollection;    
    
    public DataStagingTaskGenerator(Document jobCollecDesc) throws NotImplemented, BadParameter, NoSuccess {
        // set xpath selector
        m_selector = XPathFactory.newInstance().newXPath();
        m_selector.setNamespaceContext(new XJSDLNamespaceContext());

        // set job collection
        m_jobCollection = jobCollecDesc;
    }
    
    public void updateWorkflow(Workflow workflow) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        NodeList stagingList = this.getNodes("//ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging[jsdl:Source and ext:Step]");
        for (int i=0; i<stagingList.getLength(); i++) {
            Element staging = (Element) stagingList.item(i);
            NodeList stepList = staging.getElementsByTagNameNS("http://www.in2p3.fr/jsdl-extension", "Step");
            System.out.println("---Source---");
            for (int j=0; j<stepList.getLength(); j++) {
                Element step = (Element) stepList.item(j);
                System.out.println(step.getAttribute("uri"));
            }
        }        
    }

    ////////////////////////////////////////// XPath selection //////////////////////////////////////////

    private NodeList getNodes(String xpath) throws NoSuccess {
        try {
            return (NodeList) m_selector.evaluate(xpath, m_jobCollection, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
    }
}
