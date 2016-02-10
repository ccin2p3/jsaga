package fr.in2p3.jsaga.impl.resource.task;

import fr.in2p3.jsaga.impl.monitoring.MetricImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Callback;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ResourceStateMetricImpl<E> extends MetricImpl<E> {
    private AbstractResourceTaskImpl m_listener;

    /** constructor */
    public ResourceStateMetricImpl(AbstractResourceTaskImpl resource, String name, String desc, MetricMode mode, String unit, MetricType type, E initialValue) {
        super(resource, name, desc, mode, unit, type, initialValue);
        m_listener = resource;
    }

    /** clone */
    public ResourceStateMetricImpl<E> clone() throws CloneNotSupportedException {
        // clone attributes
        ResourceStateMetricImpl<E> clone = (ResourceStateMetricImpl<E>) super.clone();
        clone.m_listener = m_listener;
        return clone;
    }

    public synchronized int addCallback(Callback cb) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, TimeoutException, NoSuccessException {
        int nbBefore = super.getNumberOfCallbacks();
        int cookie = super.addCallback(cb);
        int nbAfter = super.getNumberOfCallbacks();
        if (nbBefore==0 && nbAfter==1) {
            m_listener.startListening();
        }
        return cookie;
    }

    public synchronized void removeCallback(int cookie) throws NotImplementedException, BadParameterException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, TimeoutException, NoSuccessException {
        int nbBefore = super.getNumberOfCallbacks();
        super.removeCallback(cookie);
        int nbAfter = super.getNumberOfCallbacks();
        if (nbBefore==1 && nbAfter==0) {
            m_listener.stopListening();
        }
    }
}
