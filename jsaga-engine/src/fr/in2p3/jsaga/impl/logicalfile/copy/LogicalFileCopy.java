package fr.in2p3.jsaga.impl.logicalfile.copy;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytesPhysical;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import fr.in2p3.jsaga.impl.logicalfile.LogicalFileImpl;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalFileCopy
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 juil. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalFileCopy {
    private Session m_session;
    private LogicalFileImpl m_sourceFile;
    private DataAdaptor m_adaptor;

    /** constructor */
    public LogicalFileCopy(Session session, LogicalFileImpl sourceFile, DataAdaptor adaptor) throws NotImplemented, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_session = session;
        m_sourceFile = sourceFile;
        m_adaptor = adaptor;
    }

    public void copy(URL effectiveTarget, FlagsBytes effectiveFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URL source = m_sourceFile.getURL();
        if (m_adaptor instanceof DataCopyDelegated && source.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        source,
                        effectiveTarget,
                        overwrite, source.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Logical file does not exist: "+source, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DataCopy && source.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copy(
                        source.getPath(),
                        effectiveTarget.getHost(), effectiveTarget.getPort(), effectiveTarget.getPath(),
                        overwrite, source.getQuery());
            } catch (ParentDoesNotExist parentDoesNotExist) {
                throw new DoesNotExist("Target parent directory does not exist: "+effectiveTarget.resolve(new URL(".")), parentDoesNotExist);
            } catch (DoesNotExist doesNotExist) {
                throw new IncorrectState("Logical file does not exist: "+source, doesNotExist);
            } catch (AlreadyExists alreadyExists) {
                throw new AlreadyExists("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof LogicalReader) {
            Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(effectiveTarget.getScheme());
            if (descriptor.hasLogical() && descriptor.getLogical()) {
                this.putToLogicalFile(effectiveTarget, effectiveFlags);
            } else {
                this.putToPhysicalFile(effectiveTarget, effectiveFlags);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+source.getScheme());
        }
    }

    private void putToPhysicalFile(URL target, FlagsBytes targetFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        // get location of source entry (may be logical or physical
        List<URL> sourceLocations = m_sourceFile.listLocations();
        if (sourceLocations!=null && sourceLocations.size()>0) {
            // open source entry
            URL source = sourceLocations.get(0);
            NSEntry sourceEntry = this.createSourceNSEntry(source);

            // copy
            sourceEntry.copy(target, targetFlags.remove(Flags.NONE));

            // close source entry (but not the source logical file)
            sourceEntry.close();
        } else {
            throw new NoSuccess("No location found for logical file: "+m_sourceFile.getURL());
        }
    }

    private void putToLogicalFile(URL target, FlagsBytes targetFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        // get location of source physical file
        List<URL> sourceLocations = m_sourceFile.listLocations();
        if (sourceLocations!=null && sourceLocations.size()>0) {
            // open target logical file
            LogicalFile targetLogicalFile = this.createTargetLogicalFile(target, targetFlags);
            try {
                // copy
                if (targetFlags.contains(Flags.OVERWRITE)) {
                    // remove all target locations
                    try {
                        List<URL> targetLocations = targetLogicalFile.listLocations();
                        for (int i=0; targetLocations !=null && i< targetLocations.size(); i++) {
                            targetLogicalFile.removeLocation(targetLocations.get(i));
                        }
                    } catch(IncorrectState e) {
                        // ignore if target logical file does not exist
                    }
                }
                // add all source locations
                for (int i=0; sourceLocations!=null && i<sourceLocations.size(); i++) {
                    targetLogicalFile.addLocation(sourceLocations.get(i));
                }
            } catch (DoesNotExist e) {
                throw new NoSuccess("Unexpected exception: DoesNotExist", e);
            } finally {
                // close target
                targetLogicalFile.close();
            }
        }
    }

    private NSEntry createSourceNSEntry(URL source) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        try {
            return NSFactory.createNSEntry(m_session, source, Flags.NONE.getValue());
        } catch (AlreadyExists e) {
            throw new NoSuccess("Unexpected exception: AlreadyExists");
        } catch (DoesNotExist doesNotExist) {
            throw new IncorrectState("Source physical file does not exist: "+source, doesNotExist);
        }
    }

    private LogicalFile createTargetLogicalFile(URL target, FlagsBytes flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess, IncorrectURL {
        FlagsBytes correctedBytes = flags.or(FlagsBytesPhysical.WRITE).or(FlagsBytes.CREATE);
        int correctedFlags =
                (correctedBytes.contains(Flags.OVERWRITE)
                        ? correctedBytes.remove(Flags.OVERWRITE)
                        : correctedBytes.add(Flags.EXCL));
        try {
            return LogicalFileFactory.createLogicalFile(m_session, target, correctedFlags);
        } catch (DoesNotExist e) {
            throw new NoSuccess("Unexpected exception: DoesNotExist", e);
        } catch (AlreadyExists alreadyExists) {
            throw new AlreadyExists("Target entry already exists: "+target, alreadyExists.getCause());
        }
    }
}
