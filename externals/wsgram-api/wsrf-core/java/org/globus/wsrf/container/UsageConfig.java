/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.container;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.IOException;
import java.net.InetAddress;

import org.globus.wsrf.container.usage.ContainerUsageBasePacket;
import org.globus.wsrf.container.usage.ContainerUsageStartPacket;
import org.globus.wsrf.container.usage.ContainerUsageStopPacket;

import org.apache.axis.deployment.wsdd.WSDDService;

public class UsageConfig {
    
    private static final String USAGE_STATISTICS_TARGETS =
        "usageStatisticsTargets";
    
    private static short containerType = ContainerUsageBasePacket.UNKNOWN;

    private ServiceManager manager;
    private List targetList;
    
    UsageConfig(ServiceManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException();
        }
        this.manager = manager;

        String targets = manager.getOption(USAGE_STATISTICS_TARGETS);
        init(targets);
    }

    UsageConfig(ServiceManager manager, String targets) {
        if (manager == null) {
            throw new IllegalArgumentException();
        }
        this.manager = manager;
        init(targets);
    }

    protected void init(String targets) {
        this.targetList = new ArrayList();
        if (targets != null) {
            StringTokenizer tokens = new StringTokenizer(targets);
            while(tokens.hasMoreTokens()) {
                this.targetList.add(tokens.nextToken());
            }
        }
    }

    /**
     * Returns UsageConfig of ServiceManager. The ServiceManager is obtained
     * based on the lookup of AxisEngine associated with the current
     * MessageContext set on the thread. Returns null if unable to get
     * the ServiceManager (MessageContext is not set on the thread).
     */
    public static UsageConfig getUsageConfig() {
        ServiceManager manager = ServiceManager.getCurrentServiceManager();
        return (manager != null) ?
            manager.getUsageConfig() :
            null;
    }
    
    public boolean hasTargets() {
        return !this.targetList.isEmpty();
    }

    public List getTargets() {
        return this.targetList;
    }
    
    public void addTarget(String target) {
        this.targetList.add(target);
    }
    
    public void removeTarget(String target) {
        this.targetList.remove(target);
    }

    public void clearTargets() {
        this.targetList.clear();
    }

    // only container stuff should call these

    static void setContainerType(short type) {
        // set it only once
        if (containerType == ContainerUsageBasePacket.UNKNOWN) {
            containerType = type;
        }
    }

    static short getContainerType() {
        return containerType;
    }

    public InetAddress getContainerHost() {
        try {
            return InetAddress.getByName(
                 ServiceHost.getHost(this.manager.getAxisEngine()));
        } catch (IOException e) {
            try {
                return InetAddress.getLocalHost();
            } catch (IOException ee) {
                return null;
            }
        }
    }

    void sendStartPacket() {
        WSDDService [] services = this.manager.getServices();
        StringBuffer buf = new StringBuffer();
        for (int i=0;i<services.length;i++) {
            String serviceName = 
                services[i].getQName().getLocalPart();
            buf.append(serviceName);
            if (i != services.length - 1) {
                buf.append(",");
            }
        }

        ContainerUsageStartPacket packet = new ContainerUsageStartPacket();
        packet.setHostIP(getContainerHost());
        packet.setContainerID(this.manager.hashCode());
        packet.setContainerType(getContainerType());
        packet.setServiceList(buf.toString());
        packet.sendPacket(getTargets());
    }
    
    void sendStopPacket() {
        ContainerUsageStopPacket packet = new ContainerUsageStopPacket();
        packet.setHostIP(getContainerHost());
        packet.setContainerID(this.manager.hashCode());
        packet.setContainerType(getContainerType());
        packet.sendPacket(getTargets());
    }
    
}
