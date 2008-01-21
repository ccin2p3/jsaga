package fr.in2p3.jsaga.engine.jobcollection.preprocess;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JavaTransformer
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class JavaTransformer {
    public byte[] transform(byte[] xmlInput) throws Exception {
        ByteArrayOutputStream xmlOutput = new ByteArrayOutputStream();
        this.transform(new ByteArrayInputStream(xmlInput), xmlOutput);
        return xmlOutput.toByteArray();
    }

    public abstract void transform(InputStream xmlInput, OutputStream xmlOutput) throws Exception;
}
