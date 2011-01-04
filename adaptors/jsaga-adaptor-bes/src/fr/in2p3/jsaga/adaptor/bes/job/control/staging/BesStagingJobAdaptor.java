package fr.in2p3.jsaga.adaptor.bes.job.control.staging;

import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesStagingJobAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   4 jan 2011
* ***************************************************/

public interface BesStagingJobAdaptor extends StagingJobAdaptorOnePhase {
    public abstract String getDataStagingProtocol();
    public abstract int getDataStagingPort();
    
}
