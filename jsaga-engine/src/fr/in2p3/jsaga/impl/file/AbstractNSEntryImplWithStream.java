package fr.in2p3.jsaga.impl.file;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.impl.namespace.AbstractNSDirectoryImpl;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.sync.file.SyncFile;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.*;
import org.ogf.saga.file.FileOutputStream;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

import java.io.IOException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSEntryImplWithStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSEntryImplWithStream extends AbstractNSEntryImpl implements SyncFile {
    protected FileOutputStream m_outStream;

    /** constructor for factory */
    protected AbstractNSEntryImplWithStream(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(session, url, adaptor, flags);
    }

    /** constructor for NSDirectory.open() */
    protected AbstractNSEntryImplWithStream(AbstractNSDirectoryImpl dir, URL relativeUrl, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(dir, relativeUrl, flags);
    }

    /** constructor for NSEntry.openAbsolute() */
    protected AbstractNSEntryImplWithStream(AbstractNSEntryImpl entry, String absolutePath, int flags) throws NotImplementedException, IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        super(entry, absolutePath, flags);
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractNSEntryImplWithStream clone = (AbstractNSEntryImplWithStream) super.clone();
        clone.m_outStream = m_outStream;
        return clone;
    }

    public boolean exists() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, TimeoutException, NoSuccessException {
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        return super.exists();
    }

    public boolean isDir() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        return super.isDir();
    }

    public boolean isEntry() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        return super.isEntry();
    }

    public boolean isLink() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        return super.isLink();
    }

    public URL readLink() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        return super.readLink();
    }

    public void link(URL link, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        super.link(link, flags);
    }

    public void move(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        super.move(target, flags);
    }

    public void remove(int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_outStream != null) {
            try {m_outStream.close();} catch (IOException e) {/*ignore*/}
        }
        super.remove(flags);
    }
}
