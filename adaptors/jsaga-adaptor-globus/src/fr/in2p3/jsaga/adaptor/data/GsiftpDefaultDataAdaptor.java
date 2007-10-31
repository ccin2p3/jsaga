package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.globus.ftp.exception.UnexpectedReplyCodeException;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpDefaultDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpDefaultDataAdaptor extends GsiftpDataAdaptorAbstract {
    public String[] getSchemeAliases() {
        throw new RuntimeException("INTERNAL ERROR: this should never occur");
    }

    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;
    }

    public boolean isDirectory(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        throw new NoSuccess("INTERNAL ERROR: this should never occur");
    }

    public FileAttributes[] listAttributes(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        throw new NoSuccess("INTERNAL ERROR: this should never occur");
    }

    protected void rethrowParsedException(UnexpectedReplyCodeException e) throws DoesNotExist, AlreadyExists, PermissionDenied, NoSuccess {
        throw new NoSuccess(e);
    }
}
