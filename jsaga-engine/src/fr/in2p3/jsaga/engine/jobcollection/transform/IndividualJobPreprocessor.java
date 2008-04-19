package fr.in2p3.jsaga.engine.jobcollection.transform;

import org.w3c.dom.Document;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IndividualJobPreprocessor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IndividualJobPreprocessor {
    public static Document preprocess(Document jobDesc) {
        //todo: insert <jobService> configuration into <ext:Job>
        return jobDesc;
    }
}
