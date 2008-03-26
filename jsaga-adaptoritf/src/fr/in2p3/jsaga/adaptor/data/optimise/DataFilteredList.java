package fr.in2p3.jsaga.adaptor.data.optimise;

import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataFilteredList
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface DataFilteredList extends DataReaderAdaptor {
    /**
     * Lists the directories under <code>absolutePath</code>, files and links are ignored.
     * @param absolutePath the directory containing entries to list.
     * @param additionalArgs adaptor specific arguments
     * @return the entry attributes.
     * @throws DoesNotExist if <code>absolutePath</code> does not exist.
     */
    public FileAttributes[] listDirectories(String absolutePath, String additionalArgs)
        throws PermissionDenied, DoesNotExist, Timeout, NoSuccess;    
}
