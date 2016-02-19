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
        // isDirectory() does not work!
//        if (m_object.isDirectory()) {
        if (m_object.getName().endsWith("/")) {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PermissionBytes getGroupPermission() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PermissionBytes getAnyPermission() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getOwner() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGroup() {
        // TODO Auto-generated method stub
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
