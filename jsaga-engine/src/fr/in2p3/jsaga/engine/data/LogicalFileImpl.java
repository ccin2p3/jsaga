package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.ExtensionFlags;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.factories.NamespaceFactoryImpl;
import org.ogf.saga.SagaBase;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.error.Exception;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.net.URISyntaxException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalFileImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalFileImpl extends AbstractNamespaceEntryImpl implements LogicalFile {
    /** constructor */
    public LogicalFileImpl(Session session, URI uri, LogicalEntryFlags flags, DataAdaptor adaptor) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, flags, adaptor);
        FlagsContainer effectiveFlags = new FlagsContainer(flags, PhysicalEntryFlags.READ);
        effectiveFlags.keepLogicalEntryFlags();
        if (effectiveFlags.contains(Flags.CREATE)) {
            // do nothing
        } else if (! new FlagsContainer(flags).contains(ExtensionFlags.LATE_EXISTENCE_CHECK)) {
            if (m_adaptor instanceof DataReaderAdaptor && !((DataReaderAdaptor)m_adaptor).exists(m_uri.getPath())) {
                throw new DoesNotExist("Logical file does not exist: "+m_uri);
            }
        }
    }

    /** constructor for deepCopy */
    protected LogicalFileImpl(AbstractNamespaceEntryImpl source) {
        super(source);
    }
    public SagaBase deepCopy() {
        return new LogicalFileImpl(this);
    }

    /**
     * Note: throws a AlreadyExists exception when the replica is already in the set.
     */
    public void addLocation(String name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriter) {
            URI replicaLocation = this._getReplicaLocation(name);
            ((LogicalWriter)m_adaptor).addLocation(m_uri.getPath(), replicaLocation);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public void removeLocation(String name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriter) {
            URI replicaLocation = this._getReplicaLocation(name);
            ((LogicalWriter)m_adaptor).removeLocation(m_uri.getPath(), replicaLocation);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public void updateLocation(String nameOld, String nameNew) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriter) {
            URI replicaLocationOld = this._getReplicaLocation(nameOld);
            URI replicaLocationNew = this._getReplicaLocation(nameNew);
            ((LogicalWriter)m_adaptor).addLocation(m_uri.getPath(), replicaLocationNew);
            ((LogicalWriter)m_adaptor).removeLocation(m_uri.getPath(), replicaLocationOld);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public String[] listLocations() throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalReader) {
            return ((LogicalReader)m_adaptor).listLocations(m_uri.getPath());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public void replicate(String name, LogicalEntryFlags flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalReader && m_adaptor instanceof LogicalWriter) {
            this._replicate_step1(name, flags);
            this._replicate_step2(name);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }
    private void _replicate_step1(String name, LogicalEntryFlags flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        final String MESSAGE = "Failed to copy replica";
        try {
            String[] locations = this.listLocations();
            if (locations==null || locations.length==0) {
                throw new IncorrectState("Can not replicate a logical entry with empty location", this);
            }
            URI replicaLocationSource = this._getReplicaLocation(locations[0]);
            URI replicaLocationTarget = this._getReplicaLocation(name);
            NamespaceEntry replicaSource = NamespaceFactoryImpl.createNamespaceEntry(m_session, replicaLocationSource, Flags.NONE);
            replicaSource.copy(replicaLocationTarget, flags);
        } catch (NotImplemented e) {
            throw new NotImplemented(MESSAGE, e);
        } catch (IncorrectURL e) {
            throw new IncorrectURL(MESSAGE, e);
        } catch (AuthenticationFailed e) {
            throw new AuthenticationFailed(MESSAGE, e);
        } catch (AuthorizationFailed e) {
            throw new AuthorizationFailed(MESSAGE, e);
        } catch (PermissionDenied e) {
            throw new PermissionDenied(MESSAGE, e);
        } catch (BadParameter e) {
            throw new BadParameter(MESSAGE, e);
        } catch (IncorrectState e) {
            throw new IncorrectState(MESSAGE, e);
        } catch (AlreadyExists e) {
            throw new AlreadyExists(MESSAGE, e);
        } catch (Timeout e) {
            throw new Timeout(MESSAGE, e);
        } catch (NoSuccess e) {
            throw new NoSuccess(MESSAGE, e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(MESSAGE, e);
        }
    }
    private void _replicate_step2(String name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        final String MESSAGE = "Failed to add location of new replica [INCORRECT STATE]";
        try {
            this.addLocation(name);
        } catch (NotImplemented e) {
            throw new NotImplemented(MESSAGE, e);
        } catch (IncorrectURL e) {
            throw new IncorrectURL(MESSAGE, e);
        } catch (AuthenticationFailed e) {
            throw new AuthenticationFailed(MESSAGE, e);
        } catch (AuthorizationFailed e) {
            throw new AuthorizationFailed(MESSAGE, e);
        } catch (PermissionDenied e) {
            throw new PermissionDenied(MESSAGE, e);
        } catch (BadParameter e) {
            throw new BadParameter(MESSAGE, e);
        } catch (IncorrectState e) {
            throw new IncorrectState(MESSAGE, e);
        } catch (AlreadyExists e) {
            throw new AlreadyExists(MESSAGE, e);
        } catch (Timeout e) {
            throw new Timeout(MESSAGE, e);
        } catch (NoSuccess e) {
            throw new NoSuccess(MESSAGE, e);
        }
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    protected void _removeAllLocations() {
        try {
            String[] locations = this.listLocations();
            for (int i=0; locations!=null && i<locations.length; i++) {
                this.removeLocation(locations[i]);
            }
        } catch(Exception e) {
            // do nothing
        }
    }

    protected void _addAllLocations(String[] locations) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        try {
            for (int i=0; locations!=null && i<locations.length; i++) {
                this.addLocation(locations[i]);
            }
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        }
    }

    protected URI _getReplicaLocation(String name) throws IncorrectURL {
        try {
            return new URI(name);
        } catch (URISyntaxException e) {
            throw new IncorrectURL(e);
        }
    }

    protected NamespaceDirectory _openParentDir(Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalDirectoryImpl(m_session, super._getParentDirURI(), PhysicalEntryFlags.cast(flags), m_adaptor);
    }
}
