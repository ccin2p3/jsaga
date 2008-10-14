package fr.in2p3.jsaga.adaptor.data.zip;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.BaseURL;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
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

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;    // no default
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return null;    // no security
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        // do nothing
    }

    public BaseURL getBaseURL() throws IncorrectURL {
        return null;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
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
            throw new NoSuccess("URL must be: zip://[.*].zip/[.*]");
        }
    }

    public void disconnect() throws NoSuccess {
        m_zipPath = null;
        m_zipFile = null;
        m_tmpFile = null;

        try {
            m_zipReader.close();
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
    }

    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDenied, Timeout, NoSuccess {
        try {
            this.getEntry(absolutePath);
            return true;
        } catch (DoesNotExist e) {
            return false;
        }
    }

    public boolean isDirectory(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return this.getEntry(absolutePath).isDirectory();
    }

    public boolean isEntry(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return !this.isDirectory(absolutePath, additionalArgs);
    }

    public long getSize(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        return this.getEntry(absolutePath).getSize();
    }

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        try {
            return m_zipReader.getInputStream(this.getEntry(absolutePath));
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        ZipEntry entry = this.getEntry(absolutePath);
        String path = absolutePath.substring(m_zipPath.length()+1);
        String baseDir = new EntryPath(path).getBaseDir();
        return new ZipFileAttributes(entry, baseDir);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        InputStream in;try{in=new FileInputStream(m_zipFile);} catch(FileNotFoundException e){throw new NoSuccess(e);}
        ZipInputStream zipReader = new ZipInputStream(in);
        String path = absolutePath.substring(m_zipPath.length()+1);
        boolean found = false;
        List list = new ArrayList();
        try {
            ZipEntry entry;
            while ( (entry=zipReader.getNextEntry()) != null ) {
                if (entry.getName().startsWith(path)) {
                    if (entry.getName().equals(path)) {
                        found = true;
                    } else {
                        FileAttributes attr = new ZipFileAttributes(entry, path);
                        if (attr.getNameOnly().indexOf('/') == -1) {
                            list.add(attr);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
        if (found || "".equals(path)) {
            return (FileAttributes[]) list.toArray(new FileAttributes[list.size()]);
        } else {
            throw new DoesNotExist("Entry not found");
        }
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
        String absolutePath = parentAbsolutePath+directoryName+"/";
        String path = absolutePath.substring(m_zipPath.length()+1);
        if (m_zipReader !=null && m_zipReader.getEntry(path)!=null) {
            throw new AlreadyExists("Directory already exists");
        }
    }

    public void putFromStream(String absolutePath, boolean append, String additionalArgs, InputStream stream) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
        String path = absolutePath.substring(m_zipPath.length()+1);
        ZipOutputStream zipWriter;try{zipWriter=new ZipOutputStream(new FileOutputStream(m_tmpFile));} catch(FileNotFoundException e){throw new NoSuccess(e);}
        try {
            if (m_zipFile.exists()) {
                ZipInputStream zipReader;try{zipReader=new ZipInputStream(new FileInputStream(m_zipFile));} catch(FileNotFoundException e){throw new NoSuccess(e);}
                ZipEntry entry;
                while ( (entry=zipReader.getNextEntry()) != null ) {
                    if (entry.getName().equals(path)) {
                        throw new AlreadyExists("File already exists");
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
            throw new NoSuccess(e);
        } finally {
            try{zipWriter.close();} catch(IOException e){}
        }
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        this.removeFile(parentAbsolutePath, directoryName+"/", additionalArgs);
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
        String absolutePath = parentAbsolutePath+fileName;
        String path = absolutePath.substring(m_zipPath.length()+1);
        ZipOutputStream zipWriter;try{zipWriter=new ZipOutputStream(new FileOutputStream(m_tmpFile));} catch(FileNotFoundException e){throw new NoSuccess(e);}
        boolean found = false;
        try {
            if (m_zipFile.exists()) {
                ZipInputStream zipReader;try{zipReader=new ZipInputStream(new FileInputStream(m_zipFile));} catch(FileNotFoundException e){throw new NoSuccess(e);}
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
            throw new NoSuccess(e);
        } finally {
            try{zipWriter.close();} catch(IOException e){}
            if (found) {
                m_tmpFile.renameTo(m_zipFile);
            } else {
                throw new DoesNotExist("Entry does not exist");
            }
        }
    }

    private ZipEntry getEntry(String absolutePath) throws DoesNotExist {
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
        throw new DoesNotExist("Entry does not exist");        
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        int len;
        byte[] buffer = new byte[1024];
        while ( (len=in.read(buffer)) > -1 ) {
            out.write(buffer, 0, len);
        }
    }
}
