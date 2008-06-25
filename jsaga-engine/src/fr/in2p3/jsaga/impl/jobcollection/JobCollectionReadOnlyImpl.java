package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.ResourceSelection;
import fr.in2p3.jsaga.engine.workflow.WorkflowImpl;
import fr.in2p3.jsaga.jobcollection.JobCollection;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionReadOnlyImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionReadOnlyImpl extends WorkflowImpl implements JobCollection {
    private String m_jobCollectionName;

    /** constructor */
    public JobCollectionReadOnlyImpl(String collectionName) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(null, collectionName);
        m_jobCollectionName = collectionName;
    }

    public String getJobCollectionName() {
        return m_jobCollectionName;
    }

    public void allocateResources(File resourcesFile) throws Exception {
        throw new NotImplemented("Not implemented yet...");
    }

    public void allocateResources(InputStream resourcesStream) throws Exception {
        throw new NotImplemented("Not implemented yet...");
    }

    public void allocateResources(URL[] resourceUrls) throws Exception {
        throw new NotImplemented("Not implemented yet...");
    }

    public void allocateResources(ResourceSelection resources) throws Exception {
        throw new NotImplemented("Not implemented yet...");
    }

    public void cleanup() throws NoSuccess {
        try {
            JobCollectionCleaner cleaner = new JobCollectionCleaner(m_session, m_jobCollectionName);
            cleaner.cleanup();
        } catch(Exception e) {
            throw new NoSuccess("Failed to cleanup job collection: "+m_jobCollectionName, e);
        }
    }

    /** override super.getStatesAsXML() */
    public Document getStatesAsXML() throws NotImplemented, Timeout, NoSuccess {
        File statusFile = JobCollectionImpl.statusFile(m_jobCollectionName);
        if (statusFile.exists()) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                return factory.newDocumentBuilder().parse(statusFile);
            } catch (Exception e) {
                throw new NoSuccess(e);
            }
        } else {
            throw new NoSuccess("Status not found for job collection: "+m_jobCollectionName+". Please check if this collection exists.");
        }
    }
}
