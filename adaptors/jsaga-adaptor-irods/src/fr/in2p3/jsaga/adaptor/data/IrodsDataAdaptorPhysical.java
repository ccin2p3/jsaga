package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.FileIOOperations.SeekWhenceType;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.core.pub.io.IRODSRandomAccessFile;
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
public class IrodsDataAdaptorPhysical extends IrodsDataAdaptorAbstract implements FileReaderStreamFactory, FileWriterStreamFactory {

    public String getType() {
        return "irods";
    }

    protected boolean isClassic(){
        return false;
    }        

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        String[] split = absolutePath.split(SEPARATOR);
        String fileName = split[split.length-1];

        String dir = absolutePath.substring(0,absolutePath.length()-fileName.length());
        try {
            IRODSFile generalFile = m_fileFactory.instanceIRODSFile( dir, fileName );
            return new BufferedInputStream(m_fileFactory.instanceIRODSFileInputStream(generalFile));
        } catch (JargonException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new DoesNotExistException(e.getMessage());
            }
            throw new NoSuccessException(e);
        }
    }

    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        IRODSFile parentFile;
        try {
            parentFile = m_fileFactory.instanceIRODSFile( parentAbsolutePath);
        } catch (JargonException e) {
            throw new NoSuccessException(e);
        }
        if (!parentFile.exists()) {throw new ParentDoesNotExist(parentAbsolutePath);}
        
        IRODSFile generalFile;
        try {
            generalFile = m_fileFactory.instanceIRODSFile( parentAbsolutePath, fileName );
        } catch (JargonException e) {
            throw new NoSuccessException(e);
        }
        
        try {
            if (!generalFile.createNewFile()) {
                if (exclusive) {
                    throw new AlreadyExistsException("File already exists");
                } else if (append) {
                    IRODSRandomAccessFile randomAccessFile = m_fileFactory.instanceIRODSRandomAccessFile(generalFile );
                    randomAccessFile.seek(0, SeekWhenceType.SEEK_END);
                    return new BufferedOutputStream(new IrodsAppendedOutputStream(randomAccessFile));
                } else {
                    generalFile.delete();
                    return new BufferedOutputStream(m_fileFactory.instanceIRODSFileOutputStream(generalFile));   //overwrite
                }
            } else {
                return new BufferedOutputStream(m_fileFactory.instanceIRODSFileOutputStream(generalFile));
            }
         } catch (IOException e) {
             throw new NoSuccessException("Failed to create file: "+fileName, e);
         } catch (JargonException e) {
             throw new NoSuccessException("Failed to create file: "+fileName, e);
        }
    }
}
