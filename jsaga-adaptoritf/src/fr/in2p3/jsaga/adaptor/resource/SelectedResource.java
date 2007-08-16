package fr.in2p3.jsaga.adaptor.resource;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SelectedResource
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SelectedResource extends SelectedGrid {
    // required
    public String resource;

    // optional: SE that can be reached from worker (preferably via shareFS, else preferably close to the worker)
    public String intermediary;

    // optional: default can be 1 or unbounded
    public int nbSlots;

    // optional: default = (size-pos)/size      (0.0 < x < 1.0)
    public float score;
}
