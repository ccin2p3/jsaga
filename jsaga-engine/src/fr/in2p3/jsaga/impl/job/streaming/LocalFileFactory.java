package fr.in2p3.jsaga.impl.job.streaming;

import java.io.File;

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
