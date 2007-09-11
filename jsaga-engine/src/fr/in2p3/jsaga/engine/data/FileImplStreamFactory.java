package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.read.FileReader;
import fr.in2p3.jsaga.adaptor.data.write.FileWriter;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NamespaceFactory;
import org.ogf.saga.session.Session;

import java.io.InputStream;
import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileImplStreamFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   29 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileImplStreamFactory {
    public static InputStream createInputStream(FileImpl entry) throws NotImplemented, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        FileReader adaptor = (FileReader) entry.m_adaptor;
        String absolutePath = entry.getURI().getPath();
        return adaptor.getInputStream(absolutePath);
    }

    public static OutputStream createOutputStream(FileImpl entry, boolean overwrite) throws NotImplemented, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, DoesNotExist, Timeout, NoSuccess {
        FileWriter adaptor = (FileWriter) entry.m_adaptor;
        String parentAbsolutePath = entry._getParentDirURI().getPath();
        String fileName = entry.getName();
        boolean exclusive = !overwrite;
        boolean append = false;
        return adaptor.getOutputStream(parentAbsolutePath, fileName, exclusive, append);
    }

    public static InputStream createInputStream(Session session, URI uri, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        FileImpl sourceFile;
        try {
            sourceFile = (FileImpl) NamespaceFactory.createNamespaceEntry(session, uri, flags);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        } catch (AlreadyExists e) {
            throw new NoSuccess("Unexpected exception", e);
        }
        return sourceFile.m_inStream;
    }

    public static OutputStream createOutputStream(Session session, URI uri, Flags flags) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, AlreadyExists, Timeout, NoSuccess {
        FileImpl targetFile;
        try {
            targetFile = (FileImpl) NamespaceFactory.createNamespaceEntry(session, uri, flags);
        } catch (IncorrectURL e) {
            throw new NoSuccess(e);
        } catch (IncorrectSession e) {
            throw new NoSuccess(e);
        } catch (DoesNotExist e) {
            throw new NoSuccess("Unexpected exception", e);
        }
        return targetFile.m_outStream;
    }
}
