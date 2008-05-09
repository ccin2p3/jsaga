package fr.in2p3.jsaga.engine.jobcollection;

import fr.in2p3.jsaga.engine.jobcollection.preprocess.XJSDLNamespaceContext;
import fr.in2p3.jsaga.engine.workflow.StartTask;
import fr.in2p3.jsaga.engine.workflow.task.*;
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
    private Document m_jobDesc;
    private String m_jobName;
    
    public DataStagingTaskGenerator(Document jobDesc, String jobName) throws NotImplemented, BadParameter, NoSuccess {
        // set xpath selector
        m_selector = XPathFactory.newInstance().newXPath();
        m_selector.setNamespaceContext(new XJSDLNamespaceContext());

        // set job
        m_jobDesc = jobDesc;
        m_jobName = jobName;
    }
    
    public void updateWorkflow(Workflow workflow) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        NodeList stagingList = this.getNodes("//ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging");
        for (int i=0; i<stagingList.getLength(); i++) {
            Element dataStaging = (Element) stagingList.item(i);
            String dataStagingName = getValueRelative(dataStaging, "@name");
            // input staging
            String sourceUri = this.getValueRelative(dataStaging, "jsdl:Source/jsdl:URI/text()");
            if (sourceUri != null) {
                boolean input = true;

                // add step tasks
                NodeList stepList = dataStaging.getElementsByTagNameNS("http://www.in2p3.fr/jsdl-extension", "Step");
                String previousTaskName = StartTask.name();
                for (int j=0; j<stepList.getLength(); j++) {
                    Element step = (Element) stepList.item(j);
                    TransferTask transferTask = new TransferTask(step.getAttribute("uri"), input);
                    workflow.add(transferTask, previousTaskName, null);
                    // next task
                    previousTaskName = transferTask.getName();
                }

                // add URI task
                if (m_jobName != null) {
                    DummyTask preStagedTask = new DummyTask(sourceUri);
                    workflow.add(preStagedTask, previousTaskName, StagedTask.name(m_jobName, dataStagingName, input));
                } else {
                    //todo
                }
            }
            // output staging
            String targetUri = this.getValueRelative(dataStaging, "jsdl:Target/jsdl:URI/text()");
            if (targetUri != null) {
                boolean notInput = false;

                // add step tasks
                NodeList stepList = dataStaging.getElementsByTagNameNS("http://www.in2p3.fr/jsdl-extension", "Step");
                String previousTaskName;
                if (m_jobName != null) {
                    previousTaskName = JobRunTask.name(m_jobName);
                } else {
                    previousTaskName = null;    //todo
                }
                for (int j=stepList.getLength()-1; j>=0; j--) {
                    Element step = (Element) stepList.item(j);
                    TransferTask transferTask = new TransferTask(step.getAttribute("uri"), notInput);
                    workflow.add(transferTask, previousTaskName, null);
                    // next task
                    previousTaskName = transferTask.getName();
                }

                // add URI task
                if (m_jobName != null) {
                    DummyTask preStagedTask = new DummyTask(targetUri);
                    workflow.add(preStagedTask, previousTaskName, StagedTask.name(m_jobName, dataStagingName, notInput));
                } else {
                    //todo
                }
            }
        }
    }

    ////////////////////////////////////////// XPath selection //////////////////////////////////////////

    private String getValueRelative(Element node, String xpath) throws NoSuccess {
        try {
            String result = (String) m_selector.evaluate(xpath, node, XPathConstants.STRING);
            if (!result.equals("")) {
                return result;
            } else {
                return null;
            }            
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
    }

    private NodeList getNodes(String xpath) throws NoSuccess {
        try {
            return (NodeList) m_selector.evaluate(xpath, m_jobDesc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
    }
}
