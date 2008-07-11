package fr.in2p3.jsaga.impl.monitoring;

import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.*;
import org.ogf.saga.session.Session;

import java.lang.Exception;
import java.util.HashMap;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractMonitorableImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractMonitorableImpl extends AbstractSagaObjectImpl implements Monitorable {
    private Map<String,Metric> m_metrics;

    /** constructor */
    public AbstractMonitorableImpl(Session session) {
        super(session);
        m_metrics = new HashMap<String,Metric>();
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractMonitorableImpl clone = (AbstractMonitorableImpl) super.clone();
        clone.m_metrics = clone(m_metrics);
        return clone;
    }

    public String[] listMetrics() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        return m_metrics.keySet().toArray(new String[m_metrics.keySet().size()]);
    }

    public Metric getMetric(String name) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Metric metric = m_metrics.get(name);
        if (metric != null) {
            return metric;
        } else {
            throw new DoesNotExist("Metric "+name+" does not exist", this);
        }
    }

    public int addCallback(String name, Callback cb) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess, IncorrectState {
        Metric metric = m_metrics.get(name);
        if (metric != null) {
            return metric.addCallback(cb);
        } else {
            throw new DoesNotExist("Metric "+name+" does not exist", this);
        }
    }

    public void removeCallback(String name, int cookie) throws NotImplemented, DoesNotExist, BadParameter, Timeout, NoSuccess, AuthenticationFailed, AuthorizationFailed, PermissionDenied {
        Metric metric = m_metrics.get(name);
        if (metric != null) {
            metric.removeCallback(cookie);
        } else {
            throw new DoesNotExist("Metric "+name+" does not exist", this);
        }
    }

    //////////////////////////////////////////// internal methods ////////////////////////////////////////////

    public MetricImpl _addMetric(MetricImpl metric) throws NotImplemented {
        try {
            String name = metric.getAttribute(Metric.NAME);
            m_metrics.put(name, metric);
        } catch (Exception e) {
            throw new NotImplemented("INTERNAL ERROR", e);
        }
        return metric;
    }
}
