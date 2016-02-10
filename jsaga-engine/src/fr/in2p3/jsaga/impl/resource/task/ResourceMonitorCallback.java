package fr.in2p3.jsaga.impl.resource.task;

import org.ogf.saga.resource.task.State;

public interface ResourceMonitorCallback {
    public void setState(State state, String stateDetail);

}
