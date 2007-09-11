package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.ExtensionFlags;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.data.copy.SourceLogicalFile;
import fr.in2p3.jsaga.engine.data.copy.TargetLogicalFile;
import fr.in2p3.jsaga.engine.factories.NamespaceFactoryImpl;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import org.ogf.saga.SagaBase;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
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
    public LogicalFileImpl(Session session, URI uri, LogicalEntryFlags flags, DataConnection connection) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, flags, connection);
        FlagsContainer effectiveFlags = new FlagsContainer(flags, PhysicalEntryFlags.READ);
        effectiveFlags.keepLogicalEntryFlags();
        if (effectiveFlags.contains(Flags.CREATE)) {
            if (m_adaptor instanceof LogicalWriter) {
                if (this.exists()) {
                    if (effectiveFlags.contains(Flags.EXCL)) {
                        throw new AlreadyExists("Entry already exists: "+m_uri);
                    }
                } else if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
                    this._makeParentDirs();
                }
            } else {
                throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme());
            }
        } else if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
            this._makeParentDirs();
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
            try {
                return ((LogicalReader)m_adaptor).listLocations(m_uri.getPath());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Logical file does not exist: "+m_uri, doesNotExist);
            }
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

    public void copy(URI target, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copy(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.CREATEPARENTS).or(Flags.OVERWRITE));
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URI effectiveTarget = this._getEffectiveURI(target);
        if (m_adaptor instanceof DataCopyDelegated && m_uri.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        m_uri,
                        effectiveTarget,
                        overwrite);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Logical file does not exist: "+m_uri, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DataCopy && m_uri.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copy(
                        m_uri.getPath(),
                        effectiveTarget.getHost(), effectiveTarget.getPort(), effectiveTarget.getPath(),
                        overwrite);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Logical file does not exist: "+m_uri, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof LogicalReader) {
            SourceLogicalFile source = new SourceLogicalFile(this);
            Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(target.getScheme());
            if (descriptor.hasLogical() && descriptor.getLogical()) {
                source.putToLogicalFile(m_session, effectiveTarget, effectiveFlags);
            } else {
                source.putToPhysicalFile(m_session, effectiveTarget, effectiveFlags);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public void copyFrom(URI source, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        FlagsContainer effectiveFlags = new FlagsContainer(flags, Flags.NONE);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copyFrom(source, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.keepNamespaceEntryFlags();
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.OVERWRITE));
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URI effectiveSource = this._getEffectiveURI(source);
        if (m_adaptor instanceof DataCopyDelegated && m_uri.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        effectiveSource,
                        m_uri,
                        overwrite);
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Logical file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+m_uri, alreadyExists);
            }
        } else if (m_adaptor instanceof DataCopy && m_uri.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copyFrom(
                        effectiveSource.getHost(), effectiveSource.getPort(), effectiveSource.getPath(),
                        m_uri.getPath(),
                        overwrite);
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Logical file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+m_uri, alreadyExists);
            }
        } else if (m_adaptor instanceof LogicalWriter) {
            Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(source.getScheme());
            if (descriptor.hasLogical() && descriptor.getLogical()) {
                TargetLogicalFile target = new TargetLogicalFile(this);
                target.getFromLogicalFile(m_session, effectiveSource, effectiveFlags);
            } else {
                throw new BadParameter("Maybe what you want to do is to register to logical file the following location: "+source);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    ///////////////////////////////////////// protected methods /////////////////////////////////////////

    public NamespaceDirectory openDir(URI absolutePath, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalDirectoryImpl(m_session, super._resolveAbsoluteURI(absolutePath), PhysicalEntryFlags.cast(flags), m_connection);
    }

    public NamespaceEntry openEntry(URI absolutePath, Flags flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalFileImpl(m_session, super._resolveAbsoluteURI(absolutePath), PhysicalEntryFlags.cast(flags), m_connection);
    }

    protected URI _getReplicaLocation(String name) throws IncorrectURL {
        try {
            return new URI(name);
        } catch (URISyntaxException e) {
            throw new IncorrectURL(e);
        }
    }
}
