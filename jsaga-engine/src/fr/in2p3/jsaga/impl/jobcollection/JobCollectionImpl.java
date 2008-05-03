package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.adaptor.evaluator.Evaluator;
import fr.in2p3.jsaga.engine.jobcollection.transform.*;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.Resource;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.ResourceSelection;
import fr.in2p3.jsaga.impl.job.instance.LateBindedJobImpl;
import fr.in2p3.jsaga.impl.task.TaskContainerImpl;
import fr.in2p3.jsaga.jobcollection.JobCollection;
import fr.in2p3.jsaga.jobcollection.JobCollectionDescription;
import org.exolab.castor.xml.Unmarshaller;
import org.ogf.saga.SagaObject;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.job.*;
import org.ogf.saga.session.Session;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

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
public class JobCollectionImpl extends TaskContainerImpl implements JobCollection {
    private LinkedList<LateBindedJobImpl> m_unallocatedJobs;

    /** constructor */
    public JobCollectionImpl(Session session, JobCollectionDescription jcDesc, Evaluator evaluator) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        super(session);

        // fill
        JobCollectionFiller filler = new JobCollectionFiller(jcDesc.getAsDocument());
        String collectionName = filler.getCollectionName();
        Document filledJcDesc = filler.getEfectiveJobCollection();
        
        // preprocess
        JobCollectionPreprocessor preprocessor = new JobCollectionPreprocessor(filledJcDesc, collectionName);
        byte[] processedJcDesc = preprocessor.getEffectiveJobCollection();

        // split parametric job
        JobCollectionSplitter splitter = new JobCollectionSplitter(processedJcDesc, evaluator);
        JobDescription[] jobDescArray = splitter.getIndividualJobArray();

        // create late binded job service
        JobService service;
        try {
            service = JobFactory.createJobService(session, null);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        }

        // create late binded jobs
        m_unallocatedJobs = new LinkedList<LateBindedJobImpl>();
        for (int i=0; i<jobDescArray.length; i++) {
            LateBindedJobImpl job = (LateBindedJobImpl) service.createJob(jobDescArray[i]);
            m_unallocatedJobs.add(job);
            super.add(job);
        }
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        JobCollectionImpl clone = (JobCollectionImpl) super.clone();
        clone.m_unallocatedJobs = m_unallocatedJobs;
        return clone;
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
                LateBindedJobImpl job = m_unallocatedJobs.removeFirst();
                job.allocate(rm);
            }
        }
    }
}
