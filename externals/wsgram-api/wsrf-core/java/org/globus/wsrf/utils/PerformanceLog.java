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
package org.globus.wsrf.utils;

import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.types.profiling.Timestamp;
import org.globus.wsrf.types.profiling.TimestampType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PerformanceLog {

    private ThreadLocal local = new ThreadLocal();
    private boolean fullEnabled = false;
    private boolean shortEnabled = false;
    private Log logger;

    public PerformanceLog(String loggerName) {
        this.logger = LogFactory.getLog(loggerName);

        if (logger.isDebugEnabled()) {
            if (
                "full".equalsIgnoreCase(
                        System.getProperty("org.globus.wsrf.performance.log")
                    )
            ) {
                this.fullEnabled = true;
            } else {
                this.shortEnabled = true;
            }
        }
    }

    public boolean enabled() {
        return (this.fullEnabled || this.shortEnabled);
    }

    public void start() {
        if (!enabled()) {
            return;
        }

        local.set(new Long(System.currentTimeMillis()));
    }

    public void stop(String operation) {
        if (!enabled()) {
            return;
        }

        long stop = System.currentTimeMillis();
        Long startLong = (Long) local.get();

        if (startLong == null) {
            return;
        }

        long start = startLong.longValue();
        String threadName = Thread.currentThread().getName();

        if (this.shortEnabled) {
            logger.debug(
                "[" + operation + "][" + threadName + "]" + "[" +
                Long.toString(stop - start) + "]"
            );
        } else {
            TimestampType timestampType = new TimestampType();
            Timestamp timestamp = new Timestamp();
            timestampType.setTimestamp(timestamp);
            timestamp.setStartTime(start);
            timestamp.setEndTime(stop);
            timestamp.setThreadID(threadName);
            timestamp.setOperation(operation);

            try {
                logger.debug(ObjectSerializer.toString(timestampType));
            } catch (Exception e) {
                logger.error("Serialization error", e);
            }
        }
    }
}
