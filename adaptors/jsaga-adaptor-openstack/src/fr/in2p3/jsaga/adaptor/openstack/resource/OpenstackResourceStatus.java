package fr.in2p3.jsaga.adaptor.openstack.resource;

import org.ogf.saga.resource.task.State;
import org.openstack4j.model.compute.Server.Status;

import fr.in2p3.jsaga.adaptor.resource.ResourceStatus;

public class OpenstackResourceStatus extends ResourceStatus {

    public OpenstackResourceStatus(Status nativeStatus) {
        super(null, nativeStatus, nativeStatus.name());
    }

    @Override
    protected String getModel() {
        return "OS";
    }
    
    @Override
    public State getSagaState() {
        Status status = (Status)m_nativeStateCode;
        if (status.equals(Status.ACTIVE)) {
            return State.ACTIVE;
        } else if (status.equals(Status.UNKNOWN) || status.equals(Status.UNRECOGNIZED)) {
            return State.UNKNOWN;
        } else if (status.equals(Status.DELETED) || status.equals(Status.SHUTOFF) || status.equals(Status.STOPPED)) {
            return State.CLOSED;
        } else if (status.equals(Status.ERROR)) {
            return State.FAILED;
        } else { 
            // BUILD | REBUILD | SUSPENDED | PAUSED | RESIZE | VERIFY_RESIZE | REVERT_RESIZE |
            // PASSWORD | REBOOT | HARD_REBOOT | MIGRATING
            return State.PENDING;
        }
    }

}
