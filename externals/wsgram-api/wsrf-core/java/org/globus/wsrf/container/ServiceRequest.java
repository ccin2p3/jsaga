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

import java.net.ServerSocket;
import java.net.Socket;


/**
 * Requests constructed by dispatcher and put on queue to be read by
 * worker threads.
 * @see ServiceRequestQueue
 * @see ServiceThread
 */
public class ServiceRequest {
    private Socket socket;
    private ServerSocket serverSocket;

    public ServiceRequest(
        Socket socket,
        ServerSocket serverSocket
    ) {
        this.socket = socket;
        this.serverSocket = serverSocket;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }
}
