package fr.in2p3.jsaga.adaptor.data.impl;

import fr.in2p3.jsaga.adaptor.schema.data.emulator.*;
import org.ogf.saga.error.*;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataEmulatorConnectionAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class DataEmulatorConnectionAbstract {
    protected DataEmulatorGrid m_grid;

    ////////////////////////////////////////// m_grid operations /////////////////////////////////////////

    protected DataEmulatorConnectionAbstract() throws NoSuccess {
        try {
            m_grid = DataEmulatorGrid.getInstance();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    public void commit() throws NoSuccess {
        try {
            m_grid.commit();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    ////////////////////////////////////////// abstract methods /////////////////////////////////////////

    protected abstract void destroy();
    protected abstract ServerType getServerRoot();

    ////////////////////////////////////////// public methods /////////////////////////////////////////

    // add
    public Directory addDirectory(String absolutePath) throws DoesNotExist {
        return addDirectory(getParentDirectory(absolutePath), getEntryName(absolutePath));
    }
    public Directory addDirectory(DirectoryType parent, String name) {
        Directory dir = new Directory();
        dir.setName(name);
        parent.addDirectory(dir);
        return dir;
    }
    public File addFile(String absolutePath) throws DoesNotExist {
        return addFile(getParentDirectory(absolutePath), getEntryName(absolutePath));
    }
    public File addFile(DirectoryType parent, String name) {
        File file = new File();
        file.setName(name);
        parent.addFile(file);
        return file;
    }

    // remove
    public void removeDirectory(String absolutePath) throws DoesNotExist, BadParameter {
        removeDirectory(getParentDirectory(absolutePath), getEntryName(absolutePath));
    }
    public void removeDirectory(DirectoryType parent, String name) throws DoesNotExist, BadParameter {
        Directory dir = getDirectory(parent, name);
        if (dir.getDirectoryCount()>0 || dir.getFileCount()>0) {
            throw new BadParameter("Directory is not empty: "+name);
        }
        parent.removeDirectory(dir);
    }
    public void removeFile(String absolutePath) throws DoesNotExist {
        removeFile(getParentDirectory(absolutePath), getEntryName(absolutePath));
    }
    public void removeFile(DirectoryType parent, String name) throws DoesNotExist {
        parent.removeFile(getFile(parent, name));
    }

    // get
    public DirectoryType getDirectory(String absolutePath) throws DoesNotExist {
        DirectoryType parent = getParentDirectory(absolutePath);
        String name = getEntryName(absolutePath);
        if (name != null) {
            return getDirectory(parent, name);
        } else {
            return parent;
        }
    }
    public Directory getDirectory(DirectoryType parent, String entryName) throws DoesNotExist {
        for (int i=0; i<parent.getDirectoryCount(); i++) {
            if (parent.getDirectory(i).getName().equals(entryName)) {
                return parent.getDirectory(i);
            }
        }
        throw new DoesNotExist("Directory does not exist");
    }
    public File getFile(String absolutePath) throws DoesNotExist {
        return getFile(getParentDirectory(absolutePath), getEntryName(absolutePath));
    }
    public File getFile(DirectoryType parent, String entryName) throws DoesNotExist {
        for (int i=0; i<parent.getFileCount(); i++) {
            if (parent.getFile(i).getName().equals(entryName)) {
                return parent.getFile(i);
            }
        }
        throw new DoesNotExist("File does not exist");
    }
    public EntryType getEntry(String absolutePath) throws DoesNotExist {
        DirectoryType parentDir = getParentDirectory(absolutePath);
        String entryName = getEntryName(absolutePath);
        return getEntry(parentDir, entryName);
    }
    public EntryType getEntry(DirectoryType parent, String entryName) throws DoesNotExist {
        if (entryName == null) {
            return parent;
        }
        try {
            return getFile(parent, entryName);
        } catch(DoesNotExist e) {
            return getDirectory(parent, entryName);
        }
    }

    // list
    public EntryType[] listEntries(String absolutePath) throws DoesNotExist {
        EntryType entry = this.getEntry(absolutePath);
        if (entry instanceof DirectoryType) {
            return listEntries((DirectoryType)entry);
        } else {
            return new EntryType[]{entry};
        }
    }
    public EntryType[] listEntries(DirectoryType parent) {
        List list = new ArrayList();
        for (int i=0; i<parent.getDirectoryCount(); i++) {
            list.add(parent.getDirectory(i));
        }
        for (int i=0; i<parent.getFileCount(); i++) {
            list.add(parent.getFile(i));
        }
        return (EntryType[]) list.toArray(new EntryType[list.size()]);
    }

    ////////////////////////////////////////// friend methods /////////////////////////////////////////

    public DirectoryType getParentDirectory(String absolutePath) throws DoesNotExist {
        String[] entryNames = toArray(absolutePath);
        DirectoryType parent = this.getServerRoot();
        for (int i=0; i<entryNames.length-1; i++) {
            parent = getDirectory(parent, entryNames[i]);
        }
        return parent;
    }

    public String getEntryName(String absolutePath) {
        String[] entryNames = toArray(absolutePath);
        if (entryNames.length > 0) {
            return entryNames[entryNames.length-1];
        } else {
            return null;
        }
    }

    private static String[] toArray(String absolutePath) {
        List list = new ArrayList();
        String[] array = absolutePath.split("/");
        for (int i=0; i<array.length; i++) {
            if (!array[i].equals("")) {
                list.add(array[i]);
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }
}
