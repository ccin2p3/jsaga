package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.engine.jobcollection.preprocess.JobPreprocessor;
import fr.in2p3.jsaga.impl.task.TaskContainerImpl;
import fr.in2p3.jsaga.jobcollection.JobCollection;
import fr.in2p3.jsaga.jobcollection.JobCollectionDescription;
import org.ogf.saga.SagaObject;
import org.ogf.saga.session.Session;

import java.io.*;

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
    private JobCollectionDescription m_description;

    /** constructor */
    public JobCollectionImpl(Session session, JobCollectionDescription description) {
        super(session);
        m_description = description;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        JobCollectionImpl clone = (JobCollectionImpl) super.clone();
        clone.m_description = m_description;
        return clone;
    }

    public void allocateResources(File resourcesFile) throws Exception {
        // read resources file
        byte[] resources = new byte[(int) resourcesFile.length()];
        InputStream in = new FileInputStream(resourcesFile);
        in.read(resources);
        in.close();
        // preprocess
        JobPreprocessor preprocessor = new JobPreprocessor(m_description.getJSDL());
        preprocessor.preprocess(resources);
        preprocessor.dump();
    }
}
