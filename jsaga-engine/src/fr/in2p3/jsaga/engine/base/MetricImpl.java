package fr.in2p3.jsaga.engine.base;

import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Callback;
import org.ogf.saga.monitoring.Metric;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MetricImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class MetricImpl extends AbstractAttributesImpl implements Metric {
    private Map m_callbacks;
    private MetricMode m_mode;
    private int m_cookieGenerator;

    /** constructor */
    public MetricImpl(String name, String description, MetricMode mode, String unit, String type) {
        super(null, true);   //not attached to a session, isExtensible=true
        super._addReadOnlyAttribute("Name", name);
        super._addReadOnlyAttribute("Description", description);
        super._addReadOnlyAttribute("Mode", mode.toString());
        super._addReadOnlyAttribute("Unit", unit);
        super._addReadOnlyAttribute("Type", type);
        m_callbacks = new HashMap();
        m_mode = mode;
        m_cookieGenerator = 1;
    }

    /** constructor for deepCopy */
    protected MetricImpl(MetricImpl source) {
        super(source);
    }

    public int addCallback(Callback cb) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        switch (m_mode.getValue()) {
            case MetricMode.FINAL_TYPE:
                throw new IncorrectState("Can not add callback to Final metric", this);
            default:
                m_callbacks.put(new Integer(m_cookieGenerator), cb);
                return m_cookieGenerator++;
        }
    }

    public void removeCallback(int cookie) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        m_callbacks.remove(new Integer(cookie));
    }

    public void fire() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, ReadOnly, Timeout, NoSuccess {
        switch(m_mode.getValue()) {
            case MetricMode.FINAL_TYPE:
                throw new IncorrectState("Can not fire callback on a Final metric", this);
            case MetricMode.READONLY_TYPE:
                throw new IncorrectState("Can not fire callback on a ReadOnly metric", this);
            default:
                for (Iterator it=m_callbacks.values().iterator(); it.hasNext(); ) {
                    Callback cb = (Callback) it.next();
                    cb.cb(null, this, null);    //todo: Implement method fire()
                }
                break;
        }
    }
}
