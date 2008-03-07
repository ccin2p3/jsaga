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
package org.globus.wsrf.container.usage;

import org.globus.usage.packets.IPTimeMonitorPacket;
import org.globus.usage.packets.CustomByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContainerUsageBasePacket extends IPTimeMonitorPacket {

    private static Log logger =
        LogFactory.getLog(ContainerUsageBasePacket.class.getName());

    public static final short UNKNOWN = 0;

    public static final short STANDALONE_CONTAINER = 1;
    public static final short SERVLET_CONTAINER = 2;

    public static final short COMPONENT_CODE = 3;
    public static final short PACKET_VERSION = 1;

    private int containerID;
    private short containerType;
    private short eventType;

    public ContainerUsageBasePacket() {
    }

    public ContainerUsageBasePacket(short eventType) {
        setTimestamp(System.currentTimeMillis());
        setComponentCode(COMPONENT_CODE);
        setPacketVersion(PACKET_VERSION);
        setEventType(eventType);
    }

    public void setContainerID(int id) {
        this.containerID = id;
    }

    public int getContainerID() {
        return this.containerID;
    }

    public void setContainerType(short type) {
        this.containerType = type;
    }

    public short getContainerType() {
        return this.containerType;
    }

    protected void setEventType(short type) {
        this.eventType = type;
    }

    public short getEventType() {
        return this.eventType;
    }

    public void packCustomFields(CustomByteBuffer buf) {
        super.packCustomFields(buf);

        buf.putInt(this.containerID);
        buf.putShort(this.containerType);
        buf.putShort(this.eventType);
    }
    
    public void unpackCustomFields(CustomByteBuffer buf) {
        super.unpackCustomFields(buf);

        setContainerID(buf.getInt());
        setContainerType(buf.getShort());
        setEventType(buf.getShort());
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append(", container id: " + getContainerID());
        buf.append(", container type: " + getContainerType());
        buf.append(", event type: " + getEventType());
        return buf.toString();
    }
}
