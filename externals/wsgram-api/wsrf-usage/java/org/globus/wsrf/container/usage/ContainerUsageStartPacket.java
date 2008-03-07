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

import org.globus.usage.packets.CustomByteBuffer;

public class ContainerUsageStartPacket extends ContainerUsageBasePacket {
    
    public static final short START_EVENT = 1;
    
    private String list;
    
    public ContainerUsageStartPacket() {
        super(START_EVENT);
    }
    
    public void setServiceList(String list) {
        this.list = list;
    }
    
    public String getServiceList() {
        return this.list;
    }

    public void packCustomFields(CustomByteBuffer buf) {
        super.packCustomFields(buf);
     
        // write service list
        byte [] data = this.list.getBytes();
        buf.putShort((short)data.length);
        int maxLen = Math.min(data.length, buf.remaining());
        buf.put(data, 0, maxLen);
    }
    
    public void unpackCustomFields(CustomByteBuffer buf) {
        super.unpackCustomFields(buf);
        
        // read service list
        short len = buf.getShort();
        int maxLen = Math.min(len, buf.remaining());
        byte [] data = new byte[maxLen];
        buf.get(data);
        this.list = new String(data);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
        buf.append(", services: " + getServiceList());
        return buf.toString();
    }
}
