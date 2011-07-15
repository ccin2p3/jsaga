package fr.in2p3.jsaga.impl.logicalfile.copy;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.impl.logicalfile.AbstractSyncLogicalFileImpl;
import fr.in2p3.jsaga.impl.namespace.FlagsHelper;
import fr.in2p3.jsaga.impl.namespace.JSAGAFlags;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

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
    private static final String JSAGA_FACTORY = Base.getSagaFactory();

    private Session m_session;
    private AbstractSyncLogicalFileImpl m_targetFile;
    private DataAdaptor m_adaptor;

    /** constructor */
    public LogicalFileCopyFrom(Session session, AbstractSyncLogicalFileImpl targetFile, DataAdaptor adaptor) throws NotImplementedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_session = session;
        m_targetFile = targetFile;
        m_adaptor = adaptor;
    }

    public void copyFrom(URL effectiveSource, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        boolean overwrite = Flags.OVERWRITE.isSet(flags);
        URL target = m_targetFile.getURL();
        if (m_adaptor instanceof DataCopyDelegated && target.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        effectiveSource,
                        target,
                        overwrite, target.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new DoesNotExistException("Logical file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExistsException alreadyExists) {
                throw new IncorrectStateException("Target entry already exists: "+target, alreadyExists);
            }
        } else if (m_adaptor instanceof DataCopy && target.getScheme().equals(effectiveSource.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copyFrom(
                        effectiveSource.getHost(), effectiveSource.getPort(), effectiveSource.getPath(),
                        target.getPath(),
                        overwrite, target.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new DoesNotExistException("Logical file does not exist: "+effectiveSource, doesNotExist.getCause());
            } catch (AlreadyExistsException alreadyExists) {
                throw new IncorrectStateException("Target entry already exists: "+target, alreadyExists);
            }
        } else if (m_adaptor instanceof LogicalWriter) {
            // todo: check that source is not a physical entry
            this.getFromLogicalFile(effectiveSource, flags);
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+target.getScheme());
        }
    }


    private void getFromLogicalFile(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        // get location of source physical file
        LogicalFile sourceLogicalFile = this.createSourceLogicalFile(source, flags);
        try {
            List<URL> sourceLocations = sourceLogicalFile.listLocations();
            if (sourceLocations!=null && sourceLocations.size()>0) {
                // remove all target locations
                try {
                    List<URL> targetLocations = m_targetFile.listLocationsSync();
                    for (int i=0; targetLocations!=null && i<targetLocations.size(); i++) {
                        m_targetFile.removeLocationSync(targetLocations.get(i));
                    }
                } catch(IncorrectStateException e) {
                    // ignore if target logical file does not exist
                }
                // add all source locations
                for (int i=0; sourceLocations!=null && i<sourceLocations.size(); i++) {
                    m_targetFile.addLocationSync(sourceLocations.get(i));
                }
            }
        } finally {
            sourceLogicalFile.close();
        }
    }

    private LogicalFile createSourceLogicalFile(URL source, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException, IncorrectURLException {
        int correctedFlags = flags;
        correctedFlags = new FlagsHelper(correctedFlags).remove(JSAGAFlags.PRESERVETIMES, Flags.OVERWRITE);
        try {
            return LogicalFileFactory.createLogicalFile(JSAGA_FACTORY, m_session, source, correctedFlags);
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException("Unexpected exception", e);
        }
    }
}
