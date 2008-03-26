package fr.in2p3.jsaga.impl.file.stream;

import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.file.FileOutputStream;
import org.ogf.saga.session.Session;

import java.util.UUID;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractAsyncFileOutputStreamImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractAsyncFileOutputStreamImpl extends FileOutputStream {
    private Session m_session;
    private UUID m_uuid;

    /** constructor */
    public AbstractAsyncFileOutputStreamImpl(Session session) {
        m_session = session;
        m_uuid = UUID.randomUUID();
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractAsyncFileOutputStreamImpl clone = (AbstractAsyncFileOutputStreamImpl) super.clone();
        clone.m_session = m_session;
        clone.m_uuid = UUID.randomUUID();
        return clone;
    }

    /////////////////////////////////// interface SagaObject ////////////////////////////////////

    public Session getSession() throws DoesNotExist {
        if (m_session != null) {
            return m_session;
        } else {
            throw new DoesNotExist("This object does not have a session attached", this);
        }
    }

    public String getId() {
        return m_uuid.toString();
    }

    public ObjectType getType() {
        return ObjectType.FILEOUTPUTSTREAM;
    }

    ///////////////////////////////// interface FileInputStream /////////////////////////////////

    //todo: implement asynchronous methods of interface FileOutputStream
}
