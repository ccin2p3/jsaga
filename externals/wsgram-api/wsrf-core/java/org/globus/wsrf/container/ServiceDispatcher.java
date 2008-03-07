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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisEngine;
import org.apache.axis.Constants;
import org.apache.axis.MessageContext;
import org.apache.axis.server.AxisServer;
import org.apache.axis.configuration.DirProvider;

import org.globus.tools.DeployConstants;
import org.globus.util.I18n;
import org.globus.wsrf.config.ContainerConfig;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.container.usage.ContainerUsageBasePacket;
import org.globus.wsrf.impl.security.descriptor.ContainerSecurityConfig;

/**
 * Dispatcher reading requests off the socket and putting them into a
 * request queue.
 */
public class ServiceDispatcher implements Runnable {

    static Log logger =
        LogFactory.getLog(ServiceDispatcher.class.getName());

    static I18n i18n = I18n.getI18n(Resources.class.getName());

    private ServerSocket serverSocket;
    private volatile Thread worker = null;
    private volatile boolean stopped = false;
    private Semaphore semaphore = new Semaphore();
    protected ServiceRequestQueue queue;
    protected ServiceThreadPool threadPool;
    protected int numThreads;
    protected int maxThreads;
    protected int highWaterMark;
    protected AxisServer engine;
    protected MessageContext msgContext;

    protected ServiceDispatcher() {}

    public ServiceDispatcher(Map properties) throws Exception {
        // single file configuration
        // this.engine = new AxisServer(new FileProvider(config));

        String configFile =
            (String)properties.get(ServiceContainer.SERVER_CONFIG);
        configFile = (configFile == null) ?
            ContainerConfig.DEFAULT_SERVER_CONFIG : configFile;

        String configProfile =
            (String)properties.get(ContainerConfig.CONFIG_PROFILE);
        if (configProfile != null) {
            configFile = configProfile + "-" + configFile;
        }

        String baseDir = ContainerConfig.getGlobusLocation() +
            File.separator + DeployConstants.CONFIG_BASE_DIR;

        // Initialize engine and message context
        this.engine = new AxisServer(new DirProvider(baseDir, configFile));
        this.msgContext = new MessageContext(this.engine);
        this.msgContext.setProperty(Constants.MC_HOME_DIR,
                                    ServiceThread.getWebRootPath(this.engine));
        this.msgContext.setProperty(Constants.MC_CONFIGPATH,
                                    ServiceThread.getConfigRootPath(this.engine));
        if (configProfile != null) {
            this.msgContext.setProperty(ContainerConfig.CONFIG_PROFILE,
                                        configProfile);
        }

        // Initialize container security config, if file is confgiured
        // off command line
        String containerSecDesc = 
            (String)properties.get(ServiceContainer.SECURITY_DESCRIPTOR_FILE);
        ContainerSecurityConfig config = null;
        if (containerSecDesc != null) {
            config = ContainerSecurityConfig.getConfig(containerSecDesc);
        }
        
        // set container type
        UsageConfig.setContainerType(
                      ContainerUsageBasePacket.STANDALONE_CONTAINER);

        this.numThreads = -1;
    }

    protected void init() throws Exception {
        // if container config is not initialized off command line,
        // this will do it.
        ServiceManager.getServiceManager(this.engine).start(this.msgContext);

        this.queue = new ServiceRequestQueue();
        setupThreadPool();
    }

    protected void setupThreadPool() throws Exception {
        this.threadPool = new ServiceThreadPool(this.queue, this.engine);
    }

    public AxisEngine getAxisEngine() {
        return this.engine;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public void setThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public void run() {
        ContainerConfig config = ContainerConfig.getConfig(this.engine);
        if (numThreads <= 0) {
            try {
                String containerThreads =
                    config.getOption(ContainerConfig.CONTAINER_THREADS);

                if (containerThreads != null) {
                    numThreads = Integer.parseInt(containerThreads);
                }

                String containerMaxThreads =
                    config.getOption(ContainerConfig.CONTAINER_THREADS_MAX);

                if (containerMaxThreads != null) {
                    maxThreads = Integer.parseInt(containerMaxThreads);
                }

                String containerWaterMarkThreads =
                    config.getOption(ContainerConfig.CONTAINER_THREADS_WATERMARK);

                if (containerWaterMarkThreads != null) {
                    highWaterMark = Integer.parseInt(containerWaterMarkThreads);
                }
            } catch (Exception e) {
                logger.error(i18n.getMessage("configError",
                                             "containerThreads"), e);
            }
        }

        if (numThreads < 2) {
            numThreads = 2;
        }

        if (maxThreads == 0) {
            maxThreads = numThreads * 4;
        }

        if (highWaterMark == 0) {
            highWaterMark = numThreads * 2;
        }

        logger.debug("Starting up Container with " + numThreads + " threads");
        this.threadPool.startThreads(numThreads);
        this.semaphore.sendSignal();

        int addedThreads = 0;

        while (!isStopped()) {
            Socket socket = null;

            try {
                socket = serverSocket.accept();
            } catch (IOException ioe) {
                break;
            }

            int waitingThreads =
                this.queue.enqueue(new ServiceRequest(socket, serverSocket));

            if (logger.isDebugEnabled()) {
                logger.debug("waiting threads: " + waitingThreads);
            }

            if ((waitingThreads == 0) &&
                (this.threadPool.getThreads() < maxThreads)) {
                addedThreads += startThreads(1);
                logger.debug("added threads " + addedThreads);
            } else if (waitingThreads > highWaterMark) {
                int stoppingThreads = waitingThreads - numThreads;
                logger.debug(
                    "requesting " + stoppingThreads +
                    " threads to be stopped - water mark: " + highWaterMark
                );

                if (addedThreads >= stoppingThreads) {
                    this.threadPool.stopThreads(stoppingThreads);
                    addedThreads -= stoppingThreads;
                }
            }
        }
    }

    public void waitForInit() throws InterruptedException {
        this.semaphore.waitForSignal();
    }

    public void waitForStop() throws InterruptedException {
        this.threadPool.waitForThreads();
        logger.debug("Stopping dispatcher");
    }

    private synchronized int startThreads(int num) {
        if (this.stopped) {
            return 0;
        }
        this.threadPool.startThreads(num);
        return num;
    }

    public synchronized void stop() throws IOException {
        if (this.stopped) {
            return;
        }

        this.stopped = true;

        logger.debug("Stopping threads");

        // close server socket first
        // so that no more connection can be made
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        } finally {
            // and then stop all running threads
            if (this.threadPool != null) {
                this.threadPool.stopThreads();
            }

            logger.debug("threads stopped interrupting worker");

            if (this.worker != null) {
                this.worker.interrupt();
            }

            if (this.engine != null) {
                ServiceManager.getServiceManager(this.engine).stop();
                
                // TODO: should wait for all threads
                // to actaully finish?
                this.engine.cleanup();
            }

            logger.debug("Stopped threads");
        }
    }

    public synchronized boolean isStopped() {
        return this.stopped;
    }

    /**
     * Start this dispatcher.
     *
     * Spawns a worker thread to listen for HTTP requests.
     *
     * @param daemon a boolean indicating if the thread should be a daemon.
     */
    public void start(boolean daemon) {
        this.worker = new Thread(this);
        this.worker.setDaemon(daemon);
        this.worker.start();
    }
}
