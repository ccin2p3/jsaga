package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.evaluator.Evaluator;
import fr.in2p3.jsaga.engine.jobcollection.*;
import fr.in2p3.jsaga.engine.jobcollection.transform.*;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.Resource;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.ResourceSelection;
import fr.in2p3.jsaga.engine.workflow.WorkflowImpl;
import fr.in2p3.jsaga.impl.job.description.XJSDLJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.instance.JobHandle;
import fr.in2p3.jsaga.jobcollection.*;
import org.exolab.castor.xml.Unmarshaller;
import org.ggf.schemas.jsdl.JobDefinition;
import org.ogf.saga.SagaObject;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.Exception;
import java.util.LinkedList;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionImpl extends WorkflowImpl implements JobCollection {
    private String m_jobCollectionName;
    private LinkedList<JobWithStaging> m_unallocatedJobs;

    /** constructor */
    public JobCollectionImpl(Session session, JobCollectionDescription jcDesc, Evaluator evaluator) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        super(session, jcDesc.getCollectionName());
        m_jobCollectionName = jcDesc.getCollectionName();

        // Set base directory
        File baseDir = new File(Base.JSAGA_VAR, "jobs");
        if(!baseDir.exists()) baseDir.mkdir();
        baseDir = new File(baseDir, m_jobCollectionName);
        if(baseDir.exists()) {
            throw new NoSuccess("Collection already exists: "+m_jobCollectionName+", please clean it up first.");
        } else {
            baseDir.mkdir();
        }

        // fill
        JobCollectionFiller filler = new JobCollectionFiller(jcDesc.getAsDocument());
        Document filledJcDesc = filler.getEfectiveJobCollection();
        
        // preprocess
        JobCollectionPreprocessor preprocessor = new JobCollectionPreprocessor(filledJcDesc, m_jobCollectionName);
        Document processedJcDesc = preprocessor.getEffectiveJobCollection();

        // split parametric job
        JobCollectionSplitter splitter = new JobCollectionSplitter(processedJcDesc, evaluator);
        XJSDLJobDescriptionImpl[] jobDescArray = splitter.getIndividualJobArray();
        JobDefinition jobTemplate = splitter.getJobCollectionBean().getJob(0).getJobDefinition();

        // update workflow
        m_unallocatedJobs = new LinkedList<JobWithStaging>();
        for (int i=0; i<jobDescArray.length; i++) {
            // get job name
            String jobName = jobDescArray[i].getJobName();
            // create job handle
            JobHandle jobHandle = new JobHandle(session);

            // update workflow (job running)
            JobRunTaskGenerator jobRun = new JobRunTaskGenerator(jobName, jobTemplate, jobHandle);
            jobRun.updateWorkflow(this);

            // update workflow (job end)
            JobEndTaskGenerator jobEnd = new JobEndTaskGenerator(jobName, jobTemplate);
            jobEnd.updateWorkflow(this);

            // update workflow (pre/post staging)
            DataPrePostStagingTaskGenerator prePostStaging = new DataPrePostStagingTaskGenerator(jobName, jobDescArray[i].getAsDocument());
            prePostStaging.updateWorkflow(this);

            // update task container
            JobWithStaging job = new JobWithStagingImpl(session, jobDescArray[i], jobHandle, this, jobEnd.getTask());
            super.add(job);

            // update list of unallocated jobs
            m_unallocatedJobs.add(job);
        }
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        JobCollectionImpl clone = (JobCollectionImpl) super.clone();
        clone.m_unallocatedJobs = m_unallocatedJobs;
        return clone;
    }

    public String getJobCollectionName() {
        return m_jobCollectionName;
    }

    public void allocateResources(File resourcesFile) throws Exception {
        InputStream resourcesStream = new FileInputStream(resourcesFile);
        this.allocateResources(resourcesStream);
        resourcesStream.close();
    }

    public void allocateResources(InputStream resourcesStream) throws Exception {
        ResourceSelection resources = (ResourceSelection) Unmarshaller.unmarshal(ResourceSelection.class, new InputSource(resourcesStream));
        this.allocateResources(resources);
    }

    public void allocateResources(URL[] resourceUrls) throws Exception {
        ResourceSelection resources = new ResourceSelection();
        for (int i=0; i<resourceUrls.length; i++) {
            Resource resource = new Resource();
            if (resourceUrls[i].getFragment() != null) {
                resource.setGrid(resourceUrls[i].getFragment());
                resourceUrls[i].setFragment(null);
            }
            resource.setId(resourceUrls[i].toString());
            resource.setNbslots(1);
            resources.addResource(resource);
        }
        this.allocateResources(resources);
    }

    public void allocateResources(ResourceSelection resources) throws Exception {
        for (int i=0; resources!=null && i<resources.getResourceCount(); i++) {
            Resource rm = resources.getResource(i);
            for (int slot=0; slot<rm.getNbslots(); slot++) {
                JobWithStaging job = m_unallocatedJobs.removeFirst();
                job.allocate(rm);
            }
        }
    }

    public void cleanup() {
        File baseDir = new File(new File(Base.JSAGA_VAR, "jobs"), m_jobCollectionName);
        if(baseDir.exists()) {
            File[] files = baseDir.listFiles();
            for (int i=0; i<files.length; i++) {
                files[i].delete();
            }
            baseDir.delete();
        }        
    }

    /** override super.getStatesAsXML() */
    public synchronized Document getStatesAsXML() throws NotImplemented, Timeout, NoSuccess {
        Document status = super.getStatesAsXML();
        try {
            OutputStream out = new FileOutputStream(statusFile(m_jobCollectionName));
            TransformerFactory.newInstance().newTransformer().transform(
                    new DOMSource(status),
                    new StreamResult(out));
            out.close();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        return status;
    }

    protected static File statusFile(String collectionName) {
        return new File(Base.JSAGA_VAR, "jobs/"+collectionName+"/_status.xml");
    }
}
