package fr.in2p3.jsaga.adaptor.data.link;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LinkAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface LinkAdaptor extends DataAdaptor {
    /**
     * Tests this entry for being a link.
     * @param absolutePath the absolute path of the entry.
     * @return true if the entry is a link.
     * @throws DoesNotExist if <code>absolutePath</code> does not exist.
     */
    public boolean isLink(String absolutePath)
        throws PermissionDenied, DoesNotExist, Timeout, NoSuccess;

    /**
     * Returns the absolute path of the link target. Resolves one link level
     * only.
     * @param absolutePath the absolute path of the entry.
     * @return the link target.
     * @throws DoesNotExist if <code>absolutePath</code> does not exist.
     */
    public String readLink(String absolutePath)
        throws NotLink, PermissionDenied, DoesNotExist, Timeout, NoSuccess;

    /**
     * Creates a symbolic link.
     * @param sourceAbsolutePath the absolute path of the physical entry to link to.
     * @param linkAbsolutePath the absolute path of the link entry to create.
     * @param overwrite if true, then link entry is overwrited if it exists.
     * @throws DoesNotExist if <code>sourceAbsolutePath</code> does not exist.
     * @throws AlreadyExists if <code>linkAbsolutePath</code> already exists and <code>overwrite</code> is false.
     */
    public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite)
        throws PermissionDenied, DoesNotExist, AlreadyExists, Timeout, NoSuccess;
}
