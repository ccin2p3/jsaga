package fr.in2p3.jsaga.adaptor.data;

import java.io.IOException;

import org.globus.ftp.exception.ServerException;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

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
public class GsiftpStoRMDataAdaptor extends Gsiftp2DataAdaptor {
    public String getType() {
        return "gsiftp-storm";
    }

    /*
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
    */
    
    protected void checkExists(String absolutePath) throws AlreadyExistsException, NoSuccessException, PermissionDeniedException, BadParameterException, TimeoutException, ParentDoesNotExist {
        boolean exists;
        try {
            exists = m_client.exists(absolutePath);
        } catch (Exception e) {
            try {
                throw rethrowExceptionFull(e);
            } catch (DoesNotExistException e1) {
                throw new ParentDoesNotExist(e);
            }
        }
        if (exists) {
        	/*
        	 * The StoRM PrepareToPut does create empty file
        	 * So, the "exists" method return true
        	 * If the file is empty, do not throw AlreadyExistsException
        	 */
        	try {
				if (m_client.getSize(absolutePath) > 0)
					throw new AlreadyExistsException("File already exists: "+absolutePath);
			} catch (ServerException e) {
				throw new NoSuccessException(e);
			} catch (IOException e) {
				throw new NoSuccessException(e);
			}
        }
    }
    

    
}
