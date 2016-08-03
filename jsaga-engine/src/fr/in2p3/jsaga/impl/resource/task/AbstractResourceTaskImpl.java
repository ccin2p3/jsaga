package fr.in2p3.jsaga.impl.resource.task;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Callback;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.monitoring.Monitorable;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.resource.task.ResourceTask;
import org.ogf.saga.resource.task.State;
import org.ogf.saga.session.Session;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.helpers.SAGAId;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public abstract class AbstractResourceTaskImpl<R extends Resource,RD extends ResourceDescription>
        extends AbstractMonitorableWithAsyncAttributes<R>
        implements Resource<R,RD>, ResourceTask, ResourceMonitorCallback
{
    protected ResourceAdaptor m_adaptor;
    private StateListener m_listener;
    private ResourceMetrics m_metrics;
    private State m_state = State.NEW;
    private long m_stateLastUpdate = 0;

    // TODO: make this a parameter
    private long m_stateLifetimeMillis = 30000;
    
    /** common to all constructors */
    public AbstractResourceTaskImpl(Session session, StateListener listener, ResourceAdaptor adaptor) {
        super(session);
        m_adaptor = adaptor;
        m_listener = listener;
        m_metrics = new ResourceMetrics(this);
    }

    public State getState() throws NotImplementedException, TimeoutException, NoSuccessException {
        // either take status from cache or ask the adaptor
        if (m_state != null && m_stateLastUpdate != 0 
                && (System.currentTimeMillis() < m_stateLastUpdate + m_stateLifetimeMillis)) {
            return m_state;
        } else {
            try {
                return m_adaptor.getResourceStatus(SAGAId.idFromSagaId(getResourceId())).getSagaState();
            } catch (DoesNotExistException e) {
                throw new NoSuccessException(e);
            } catch (BadParameterException e) {
                throw new NoSuccessException(e);
            }
        }
    }
    
    public void waitFor() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        this.waitFor(WAIT_FOREVER, State.FINAL);
    }
    public void waitFor(float timeoutInSeconds) throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        this.waitFor(timeoutInSeconds, State.FINAL);
    }
    public void waitFor(State state) throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        this.waitFor(WAIT_FOREVER, state);
    }
    public void waitFor(float timeoutInSeconds, State state) throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            // start listening
            int cookie = m_metrics.m_State.addCallback(new Callback() {
                public boolean cb(Monitorable mt, Metric metric, Context ctx) throws NotImplementedException, AuthorizationFailedException {
                    AbstractResourceTaskImpl resource = (AbstractResourceTaskImpl) mt;
                    try {
                        String value = metric.getAttribute(Metric.VALUE);
                        State current = State.valueOf(value);
                        resource.setState(current, null);
                    }
                    catch (NotImplementedException e) {throw e;}
                    catch (AuthorizationFailedException e) {throw e;}
                    catch (Exception e) {e.printStackTrace();}
                    // callback must stay registered
                    return true;
                }
            });

            // wait for specified state
            long endTime;
            if (timeoutInSeconds == WAIT_FOREVER) {
                endTime = -1;
            } else {
                endTime = System.currentTimeMillis() + (long) (timeoutInSeconds*1000f);
            }
            int mask = state.getValue();
            State current;
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new NoSuccessException(e);
                }
                current = this.m_state;
                if (System.currentTimeMillis()>=endTime) {
                    throw new TimeoutException();
                }
            } while ((current.getValue() & mask) == 0);

            // stop listening
            m_metrics.m_State.removeCallback(cookie);
        } catch (AuthenticationFailedException e) {
            throw new NoSuccessException(e);
        } catch (AuthorizationFailedException e) {
            throw new NoSuccessException(e);
        } catch (PermissionDeniedException e) {
            throw new NoSuccessException(e);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }

    ////////////////////////////////////////// internal methods //////////////////////////////////////////

    public void startListening() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        // forward to manager
        try {
            m_listener.startListening(SAGAId.idFromSagaId(getResourceId()), this);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }
    public void stopListening() throws NotImplementedException, TimeoutException, NoSuccessException {
        // forward to manager
        try {
            m_listener.stopListening(SAGAId.idFromSagaId(getResourceId()));
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        }
    }
    
    @Override
    public void setState(State state, String stateDetail) {
        // save the notified state
        m_state = state;
        m_metrics.m_StateDetail.setValue(stateDetail);
        m_stateLastUpdate = System.currentTimeMillis();
    }

}
