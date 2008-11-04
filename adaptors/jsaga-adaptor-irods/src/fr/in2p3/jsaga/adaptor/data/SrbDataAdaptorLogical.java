package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.optimise.LogicalReaderMetaDataExtended;
import fr.in2p3.jsaga.adaptor.data.optimise.expr.BooleanExpr;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SrbDataAdaptorLogical
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SrbDataAdaptorLogical extends SrbDataAdaptor implements LogicalReaderMetaDataExtended {
    public String[] listLocations(String logicalEntry, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map listMetaData(String logicalEntry, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public FileAttributes[] listAttributes(String logicalDir, Map keyValuePatterns, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new FileAttributes[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public FileAttributes[] listAttributes(String logicalDir, BooleanExpr filter, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        return new FileAttributes[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
