package fr.in2p3.jsaga.impl.logicalfile.copy;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.descriptors.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.factories.DataAdaptorFactory;
import fr.in2p3.jsaga.impl.logicalfile.AbstractSyncLogicalFileImpl;
import fr.in2p3.jsaga.impl.namespace.FlagsHelper;
import fr.in2p3.jsaga.impl.namespace.JSAGAFlags;
import org.ogf.saga.error.*;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.logicalfile.LogicalFileFactory;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
    private AbstractSyncLogicalFileImpl m_sourceFile;
    private DataAdaptor m_adaptor;

    /** constructor */
    public LogicalFileCopy(Session session, AbstractSyncLogicalFileImpl sourceFile, DataAdaptor adaptor) throws NotImplementedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        m_session = session;
        m_sourceFile = sourceFile;
        m_adaptor = adaptor;
    }

    public void copy(URL effectiveTarget, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        boolean overwrite = Flags.OVERWRITE.isSet(flags);
        URL source = m_sourceFile.getURL();
        if (m_adaptor instanceof DataCopyDelegated && source.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopyDelegated)m_adaptor).requestTransfer(
                        source,
                        effectiveTarget,
                        overwrite, source.getQuery());
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Logical file does not exist: "+source, doesNotExist);
            } catch (AlreadyExistsException alreadyExists) {
                throw new AlreadyExistsException("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof DataCopy && source.getScheme().equals(effectiveTarget.getScheme())) {
            try {
                ((DataCopy)m_adaptor).copy(
                        source.getPath(),
                        effectiveTarget.getHost(), effectiveTarget.getPort(), effectiveTarget.getPath(),
                        overwrite, source.getQuery());
            } catch (ParentDoesNotExist parentDoesNotExist) {
                throw new DoesNotExistException("Target parent directory does not exist: "+effectiveTarget.resolve(URLFactory.createURL(".")), parentDoesNotExist);
            } catch (DoesNotExistException doesNotExist) {
                throw new IncorrectStateException("Logical file does not exist: "+source, doesNotExist);
            } catch (AlreadyExistsException alreadyExists) {
                throw new AlreadyExistsException("Target entry already exists: "+effectiveTarget, alreadyExists.getCause());
            }
        } else if (m_adaptor instanceof LogicalReader) {
            DataAdaptor targetAdaptor = new DataAdaptorFactory(AdaptorDescriptors.getInstance()).getDataAdaptor(effectiveTarget, m_session);
            if (targetAdaptor instanceof LogicalWriter) {
                this.putToLogicalFile(effectiveTarget, flags);
            } else {
                this.putToPhysicalFile(effectiveTarget, flags);
            }
        } else {
            throw new NotImplementedException("Not supported for this protocol: "+source.getScheme());
        }
    }

    private void putToPhysicalFile(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        // get location of source entry (may be logical or physical
        List<URL> sourceLocations = m_sourceFile.listLocationsSync();
        if (sourceLocations!=null && sourceLocations.size()>0) {
            // open source entry
            URL source = sourceLocations.get(0);
            NSEntry sourceEntry = this.createSourceNSEntry(source);

            // copy
            try {
    			sourceEntry.copy(target, flags);
    		} finally {
                // close source entry (but not the source logical file)
                sourceEntry.close();
    		}
        } else {
            throw new NoSuccessException("No location found for logical file: "+m_sourceFile.getURL());
        }
    }

    private void putToLogicalFile(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        // get location of source physical file
        List<URL> sourceLocations = m_sourceFile.listLocationsSync();
        if (sourceLocations!=null && sourceLocations.size()>0) {
            // open target logical file
            LogicalFile targetLogicalFile = this.createTargetLogicalFile(target, flags);
            try {
                // copy
                if (Flags.OVERWRITE.isSet(flags)) {
                    // remove all target locations
                    try {
                        List<URL> targetLocations = targetLogicalFile.listLocations();
                        for (int i=0; targetLocations !=null && i< targetLocations.size(); i++) {
                            targetLogicalFile.removeLocation(targetLocations.get(i));
                        }
                    } catch(IncorrectStateException e) {
                        // ignore if target logical file does not exist
                    }
                }
                // add all source locations
                for (int i=0; sourceLocations!=null && i<sourceLocations.size(); i++) {
                    targetLogicalFile.addLocation(sourceLocations.get(i));
                }
            } catch (DoesNotExistException e) {
                throw new NoSuccessException("Unexpected exception", e);
            } finally {
                // close target
                targetLogicalFile.close();
            }
        }
    }

    private NSEntry createSourceNSEntry(URL source) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        try {
            return NSFactory.createNSEntry(m_session, source, Flags.NONE.getValue());
        } catch (AlreadyExistsException e) {
            throw new NoSuccessException("Unexpected exception", e);
        } catch (DoesNotExistException doesNotExist) {
            throw new IncorrectStateException("Source physical file does not exist: "+source, doesNotExist);
        }
    }

    private LogicalFile createTargetLogicalFile(URL target, int flags) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, BadParameterException, IncorrectStateException, AlreadyExistsException, TimeoutException, NoSuccessException, IncorrectURLException {
        int correctedFlags = flags;
        correctedFlags = new FlagsHelper(correctedFlags).add(Flags.WRITE, Flags.CREATE);
        correctedFlags = new FlagsHelper(correctedFlags).remove(JSAGAFlags.PRESERVETIMES);
        if (Flags.OVERWRITE.isSet(correctedFlags)) {
            correctedFlags = correctedFlags - Flags.OVERWRITE.getValue();
        } else {
            correctedFlags = correctedFlags + Flags.EXCL.getValue();
        }
        try {
            return LogicalFileFactory.createLogicalFile(m_session, target, correctedFlags);
        } catch (DoesNotExistException e) {
            throw new NoSuccessException("Unexpected exception", e);
        } catch (AlreadyExistsException alreadyExists) {
            throw new AlreadyExistsException("Target entry already exists: "+target, alreadyExists.getCause());
        }
    }
}
