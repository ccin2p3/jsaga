package integration.abstracts;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.command.GraphGenerator;
import fr.in2p3.jsaga.impl.job.description.AbstractJobDescriptionImpl;
import fr.in2p3.jsaga.jobcollection.*;
import org.ogf.saga.job.Job;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.Task;
import org.w3c.dom.Document;

import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractJobCollectionTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractJobCollectionTest extends AbstractXMLTestCase {
    private static final boolean CLEANUP = true;
    private ResourcesLoader m_resources;
    private JobCollectionManager m_manager;

    public AbstractJobCollectionTest() throws Exception {
        super();
        
        // SAGA bootstrap
        System.setProperty("saga.factory", "fr.in2p3.jsaga.impl.SagaFactoryImpl");
        // configure JSAGA engine
        System.setProperty("debug", "true");
        EngineProperties.setProperty(EngineProperties.JSAGA_UNIVERSE, "../test/resources/jobcollection/jsaga-universe.xml");
        EngineProperties.setProperty(EngineProperties.JSAGA_UNIVERSE_ENABLE_CACHE, "false");
        EngineProperties.setProperty(EngineProperties.JOB_CONTROL_CHECK_AVAILABILITY, "false");

        // set class attributes
        Session session = SessionFactory.createSession();
        m_manager = JobCollectionFactory.createJobCollectionManager(session);
    }

    protected void setUp() throws Exception {
        super.setUp();

        // set resources loader
        m_resources = new ResourcesLoader(this.getName());
    }

    protected void checkLanguage(String language, String jcDescFilename) throws Exception {
        InputStream jcDescStream = m_resources.getInputStreamByFileName(jcDescFilename);

        // get job description
        JobCollectionDescription jcDesc = JobCollectionFactory.createJobCollectionDescription(language, jcDescStream);
        InputStream expectedStream = m_resources.getInputStreamByPropertyName(ResourcesLoader.EXPECTED_TRANSLATED);
        assertXMLSimilarDetailed(expectedStream, jcDesc.getAsDocument());
    }

    protected void checkPreprocess() throws Exception {
        InputStream jcDescStream = m_resources.getInputStreamByPropertyName(ResourcesLoader.JOB);
        InputStream resourcesStream = m_resources.getInputStreamByPropertyName(ResourcesLoader.RESOURCES);

        // preprocess and split job collection
        JobCollectionDescription jcDesc = JobCollectionFactory.createJobCollectionDescription("JSDL", jcDescStream);
        JobCollection jc = m_manager.createJobCollection(jcDesc, CLEANUP);
        Task[] jobs = jc.getTasks();

        // check splitted job
        Job thirdJob = findJob(jobs, 3);
        AbstractJobDescriptionImpl splittedJobDesc = (AbstractJobDescriptionImpl) thirdJob.getJobDescription();
        InputStream expectedSplitted = m_resources.getInputStreamByPropertyName(ResourcesLoader.EXPECTED_SPLITTED);
        assertXMLSimilarDetailed(expectedSplitted, splittedJobDesc.getAsDocument());

        // check allocated job
        jc.allocateResources(resourcesStream);
        int nbTests = 0;
        for (int i=0; i<jobs.length; i++) {
            Job job = (Job) jobs[i];
            String jobName = job.getJobDescription().getAttribute("JobName");
            if ("MYJOB_1".equals(jobName) || "MYJOB_2".equals(jobName) || "MYJOB_3".equals(jobName)) {
                AbstractJobDescriptionImpl allocatedJobDesc = (AbstractJobDescriptionImpl) job.getJobDescription();
                InputStream expectedAllocated = m_resources.getInputStreamByPropertyName(ResourcesLoader.EXPECTED_ALLOCATED+jobName);
                assertXMLSimilarDetailed(jobName, expectedAllocated, allocatedJobDesc.getAsDocument());
                nbTests++;
            }
        }
        assertEquals(3, nbTests);

        // check staging graph
        Document xmlStatus = jc.getStatesAsXML();
        InputStream expectedStaging = m_resources.getInputStreamByPropertyName(ResourcesLoader.EXPECTED_STAGING);
        assertXMLSimilarDetailed(expectedStaging, xmlStatus);

        // graphviz
        if (true) {
            GraphGenerator generator = new GraphGenerator(jc.getJobCollectionName(), xmlStatus);
            generator.generateStatusGraph();
            generator.generateStagingGraph();
        }
    }
    private static Job findJob(Task[] jobs, int indice) throws Exception {
        for (int i=0; i<jobs.length; i++) {
            Job job = (Job) jobs[i];
            String jobName = job.getJobDescription().getAttribute("JobName");
            if (jobName.endsWith("_"+indice)) {
                return job;
            }
        }
        fail("Job not found: "+indice);
        return null;
    }
}
