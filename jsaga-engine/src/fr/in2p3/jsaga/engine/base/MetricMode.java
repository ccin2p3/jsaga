package fr.in2p3.jsaga.engine.base;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MetricMode
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MetricMode {
    public static final int READONLY_TYPE = 1;
    public static final int READWRITE_TYPE = 2;
    public static final int FINAL_TYPE = 3;
    public static final MetricMode READONLY = new MetricMode(READONLY_TYPE, "ReadOnly");
    public static final MetricMode READWRITE = new MetricMode(READWRITE_TYPE, "ReadWrite");
    public static final MetricMode FINAL = new MetricMode(FINAL_TYPE, "Final");

    private int value;
    private String label;

    MetricMode(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return label;
    }
}
