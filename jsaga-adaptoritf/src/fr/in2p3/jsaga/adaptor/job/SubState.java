package fr.in2p3.jsaga.adaptor.job;

import org.ogf.saga.task.State;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SubState
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SubState {
    private static final int SUBMIT=0;  public static final SubState SUBMITTED=new SubState(SUBMIT);
    private static final int RUNN_Q=1;  public static final SubState RUNNING_QUEUED=new SubState(RUNN_Q);
    private static final int RUNN_A=2;  public static final SubState RUNNING_ACTIVE=new SubState(RUNN_A);
    private static final int _DONE_=3;  public static final SubState DONE=new SubState(_DONE_);
    private static final int CANCEL=4;  public static final SubState CANCELED=new SubState(CANCEL);
    private static final int FAIL_E=5;  public static final SubState FAILED_ERROR=new SubState(FAIL_E);
    private static final int FAIL_A=6;  public static final SubState FAILED_ABORTED=new SubState(FAIL_A);
    private static final int SUSP_Q=7;  public static final SubState SUSPENDED_QUEUED=new SubState(SUSP_Q);
    private static final int SUSP_A=8;  public static final SubState SUSPENDED_ACTIVE=new SubState(SUSP_A);

    private int value;

    SubState(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this enumeration literal.
     * @return the value.
     */
    public int getValue() {
        return value;
    }

    public State toSagaState() {
        switch(value) {
            case SUBMIT:
                return State.NEW;
            case RUNN_Q:
                return State.RUNNING;
            case RUNN_A:
                return State.RUNNING;
            case _DONE_:
                return State.DONE;
            case CANCEL:
                return State.CANCELED;
            case FAIL_E:
                return State.FAILED;
            case FAIL_A:
                return State.FAILED;
            case SUSP_Q:
                return State.SUSPENDED;
            case SUSP_A:
                return State.SUSPENDED;
            default:
                return null;
        }
    }
}
