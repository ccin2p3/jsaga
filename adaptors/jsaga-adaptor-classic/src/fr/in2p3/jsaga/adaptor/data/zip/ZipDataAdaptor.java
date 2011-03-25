package fr.in2p3.jsaga.adaptor.data.zip;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.helpers.EntryPath;
import org.ogf.saga.error.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ZipDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ZipDataAdaptor implements FileReaderStreamFactory{//, FileWriterPutter {
    private String m_zipPath;
    private File m_zipFile;
    private File m_tmpFile;

    private ZipFile m_zipReader;

    public String getType() {
        return "zip";
    }

    public Usage getUsage() {
        return null;    // no usage
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;    // no default
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return null;    // no security
    }

    public void setSecurityCredential(SecurityCredential credential) {
        // do nothing
    }

    public int getDefaultPort() {
        return NO_PORT;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        int pos = basePath.indexOf(".zip/");
        if (pos > -1) {
            m_zipPath = basePath.substring(0, pos+4);
            m_zipFile = new File(m_zipPath);
            m_tmpFile = new File(m_zipPath+".tmp");

            try {
                m_zipReader = new ZipFile(m_zipFile);
            } catch (IOException e) {
                m_zipReader = null;
            }
        } else {
            throw new NoSuccessException("URL must be: zip://[.*].zip/[.*]");
        }
    }

    public void disconnect() throws NoSuccessException {
        m_zipPath = null;
        m_zipFile = null;
        m_tmpFile = null;

        try {
            m_zipReader.close();
        } catch (IOException e) {
            throw new NoSuccessException(e);
        } catch (NullPointerException npe) {
        	// Nothing to do
        }
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        try {
            this.getEntry(absolutePath);
            return true;
        } catch (DoesNotExistException e) {
            return false;
        }
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            return m_zipReader.getInputStream(this.getEntry(absolutePath));
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        ZipEntry entry = this.getEntry(absolutePath);
        String path = absolutePath.substring(m_zipPath.length()+1);
        String baseDir = new EntryPath(path).getBaseDir();
        return new ZipFileAttributes(entry, baseDir);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        InputStream in;try{in=new FileInputStream(m_zipFile);} catch(FileNotFoundException e){throw new NoSuccessException(e);}
        ZipInputStream zipReader = new ZipInputStream(in);
        String path = absolutePath.substring(m_zipPath.length()+1);
        boolean found = false;
        List list = new ArrayList();
        Set<String> entries = new HashSet<String>();
        Set<String> candidates = new HashSet<String>();
        try {
            ZipEntry entry;
            while ( (entry=zipReader.getNextEntry()) != null ) {
                if (entry.getName().startsWith(path)) {
                    if (entry.getName().equals(path)) {
                        found = true;
                    } else {
                        FileAttributes attr = new ZipFileAttributes(entry, path);
                        if (attr.getName().indexOf('/') == -1) {
                            list.add(attr);
                            entries.add(attr.getName());
                        } else {
                            candidates.add(attr.getName().substring(0, attr.getName().indexOf('/')));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new NoSuccessException(e);
        }
        if (! entries.containsAll(candidates)) {
            throw new NoSuccessException("Unsupported ZIP file (does not contain entries for directories)");
        }
        if (found || "".equals(path)) {
            return (FileAttributes[]) list.toArray(new FileAttributes[list.size()]);
        } else {
            throw new DoesNotExistException("Entry not found");
        }
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        String absolutePath = parentAbsolutePath+directoryName+"/";
        String path = absolutePath.substring(m_zipPath.length()+1);
        if (m_zipReader !=null && m_zipReader.getEntry(path)!=null) {
            throw new AlreadyExistsException("Directory already exists");
        }
    }

    public void putFromStream(String absolutePath, boolean append, String additionalArgs, InputStream stream) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        String path = absolutePath.substring(m_zipPath.length()+1);
        ZipOutputStream zipWriter;try{zipWriter=new ZipOutputStream(new FileOutputStream(m_tmpFile));} catch(FileNotFoundException e){throw new NoSuccessException(e);}
        try {
            if (m_zipFile.exists()) {
                ZipInputStream zipReader;try{zipReader=new ZipInputStream(new FileInputStream(m_zipFile));} catch(FileNotFoundException e){throw new NoSuccessException(e);}
                ZipEntry entry;
                while ( (entry=zipReader.getNextEntry()) != null ) {
                    if (entry.getName().equals(path)) {
                        throw new AlreadyExistsException("File already exists");
                    }
                    zipWriter.putNextEntry(entry);
                    copy(zipReader, zipWriter);
                    zipWriter.closeEntry();
                    zipReader.closeEntry();
                }
                zipReader.close();
            }
            zipWriter.putNextEntry(new ZipEntry(path));
            copy(stream, zipWriter);
            zipWriter.closeEntry();
            zipWriter.close();
            m_tmpFile.renameTo(m_zipFile);
        } catch (IOException e) {
            throw new NoSuccessException(e);
        } finally {
            try{zipWriter.close();} catch(IOException e){}
        }
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        this.removeFile(parentAbsolutePath, directoryName+"/", additionalArgs);
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        String absolutePath = parentAbsolutePath+fileName;
        String path = absolutePath.substring(m_zipPath.length()+1);
        ZipOutputStream zipWriter;try{zipWriter=new ZipOutputStream(new FileOutputStream(m_tmpFile));} catch(FileNotFoundException e){throw new NoSuccessException(e);}
        boolean found = false;
        try {
            if (m_zipFile.exists()) {
                ZipInputStream zipReader;try{zipReader=new ZipInputStream(new FileInputStream(m_zipFile));} catch(FileNotFoundException e){throw new NoSuccessException(e);}
                ZipEntry entry;
                while ( (entry=zipReader.getNextEntry()) != null ) {
                    if (entry.getName().equals(path)) {
                        found = true;
                    } else {
                        zipWriter.putNextEntry(entry);
                        copy(zipReader, zipWriter);
                        zipWriter.closeEntry();
                    }
                    zipReader.closeEntry();
                }
                zipReader.close();
            }
            zipWriter.close();
        } catch (IOException e) {
            throw new NoSuccessException(e);
        } finally {
            try{zipWriter.close();} catch(IOException e){}
            if (found) {
                m_tmpFile.renameTo(m_zipFile);
            } else {
                throw new DoesNotExistException("Entry does not exist");
            }
        }
    }

    private ZipEntry getEntry(String absolutePath) throws DoesNotExistException {
        String path = absolutePath.substring(m_zipPath.length()+1);
        if ("".equals(path)) {
            return new ZipEntry("/");
        }
        if (m_zipReader != null) {
            ZipEntry entry = m_zipReader.getEntry(path);
            if (entry != null) {
                return entry;
            }
        }
        throw new DoesNotExistException("Entry does not exist");
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        int len;
        byte[] buffer = new byte[1024];
        while ( (len=in.read(buffer)) > -1 ) {
            out.write(buffer, 0, len);
        }
    }
}
