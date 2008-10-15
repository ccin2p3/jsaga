package fr.in2p3.jsaga.adaptor.data.read;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataReaderAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface DataReaderAdaptor extends DataAdaptor {
    /**
     * Tests this entry for existing.
     * @param absolutePath the absolute path of the entry.
     * @param additionalArgs adaptor specific arguments.
     * @return true if the entry exists.
     */
    public boolean exists(String absolutePath, String additionalArgs)
        throws PermissionDenied, Timeout, NoSuccess;

    /**
     * Tests this entry for being a directory.
     * @param absolutePath the absolute path of the entry.
     * @param additionalArgs adaptor specific arguments.
     * @return true if the entry is a directory.
     * @throws DoesNotExist if <code>absolutePath</code> does not exist.
     */
    public boolean isDirectory(String absolutePath, String additionalArgs)
        throws PermissionDenied, DoesNotExist, Timeout, NoSuccess;

    /**
     * Get the file attributes of the entry <code>absolutePath</code>.
     * @param absolutePath the absolute path of the entry.
     * @param additionalArgs adaptor specific arguments.
     * @return the file attributes.
     * @throws DoesNotExist if <code>absolutePath</code> does not exist.
     */
    public FileAttributes getAttributes(String absolutePath, String additionalArgs)
        throws PermissionDenied, DoesNotExist, Timeout, NoSuccess;

    /**
     * Lists all the entries in the directory <code>absolutePath</code>.
     * @param absolutePath the directory containing entries to list.
     * @param additionalArgs adaptor specific arguments.
     * @return the entry attributes.
     * @throws BadParameter if <code>absolutePath</code> is not a directory.
     * @throws DoesNotExist if <code>absolutePath</code> does not exist.
     */
    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs)
        throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess;
}
