package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.globus.ftp.*;
import org.globus.ftp.exception.UnexpectedReplyCodeException;
import org.globus.ftp.exception.ServerException;
import org.ogf.saga.error.*;

import java.lang.Exception;
import java.util.*;
import java.io.IOException;

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
public class GsiftpDCacheDataAdaptor extends GsiftpDataAdaptorAbstract {
    public String getType() {
        return "gsiftp-dcache";
    }

    /** setting protection level is not supported */
    public Usage getUsage() {
        return null;
    }

    /** setting protection level is not supported */
    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;
    }

    /** RNFR command is not supported */
    public boolean exists(String absolutePath) throws PermissionDenied, Timeout, NoSuccess {
        try {
            m_client.getSize(absolutePath);
            return true;
        } catch (ServerException e) {
            if (e.getCode() == 1) {
                return false;
            } else {
                throw new NoSuccess(e);
            }
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
    }

    /** MLST command is not supported */
    public boolean isDirectory(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        boolean isDirectory;
        String savDir;
        try {
            savDir = m_client.getCurrentDir();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        try {
            m_client.changeDir(absolutePath);
            isDirectory = true;
        } catch(Exception e) {
            isDirectory = false;
        } finally {
            try {
                m_client.changeDir(savDir);
            } catch(Exception e) {/*ignore*/}
        }
        return isDirectory;
    }

    //fixme: listAttributes does not work
    /** MLSD command is not supported */
    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Vector v;
        try {
            m_client.setMode(GridFTPSession.MODE_STREAM);
            m_client.setPassiveMode(true);
            String sav = m_client.getCurrentDir();
            m_client.changeDir(absolutePath);
            v = m_client.list();
            m_client.changeDir(sav);
        } catch (Exception e) {
            try {
                throw rethrowException(e);
            } catch (BadParameter badParameter) {
                throw new NoSuccess("Unexpected exception", e);
            }
        }
        FileAttributes[] ret = new FileAttributes[v.size()];
        for (int i=0; i<v.size(); i++) {
            FileInfo entry = (FileInfo) v.get(i);
            ret[i] = new Gsiftp1FileAttributes(entry);
        }
        return ret;
    }

    protected void rethrowParsedException(UnexpectedReplyCodeException e) throws DoesNotExist, AlreadyExists, PermissionDenied, NoSuccess {
        String message = e.getReply().getMessage();
        if (message.indexOf("not a plain file") > -1) {
            throw new DoesNotExist(e);
        } else if (message.indexOf("exists") > -1) {
            throw new AlreadyExists(e);
        } else if (message.indexOf("Permission denied") > -1) {
            throw new PermissionDenied(e);
        } else {
            throw new NoSuccess(e);
        }
    }
}
