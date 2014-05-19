package fr.in2p3.jsaga.adaptor.data;

import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   GsiftpDPMDataAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   2 juil. 2009
 * ***************************************************
 * Description:                                      */
/**
 * workaround for DPM
 */
public class GsiftpDPMDataAdaptor extends Gsiftp2DataAdaptor {

    public String getType() {
        return "gsiftp-dpm";
    }

    
    // DPM from version 1.8.8 writes an empty file in GridFTP probably at srmPrepareToPut, so if exclusive flag is
    // used, the parent class will throw a "AlreadyExistException...
    @Override
    protected void checkExists(String absolutePath) throws AlreadyExistsException, NoSuccessException, PermissionDeniedException, BadParameterException, TimeoutException, ParentDoesNotExist {
        try {
            super.checkExists(absolutePath);
        } catch (AlreadyExistsException aee) {
            // The file has just been written by srmPrepareToPut and has a size of 0
            try {
                FileAttributes fa = this.getAttributes(absolutePath, null);
                if (fa.getType() == FileAttributes.TYPE_FILE && fa.getSize() == 0) {
                    return;
                } else {
                    throw aee;
                }
            } catch (DoesNotExistException e) {
                throw new ParentDoesNotExist(e);
            }
        }
    }
    

}
