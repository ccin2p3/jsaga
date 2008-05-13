package fr.in2p3.jsaga.jobcollection;

import org.ogf.saga.SagaObject;
import org.w3c.dom.Document;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionDescription
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface JobCollectionDescription extends SagaObject {
    public String getCollectionName();
    public Document getAsDocument();
}
