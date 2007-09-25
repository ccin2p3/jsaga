package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.data.copy.SourceLogicalFile;
import fr.in2p3.jsaga.engine.data.copy.TargetLogicalFile;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytesLogical;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import fr.in2p3.jsaga.impl.namespace.AbstractNamespaceEntryImpl;
import org.ogf.saga.*;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalFileImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalFileImpl extends AbstractLogicalFileTaskImpl implements LogicalFile {
    /** constructor for factory */
    public LogicalFileImpl(Session session, URI uri, DataAdaptor adaptor, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, uri, adaptor, flags);
        this.init(flags);
    }

    /** constructor for open() */
    public LogicalFileImpl(AbstractNamespaceEntryImpl entry, URI uri, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, uri, flags);
        this.init(flags);
    }

    private void init(Flags... flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytesLogical(Flags.READ, flags);
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
        } else if (! m_skipExistenceCheck) {
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

    public ObjectType getType() {
        return ObjectType.LOGICALFILE;
    }

    /** implements super.copy() */
    public void copy(URI target, Flags... flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytes(Flags.NONE, flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copy(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE, Flags.CREATEPARENTS, Flags.OVERWRITE);
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

    public void copyFrom(URI source, Flags... flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytes(Flags.NONE, flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copyFrom(source, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE, Flags.OVERWRITE);
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

    public NamespaceDirectory openDir(URI absolutePath, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalDirectoryImpl(this, super._resolveAbsoluteURI(absolutePath), flags);
    }

    public NamespaceEntry open(URI absolutePath, Flags... flags) throws NotImplemented, IncorrectURL, IncorrectSession, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalFileImpl(this, super._resolveAbsoluteURI(absolutePath), flags);
    }

    public void addLocation(URI name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriter) {
            ((LogicalWriter)m_adaptor).addLocation(m_uri.getPath(), name);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public void removeLocation(URI name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriter) {
            ((LogicalWriter)m_adaptor).removeLocation(m_uri.getPath(), name);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public void updateLocation(URI nameOld, URI nameNew) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriter) {
            ((LogicalWriter)m_adaptor).addLocation(m_uri.getPath(), nameNew);
            ((LogicalWriter)m_adaptor).removeLocation(m_uri.getPath(), nameOld);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public List<URI> listLocations() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalReader) {
            try {
                String[] array = ((LogicalReader)m_adaptor).listLocations(m_uri.getPath());
                List<URI> list = new ArrayList<URI>();
                try {
                    for (String location : array) {
                        list.add(new URI(location));
                    }
                } catch (URISyntaxException e) {
                    throw new NoSuccess(e);
                }
                return list;
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Logical file does not exist: "+m_uri, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }

    public void replicate(URI name, Flags... flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalReader && m_adaptor instanceof LogicalWriter) {
            this._replicate_step1(name, flags);
            this._replicate_step2(name);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+m_uri.getScheme(), this);
        }
    }
    private void _replicate_step1(URI physicalTarget, Flags... flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        final String MESSAGE = "Failed to copy replica (state is still consistent)";
        try {
            List<URI> locations = this.listLocations();
            if (locations==null || locations.size()==0) {
                throw new IncorrectState("Can not replicate a logical entry with empty location", this);
            }
            URI physicalSource = locations.get(0);
            NamespaceEntry physicalSourceEntry = NamespaceFactory.createNamespaceEntry(m_session, physicalSource, Flags.NONE);
            physicalSourceEntry.copy(physicalTarget, flags);
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
    private void _replicate_step2(URI name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        final String MESSAGE = "INCONSISTENT STATE: Replica has been copied but failed to register the new location";
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
        } catch (Timeout e) {
            throw new Timeout(MESSAGE, e);
        } catch (NoSuccess e) {
            throw new NoSuccess(MESSAGE, e);
        }
    }
}
