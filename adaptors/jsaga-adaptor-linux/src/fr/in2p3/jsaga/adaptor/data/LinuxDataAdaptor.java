package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.commons.filesystem.FileStat;
import fr.in2p3.commons.filesystem.FileSystem;
import fr.in2p3.jsaga.adaptor.data.file.FileDataAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.error.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
public class LinuxDataAdaptor extends FileDataAdaptor implements LinkAdaptor{
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

	public boolean isLink(String absolutePath)
			throws PermissionDeniedException, DoesNotExistException,
			TimeoutException, NoSuccessException {
        return ((LinuxFileAttributes)getAttributes(absolutePath, null)).isLink();
	}

	public String readLink(String absolutePath) throws NotLink,
			PermissionDeniedException, DoesNotExistException, TimeoutException,
			NoSuccessException {
		return ((LinuxFileAttributes)getAttributes(absolutePath,null)).readLink();
	}

	public void link(String sourceAbsolutePath, String linkAbsolutePath,
			boolean overwrite) throws PermissionDeniedException,
			DoesNotExistException, AlreadyExistsException, TimeoutException,
			NoSuccessException {
		// TODO link
        File sourceEntry = super.newEntry(sourceAbsolutePath);
		if (!sourceEntry.exists()) { throw new DoesNotExistException("File does not exist: " + sourceAbsolutePath);}
		try {
	        File linkEntry = super.newEntry(linkAbsolutePath);
			if (!overwrite) { throw new AlreadyExistsException("File already exists: " + linkAbsolutePath);}
			linkEntry.delete();
		} catch (DoesNotExistException e) {
			// Nothing to do
		}
		try {
			new FileSystem().symlink(sourceEntry, linkAbsolutePath);
		} catch (FileNotFoundException fnfe) {
			throw new DoesNotExistException(fnfe);
		} catch (fr.in2p3.commons.filesystem.FileAlreadyExistsException faee) {
			throw new AlreadyExistsException("File already exists: " + linkAbsolutePath);
		} catch (fr.in2p3.commons.filesystem.PermissionDeniedException pde) {
			throw new PermissionDeniedException(pde);
		} catch (IOException ioe) {
			throw new NoSuccessException(ioe);
		}
	}
	
}
