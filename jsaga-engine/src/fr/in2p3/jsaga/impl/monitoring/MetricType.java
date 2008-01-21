package fr.in2p3.jsaga.impl.monitoring;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MetricType
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public enum MetricType {
    String (1),
    Int (2),
    Enum (3),
    Float (4),
    Bool (5),
    Time (6),
    Trigger (7);

    private int value;

    MetricType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
