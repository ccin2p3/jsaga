package fr.in2p3.jsaga.engine.base;

import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.*;
import org.ogf.saga.session.Session;

import java.util.HashMap;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractMonitorableImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractMonitorableImpl extends AbstractSagaBaseImpl implements Monitorable {
    private Map m_metrics;

    /** constructor */
    public AbstractMonitorableImpl(Session session) {
        super(session);
        m_metrics = new HashMap();
    }

    /** constructor for deepCopy */
    protected AbstractMonitorableImpl(AbstractMonitorableImpl source) {
        super(source);
        m_metrics = deepCopy(source.m_metrics);
    }

    public String[] listMetrics() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        return (String[]) m_metrics.keySet().toArray(new String[m_metrics.keySet().size()]);
    }

    public Metric getMetric(String name) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Metric metric = (Metric) m_metrics.get(name);
        if (metric != null) {
            return metric;
        } else {
            throw new DoesNotExist("Metric "+name+" does not exist", this);
        }
    }

    public int addCallback(String name, Callback cb) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess, IncorrectState {
        Metric metric = (Metric) m_metrics.get(name);
        if (metric != null) {
            return metric.addCallback(cb);
        } else {
            throw new DoesNotExist("Metric "+name+" does not exist", this);
        }
    }

    public void removeCallback(String name, int cookie) throws NotImplemented, DoesNotExist, BadParameter, Timeout, NoSuccess {
        Metric metric = (Metric) m_metrics.get(name);
        if (metric != null) {
            metric.removeCallback(cookie);
        } else {
            throw new DoesNotExist("Metric "+name+" does not exist", this);
        }
    }
}
