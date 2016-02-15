package fr.in2p3.jsaga.adaptor.openstack.resource;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.log4j.Logger;
import org.ogf.saga.resource.task.State;
import org.openstack4j.model.compute.Address;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.Server.Status;

import fr.in2p3.jsaga.adaptor.resource.ResourceStatus;

public class OpenstackResourceStatus extends ResourceStatus {

    private Server m_server;
    protected Logger m_logger = Logger.getLogger(OpenstackResourceStatus.class);
    
    public OpenstackResourceStatus(Server server) {
        super(null, server.getStatus(), server.getStatus().name());
        m_server = server;
    }

    @Override
    protected String getModel() {
        return "OS";
    }
    
    @Override
    public State getSagaState() {
        Status status = (Status)m_nativeStateCode;
        if (status.equals(Status.ACTIVE)) {
            if (!"active".equals(m_server.getVmState())) {
                return State.PENDING;
            }
            // telnet the SSH server
            for (List<? extends Address> addrs: m_server.getAddresses().getAddresses().values()) {
                for (Address addr: addrs) {
                    TelnetClient tc = new TelnetClient();
                    tc.setConnectTimeout(1000); // millis
                    try {
                        m_logger.debug("telnet " + addr.getAddr());
                        // TODO port 22 configurable?
                        tc.connect(InetAddress.getByName(addr.getAddr()), 22);
                        // vmState can be "building" when status is "ACTIVE"...
                        // vmState should be "active" for SAGA state to become "ACTIVE
                        return State.ACTIVE;
                    } catch (Exception e) {
                        m_logger.debug("SSHD not ready: " + e.getMessage());
                    } finally {
                        try {
                            tc.disconnect();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            return State.PENDING;
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
