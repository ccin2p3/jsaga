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
    private static final int CREATE=10; public static final SubState NEW_CREATED=new SubState(CREATE, "NEW_CREATED");

    private static final int R__PRE=20; public static final SubState RUNNING_PRE_STAGING =new SubState(R__PRE, "RUNNING_PRE_STAGING");
    private static final int RUNN_S=21; public static final SubState RUNNING_SUBMITTED=new SubState(RUNN_S, "RUNNING_SUBMITTED");
    private static final int RUNN_Q=22; public static final SubState RUNNING_QUEUED=new SubState(RUNN_Q, "RUNNING_QUEUED");
    private static final int RUNN_A=23; public static final SubState RUNNING_ACTIVE=new SubState(RUNN_A, "RUNNING_ACTIVE");
    private static final int R_POST=24; public static final SubState RUNNING_POST_STAGING=new SubState(R_POST, "RUNNING_POST_STAGING");

    private static final int _DONE_=30; public static final SubState DONE=new SubState(_DONE_, "DONE");
    private static final int CANC_R=39; public static final SubState CANCEL_REQUESTED=new SubState(CANC_R, "CANCEL_REQUESTED");
    private static final int CANCEL=40; public static final SubState CANCELED=new SubState(CANCEL, "CANCELED");
    private static final int FAIL_E=50; public static final SubState FAILED_ERROR=new SubState(FAIL_E, "FAILED_ERROR");
    private static final int FAIL_A=51; public static final SubState FAILED_ABORTED=new SubState(FAIL_A, "FAILED_ABORTED");

    private static final int SUSP_Q=60; public static final SubState SUSPENDED_QUEUED=new SubState(SUSP_Q, "SUSPENDED_QUEUED");
    private static final int SUSP_A=61; public static final SubState SUSPENDED_ACTIVE=new SubState(SUSP_A, "SUSPENDED_ACTIVE");

    private int value;
    private String label;

    SubState(int value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * Returns the integer value of this enumeration literal.
     * @return the value.
     */
    public int getValue() {
        return value;
    }

    public String toString() {
        return label;
    }

    public boolean equals(Object subState) {
        return this.toString().equals(subState.toString());
    }

    public State toSagaState() {
        switch(value) {
            case CREATE:
                return State.NEW;

            case R__PRE:
                return State.RUNNING;
            case RUNN_S:
                return State.RUNNING;
            case RUNN_Q:
                return State.RUNNING;
            case RUNN_A:
                return State.RUNNING;
            case R_POST:
                //fixme: should return RUNNING instead of DONE
                return State.DONE;

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
