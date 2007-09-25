package fr.in2p3.jsaga.impl.monitoring;

import fr.in2p3.jsaga.impl.AbstractSagaBaseImpl;
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
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractMonitorableImpl extends AbstractSagaBaseImpl implements Monitorable {
    private Map<String,Metric> m_metrics;

    /** constructor */
    public AbstractMonitorableImpl(Session session) {
        super(session);
        m_metrics = new HashMap<String,Metric>();
    }

    /** constructor for deepCopy */
    protected AbstractMonitorableImpl(AbstractMonitorableImpl source) {
        super(source);
        m_metrics = deepCopy(source.m_metrics);
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

    public int addCallback(String name, Callback cb) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Metric metric = m_metrics.get(name);
        if (metric != null) {
            try {
                return metric.addCallback(cb);
            } catch (IncorrectState e) {
                throw new NoSuccess(e);
            }
        } else {
            throw new DoesNotExist("Metric "+name+" does not exist", this);
        }
    }

    public void removeCallback(String name, int cookie) throws NotImplemented, DoesNotExist, BadParameter, Timeout, NoSuccess {
        Metric metric = m_metrics.get(name);
        if (metric != null) {
            metric.removeCallback(cookie);
        } else {
            throw new DoesNotExist("Metric "+name+" does not exist", this);
        }
    }
}
