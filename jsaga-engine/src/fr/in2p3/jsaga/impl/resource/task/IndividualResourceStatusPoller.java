package fr.in2p3.jsaga.impl.resource.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.ResourceStatus;

public class IndividualResourceStatusPoller implements Runnable {

    private static Logger s_logger = Logger.getLogger(IndividualResourceStatusPoller.class);
    private ResourceAdaptor m_adaptor;
    private ResourceStatusPollerTask m_pollerTask;
    // a map but only one entry is used???
    protected final Map<String,ResourceMonitorCallback> m_subscribedResources;
    
    public IndividualResourceStatusPoller(ResourceAdaptor adaptor) {
        m_adaptor = adaptor;
        m_subscribedResources = new HashMap<String,ResourceMonitorCallback>();
    }

    public void subscribeResource(String nativeResourceId, ResourceMonitorCallback callback) {
        synchronized(m_subscribedResources) {
            boolean toBeStarted = m_subscribedResources.isEmpty();

            // subscribe job
            m_subscribedResources.put(nativeResourceId, callback);

            // may start timer
            if (toBeStarted) {
                m_pollerTask = new ResourceStatusPollerTask(this);
                m_pollerTask.start();
            }
        }
    }

    public void unsubscribeResource(String nativeResourceId) {
        synchronized(m_subscribedResources) {
            // unsubscribe job
            m_subscribedResources.remove(nativeResourceId);

            // may stop timer
            if (m_subscribedResources.isEmpty() && m_pollerTask!=null) {
                m_pollerTask.stop();
                m_pollerTask = null;
            }
        }
    }

    @Override
    public void run() {
        //TODO: should be multi-threaded
        Set<Entry<String, ResourceMonitorCallback>> entries;
        synchronized(m_subscribedResources) {
            entries = m_subscribedResources.entrySet();
        }
        for (Entry<String, ResourceMonitorCallback> entry : entries) {
            String nativeResourceId = (String) entry.getKey();
            ResourceMonitorCallback callback = (ResourceMonitorCallback) entry.getValue();
            try {
                ResourceStatus status = m_adaptor.getResourceStatus(nativeResourceId);
                callback.setState(status.getSagaState(), status.getStateDetail());
            } catch (Exception e) {
                s_logger.warn("Failed to get status for resource: "+ nativeResourceId, e);
            }
        }
    }
}
