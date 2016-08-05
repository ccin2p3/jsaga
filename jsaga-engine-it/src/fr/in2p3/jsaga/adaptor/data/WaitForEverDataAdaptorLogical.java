package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   WaitForEverDataAdaptorLogical
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class WaitForEverDataAdaptorLogical extends WaitForEverDataAdaptorAbstract implements LogicalReader, LogicalWriter {
    public String getType() {
        return "waitforever-logical";
    }

    ////////////////////////////////////////// interfaces Logical* //////////////////////////////////////////

    public String[] listLocations(String logicalEntry, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        hang();
        return new String[0];
    }

    public void create(String logicalEntry, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        hang();
    }

    public void addLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, BadParameterException, IncorrectStateException, TimeoutException, NoSuccessException {
        hang();
    }
    public void removeLocation(String logicalEntry, URL replicaEntry, String additionalArgs) throws PermissionDeniedException, BadParameterException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        hang();
    }
}
