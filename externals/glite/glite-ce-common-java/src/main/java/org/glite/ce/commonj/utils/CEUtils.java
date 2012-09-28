/*
 * Copyright (c) Members of the EGEE Collaboration. 2004. 
 * See http://www.eu-egee.org/partners/ for details on the copyright
 * holders.  
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
 
package org.glite.ce.commonj.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.axis.MessageContext;
import org.bouncycastle.util.encoders.Base64;
import org.glite.ce.commonj.Constants;
import org.glite.ce.commonj.certificate.ProxyCertificate;
import org.glite.ce.commonj.certificate.ProxyCertificateException;
import org.glite.ce.commonj.certificate.ProxyCertificateStorageInterface;
import org.glite.ce.commonj.certificate.ProxyCertificate.ProxyCertificateType;
import org.glite.ce.commonj.certificate.db.ProxyCertificateDBManager;
import org.glite.ce.faults.GenericFault;
import org.glite.voms.VOMSAttribute;
import org.glite.voms.ac.AttributeCertificate;

public final class CEUtils {
    public static final String CREAM_LABEL = "cream";
    public static final String CEMONITOR_LABEL = "monitor";
    
    public static String getServiceURL() {         
        MessageContext messageContext = MessageContext.getCurrentContext();
        return (String)messageContext.getProperty(MessageContext.TRANS_URL);        
    }
    
    public static ProxyCertificate getUserAuthNProxy() throws GenericFault {
        MessageContext messageContext = MessageContext.getCurrentContext();

        ProxyCertificate proxy = (ProxyCertificate) messageContext.getProperty(ProxyCertificateType.AUTHENTICATION.getName());

        if (proxy == null) {
            ProxyCertificateStorageInterface proxyStorage = ProxyCertificateDBManager.getInstance();
            try {
                proxy = proxyStorage.getProxyCertificate(ProxyCertificateType.AUTHENTICATION.getName(), getUserDN_RFC2253(), getUserDefaultVO());
            } catch (IllegalArgumentException e) {
                throw (new GenericFault("getUserAuthNProxy", Calendar.getInstance(), "0", "IllegalArgumentException", e.getMessage()));
            } catch (ProxyCertificateException e) {
                throw (new GenericFault("getUserAuthNProxy", Calendar.getInstance(), "0", "ProxyCertificateException", e.getMessage()));
            }
        }

        return proxy;
    }

    public static String getUserDN_RFC2253() throws GenericFault {
        MessageContext messageContext = MessageContext.getCurrentContext();

        String dn = (String) messageContext.getProperty(Constants.USERDN_RFC2253_LABEL);
        if (dn == null) {
            throw (new GenericFault("getUserDN_RFC2253", Calendar.getInstance(), "1", "User DN not found!", "userDN == null"));
        }

        return dn;
    }

    public static String getUserDN_X500() throws GenericFault {
        MessageContext messageContext = MessageContext.getCurrentContext();

        String dn = (String) messageContext.getProperty(Constants.USERDN_X500_LABEL);
        if (dn == null) {
            throw (new GenericFault("getUserDN_X500", Calendar.getInstance(), "1", "User DN not found!", "userDN == null"));
        }

        return dn;
    }

    public static String getUserDefaultVO() {
        MessageContext messageContext = MessageContext.getCurrentContext();

        HashSet voSet = (HashSet) messageContext.getProperty(Constants.USER_VO_LABEL);

        if (voSet != null && voSet.size() > 0) {
            return (String) voSet.iterator().next();
        }

        return null;
    }

    public static HashSet getUserVO() throws GenericFault {
        MessageContext messageContext = MessageContext.getCurrentContext();

        return (HashSet) messageContext.getProperty(Constants.USER_VO_LABEL);
    }

    public static List<String> getFQANForVO(String vo) throws GenericFault {
        MessageContext messageContext = MessageContext.getCurrentContext();

        List<VOMSAttribute> vomsList = (List<VOMSAttribute>) messageContext.getProperty(Constants.USER_VOMSATTRS_LABEL);
        List<String> result = new ArrayList<String>();
        
        if (vomsList != null) {
            List fqanList = null;
            
            for(VOMSAttribute vomsAttr : vomsList) {      
                if(vo == null || vo.equals(vomsAttr.getVO())) {
                    fqanList = vomsAttr.getListOfFQAN();
                    
                    Iterator item2 = fqanList.iterator();
                    for (int k = 0; item2.hasNext(); k++) {
                        result.add(item2.next().toString());
                    }

                    if (vo != null) {
                        break;
                    }
                    
                    fqanList = null;
                }
            }
        }

        return result;
    }

    public static X509Certificate[] getUserCertChain() {
        MessageContext messageContext = MessageContext.getCurrentContext();

        return (X509Certificate[]) messageContext.getProperty(Constants.USER_CERTCHAIN_LABEL);
    }

    public static String getRemoteRequestAddress() {
        MessageContext messageContext = MessageContext.getCurrentContext();

        return (String) messageContext.getProperty(Constants.REMOTE_REQUEST_ADDRESS);
    }
    
    public static boolean isAdmin() {
        MessageContext messageContext = MessageContext.getCurrentContext();

        Boolean b = (Boolean) messageContext.getProperty(Constants.IS_ADMIN);

        if (b == null) {
            return false;
        }

        return b.booleanValue();
    }

    public static synchronized File makeDir(String path) throws IOException {
        File dir = null;

        if (path != null) {
            dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        return dir;
    }

    public static synchronized void makeFile(String filename, String message, boolean append) throws IOException {
        FileWriter fw = new FileWriter(filename, append);
        fw.write(message);
        fw.flush();
        fw.close();
    }

    public static void makeFile(String filename, String message, boolean append, boolean lock) throws IOException, IllegalArgumentException {
        if (filename == null) {
            throw new IllegalArgumentException("filename not specified!");
        }

        makeFile(new File(filename), message, append, lock);
    }

    public static void makeFile(File file, String message, boolean append, boolean lock) throws IOException, IllegalArgumentException {
        if (file == null) {
            throw new IllegalArgumentException("file not specified!");
        }

        if (message == null) {
            throw new IllegalArgumentException("message not specified!");
        }

        if (lock) {
            File tmpFile = File.createTempFile(file.getName(), null, file.getParentFile());
            FileWriter fw = new FileWriter(tmpFile, append);

            String chmod_command = "chmod 0600 " + tmpFile.toString();
            int retcod;
            try {
                retcod = Runtime.getRuntime().exec(chmod_command).waitFor();
            } catch (InterruptedException e) {
                retcod = -1;
            }

            if (retcod != 0) {
                fw.close();
                tmpFile.delete();
                throw new IOException("Could not set permissions of file " + tmpFile.toString());
            }

            fw.write(message);
            fw.flush();
            fw.close();

            // Get a file channel for the file
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

            // Use the file channel to create a lock on the file.
            // This method blocks until it can retrieve the lock.
            FileLock fileLock = channel.lock();

            // runtime.exec("mv " + tmpFile.getAbsoluteFile() + " " +
            // file.getAbsoluteFile());
            tmpFile.renameTo(file);

            // FileWriter fw = new FileWriter(file, append);
            // fw.write(message);
            // fw.flush();
            // fw.close();

            // Release the lock
            fileLock.release();

            // Close the file
            channel.close();
        } else {
            FileWriter fw = new FileWriter(file, append);
            fw.write(message);
            fw.flush();
            fw.close();
        }
    }

    public static Object loadObject(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object obj = ois.readObject();
        ois.close();
        fis.close();

        return obj;
    }

    public static synchronized void saveObject(String filename, Object obj) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        fos.close();
    }

    public static synchronized File copyFile(String src, String dst) throws IOException {
        File srcFile = new File(src);
        File dstFile = new File(dst);
        if (dstFile.isDirectory()) {
            dstFile = new File(dst + "/" + srcFile.getName());
        }
        dstFile.createNewFile();

        InputStream in = new FileInputStream(srcFile);
        OutputStream out = new FileOutputStream(dstFile);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.flush();

        in.close();
        out.close();

        return dstFile;
    }

    public static String readFile(String filename) throws IOException {
        String res = "";

        FileReader in = new FileReader(filename);

        char[] buffer = new char[1024];
        int n = 1;

        while (n > 0) {
            n = in.read(buffer, 0, buffer.length);

            if (n > 0) {
                res += new String(buffer, 0, n);
            }
        }
        in.close();
        return res;
    }

    public static boolean deleteDir(String path) {
        File dir = new File(path);
        return deleteDir(dir);
    }

    public static boolean deleteDir(File dir) {
        emptyDir(dir);
        return dir.delete();
    }

    public static void emptyDir(File dir) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                emptyDir(files[i]);
            }
            files[i].delete();
        }
    }

    public static String getPEM(X509Certificate[] certChain) throws CertificateEncodingException {
        if (certChain == null)
            return "";

        StringBuffer result = new StringBuffer();

        for (int k = 0; k < certChain.length; k++) {
            byte[] pemBytes = Base64.encode(certChain[k].getEncoded());

            result.append("-----BEGIN CERTIFICATE-----\n");
            for (int n = 0; n < pemBytes.length; n = n + 64) {

                if ((pemBytes.length - n) < 64) {
                    result.append(new String(pemBytes, n, pemBytes.length - n));
                } else {
                    result.append(new String(pemBytes, n, 64));
                }

                result.append("\n");
            }

            result.append("-----END CERTIFICATE-----\n");
        }

        return result.toString();
    }

    public static String getConfigurationProvider(Object info) {
        return getConfigurationParam(info, "configuration_provider");
    }

    public static String getConfigurationURL(Object info) {
        return getConfigurationParam(info, "configuration_provider_url");
    }

    private static String getConfigurationParam(Object info, String param) {
        Hashtable containerConfigEnv = new Hashtable(0);
        containerConfigEnv.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        containerConfigEnv.put(Context.URL_PKG_PREFIXES, "org.apache.naming");

        try {
            Context context = (Context) (new InitialContext(containerConfigEnv)).lookup("java:comp/env");
            return (String) context.lookup(param);
        } catch (NamingException e) {
        }

        if (info == null)
            return null;

        if (info instanceof MessageContext) {
            String servicePort = (String) ((MessageContext) info).getProperty("wsdlServicePort");
            if (servicePort == null)
                return null;
            else if (servicePort.equals("CEMonitor"))
                return System.getProperty("monitor_" + param);
            else if (servicePort.equals("CREAM"))
                return System.getProperty("cream_" + param);
        }

        if (info instanceof String) {
            if (info.equals(CREAM_LABEL))
                return System.getProperty("cream_" + param);
            if (info.equals(CEMONITOR_LABEL))
                return System.getProperty("monitor_" + param);
        }

        return null;
    }

    public static Properties getLoggingParam(Object info) {
        Properties logProps = new Properties();
        Hashtable containerConfigEnv = new Hashtable(0);
        containerConfigEnv.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        containerConfigEnv.put(Context.URL_PKG_PREFIXES, "org.apache.naming");

        try {
            Context context = (Context) (new InitialContext(containerConfigEnv)).lookup("java:comp/env");
            File logConfFile = new File((String) context.lookup("log_configuration_file"));

            FileInputStream in = null;

            try {
                in = new FileInputStream(logConfFile);
                logProps.load(in);
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (Exception ex) {
                    }
            }

        } catch (Exception e) {
        }

        if (logProps.size() == 0) {
            String fileout = null;
            if (info.equals(CREAM_LABEL)) {
                fileout = "${catalina.base}/logs/glite-ce-cream.log";
            } else if (info.equals(CEMONITOR_LABEL)) {
                fileout = "${catalina.base}/logs/glite-ce-monitor.log";
            } else {
                fileout = "${catalina.base}/logs/glite-ce-common.log";
            }

            logProps.setProperty("log4j.rootLogger", "ERROR, fileout");
            logProps.setProperty("log4j.logger.org.glite", "INFO, fileout");
            logProps.setProperty("log4j.appender.fileout", "org.apache.log4j.RollingFileAppender");
            logProps.setProperty("log4j.appender.fileout.File", fileout);
            logProps.setProperty("log4j.appender.fileout.MaxFileSize", "500KB");
            logProps.setProperty("log4j.appender.fileout.MaxBackupIndex", "1");
            logProps.setProperty("log4j.appender.fileout.layout", "org.apache.log4j.PatternLayout");
            logProps.setProperty("log4j.appender.fileout.layout.ConversionPattern", "%d{dd MMM yyyy HH:mm:ss,SSS} %c - %m%n");
        }

        return logProps;
    }

    public static String normalize(String s) {
        if (s != null) {
            return s.replace('=', '_').replace(' ', '_').replace('/', '_').replace(',', '_').replace('@', '_');
        }
        return null;
    }

    public static String getProxyInfo(X509Certificate[] certChain, List vomsAttributes) {
        StringBuffer buff = new StringBuffer();
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        FieldPosition fp = new FieldPosition(SimpleDateFormat.YEAR_FIELD);

        X509Certificate cert = certChain[0];
        buff.append("Valid From      : ");
        dateFormat.format(cert.getNotBefore(), buff, fp);
        buff.append("\nValid To       : ");
        dateFormat.format(cert.getNotAfter(), buff, fp);

        cert = certChain[certChain.length - 1];
        if (cert.getIssuerDN().equals(cert.getSubjectDN())) {
            cert = certChain[certChain.length - 2];
        }

        buff.append("\nHolder Subject : ").append(cert.getSubjectDN());
        buff.append("\nHolder CA      : ").append(cert.getIssuerDN());
        buff.append("\n");

        Iterator item = vomsAttributes.iterator();
        while (item.hasNext()) {
            VOMSAttribute vomsAttr = (VOMSAttribute) item.next();
            AttributeCertificate ac = vomsAttr.getAC();

            buff.append("\nVO              : ").append(vomsAttr.getVO());
            buff.append("\nAC Issuer       : ").append(ac.getIssuer().toString());
            buff.append("\nAttribute       : ");
            Iterator attrs = vomsAttr.getFullyQualifiedAttributes().iterator();
            while (attrs.hasNext()) {
                buff.append(attrs.next().toString()).append(" ");
            }

            buff.append("\n");
        }

        return buff.toString();
    }

}
