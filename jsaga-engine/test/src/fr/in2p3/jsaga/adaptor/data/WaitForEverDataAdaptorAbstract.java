package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.WaitForEverAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   WaitForEverDataAdaptorAbstract
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class WaitForEverDataAdaptorAbstract extends WaitForEverAdaptorAbstract implements DataReaderAdaptor, DataWriterAdaptor {
    ////////////////////////////////////// interface DataReaderAdaptor //////////////////////////////////////

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        // do nothing
        return true;
    }
    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        hang();
        return new FileAttributes(){};
    }
    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        hang();
        return new FileAttributes[0];
    }

    ////////////////////////////////////// interface DataWriterAdaptor //////////////////////////////////////

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        hang();
    }
    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        hang();
    }
    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        hang();
    }
}
