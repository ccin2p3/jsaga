package fr.in2p3.jsaga.engine.jobcollection;

import fr.in2p3.jsaga.engine.workflow.StartTask;
import fr.in2p3.jsaga.engine.workflow.task.*;
import fr.in2p3.jsaga.helpers.xpath.XJSDLXPathSelector;
import fr.in2p3.jsaga.workflow.Workflow;
import fr.in2p3.jsaga.workflow.WorkflowTask;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.w3c.dom.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataPrePostStagingTaskGenerator
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DataPrePostStagingTaskGenerator {
    private String m_jobName;
    private XJSDLXPathSelector m_selector;

    public DataPrePostStagingTaskGenerator(String jobName, Document jobDesc) throws NotImplemented, BadParameter, NoSuccess {
        m_jobName = jobName;
        m_selector = new XJSDLXPathSelector(jobDesc);
    }

    public void updateWorkflow(Session session, Workflow workflow) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        final boolean keep = true;
        final boolean notKeep = false;

        // input pre-staging
        String inSbxUri = m_selector.getString("/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging[@name='INPUT_SANDBOX']/jsdl:Source/jsdl:URI/text()");
        if (inSbxUri != null) {
            boolean input = true;
            WorkflowTask currentTask = new SourceTask(inSbxUri, input, notKeep);
            workflow.add(currentTask, null, null);
            NodeList stagingList = m_selector.getNodes("/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging[@name!='INPUT_SANDBOX' and jsdl:Source/jsdl:URI/text()]");
            for (int i=0; i<stagingList.getLength(); i++) {
                Element dataStaging = (Element) stagingList.item(i);
                String dataStagingName = m_selector.getString(dataStaging, "@name");
                String overwriteStr = m_selector.getString(dataStaging, "jsdl:CreationFlag/text()");
                boolean overwrite = (overwriteStr!=null && overwriteStr.equalsIgnoreCase("overwrite"));

                String previousTaskName = StartTask.name();
                NodeList stepList = dataStaging.getElementsByTagNameNS("http://www.in2p3.fr/jsdl-extension", "Step");
                if (stepList.getLength() > 0) {
                    // create dummy task
                    Element step = (Element) stepList.item(0);
                    currentTask = new SourceTask(step.getAttribute("uri"), input, keep);
                    for (int j=1; j<stepList.getLength(); j++) {
                        // add task
                        workflow.add(currentTask, previousTaskName, null);
                        previousTaskName = currentTask.getName();
                        // create transfer task
                        step = (Element) stepList.item(j);
                        currentTask = new TransferTask(session, step.getAttribute("uri"), input, overwrite, notKeep);
                    }
                    // add last task
                    workflow.add(currentTask, previousTaskName, inSbxUri);
                    // remove staged task
                    String nextTaskName = StagedTask.name(m_jobName, dataStagingName, input);
                    workflow.remove(nextTaskName);
                }
            }
        }

        // output post-staging
        String outSbxUri = m_selector.getString("/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging[@name='OUTPUT_SANDBOX']/jsdl:Target/jsdl:URI/text()");
        if (outSbxUri != null) {
            boolean notInput = false;
            // remove staged task
            String outputSandboxTaskName = StagedTask.name(m_jobName, "OUTPUT_SANDBOX", notInput);
            workflow.remove(outputSandboxTaskName);
            // add first task
            final boolean alwaysOverwrite = true;  //always overwrite
            WorkflowTask currentTask = new TransferTask(session, outSbxUri, notInput, alwaysOverwrite, notKeep);
            workflow.add(currentTask, null, null);
            NodeList stagingList = m_selector.getNodes("/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:DataStaging[@name!='OUTPUT_SANDBOX' and jsdl:Target/jsdl:URI/text()]");
            for (int i=0; i<stagingList.getLength(); i++) {
                Element dataStaging = (Element) stagingList.item(i);
                String dataStagingName = m_selector.getString(dataStaging, "@name");
                String overwriteStr = m_selector.getString(dataStaging, "jsdl:CreationFlag/text()");
                boolean overwrite = (overwriteStr!=null && overwriteStr.equalsIgnoreCase("overwrite"));

                String previousTaskName = outSbxUri;
                NodeList stepList = dataStaging.getElementsByTagNameNS("http://www.in2p3.fr/jsdl-extension", "Step");
                if (stepList.getLength() > 0) {
                    // create dummy task
                    Element step = (Element) stepList.item(stepList.getLength()-1);
                    currentTask = new SourceTask(step.getAttribute("uri"), notInput, notKeep);
                    for (int j=stepList.getLength()-2; j>=0; j--) {
                        // add task
                        workflow.add(currentTask, previousTaskName, null);
                        previousTaskName = currentTask.getName();
                        // create transfer task
                        step = (Element) stepList.item(j);
                        boolean keepLastOnly = (j==0);
                        currentTask = new TransferTask(session, step.getAttribute("uri"), notInput, overwrite, keepLastOnly);
                    }
                    // add last task
                    String nextTaskName = StagedTask.name(m_jobName, dataStagingName, notInput);
                    workflow.add(currentTask, previousTaskName, nextTaskName);
                }
            }
        }
    }
}
