package fr.in2p3.jsaga.engine.workflow.task;

import fr.in2p3.jsaga.engine.schema.status.Task;
import fr.in2p3.jsaga.engine.schema.status.types.TaskTypeType;
import fr.in2p3.jsaga.engine.workflow.AbstractWorkflowTaskImpl;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;

import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TransferTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TransferTask extends AbstractWorkflowTaskImpl {
    private Session m_session;
    private URL m_source;
    private URL m_destination;
    private boolean m_overwrite;

    /** constructor */
    public TransferTask(Session session, String destination, boolean input, boolean overwrite, boolean keep) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(null, destination);
        // set URL
        m_session = session;
        m_source = null;
        m_destination = new URL(destination);
        m_overwrite = overwrite;
        // update XML status
        URLDecomposer u = new URLDecomposer(destination);
        Task xmlStatus = super.getStateAsXML();
        xmlStatus.setType(TaskTypeType.TRANSFER);
        xmlStatus.setGroup(u.getGroup());
        xmlStatus.setLabel(u.getLabel());
        xmlStatus.setContext(u.getContext());
        xmlStatus.setInput(input);
        xmlStatus.setKeep(keep);
    }

    public void setSource(String source) throws NotImplemented, BadParameter, NoSuccess {
        m_source = new URL(source);
    }

    //////////////////////////////////////////// abstract methods ////////////////////////////////////////////

    protected void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        try {
            int flags = (m_overwrite
                    ? Flags.CREATEPARENTS.or(Flags.OVERWRITE)
                    : Flags.CREATEPARENTS.getValue());
            NSFactory.createNSEntry(m_session, m_source).copy(m_destination, flags);
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        super.setState(State.DONE);
    }

    protected void doCancel() {
        super.setState(State.CANCELED);
    }

    protected State queryState() throws NotImplemented, Timeout, NoSuccess {
        return super.getState_LocalCheckOnly();
    }

    public boolean startListening() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        return true;    // do nothing
    }

    public void stopListening() throws NotImplemented, Timeout, NoSuccess {
        // do nothing
    }
}
