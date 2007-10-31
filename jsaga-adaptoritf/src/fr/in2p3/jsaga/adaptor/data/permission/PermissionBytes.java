package fr.in2p3.jsaga.adaptor.data.permission;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PermissionBytes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class PermissionBytes {
    public static final PermissionBytes UNKNOWN = new PermissionBytes(-1);
    public static final PermissionBytes NONE = new PermissionBytes(0);
    public static final PermissionBytes QUERY = new PermissionBytes(1);
    public static final PermissionBytes READ = new PermissionBytes(2);
    public static final PermissionBytes WRITE = new PermissionBytes(4);
    public static final PermissionBytes EXEC = new PermissionBytes(8);
    public static final PermissionBytes OWNER = new PermissionBytes(16);
    public static final PermissionBytes ALL = new PermissionBytes(31);

    private int value;

    public PermissionBytes(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this permission.
     * @return the integer value.
     */
    public int getValue() {
        return value;
    }

    public PermissionBytes or(PermissionBytes perm) {
        return new PermissionBytes(this.value | perm.value);
    }
}
