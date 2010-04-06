package fr.in2p3.jsaga.adaptor.lfc;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

/**
 * @author Jerome Revillard
 */
public class LFCFileAttributes extends FileAttributes {
    private LfcConnection.LFCFile m_file;

    public LFCFileAttributes(LfcConnection.LFCFile file) {
        m_file = file;
    }

    public String getName() {
        return m_file.getFileName();
    }

    public int getType() {
        if ((m_file.getFileMode() & LfcConnection.S_IFDIR) != 0) {
            return TYPE_DIRECTORY;
        } else {
            //FIXME: CHECK LINKS!!
            if(true){
                return TYPE_FILE;
            }else{
                return TYPE_LINK;
            }
        }
    }

    public long getSize() {
        return m_file.getFileSize();
    }

    public PermissionBytes getUserPermission() {
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getGroupPermission() {
        return PERMISSION_UNKNOWN;
    }

    public PermissionBytes getAnyPermission() {
        return PERMISSION_UNKNOWN;
    }

    public String getOwner() {
        return ID_UNKNOWN;
    }

    public String getGroup() {
        return ID_UNKNOWN;
    }

    public long getLastModified() {
        return DATE_UNKNOWN;
    }
}
