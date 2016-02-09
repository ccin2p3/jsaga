package fr.in2p3.jsaga.impl.resource.task;

import java.util.Date;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Callback;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.monitoring.Monitorable;
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
public class AbstractResourceTaskImpl<R extends Resource>
        extends AbstractMonitorableWithAsyncAttributes<R>
        implements ResourceTask, StateListener
{
    protected ResourceAdaptor m_adaptor;
    private StateListener m_listener;
    private ResourceMetrics m_metrics;
    private State m_state;
    private Date m_stateLastUpdate;

    /** common to all constructors */
    public AbstractResourceTaskImpl(Session session, StateListener listener, ResourceAdaptor adaptor) {
        super(session);
        m_adaptor = adaptor;
        m_listener = listener;
        m_metrics = new ResourceMetrics(this);
    }

    public State getState() throws NotImplementedException, TimeoutException, NoSuccessException {
        //TODO: m_state=null when the notified state has expired...
        if (m_state != null) {
            return m_state;
        } else {
            try {
                return m_adaptor.getState(SAGAId.idFromSagaId(getId()));
            } catch (DoesNotExistException e) {
                throw new NoSuccessException(e);
            } catch (BadParameterException e) {
                throw new NoSuccessException(e);
            }
        }
    }
    
    public String getStateDetail() {
        return null;        //TODO: query the current state
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
                        resource.setState(current);
                    }
                    catch (NotImplementedException e) {throw e;}
                    catch (AuthorizationFailedException e) {throw e;}
                    catch (Exception e) {e.printStackTrace();}
                    // callback must stay registered
                    return true;
                }
            });

            // wait for specified state
            int mask = state.getValue();
            State current;
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new NoSuccessException(e);
                }
                current = this.m_state;
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
        m_listener.startListening();
    }
    public void stopListening() throws NotImplementedException, TimeoutException, NoSuccessException {
        // forward to manager
        m_listener.stopListening();
    }
    public void setState(State state) {
        // save the notified state
        m_state = state;
        m_stateLastUpdate = new Date();
    }
}
