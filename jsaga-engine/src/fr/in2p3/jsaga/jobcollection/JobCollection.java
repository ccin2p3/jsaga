package fr.in2p3.jsaga.jobcollection;

import org.ogf.saga.task.TaskContainer;

import java.io.File;

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
public interface JobCollection extends TaskContainer {
    public void allocateResources(File resourcesFile) throws Exception;
}
