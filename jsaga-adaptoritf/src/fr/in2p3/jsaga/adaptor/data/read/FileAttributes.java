package fr.in2p3.jsaga.adaptor.data.read;

import org.ogf.saga.permissions.Permission;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FileAttributes {
    public static final int UNKNOWN_TYPE = 0;
    public static final int FILE_TYPE = 1;
    public static final int DIRECTORY_TYPE = 2;
    public static final int LINK_TYPE = 3;

    public String name = null;
    public Permission permission = null;
    public int type = UNKNOWN_TYPE;
    public int size = -1;
}
