package fr.in2p3.jsaga.adaptor.data;


/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   IrodsDataAdaptor
 * Author: Pascal Calvat (pcalvat@cc.in2p3.fr)
 * Date:   13 may 2008
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class  IrodsDataAdaptor extends IrodsDataAdaptorAbstract {

    public String getType() {
        return "irods";
    }

    protected boolean isClassic(){
        return false;
    }        
}
