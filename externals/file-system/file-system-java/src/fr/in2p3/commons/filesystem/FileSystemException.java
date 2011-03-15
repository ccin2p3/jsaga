package fr.in2p3.commons.filesystem;

public class FileSystemException extends Exception {

	private int error;
	public static int FILENOTFOUND = 1;
	public static int FILEEXISTS = 2;
	public static int USERNOTFOUND = 3;
	public static int GROUPNOTFOUND = 4;
	public static int PERMISSIONDENIED = 5;
	public static int NOTSUPPORTED = 6;
	public static int IOERROR = 7;
	public static int INTERNALERROR = 8;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3037909417757326155L;

	public FileSystemException(String errmsg) {
		super((errmsg.split(":"))[1]);
		error = new Integer((errmsg.split(":"))[0]).intValue();
	}
	
	public int getError() {
		return error;
	}
}
