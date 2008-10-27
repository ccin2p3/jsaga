package fr.in2p3.jsaga.jobcollection;

import fr.in2p3.jsaga.engine.schema.jsdl.extension.ResourceSelection;
import fr.in2p3.jsaga.workflow.Workflow;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.url.URL;

import java.io.File;
import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollection
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobCollection extends Workflow {
    public String getJobCollectionName();
    public void allocateResources(File resourcesFile) throws Exception;
    public void allocateResources(InputStream resourcesStream) throws Exception;
    public void allocateResources(URL[] resourceUrls) throws Exception;
    public void allocateResources(ResourceSelection resources) throws Exception;
    public void cleanup() throws NoSuccessException;
}
