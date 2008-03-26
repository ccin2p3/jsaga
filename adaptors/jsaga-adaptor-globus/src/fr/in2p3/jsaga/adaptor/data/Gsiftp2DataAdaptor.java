package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.exception.UnexpectedReplyCodeException;
import org.ogf.saga.error.*;

import java.io.IOException;
import java.lang.Exception;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Gsiftp2DataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class Gsiftp2DataAdaptor extends GsiftpDataAdaptorAbstract {
    private static final String PROTECTION = "Protection";

    public String getType() {
        return "gsiftp-v2";
    }

    public Usage getUsage() {
        return new UOptional(PROTECTION);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return new Default[]{new Default(PROTECTION, "none")};
    }

    /** override super.connect() to set data channel protection level */
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        super.connect(userInfo, host, port, basePath, attributes);
        if (attributes!=null && attributes.containsKey(PROTECTION)) {
            String value = ((String) attributes.get(PROTECTION)).toLowerCase();

            // get protection level from attributes
            int protection;
            if (value.equalsIgnoreCase("none")) {
                protection = GridFTPSession.PROTECTION_CLEAR;
            } else if (value.equalsIgnoreCase("integrity")) {
                protection = GridFTPSession.PROTECTION_SAFE;
            } else if (value.equalsIgnoreCase("confidentiality")) {
                protection = GridFTPSession.PROTECTION_CONFIDENTIAL;
            } else if (value.equalsIgnoreCase("privacy")) {
                protection = GridFTPSession.PROTECTION_PRIVATE;
            } else {
                throw new BadParameter("Attribute '"+PROTECTION+"' has unexpected value: "+value);
            }

            // set protection level to data channel
            try {
                m_client.setDataChannelProtection(protection);
                m_client.setProtectionBufferSize(16384);
            } catch (IOException e) {
                throw new NoSuccess(e);
            } catch (ServerException e) {
                throw new NoSuccess(e);
            }
        }
    }

    /** override super.isDirectory() to use mlst command */
    public boolean isDirectory(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        try {
            m_client.setPassiveMode(false);
            MlsxEntry entry = m_client.mlst(absolutePath);
            return entry.get("type").endsWith("dir");
        } catch (Exception e) {
            try {
                throw rethrowException(e);
            } catch (BadParameter badParameter) {
                throw new NoSuccess("Unexpected exception", e);
            }
        }
    }

    /** override super.listAttributes() to use mlsd command */
    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        Vector v;
        try {
            m_client.setMode(GridFTPSession.MODE_STREAM);
            m_client.setPassiveMode(true);
            v = m_client.mlsd(absolutePath);
        } catch (Exception e) {
            try {
                throw rethrowException(e);
            } catch (BadParameter badParameter) {
                throw new NoSuccess("Unexpected exception", e);
            }
        }
        List files = new ArrayList();
        for (int i=0; i<v.size(); i++) {
            MlsxEntry entry = (MlsxEntry) v.get(i);
            try {
                files.add(new Gsiftp2FileAttributes(entry));
            } catch(DoesNotExist e) {
                // ignore this entry: ., .. or null
            }
        }
        return (FileAttributes[]) files.toArray(new FileAttributes[files.size()]);
    }

    protected void rethrowParsedException(UnexpectedReplyCodeException e) throws DoesNotExist, AlreadyExists, PermissionDenied, NoSuccess {
        String message = e.getReply().getMessage();
        if (message.indexOf("No such") > -1) {
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
