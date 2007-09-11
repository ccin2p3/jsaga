package fr.in2p3.jsaga.adaptor.resource;

import fr.in2p3.jsaga.adaptor.language.JobDescriptionContainer;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GridSelectionAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface GridSelectionAdaptor {
    public SelectedGrid select(JobDescriptionContainer desc, String[] args)
        throws NoMatch, AmbiguousMatch;
}
