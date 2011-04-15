package fr.in2p3.jsaga.adaptor.job.local;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LocalAdaptorAbstract
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   29 avril 2008
* ***************************************************/

public abstract class LocalAdaptorAbstract implements ClientAdaptor {
	
    public Class[] getSupportedSecurityCredentialClasses() {
        return null;
    }

    public String getType() {
		return "local";
	}
	
    public void setSecurityCredential(SecurityCredential credential) {
    } 
	
    public int getDefaultPort() {
        return 0;
    }
        
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	// create temp directory
    	new File(LocalJobProcess.getRootDir()).mkdirs();
    }

    public void disconnect() throws NoSuccessException {
    }

    public static void store(LocalJobProcess p) throws IOException {
    	store(p, p.getJobId());
    }
    public static void store(LocalJobProcess p, String nativeJobId) throws IOException {
    	byte[] buf = serialize(p);
    	FileOutputStream f = new FileOutputStream(new File(LocalJobProcess.getRootDir() + "/" + nativeJobId + ".process"));
    	f.write(buf);
    	f.close();
    }
    
    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(buffer);
        oos.writeObject(obj);
        oos.close();
        return buffer.toByteArray();
    }

    public static LocalJobProcess restore(String nativeJobId) throws IOException, ClassNotFoundException {
    	File f = new File(LocalJobProcess.getRootDir() + "/" + nativeJobId + ".process");
    	FileInputStream fis = new FileInputStream(f);
    	byte[] buf = new byte[(int)f.length()];
    	int len = fis.read(buf);
    	fis.close();
    	return (LocalJobProcess)deserialize(buf);
    }
    
    private static Object deserialize(byte[] bytes)
            throws ClassNotFoundException {
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(input);
            return ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("error reading from byte-array!");
        }
    }
    
}
