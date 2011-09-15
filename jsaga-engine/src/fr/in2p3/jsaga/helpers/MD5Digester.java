package fr.in2p3.jsaga.helpers;

import fr.in2p3.jsaga.Base;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MD5Digester
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   5 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MD5Digester {
    public static boolean isSame(File refFile, byte[] data) throws IOException, NoSuchAlgorithmException {
        // compute MD5
        boolean same;
        byte[] md5 = MessageDigest.getInstance("MD5").digest(data);
        if (refFile.exists()) {
            byte[] ref = new byte[(int)refFile.length()];
            InputStream in = new FileInputStream(refFile);
            if (in.read(ref) > -1) {
                same = equals(ref, md5);
            } else {
                same = false;
            }
            in.close();
        } else {
            same = false;
        }

        // save MD5 to file
        OutputStream out = new FileOutputStream(refFile);
        out.write(md5);
        out.close();

        // debug
        if (Base.DEBUG) {
            File debugBaseDir = new File(refFile.getParentFile(), "debug");
            if (!debugBaseDir.exists()) {
                debugBaseDir.mkdir();
            }
            File debugFile = new File(debugBaseDir, refFile.getName()+".xml");
            OutputStream f = new FileOutputStream(debugFile);
            f.write(data);
            f.close();
        }
        return same;
    }

    private static boolean equals(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) {
            return false;
        }
        for (int i=0; i<b1.length && i<b2.length; i++) {
            if (b1[i]!=b2[i]) {
                return false;
            }
        }
        return true;
    }
}
