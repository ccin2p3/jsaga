package fr.in2p3.jsaga.adaptor.openstack.data;

import org.openstack4j.model.storage.object.SwiftObject;

import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.openstack.util.SwiftURL;

public class SwiftObjectAttributes extends FileAttributes {

    private SwiftObject m_object;
    
    public SwiftObjectAttributes(SwiftObject so) {
        m_object = so;
    }

    @Override
    public String getName() {
        // m_object.getName returns full path, must extract directory
        // getDirectoryName() does not work
        return SwiftURL.getFileName(m_object.getName());
    }

    @Override
    public int getType() {
//        if (m_object.getMimeType().equals("application/directory")) {
//        if (m_object.getName().endsWith("/")) {
        if (m_object.isDirectory()) {
            return FileAttributes.TYPE_DIRECTORY;
        } else {
            return FileAttributes.TYPE_FILE;
        }
    }

    @Override
    public long getSize() {
        return m_object.getSizeInBytes();
    }

    @Override
    public PermissionBytes getUserPermission() {
        return null;
    }

    @Override
    public PermissionBytes getGroupPermission() {
        return null;
    }

    @Override
    public PermissionBytes getAnyPermission() {
        return null;
    }

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public long getLastModified() {
        return m_object.getLastModified().getTime();
    }

    public String getDirectoryName() {
        return SwiftURL.getDirectoryName(m_object.getName());
    }
}
