package fr.in2p3.jsaga.impl.logicalfile.copy;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.data.flags.FlagsBytes;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import fr.in2p3.jsaga.impl.logicalfile.LogicalFileImpl;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalFileCopyFrom
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 juil. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalFileCopyFrom {
    private Session m_session;
    private LogicalFileImpl m_targetFile;
    private DataAdaptor m_adaptor;

    /** constructor */
    public LogicalFileCopyFrom(Session session, LogicalFileImpl targetFile, DataAdaptor adaptor) throws NotImplemented, BadParameter, IncorrectState, Timeout, NoSuccess {
        m_session = session;
        m_targetFile = targetFile;
        m_adaptor = adaptor;
    }

    public void copyFrom(URL effectiveSource, FlagsBytes effectiveFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        boolean overwrite = effectiveFlags.contains(Flags.OVERWRITE);
        URL target = m_targetFile.getURL();
        if (m_adaptor instanceof DataCopyDelegated && target.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        effectiveSource,
                        target,
                        overwrite, target.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Logical file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+target, alreadyExists);
            }
        } else if (m_adaptor instanceof DataCopy && target.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copyFrom(
                        effectiveSource.getHost(), effectiveSource.getPort(), effectiveSource.getPath(),
                        target.getPath(),
                        overwrite, target.getQuery());
            } catch (DoesNotExist doesNotExist) {
                throw new DoesNotExist("Logical file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExists alreadyExists) {
                throw new IncorrectState("Target entry already exists: "+target, alreadyExists);
            }
        } else if (m_adaptor instanceof LogicalWriter) {
            Protocol descriptor = Configuration.getInstance().getConfigurations().getProtocolCfg().findProtocol(effectiveSource.getScheme());
            if (descriptor.hasLogical() && descriptor.getLogical()) {
                this.getFromLogicalFile(effectiveSource, effectiveFlags);
            } else {
                throw new BadParameter("Maybe what you want to do is to register to logical file the following location: "+effectiveSource);
            }
        } else {
            throw new NotImplemented("Not supported for this protocol: "+target.getScheme());
        }
    }


    private void getFromLogicalFile(URL source, FlagsBytes sourceFlags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        // get location of source physical file
        LogicalFile sourceLogicalFile = this.createSourceLogicalFile(source, sourceFlags);
        try {
            List<URL> sourceLocations = sourceLogicalFile.listLocations();
            if (sourceLocations!=null && sourceLocations.size()>0) {
                // remove all target locations
                try {
                    List<URL> targetLocations = m_targetFile.listLocations();
                    for (int i=0; targetLocations!=null && i<targetLocations.size(); i++) {
                        m_targetFile.removeLocation(targetLocations.get(i));
                    }
                } catch(IncorrectState e) {
                    // ignore if target logical file does not exist
                }
                // add all source locations
                for (int i=0; sourceLocations!=null && i<sourceLocations.size(); i++) {
                    m_targetFile.addLocation(sourceLocations.get(i));
                }
            }
        } finally {
            sourceLogicalFile.close();
        }
    }

    private LogicalFile createSourceLogicalFile(URL source, FlagsBytes flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess, IncorrectURL {
        int correctedFlags = flags.remove(Flags.OVERWRITE);
        try {
            return LogicalFileFactory.createLogicalFile(m_session, source, correctedFlags);
        } catch (AlreadyExists e) {
            throw new NoSuccess("Unexpected exception: AlreadyExists", e);
        }
    }
}
