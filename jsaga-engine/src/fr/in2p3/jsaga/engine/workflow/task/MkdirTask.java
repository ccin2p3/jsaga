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
* File:   MkdirTask
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MkdirTask extends AbstractWorkflowTaskImpl {
    private Session m_session;
    private URL m_dir;

    /** constructor */
    public MkdirTask(Session session, String dir, boolean keep) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        super(null, "mkdir_"+dir);
        // set URL
        m_session = session;
        m_dir = new URL(dir);
        // update XML status
        URLDecomposer u = new URLDecomposer(dir);
        Task xmlStatus = super.getStateAsXML();
        xmlStatus.setType(TaskTypeType.MKDIR);
        xmlStatus.setGroup(u.getGroup());
        xmlStatus.setLabel(u.getLabel());
        xmlStatus.setContext(u.getContext());
        xmlStatus.setKeep(keep);
    }

    //////////////////////////////////////////// abstract methods ////////////////////////////////////////////

    protected void doSubmit() throws NotImplemented, IncorrectState, Timeout, NoSuccess {
        try {
            NSFactory.createNSDirectory(m_session, m_dir, Flags.CREATE.or(Flags.CREATEPARENTS));
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
