package fr.in2p3.jsaga.engine.jobcollection;

import fr.in2p3.jsaga.engine.workflow.StartTask;
import fr.in2p3.jsaga.engine.workflow.task.*;
import fr.in2p3.jsaga.helpers.xpath.XJSDLXPathSelector;
import fr.in2p3.jsaga.workflow.Workflow;
import fr.in2p3.jsaga.workflow.WorkflowTask;
import org.ogf.saga.error.*;
import org.w3c.dom.*;

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
    private String m_jobName;
    private XJSDLXPathSelector m_selector;

    public DataStagingTaskGenerator(String jobName, Document jobDesc) throws NotImplemented, BadParameter, NoSuccess {
        m_jobName = jobName;
        m_selector = new XJSDLXPathSelector(jobDesc);
    }
    
    public void updateWorkflow(Workflow workflow) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        NodeList stagingList = m_selector.getNodes("/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging[not(translate(ext:Sandbox/text(),'TRUE','true')='true')]");
        for (int i=0; i<stagingList.getLength(); i++) {
            Element dataStaging = (Element) stagingList.item(i);
            String dataStagingName = m_selector.getString(dataStaging, "@name");

            // input staging
            String sourceUri = m_selector.getString(dataStaging, "jsdl:Source/jsdl:URI/text()");
            if (sourceUri != null) {
                boolean input = true;
                WorkflowTask currentTask;
                String previousTaskName = ("INPUT_SANDBOX".equals(dataStagingName)
                        ? null
                        : StartTask.name());
                NodeList stepList = dataStaging.getElementsByTagNameNS("http://www.in2p3.fr/jsdl-extension", "Step");
                if (stepList.getLength() > 0) {
                    // create dummy task
                    Element step = (Element) stepList.item(0);
                    currentTask = new SourceTask(step.getAttribute("uri"), input);
                    for (int j=1; j<stepList.getLength(); j++) {
                        // add task
                        workflow.add(currentTask, previousTaskName, null);
                        previousTaskName = currentTask.getName();
                        // create transfer task
                        step = (Element) stepList.item(j);
                        currentTask = new TransferTask(step.getAttribute("uri"), input);
                    }
                } else {
                    currentTask = new SourceTask(sourceUri, input);
                }
                // add last task
                String nextTaskName = StagedTask.name(m_jobName, dataStagingName, input);
                workflow.add(currentTask, previousTaskName, nextTaskName);
            }

            // output staging
            String targetUri = m_selector.getString(dataStaging, "jsdl:Target/jsdl:URI/text()");
            if (targetUri != null) {
                boolean notInput = false;
                WorkflowTask currentTask;
                String previousTaskName = JobRunTask.name(m_jobName);
                NodeList stepList = dataStaging.getElementsByTagNameNS("http://www.in2p3.fr/jsdl-extension", "Step");
                if (stepList.getLength() > 0) {
                    // create dummy task
                    Element step = (Element) stepList.item(stepList.getLength()-1);
                    currentTask = new SourceTask(step.getAttribute("uri"), notInput);
                    for (int j=stepList.getLength()-2; j>=0; j--) {
                        // add task
                        workflow.add(currentTask, previousTaskName, null);
                        previousTaskName = currentTask.getName();
                        // create transfer task
                        step = (Element) stepList.item(j);
                        currentTask = new TransferTask(step.getAttribute("uri"), notInput);
                    }
                } else {
                    currentTask = new SourceTask(targetUri, notInput);
                }
                String nextTaskName = ("OUTPUT_SANDBOX".equals(dataStagingName)
                        ? null
                        : StagedTask.name(m_jobName, dataStagingName, notInput));
                workflow.add(currentTask, previousTaskName, nextTaskName);
            }

            // no staging needed
            if (sourceUri==null && targetUri==null && !m_selector.exists("/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging[@name='"+dataStagingName+"']/jsdl:*/jsdl:URI/text()")) {
                // try to remove input
                boolean input = true;
                String inputTaskName = StagedTask.name(m_jobName, dataStagingName, input);
                workflow.remove(inputTaskName);
                // try to remove output
                boolean notInput = false;
                String outputTaskName = StagedTask.name(m_jobName, dataStagingName, notInput);
                workflow.remove(outputTaskName);
            }
        }
    }
}
