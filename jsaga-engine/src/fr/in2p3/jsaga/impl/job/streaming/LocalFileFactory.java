package fr.in2p3.jsaga.impl.job.streaming;

import java.io.File;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LocalFileFactory
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   7 avril 2011
* ***************************************************
* Description:                                      */


public class LocalFileFactory {

	public static File getLocalFile(String uniqId, String suffix) {
		return new File(System.getProperty("java.io.tmpdir"), "local-" + uniqId + "." + suffix);
	}
	public static File getLocalInputFile(String uniqId) {
		return getLocalFile(uniqId, "input");
	}
	public static File getLocalOutputFile(String uniqId) {
		return getLocalFile(uniqId, "output");
	}
	public static File getLocalErrorFile(String uniqId) {
		return getLocalFile(uniqId, "error");
	}
}
