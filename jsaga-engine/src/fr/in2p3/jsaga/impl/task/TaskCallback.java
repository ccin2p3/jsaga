package fr.in2p3.jsaga.impl.task;

import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskCallback
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   11 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface TaskCallback<E> {
    public void setState(State state);
    public void setResult(E result);
    public void setException(org.ogf.saga.error.Exception exception);
}
