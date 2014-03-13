package fr.in2p3.jsaga.adaptor.data;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   GsiftpDPMDataAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   2 juil. 2009
 * ***************************************************
 * Description:                                      */
/**
 * workaround for DPM
 */
public class GsiftpDPMDataAdaptor extends Gsiftp2DataAdaptor {

    public String getType() {
        return "gsiftp-dpm";
    }

}
