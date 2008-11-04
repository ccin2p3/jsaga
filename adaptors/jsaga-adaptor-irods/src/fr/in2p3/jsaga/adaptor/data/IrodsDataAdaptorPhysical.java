package fr.in2p3.jsaga.adaptor.data;

import edu.sdsc.grid.io.FileFactory;
import edu.sdsc.grid.io.irods.*;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import org.ogf.saga.error.*;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IrodsDataAdaptorPhysical
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IrodsDataAdaptorPhysical extends IrodsDataAdaptor implements FileReaderStreamFactory, FileWriterStreamFactory {
	public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
		try {
			String[] split = absolutePath.split(SEPARATOR);
			String fileName = split[split.length-1];

			String dir = absolutePath.substring(0,absolutePath.length()-fileName.length());
			IRODSFile generalFile =  (IRODSFile)FileFactory.newFile(fileSystem, dir, fileName );

			return new BufferedInputStream(new IRODSFileInputStream(generalFile));
			/*
			GeneralRandomAccessFile generalRandomAccessFile = FileFactory.newRandomAccessFile( generalFile, "r" );
			int filesize = (int)generalFile.length();
			byte[] buffer = new byte[filesize];
			generalRandomAccessFile.readFully(buffer);
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer);

			return bais;*/
       	} catch (java.lang.Exception e) {
			throw new NoSuccessException(e);
        }
	}

	public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
		try {
			IRODSFile generalFile =  (IRODSFile)FileFactory.newFile((IRODSFileSystem)fileSystem, parentAbsolutePath, fileName );
			return new BufferedOutputStream(new IRODSFileOutputStream(generalFile));
       	} catch (java.lang.Exception e) {
			throw new NoSuccessException(e);
        }
	}
}
