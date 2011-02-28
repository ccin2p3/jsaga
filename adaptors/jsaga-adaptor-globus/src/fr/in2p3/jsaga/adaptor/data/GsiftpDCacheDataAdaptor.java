package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import org.globus.ftp.exception.UnexpectedReplyCodeException;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpDCacheDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpDCacheDataAdaptor extends Gsiftp2DataAdaptor {
    public String getType() {
        return "gsiftp-dcache";
    }

    /** setting protection level is not supported */
    public Usage getUsage() {
        return null;
    }

    /** setting protection level is not supported */
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    protected void rethrowParsedException(UnexpectedReplyCodeException e) throws DoesNotExistException, AlreadyExistsException, PermissionDeniedException, NoSuccessException {
        String message = e.getReply().getMessage();
        if (message.indexOf("not a plain file") > -1 || message.indexOf("Local error") > -1 || message.indexOf("File not found") > -1) {
            throw new DoesNotExistException(e);
        } else if (message.indexOf("exists") > -1) {
            throw new AlreadyExistsException(e);
        } else if (message.indexOf("Permission denied") > -1) {
            throw new PermissionDeniedException(e);
        } else {
            throw new NoSuccessException(e);
        }
    }
}
