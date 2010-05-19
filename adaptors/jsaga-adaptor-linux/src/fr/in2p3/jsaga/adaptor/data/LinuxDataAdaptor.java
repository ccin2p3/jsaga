package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.commons.filesystem.FileStat;
import fr.in2p3.commons.filesystem.FileSystem;
import fr.in2p3.jsaga.adaptor.data.file.FileDataAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.*;

import java.io.File;
import java.io.FileNotFoundException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LinuxDataAdaptor
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LinuxDataAdaptor extends FileDataAdaptor {
    public String getType() {
        return "linux";
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        File entry = super.newEntry(absolutePath);
        FileStat stat;
        try {
            stat = new FileSystem().stat(entry);
        } catch (FileNotFoundException e) {
            throw new DoesNotExistException("Entry does not exist: "+entry, e);
        }
        return new LinuxFileAttributes(stat);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        File[] list = super.listFiles(absolutePath);
        FileAttributes[] ret = new FileAttributes[list.length];
        for (int i=0; i<list.length; i++) {
            FileStat stat;
            try {
                stat = new FileSystem().stat(list[i]);
            } catch (FileNotFoundException e) {
                throw new DoesNotExistException("Entry does not exist: "+list[i], e);
            }
            ret[i] = new LinuxFileAttributes(stat);
        }
        return ret;
    }
}
