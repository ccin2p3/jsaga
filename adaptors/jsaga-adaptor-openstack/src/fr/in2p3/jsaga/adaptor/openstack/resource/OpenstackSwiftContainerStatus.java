package fr.in2p3.jsaga.adaptor.openstack.resource;

import org.ogf.saga.resource.task.State;
import org.openstack4j.model.storage.object.SwiftContainer;

import fr.in2p3.jsaga.adaptor.resource.ResourceStatus;

public class OpenstackSwiftContainerStatus extends ResourceStatus {

    public OpenstackSwiftContainerStatus(SwiftContainer sc) {
        super(null, null);
    }

    @Override
    public State getSagaState() {
        return State.ACTIVE;
    }

    @Override
    protected String getModel() {
        return "OS";
    }

}
