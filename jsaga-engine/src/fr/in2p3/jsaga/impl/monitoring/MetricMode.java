package fr.in2p3.jsaga.impl.monitoring;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MetricMode
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public enum MetricMode {
    ReadOnly(1),
    ReadWrite(2),
    Final(3);

    private int value;

    MetricMode(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
