/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 * 
 * Authors: Luigi Zangrando <zangrando@pd.infn.it>
 *
 * Version info: $Id: EventDispatcher.java,v 1.4 2009/03/10 10:15:19 zangran Exp $
 */

package org.glite.ce.commonj.certificate;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class EventDispatcher extends Thread {
    private static EventDispatcher eventDispatcher = null;
    private List<ProxyCertificateListener> listeners;
    private BlockingQueue<ProxyCertificateEvent> queue;
    private boolean exit = false;
    
    public static EventDispatcher getInstance() {
        if(eventDispatcher == null) {
            eventDispatcher = new EventDispatcher();
        }

        return eventDispatcher;
    }
    

    private EventDispatcher() {
        queue = new LinkedBlockingQueue<ProxyCertificateEvent>();
        listeners = new CopyOnWriteArrayList<ProxyCertificateListener>(); 
        
        start();
    }

    public void addDelegationProxyListener(ProxyCertificateListener l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    public void destroy() {
        exit = true;
        interrupt();
    }

    public void fireProxyCertificateEvent(ProxyCertificateEvent event) throws Exception, IllegalArgumentException {
        if(event == null) {
            throw new IllegalArgumentException("event not specified");
        }
        
        try {
            queue.put(event);
        } catch (InterruptedException e) {
            throw new Exception(e.getMessage());
        }
    }

    public List<ProxyCertificateListener> getDelegationProxyListeners() {
        return listeners;
    }
        
    public void removeDelegationProxyListener(ProxyCertificateListener l) {
        if (l != null) {
            listeners.remove(l);
        }
    }
    
    public void run() {
        while(!exit || !isInterrupted()) {
            ProxyCertificateEvent event;
            
            try {
                event = queue.take();

                for (ProxyCertificateListener pcListener : listeners) {
                    switch (event.getEventType()) {
                    case ProxyCertificateEvent.PROXY_CERTIFICATE_ADDED:
                        pcListener.proxyCertificateAdded(event.getProxyCertificate());
                        break;
                    case ProxyCertificateEvent.PROXY_CERTIFICATE_REMOVED:
                        pcListener.proxyCertificateRemoved(event.getProxyCertificate());
                        break;
                    case ProxyCertificateEvent.PROXY_CERTIFICATE_UPDATED:
                        pcListener.proxyCertificateUpdated(event.getProxyCertificate());
                        break;
                    }
                }
            } catch (InterruptedException e) {
            }            
        }
        
        queue.clear();
        listeners.clear();
    }
}
