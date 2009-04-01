package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.*;
import edu.sdsc.grid.io.srb.*;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import org.ogf.saga.error.*;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SrbDataAdaptorPhysical
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SrbDataAdaptorPhysical extends SrbDataAdaptor implements FileReaderStreamFactory, FileWriterStreamFactory {
	public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		try {
			String[] split = absolutePath.split(SEPARATOR);
			String fileName = split[split.length-1];

			String dir = absolutePath.substring(0,absolutePath.length()-fileName.length());
			SRBFile generalFile =  (SRBFile)FileFactory.newFile(fileSystem, dir, fileName );

			return new BufferedInputStream(new SRBFileInputStream(generalFile));
       	} catch (SRBException e) {
			if (e.getType() == -3201) {
				throw new DoesNotExistException(e);
			} else {
				throw new NoSuccessException(e);
			}	
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
	}

	public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
		
		GeneralFile parentFile =  FileFactory.newFile(fileSystem, parentAbsolutePath);
		if (!parentFile.exists()) {throw new ParentDoesNotExist(parentAbsolutePath);}
		
		SRBFile generalFile =  (SRBFile)FileFactory.newFile((SRBFileSystem)fileSystem, parentAbsolutePath, fileName );
		
		try {
			if (!generalFile.createNewFile()) {
				if (exclusive) {
					throw new AlreadyExistsException("File already exist");
				} else if (append) {
					GeneralRandomAccessFile randomAccessFile = FileFactory.newRandomAccessFile( generalFile, "rw" );
					randomAccessFile.seek( generalFile.length() );
					return new BufferedOutputStream(new IrodsAppendedOutputStream(randomAccessFile));
				} else {
					generalFile.delete(true);
					return new BufferedOutputStream(new SRBFileOutputStream(generalFile));   //overwrite
				}
			} else {
				return new BufferedOutputStream(new SRBFileOutputStream(generalFile));
			}
         } catch (IOException e) {
             throw new NoSuccessException("Failed to create file: "+fileName, e);
         }
	}
}
