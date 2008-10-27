package integration.abstracts;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.jobcollection.*;
import junit.framework.TestCase;
import org.ogf.saga.url.URL;
import org.ogf.saga.job.Job;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;
import org.ogf.saga.task.WaitMode;

import java.io.*;

import integration.SubmitTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractSubmitTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   22 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractSubmitTest extends TestCase {
    private static final boolean CLEANUP = true;
    private JobCollectionManager m_manager;
    private ClassLoader m_resources;

    public AbstractSubmitTest() throws Exception {
        super();

        // configure JSAGA engine
        System.setProperty("debug", "true");
        EngineProperties.setProperty(EngineProperties.JSAGA_UNIVERSE, "../test/resources/submit/jsaga-universe.xml");
        EngineProperties.setProperty(EngineProperties.JSAGA_UNIVERSE_ENABLE_CACHE, "false");
        EngineProperties.setProperty(EngineProperties.JOB_CONTROL_CHECK_AVAILABILITY, "false");

        // set class attributes
        Session session = SessionFactory.createSession();
        m_manager = JobCollectionFactory.createJobCollectionManager(session);
        m_resources = SubmitTest.class.getClassLoader();
    }

    protected void checkSubmit(URL[] resources) throws Exception {
        String jcDescFilename = "submit/"+this.getName()+".xml";
        InputStream jcDescStream = m_resources.getResourceAsStream(jcDescFilename);

        // run job collection
        JobCollectionDescription jcDesc = JobCollectionFactory.createJobCollectionDescription("JSDL", jcDescStream);
        JobCollection jc = m_manager.createJobCollection(jcDesc, CLEANUP);
        jc.run();
        Thread.currentThread().sleep(100);
        jc.allocateResources(resources);

        // check status
        while (jc.size() > 0) {
            Job job = (Job) jc.waitFor(WaitMode.ALL);
            dump("stdout", job.getStdout(), System.out);
            dump("stderr", job.getStderr(), System.err);
            switch(job.getState()) {
                case FAILED: job.rethrow(); break;
                default:     assertEquals(State.DONE, job.getState()); break;
            }
        }
    }

    /////////////////////////////////////////// private methods ///////////////////////////////////////////

    private void dump(String header, InputStream in, PrintStream out) throws IOException {
        out.println("*** BEGIN OF "+header+" ***");
        byte[] buffer = new byte[8192]; //fixme: this is a workaround to a bug on stdout with StreamableJobInteractiveSet
        for (int len; (len=in.read(buffer))>-1; ) {
            out.write(buffer, 0, len);
        }
        out.println("***  END  OF "+header+" ***");
    }
}
