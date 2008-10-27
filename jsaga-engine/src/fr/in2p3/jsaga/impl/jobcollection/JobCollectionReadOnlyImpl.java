package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.engine.schema.jsdl.extension.ResourceSelection;
import fr.in2p3.jsaga.engine.workflow.WorkflowImpl;
import fr.in2p3.jsaga.jobcollection.JobCollection;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;

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
    public JobCollectionReadOnlyImpl(String collectionName) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(null, collectionName);
        m_jobCollectionName = collectionName;
    }

    public String getJobCollectionName() {
        return m_jobCollectionName;
    }

    public void allocateResources(File resourcesFile) throws Exception {
        throw new NotImplementedException("Not implemented yet...");
    }

    public void allocateResources(InputStream resourcesStream) throws Exception {
        throw new NotImplementedException("Not implemented yet...");
    }

    public void allocateResources(URL[] resourceUrls) throws Exception {
        throw new NotImplementedException("Not implemented yet...");
    }

    public void allocateResources(ResourceSelection resources) throws Exception {
        throw new NotImplementedException("Not implemented yet...");
    }

    public void cleanup() throws NoSuccessException {
        try {
            JobCollectionCleaner cleaner = new JobCollectionCleaner(m_session, m_jobCollectionName);
            cleaner.cleanup();
        } catch(Exception e) {
            throw new NoSuccessException("Failed to cleanup job collection: "+m_jobCollectionName, e);
        }
    }

    /** override super.getStatesAsXML() */
    public Document getStatesAsXML() throws NotImplementedException, TimeoutException, NoSuccessException {
        File statusFile = JobCollectionImpl.statusFile(m_jobCollectionName);
        if (statusFile.exists()) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                return factory.newDocumentBuilder().parse(statusFile);
            } catch (Exception e) {
                throw new NoSuccessException(e);
            }
        } else {
            throw new NoSuccessException("Status not found for job collection: "+m_jobCollectionName+". Please check if this collection exists.");
        }
    }
}
