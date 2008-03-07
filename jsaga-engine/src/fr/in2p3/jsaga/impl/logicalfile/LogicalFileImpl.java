package fr.in2p3.jsaga.impl.logicalfile;

import fr.in2p3.jsaga.JSagaURL;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
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
import fr.in2p3.jsaga.helpers.URLFactory;
import fr.in2p3.jsaga.impl.namespace.AbstractNSEntryImpl;
import fr.in2p3.jsaga.impl.namespace.JSAGAFlags;
import org.ogf.saga.*;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

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
public class LogicalFileImpl extends AbstractAsyncLogicalFileImpl implements LogicalFile {
    /** constructor for factory */
    public LogicalFileImpl(Session session, URL url, DataAdaptor adaptor, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(session, URLFactory.toFileURL(url), adaptor, flags);
        this.init(flags);
    }

    /** constructor for open() */
    public LogicalFileImpl(AbstractNSEntryImpl entry, URL url, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        super(entry, URLFactory.toFileURL(url), flags);
        this.init(flags);
    }

    private void init(int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        FlagsBytes effectiveFlags = new FlagsBytesLogical(flags);
        if (effectiveFlags.contains(Flags.CREATE)) {
            if (m_adaptor instanceof LogicalWriter) {
                if (this.exists()) {
                    if (effectiveFlags.contains(Flags.EXCL)) {
                        throw new AlreadyExists("Entry already exists: "+ m_url);
                    }
                } else if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
                    this._makeParentDirs();
                }
            } else {
                throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme());
            }
        } else if (effectiveFlags.contains(Flags.CREATEPARENTS)) {
            this._makeParentDirs();
        } else if (!JSAGAFlags.BYPASSEXIST.isSet(flags) && !(m_url instanceof JSagaURL) && m_adaptor instanceof DataReaderAdaptor) {
            boolean exists = ((DataReaderAdaptor)m_adaptor).exists(m_url.getPath());
            if (! exists) {
                throw new DoesNotExist("Logical file does not exist: "+ m_url);
            }
        }
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public ObjectType getType() {
        return ObjectType.LOGICALFILE;
    }

    /** implements super.copy() */
    public void copy(URL target, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copy(target, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.CREATEPARENTS.or(Flags.OVERWRITE)));
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URL effectiveTarget = this._getEffectiveURL(target);
        if (m_adaptor instanceof DataCopyDelegated && m_url.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        m_url,
                        effectiveTarget,
                        overwrite, m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Logical file does not exist: "+ m_url, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DataCopy && m_url.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copy(
                        m_url.getPath(),
                        effectiveTarget.getHost(), effectiveTarget.getPort(), effectiveTarget.getPath(),
                        overwrite, m_url.getQuery());
            } catch (ParentDoesNotExist parentDoesNotExist) {
                throw new DoesNotExist("Target parent directory does not exist: "+effectiveTarget.resolve(new URL(".")), parentDoesNotExist);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Logical file does not exist: "+ m_url, doesNotExist);
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
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void copyFrom(URL source, int flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes effectiveFlags = new FlagsBytes(flags);
        if (effectiveFlags.contains(Flags.DEREFERENCE)) {
            this._dereferenceEntry().copyFrom(source, effectiveFlags.remove(Flags.DEREFERENCE));
            return; //==========> EXIT
        }
        effectiveFlags.checkAllowed(Flags.DEREFERENCE.or(Flags.OVERWRITE));
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URL effectiveSource = this._getEffectiveURL(source);
        if (m_adaptor instanceof DataCopyDelegated && m_url.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        effectiveSource,
                        m_url,
                        overwrite, m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Logical file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+ m_url, alreadyExists);
            }
        } else if (m_adaptor instanceof DataCopy && m_url.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copyFrom(
                        effectiveSource.getHost(), effectiveSource.getPort(), effectiveSource.getPath(),
                        m_url.getPath(),
                        overwrite, m_url.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Logical file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+ m_url, alreadyExists);
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
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public NSDirectory openDir(URL absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        return new LogicalDirectoryImpl(this, super._resolveAbsoluteURL(absolutePath), flags);
    }

    public NSEntry open(URL absolutePath, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (URLFactory.isDirectory(absolutePath)) {
            return new LogicalDirectoryImpl(this, super._resolveAbsoluteURL(absolutePath), flags);
        } else {
            return new LogicalFileImpl(this, super._resolveAbsoluteURL(absolutePath), flags);
        }
    }

    public void addLocation(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriter) {
            ((LogicalWriter)m_adaptor).addLocation(m_url.getPath(), name, m_url.getQuery());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void removeLocation(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriter) {
            ((LogicalWriter)m_adaptor).removeLocation(m_url.getPath(), name, m_url.getQuery());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void updateLocation(URL nameOld, URL nameNew) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalWriter) {
            ((LogicalWriter)m_adaptor).addLocation(m_url.getPath(), nameNew, m_url.getQuery());
            ((LogicalWriter)m_adaptor).removeLocation(m_url.getPath(), nameOld, m_url.getQuery());
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public List<URL> listLocations() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalReader) {
            try {
                String[] array = ((LogicalReader)m_adaptor).listLocations(m_url.getPath(), m_url.getQuery());
                List<URL> list = new ArrayList<URL>();
                try {
                    for (String location : array) {
                        list.add(new URL(location));
                    }
                } catch (BadParameter e) {
                    throw new NoSuccess(e);
                }
                return list;
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Logical file does not exist: "+ m_url, doesNotExist);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }

    public void replicate(URL name, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        if (m_adaptor instanceof LogicalReader && m_adaptor instanceof LogicalWriter) {
            this._replicate_step1(name, flags);
            this._replicate_step2(name);
        } else {
            throw new NotImplemented("Not supported for this protocol: "+ m_url.getScheme(), this);
        }
    }
    public void replicate(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        this.replicate(name, Flags.NONE.getValue());
    }

    private void _replicate_step1(URL physicalTarget, int flags) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        final String MESSAGE = "Failed to copy replica (state is still consistent)";
        try {
            List<URL> locations = this.listLocations();
            if (locations==null || locations.size()==0) {
                throw new IncorrectState("Can not replicate a logical entry with empty location", this);
            }
            URL physicalSource = locations.get(0);
            NSEntry physicalSourceEntry = NSFactory.createNSEntry(m_session, physicalSource, Flags.NONE.getValue());
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
        }
    }
    private void _replicate_step2(URL name) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
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
