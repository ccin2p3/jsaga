package fr.in2p3.jsaga.engine.workflow.task;

import fr.in2p3.jsaga.engine.schema.status.Task;
import fr.in2p3.jsaga.engine.schema.status.types.TaskTypeType;
import fr.in2p3.jsaga.engine.workflow.AbstractWorkflowTaskImpl;
import fr.in2p3.jsaga.impl.url.URLFactoryImpl;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.task.State;
import org.ogf.saga.url.URL;

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
    public MkdirTask(Session session, String dir, boolean keep) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        super(null, "mkdir_"+dir);
        // set URL
        m_session = session;
        m_dir = URLFactoryImpl.createUnencodedURL(dir);
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

    protected void doSubmit() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            NSFactory.createNSDirectory(m_session, m_dir, Flags.CREATE.or(Flags.CREATEPARENTS));
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
        super.setState(State.DONE);
    }

    protected void doCancel() {
        super.setState(State.CANCELED);
    }

    protected State queryState() throws NotImplementedException, TimeoutException, NoSuccessException {
        return super.getState_fromCache();
    }

    public boolean startListening() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        return true;    // do nothing
    }

    public void stopListening() throws NotImplementedException, TimeoutException, NoSuccessException {
        // do nothing
    }
}
