package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.*;

import java.lang.Exception;
import java.util.Map;
import java.util.Vector;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Gsiftp1DataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class Gsiftp1DataAdaptor extends GsiftpDataAdaptorAbstract {
    public String[] getSchemeAliases() {
        return new String[]{"gsiftp-old", "gridftp-old", "gsiftp1", "gridftp1"};
    }

    /** setting protection level is not supported */
    public Usage getUsage() {
        return null;
    }

    /** setting protection level is not supported */
    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;
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

    /** MLSD command is not supported */
    public FileAttributes[] listAttributes(String absolutePath) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Vector v;
        try {
            m_client.setPassiveMode(true);
            v = m_client.list();
        } catch (Exception e) {
            try {
                throw rethrowException(e);
            } catch (BadParameter badParameter) {
                throw new NoSuccess("Unexpected exception", e);
            }
        }
        FileAttributes[] ret = new FileAttributes[v.size()];
        for (int i=0; i<v.size(); i++) {
            String entryName = (String) v.get(i);
            ret[i] = new Gsiftp1FileAttributes(entryName);
        }
        return ret;
    }
}
