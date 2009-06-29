package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.exception.UnexpectedReplyCodeException;
import org.ogf.saga.error.*;

import java.io.IOException;
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
        return new UOr(new U[]{
                new UOptional(TCP_BUFFER_SIZE),
                new UOptional(PROTECTION)
        });
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{new Default(PROTECTION, "none")};
    }

    /** override super.connect() to set data channel protection level */
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
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
                throw new BadParameterException("Attribute '"+PROTECTION+"' has unexpected value: "+value);
            }

            // set protection level to data channel
            try {
                m_client.setDataChannelProtection(protection);
                m_client.setProtectionBufferSize(16384);
            } catch (IOException e) {
                throw new NoSuccessException(e);
            } catch (ServerException e) {
                throw new NoSuccessException(e);
            }
        }
    }

    /** override super.getAttributes() to use mlst command */
    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        MlsxEntry entry;
        try {
            m_client.setPassiveMode(false);
            entry = m_client.mlst(absolutePath);
        } catch (Exception e) {
            try {
                throw rethrowException(e);
            } catch (BadParameterException badParameter) {
                throw new NoSuccessException("Unexpected exception", e);
            }
        }
        return new Gsiftp2FileAttributes(entry);
    }

    /** override super.listAttributes() to use mlsd command */
    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        Vector v;
        try {
            m_client.setMode(GridFTPSession.MODE_STREAM);
            m_client.setPassiveMode(true);
            v = m_client.mlsd(absolutePath);
        } catch (Exception e) {
            try {
                throw rethrowException(e);
            } catch (BadParameterException badParameter) {
                throw new NoSuccessException("Unexpected exception", e);
            }
        }
        List files = new ArrayList();
        for (int i=0; i<v.size(); i++) {
            MlsxEntry entry = (MlsxEntry) v.get(i);
            if (absolutePath.equals(entry.getFileName())) {
                // ignore this entry: absolutePath
                continue;
            }
            try {
                files.add(new Gsiftp2FileAttributes(entry));
            } catch(DoesNotExistException e) {
                // ignore this entry: ., .. or null
            }
        }
        return (FileAttributes[]) files.toArray(new FileAttributes[files.size()]);
    }

    protected void rethrowParsedException(UnexpectedReplyCodeException e) throws DoesNotExistException, AlreadyExistsException, PermissionDeniedException, NoSuccessException {
        String message = e.getReply().getMessage();
        if (message.indexOf("No such") > -1) {
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
