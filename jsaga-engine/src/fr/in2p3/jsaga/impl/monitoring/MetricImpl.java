package fr.in2p3.jsaga.impl.monitoring;

import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaBase;
import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Callback;
import org.ogf.saga.monitoring.Metric;

import java.util.HashMap;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MetricImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MetricImpl extends AbstractAttributesImpl implements Metric {
    private Map<Integer,Callback> m_callbacks;
    private Mode m_mode;
    private int m_cookieGenerator;

    /** constructor */
    public MetricImpl(String name, String desc, Mode mode, String unit, String type) {
        super(null, true);   //not attached to a session, isExtensible=true
        super._addReadOnlyAttribute("Name", name);
        super._addReadOnlyAttribute("Description", desc);
        super._addReadOnlyAttribute("Mode", mode.name());
        super._addReadOnlyAttribute("Unit", unit);
        super._addReadOnlyAttribute("Type", type);
        m_callbacks = new HashMap<Integer,Callback>();
        m_mode = mode;
        m_cookieGenerator = 1;
    }

    /** constructor for deepCopy */
    protected MetricImpl(MetricImpl source) {
        super(source);
    }
    public SagaBase deepCopy() {
        return new MetricImpl(this);
    }

    public ObjectType getType() {
        return ObjectType.METRIC;
    }

    public int addCallback(Callback cb) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        switch (m_mode) {
            case FINAL:
                throw new IncorrectState("Can not add callback to Final metric", this);
            default:
                m_callbacks.put(m_cookieGenerator, cb);
                return m_cookieGenerator++;
        }
    }

    public void removeCallback(int cookie) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        m_callbacks.remove(cookie);
    }

    public void fire() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, ReadOnly, Timeout, NoSuccess {
        switch(m_mode) {
            case FINAL:
                throw new IncorrectState("Can not fire callback on a Final metric", this);
            case READONLY:
                throw new IncorrectState("Can not fire callback on a ReadOnly metric", this);
            default:
                for (Callback callback : m_callbacks.values()) {
                    callback.cb(null, this, null);    //todo: Implement method fire()
                }
                break;
        }
    }
}
