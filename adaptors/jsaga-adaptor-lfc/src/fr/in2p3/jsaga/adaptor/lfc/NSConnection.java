package fr.in2p3.jsaga.adaptor.lfc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.GlobusGSSManagerImpl;
import org.gridforum.jgss.ExtendedGSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;


/**
 * Low-Level connection to an NS server.
 * 
 * @author Jerome Revillard
 */
public class NSConnection {
	private static Logger s_logger = Logger.getLogger(NSConnection.class);
	
	public final static int MAX_RETRY_IF_BROKEN_PIPE = 3;
	
	/**
	 * Bitmask for <b>file mode</b>
	 */
	public static final int S_IFMT = 0xF000;
	/**
	 * Bitmask for <b>symbolic link</b>
	 */
	public static final int S_IFLNK = 0xA000;
	/**
	 * Bitmask for <b>regular file</b>
	 */
	public static final int S_IFREG = 0x8000;
	/**
	 * Bitmask for <b>directory</b>
	 */
	public static final int S_IFDIR = 0x4000;
	/**
	 * Bitmask for <b>set user ID on execution</b>
	 */
	public static final int S_ISUID = 0004000;
	/**
	 * Bitmask for <b>set group ID on execution</b>
	 */
	public static final int S_ISGID = 0002000;
	/**
	 * Bitmask for <b>sticky bit</b>
	 */
	public static final int S_ISVTX = 0001000;
	/**
	 * Bitmask for <b>read by owner</b>
	 */
	public static final int S_IRUSR = 0000400;
	/**
	 * Bitmask for <b>write by owner</b>
	 */
	public static final int S_IWUSR = 0000200;
	/**
	 * Bitmask for <b>execute/search by owner</b>
	 */
	public static final int S_IXUSR = 0000100;
	/**
	 * Bitmask for <b>read by group</b>
	 */
	public static final int S_IRGRP = 0000040;
	/**
	 * Bitmask for <b>write by group</b>
	 */
	public static final int S_IWGRP = 0000020;
	/**
	 * Bitmask for <b>execute/search by group</b>
	 */
	public static final int S_IXGRP = 0000010;
	/**
	 * Bitmask for <b>read by others</b>
	 */
	public static final int S_IROTH = 0000004;
	/**
	 * Bitmask for <b>write by others</b>
	 */
	public static final int S_IWOTH = 0000002;
	/**
	 * Bitmask for <b>execute/search by others</b>
	 */
	public static final int S_IXOTH = 0000001;

	private static final int HEADER_SIZE = 12;

	private static final int REPBUFSZ = 4100;
	private static final int REQBUFSZ = 10240; //2854;
	
	
//	private static final int DIRBUFSZ = 4096;
//	private static final int LISTBUFSZ = 4096;
	
	/* maximum length for a pathname */
//	private static final int CA_MAXPATHLEN = 1023;

	private static final int CSEC_TOKEN_MAGIC_1 = 0xCA03;
	private static final int CSEC_TOKEN_TYPE_PROTOCOL_REQ = 0x1;
	private static final int CSEC_TOKEN_TYPE_PROTOCOL_RESP = 0x2;
	private static final int CSEC_TOKEN_TYPE_HANDSHAKE = 0x3;
	private static final int CSEC_TOKEN_TYPE_HANDSHAKE_FINAL = 0x5;
//	private static final int CSEC_TOKEN_TYPE_ERROR = 0x6;

	private static final int CNS_MAGIC = 0x030E1301;
	private static final int CNS_MAGIC2 = 0x030E1302;
	private static final int CNS_MAGIC4 = 0x030E1304;

	private static final int MSG_ERR = 1;
	private static final int MSG_DATA = 2;
	private static final int CNS_RC = 3;
	private static final int CNS_IRC = 4;
	private static final int MSG_LINKS = 5;
	private static final int MSG_REPLIC = 6;
	private static final int MSG_REPLICP = 7;
	private static final int MSG_REPLICX = 8;
	private static final int MSG_REPLICS = 9;
	private static final int MSG_GROUPS = 10;
	private static final int MSG_STATUSES = 11;
	private static final int MSG_FILEST = 12;
	private static final int MSG_GRPINFO = 13;
	private static final int MSG_USRINFO = 14;

	private static final int CNS_ACCESS = 0;
	// private static final int CNS_CHDIR = 1;
	private static final int CNS_CHMOD = 2;
	private static final int CNS_CHOWN = 3;
	private static final int CNS_CREAT = 4;
	private static final int CNS_MKDIR = 5;
	private static final int CNS_RENAME = 6;
	private static final int CNS_RMDIR = 7;
	private static final int CNS_STAT = 8;
	private static final int CNS_UNLINK = 9;
	private static final int CNS_OPENDIR = 10;
	private static final int CNS_READDIR = 11;
	private static final int CNS_CLOSEDIR = 12;
	// private static final int CNS_OPEN = 13;
	// private static final int CNS_CLOSE = 14;
	// private static final int CNS_SETATIME = 15;
	private static final int CNS_SETFSIZE = 16;
	// private static final int CNS_SHUTDOWN = 17;
	// private static final int CNS_GETSEGAT = 18;
	// private static final int CNS_SETSEGAT = 19;
	// private static final int CNS_LISTTAPE = 20;
	// private static final int CNS_ENDLIST = 21;
	// private static final int CNS_GETPATH = 22;
	private static final int CNS_DELETE = 23;
	// private static final int CNS_UNDELETE = 24;
	// private static final int CNS_CHCLASS = 25;
	// private static final int CNS_DELCLASS = 26;
	// private static final int CNS_ENTCLASS = 27;
	// private static final int CNS_MODCLASS = 28;
	// private static final int CNS_QRYCLASS = 29;
	// private static final int CNS_LISTCLASS = 30;
	// private static final int CNS_DELCOMMENT = 31;
	// private static final int CNS_GETCOMMENT = 32;
	// private static final int CNS_SETCOMMENT = 33;
	// private static final int CNS_UTIME = 34;
	// private static final int CNS_REPLACESEG = 35;
	// private static final int CNS_GETACL = 37;
	// private static final int CNS_SETACL = 38;
	private static final int CNS_LCHOWN = 39;
	private static final int CNS_LSTAT = 40;
	private static final int CNS_READLINK = 41;
	private static final int CNS_SYMLINK = 42;
	private static final int CNS_ADDREPLICA = 43;
	private static final int CNS_DELREPLICA = 44;
	private static final int CNS_LISTREPLICA = 45;
	private static final int CNS_STARTTRANS = 46;
	private static final int CNS_ENDTRANS = 47;
	private static final int CNS_ABORTTRANS = 48;
	// private static final int CNS_LISTLINKS = 49;
	// private static final int CNS_SETFSIZEG = 50;
	private static final int CNS_STATG = 51;
	private static final int CNS_STATR = 52;
	// private static final int CNS_SETPTIME = 53;
	// private static final int CNS_SETRATIME = 54;
	// private static final int CNS_SETRSTATUS = 55;
	// private static final int CNS_ACCESSR = 56;
	// private static final int CNS_LISTREP4GC = 57;
	// private static final int CNS_LISTREPLICAX = 58;
	private static final int CNS_STARTSESS = 59;
	private static final int CNS_ENDSESS = 60;
	// private static final int CNS_DU = 61;
	private static final int CNS_GETGRPID = 62;
	private static final int CNS_GETGRPNAM = 63;
	// private static final int CNS_GETIDMAP = 64;
	private static final int CNS_GETUSRID = 65;
	private static final int CNS_GETUSRNAM = 66;
	// private static final int CNS_MODGRPMAP = 67;
	// private static final int CNS_MODUSRMAP = 68;
	// private static final int CNS_RMGRPMAP = 69;
	// private static final int CNS_RMUSRMAP = 70;
	// private static final int CNS_GETLINKS = 71;
	// private static final int CNS_GETREPLICA = 72;
	// private static final int CNS_ENTGRPMAP = 73;
	// private static final int CNS_ENTUSRMAP = 74;
	// private static final int CNS_SETRTYPE = 75;
	// private static final int CNS_MODREPLICA = 76;
	// private static final int CNS_GETREPLICAX = 77;
	// private static final int CNS_LISTREPSET = 78;
	// private static final int CNS_SETRLTIME = 79;
	// private static final int CNS_GETREPLICAS = 80;
	private static final int CNS_GETGRPNAMES = 81;
	// private static final int CNS_PING = 82;
	private static final int CNS_DELFILES = 83;
	// private static final int CNS_DELFILESBYP = 84;
	// private static final int CNS_DELREPLICAS = 85;
	// private static final int CNS_GETGRPMAP = 86;
	// private static final int CNS_GETUSRMAP = 87;
	// private static final int CNS_GETREPLICAL = 88;

	public static final short CNS_LIST_BEGIN = 0;
	public static final short CNS_LIST_CONTINUE = 1;
	public static final short CNS_LIST_END = 2;

	private static final byte REQUEST_GSI_TOKEN[] = { 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x47, 0x53, 0x49, 0x00, 0x49, 0x44, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01 };

	private boolean sessionStarted = false;
	
	/**
	 * Messages that are returned when CNS_IRC or CNS_RC are received
	 */
	public enum NSError{
		OPERATION_NOT_PERMITTED(1,"Operation not permitted"),
		NO_SUCH_FILE_OR_DIRECTORY(2, "No such file or directory"),
		NO_SUCH_PROCESS(3, "No such process"),
		INTERRUPTED_SYSTEM_CALL(4, "Interrupted system call"),
		IO_ERROR(5, "I/O error"),
		NO_SUCH_DEVICE_OR_ADDRESS(6, "No such device or address"),
		ARGUMENT_LIST_TOO_LONG(7, "Argument list too long"),
		EXEC_FORMAT_ERROR(8, "Exec format error"),
		BAD_FILE_NUMBER(9, "Bad file number"),
		NO_CHILD_PROCESSES(10, "No child processes"),
		TRY_AGAIN(11, "Try again"),
		OUT_OF_MEMORRY(12, "Out of memory"),
		PERMISSION_DENIED(13, "Permission denied"),
		BAD_ADDRESS(14, "Bad address"),
		BLOCK_DEVICE_REQUIRED(15, "Block device required"),
		DEVICE_OR_RESOURCE_BUSY(16, "Device or resource busy"),
		FILE_EXISTS(17, "File exists"),
		CROSS_DEVICE_LINK(18, "Cross-device link"),
		NO_SUCH_DEVICE(19, "No such device"),
		NOT_A_DIRECTORY(20, "Not a directory"),
		IS_A_DIRECOTRY(21, "Is a directory"),
		INVALID_ARGUMENT(22, "Invalid argument"),
		FILE_TABLE_OVERFLOW(23, "File table overflow"),
		TOO_MANY_OPEN_FILES(24, "Too many open files"),
		NOT_A_TYPEWRITER(25, "Not a typewriter"),
		TEXT_FILE_BUSY(26, "Text file busy"),
		FILE_TOO_LARGE(27, "File too large"),
		NO_SPACE_LEFT_ON_DEVICE(28, "No space left on device"),
		ILLEGAL_SEEK(29, "Illegal seek"),
		READ_ONLY(30, "Read-only file system"),
		TOO_MANY_LINKS(31, "Too many links"),
		BROKEN_PIPE(32, "Broken pipe"),
		MATH_ARGUMENT_OUT_OF_DOMAIN_OF_FUNC(33, "Math argument out of domain of func"),
		MATH_RESULT_NOT_REPRESENTABLE(34, "Math result not representable"),
		
		/* Extract from serrno.man */
		TIMED_OUT(1004, "Has timed out"),
		INTERNAL_ERROR(1015, "Internal error"),
		
		UNKOWN_ERROR(9999, "Unknown LFC error");
		
		private String message;
		private int error;
		
		private NSError(int error, String message) {
			this.error = error;
			this.message = message;
		}
		public String getMessage(){
			return message;
		}
		public int getError(){
			return error;
		}
		
		@Override
		public String toString() {
			return "Error "+error+": "+message;
		}
		
		public static NSError fromError(int error){
			NSError[] lfcErrors = NSError.values();
			for (int i = 0; i < lfcErrors.length; i++) {
				if(lfcErrors[i].error == error){
					return lfcErrors[i];
				}
			}
			return null;
		}
	}
	
	/**
	 * Values that can be passed to the access function.
	 */
	public static enum AccessType {
		READ_OK(4), /* Test for read permission. */
		WRITE_OK(2), /* Test for write permission. */
		EXECUTE_OK(1), /* Test for execute permission. */
		EXIST_OK(0); /* Test for existence. */

		private int value = -1;

		private AccessType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	private final ByteBuffer sendBuf = ByteBuffer.allocateDirect(REQBUFSZ);
	private final ByteBuffer recvBuf = ByteBuffer.allocateDirect(REPBUFSZ);
	private SocketChannel channel;

	private final GSSCredential gssCredential;
	private final String host;
	private final int port;

	NSConnection(String host, int port, final GSSCredential gssCredential) throws IOException, ReceiveException, LFCBrokenPipeException {
		this.host = host;
		this.port = port;
		this.gssCredential = gssCredential;
		init();
	}
		
	void init() throws IOException, ReceiveException, LFCBrokenPipeException {
		channel = SocketChannel.open(new InetSocketAddress(host, port));
		s_logger.debug("Establishing Connection with " + host + ":" + port);
		try{
			authenticate();
		}catch (IOException e) {
			throw e;
		}catch (ReceiveException e) {
			e.setExecutedCmd("initNSConnection()");
			throw e;
		}
		s_logger.debug("Connection established with " + host + ":" + port);
		if(sessionStarted){
		    startSession(true);
		}
	}

	private void preparePacket(int magic, int command) {
		sendBuf.clear();
		sendBuf.putInt(magic);
		sendBuf.putInt(command);
		sendBuf.mark();
		sendBuf.putInt(0);
	}

	private NSResponse sendAndReceive(boolean includeHeaderInLength) throws IOException, ReceiveException, LFCBrokenPipeException {
		send(includeHeaderInLength);
		return receive();
	}

	private void send(boolean includeHeaderInLength) throws IOException, ReceiveException, LFCBrokenPipeException {
		int posNow = sendBuf.position();
		sendBuf.reset();
		if (includeHeaderInLength){
			sendBuf.putInt(posNow);
		}else{
			sendBuf.putInt(posNow - HEADER_SIZE);
		}
		sendBuf.position(posNow);
		sendBuf.flip();
		//Try to reconnect if the channel was closed...
//		boolean done = false;
//		int max_connection = 2;
//		while (!done && max_connection-- > 0) {
//			try{
				int writtenBites = channel.write(sendBuf);
				if(writtenBites != posNow){
					throw new IOException("Socket Error: writtenBites="+writtenBites+" - bytesToWrite="+posNow );
				}
//				done = true;
//			}catch (IOException e) {
//				throw new LFCBrokenPipeException(e.getMessage());
//			}
//		}
	}

	private NSResponse receive() throws IOException, ReceiveException, LFCBrokenPipeException {
		try{
			int sizeOrError;
			ByteBuffer respBuffer = null;
			while(true){
				recvBuf.clear();
				//Read only the header.
				recvBuf.limit(HEADER_SIZE);
				int ret = channel.read(recvBuf);
				if(ret == -1){
					throw new LFCBrokenPipeException("Broken pipe...The socket connection with the NS server was closed unexpectedly.");
				}
				int timeout = 5;
			    int delay = 100;
				while ((recvBuf.position() < HEADER_SIZE) && timeout-- > 0) {
					try {
						ret = channel.read(recvBuf);
						if(ret == -1){
							throw new LFCBrokenPipeException("Broken pipe...The socket connection with the NS server was closed unexpectedly.");
						}
						s_logger.debug("\t HEAD: waiting " + delay + " [ms]... AVAIL=" + recvBuf.position() + ", EXP="+HEADER_SIZE);
						Thread.sleep(delay);
						delay *= 2;
					} catch (InterruptedException exc) {
						s_logger.error(exc);
					}
				}
			    if ( recvBuf.position() < HEADER_SIZE ) {
			      throw new LFCBrokenPipeException( "Connection timeout during receiving header." );
			    }
	//		    s_logger.debug("AVAIL=" + recvBuf.position() + ", EXP="+HEADER_SIZE);
			    recvBuf.flip();
				int magic = recvBuf.getInt();
				int rep_type = recvBuf.getInt();
				sizeOrError = recvBuf.getInt();
				s_logger.debug("Received M/T/S: " + magic + " " + rep_type + " " + sizeOrError);
		
				if (sizeOrError > REPBUFSZ){
					throw new ReceiveException(NSError.INTERNAL_ERROR.getError(), "reply too large: Received M/T/S: " + magic + " " + rep_type + " " + sizeOrError);
				}
				
				//Put a mark at the end of the header and allow o read the rest of the data
				recvBuf.mark();
				recvBuf.limit(recvBuf.capacity());
				
				if (magic == CSEC_TOKEN_MAGIC_1) {
					//SSL Protocol error
					if ((rep_type != CSEC_TOKEN_TYPE_PROTOCOL_RESP) && (rep_type != CSEC_TOKEN_TYPE_HANDSHAKE) && (rep_type != CSEC_TOKEN_TYPE_HANDSHAKE_FINAL)) {
						throw new ReceiveException(sizeOrError, "Received invalid CSEC Type: " + rep_type);
					}
					readChannelData(recvBuf, sizeOrError);
					recvBuf.reset();
					return new NSResponse(sizeOrError, null);
				} else if (magic == CNS_MAGIC) {
					
					if ((rep_type == CNS_IRC) || (rep_type == CNS_RC)) {
						if (sizeOrError == 0){
							return new NSResponse(0, respBuffer);
						}
						NSError nSError = NSError.fromError(sizeOrError);
						if (nSError == null){
							String errorMessage = "Unknown NS error value: " + sizeOrError;
							throw new ReceiveException(sizeOrError, errorMessage);
						}
						throw new ReceiveException(nSError);
					} else if (rep_type == MSG_ERR) {
						String errorMessage = getString(recvBuf);
						throw new ReceiveException(sizeOrError, errorMessage);
					} else if (rep_type == MSG_DATA){
						respBuffer = ByteBuffer.allocateDirect(sizeOrError);
						readChannelData(respBuffer, sizeOrError);
						respBuffer.flip();
					} else if (rep_type == MSG_LINKS){
						throw new ReceiveException(NSError.UNKOWN_ERROR.getError(), "MSG_LINKS Not implemented yet");
					} else if (rep_type == MSG_REPLIC){
						throw new ReceiveException(NSError.UNKOWN_ERROR.getError(), "MSG_REPLIC Not implemented yet");
					} else if (rep_type == MSG_REPLICP){
						throw new ReceiveException(NSError.UNKOWN_ERROR.getError(), "MSG_REPLICP Not implemented yet");
					} else if (rep_type == MSG_REPLICX){
						throw new ReceiveException(NSError.UNKOWN_ERROR.getError(), "MSG_REPLICX Not implemented yet");
					} else if (rep_type == MSG_REPLICS){
						throw new ReceiveException(NSError.UNKOWN_ERROR.getError(), "MSG_REPLICS Not implemented yet");
					} else if (rep_type == MSG_GROUPS){
						throw new ReceiveException(NSError.UNKOWN_ERROR.getError(), "MSG_GROUPS Not implemented yet");
					} else if (rep_type == MSG_STATUSES){
						throw new ReceiveException(NSError.UNKOWN_ERROR.getError(), "MSG_STATUSES Not implemented yet");
					} else if (rep_type == MSG_FILEST){
						throw new ReceiveException(NSError.UNKOWN_ERROR.getError(), "MSG_FILEST Not implemented yet");
					} else if (rep_type == MSG_GRPINFO){
						throw new ReceiveException(NSError.UNKOWN_ERROR.getError(), "MSG_GRPINFO Not implemented yet");
					} else if (rep_type == MSG_USRINFO){
						throw new ReceiveException(NSError.UNKOWN_ERROR.getError(), "MSG_USRINFO Not implemented yet");
					} else {
						throw new ReceiveException(sizeOrError, "Received invalid MSG Type: " + rep_type);
					}
				} else {
					throw new ReceiveException(sizeOrError, "Recieved invalid Magic/Type: " + magic + "/" + rep_type);
				}
			}
		}catch (BufferUnderflowException e) {
			throw new ReceiveException(-9999, e.getMessage());
		}
	}
	
	private class NSResponse{
		private final int responseCode;
		
		private ByteBuffer dataRespBuffer;

		public NSResponse(int responseCode, ByteBuffer dataRespBuffer) {
			this.responseCode = responseCode;
			this.dataRespBuffer = dataRespBuffer;
		}
		
		public ByteBuffer getDataRespBuffer() {
			return dataRespBuffer;
		}
		
		public int getResponseCode() {
			return responseCode;
		}
	}
	
	private void readChannelData(ByteBuffer respBuffer, int dataSize) throws IOException, LFCBrokenPipeException{
		int ret = channel.read(respBuffer);
		if(ret == -1){
			throw new LFCBrokenPipeException("Broken pipe...The socket connection with the NS server was closed unexpectedly.");
		}
		int timeout = 10;
	    int delay = 1;
		while ((respBuffer.position() < dataSize) && timeout-- > 0) {
			s_logger.debug("Limit: " + respBuffer.limit() + ", Pos: " + respBuffer.position() + " MinContent: " + dataSize + " Avail " + respBuffer.remaining());
			try {
				ret = channel.read(respBuffer);
				if(ret == -1){
					throw new LFCBrokenPipeException("Broken pipe...The socket connection with the NS server was closed unexpectedly.");
				}
				if(delay > 3){
					s_logger.debug("\t DATA: waiting " + delay + " [ms]... AVAIL=" + respBuffer.position() + ", EXP="+dataSize);
					Thread.sleep(delay);
				}
				delay *= 2;
			} catch (InterruptedException exc) {
				s_logger.error(exc);
			}
		}
		s_logger.debug("Limit: " + respBuffer.limit() + ", Pos: " + respBuffer.position() + " MinContent: " + dataSize + " Avail " + respBuffer.remaining());
	    if ( respBuffer.position() < dataSize ) {
	      throw new LFCBrokenPipeException( "Connection timeout during receiving header." );
	    }
	}

	private void addIDs() {
		int uid = 0;
		int gid = 0;
		sendBuf.putInt(uid);
		sendBuf.putInt(gid);
	}

	private void authenticate() throws IOException, ReceiveException, LFCBrokenPipeException {
		preparePacket(CSEC_TOKEN_MAGIC_1, CSEC_TOKEN_TYPE_PROTOCOL_REQ);
		sendBuf.put(REQUEST_GSI_TOKEN);
		sendAndReceive(false);
		GSSManager manager = new GlobusGSSManagerImpl();
		try {
			ExtendedGSSContext secureContext = (ExtendedGSSContext) manager.createContext(null, GSSConstants.MECH_OID, gssCredential, 12 * 3600);

			secureContext.requestMutualAuth(true);
			secureContext.requestAnonymity(false);
			secureContext.requestConf(false);
			secureContext.requestCredDeleg(false);
			secureContext.setOption(GSSConstants.GSS_MODE, GSIConstants.MODE_GSI);
			secureContext.setOption(GSSConstants.REJECT_LIMITED_PROXY, Boolean.FALSE);
			byte[] recvToken = new byte[0];
			while (!secureContext.isEstablished()) {
				byte[] sendToken = secureContext.initSecContext(recvToken, 0, recvToken.length);
//				s_logger.debug("Called initSecContext, doing another iteration");

				if (sendToken != null) {
					preparePacket(CSEC_TOKEN_MAGIC_1, CSEC_TOKEN_TYPE_HANDSHAKE);
					sendBuf.put(sendToken);
					send(false);
				}

				if (!secureContext.isEstablished()) {
					int l = receive().getResponseCode();
					recvToken = new byte[l];
					recvBuf.get(recvToken);
				}
			}			
		} catch (GSSException e) {
			s_logger.warn(e.toString());
			throw new IOException("Error processing credential");
		}
		s_logger.debug("Secure Context established!");
	}

	private void putString(String s) {
		try {
			if (s != null) {
				// TODO: Check if UTF-8 is correct!
				sendBuf.put(s.getBytes("UTF-8"));
			}
		} catch (java.io.UnsupportedEncodingException e) {
			s_logger.warn(e.toString());
		}
		sendBuf.put((byte) 0);
	}

	static String getString(ByteBuffer respBuffer) throws IOException {
		// TODO: This uses Latin-1!
		StringBuilder builder = new StringBuilder();
		byte b = respBuffer.get();
		while (b != 0) {
			builder.append((char) b);
//			if (respBuffer.remaining() == 0) {
//				respBuffer.clear();
//				channel.read(respBuffer);
//			}
			b = respBuffer.get();
		}
		return builder.toString();
	}

	
	
	void startSession() throws IOException, ReceiveException, LFCBrokenPipeException {
	    startSession(false);
	}
	
	private void startSession(boolean force) throws IOException, ReceiveException, LFCBrokenPipeException {
	    if(!sessionStarted || force){
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_STARTSESS);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(null); //Session comment in the LFC logs.
				sendAndReceive(true);
				sessionStarted = true;
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	    }
	}
	
	void endSession() throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_ENDSESS);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				sendAndReceive(true);
				sessionStarted = false;
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}
	
	/**
	 * Start a new transaction. The comment parameter can be <code>null</code>
	 * @param comment The transaction comment
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws LFCBrokenPipeException
	 */
	void startTransaction(String comment) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(comment != null ? CNS_MAGIC2 : CNS_MAGIC, CNS_STARTTRANS);
				addIDs();
				putString(comment);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}
	
	void endTransaction() throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_ENDTRANS);
				addIDs();
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}
	
	void abordTransaction() throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_ABORTTRANS);
				addIDs();
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}
	
	NSFile stat(String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_STAT);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				sendBuf.putLong(0L); // 0
				putString(path);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				NSFile file = new NSFile(respBuffer, false, false, false, false);
				if(respBuffer.hasRemaining()){
					throw new IOException("stat: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return file;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}
	
	NSFile statg(String path, String guid) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_STATG);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(path);
				putString(guid);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				NSFile file = new NSFile(respBuffer, false, true, true, false);
				if(respBuffer.hasRemaining()){
					throw new IOException("statg: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return file;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}
	
	//sfn = physical path
	NSFile statr(String sfn) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_STATR);
				addIDs();
				putString(sfn);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				NSFile file = new NSFile(respBuffer, false, true, true, false);
				if(respBuffer.hasRemaining()){
					throw new IOException("statr: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return file;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}


	NSFile lstat(String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_LSTAT);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				sendBuf.putLong(0L); // 0
				putString(path);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				NSFile file = new NSFile(respBuffer, false, false, false, false);
				if(respBuffer.hasRemaining()){
					throw new IOException("lstat: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return file;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}
	
	String readlink(String link) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_READLINK);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(link);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				String path = getString(respBuffer);
				if(respBuffer.hasRemaining()){
					throw new IOException("readlink: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return path;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}
	
	void link(String path, String target) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_SYMLINK);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(path);
				putString(target);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	int getGrpByName(String grpName) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				if ("root".equals(grpName)) {
					return 0;
				}
				preparePacket(CNS_MAGIC, CNS_GETGRPID);
				putString(grpName);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				int s = respBuffer.getInt();
				if(respBuffer.hasRemaining()){
					throw new IOException("getGrpByName: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return s;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	String getGrpByGid(int gid) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				if (gid == 0) {
					return "root";
				}
				preparePacket(CNS_MAGIC, CNS_GETGRPNAM);
				sendBuf.putShort((short) 0);
				sendBuf.putShort((short) gid);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				String group = getString(respBuffer);
				if(respBuffer.hasRemaining()){
					throw new IOException("getGrpByGid: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return group;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	Collection<String> getGrpByGids(int[] gids) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				ArrayList<Integer> rootGIDIndexes = new ArrayList<Integer>();
				ArrayList<Integer> newGids = new ArrayList<Integer>();
				for (int i = 0; i < gids.length; i++) {
					if (gids[i] == 0) {
						rootGIDIndexes.add(i);
					} else {
						newGids.add(gids[i]);
					}
				}
		
				gids = new int[newGids.size()];
				for (int i = 0; i < newGids.size(); i++) {
					gids[i] = newGids.get(i);
				}
		
				preparePacket(CNS_MAGIC, CNS_GETGRPNAMES);
				sendBuf.putShort((short) 0);
				sendBuf.putShort((short) gids.length);
				for (int i = 0; i < gids.length; i++) {
					sendBuf.putInt(gids[i]);
				}
				
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
		
				Collection<String> grpNames = new ArrayList<String>(gids.length);
				for (int i = 0; i < gids.length; i++) {
					if (rootGIDIndexes.contains(i)) {
						grpNames.add("root");
					} else {
						grpNames.add(getString(respBuffer));
					}
				}
				if(respBuffer.hasRemaining()){
					throw new IOException("getGrpByGids: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return grpNames;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	int getUsrByName(String usrName) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				if ("root".equals(usrName)) {
					return 0;
				}
				preparePacket(CNS_MAGIC, CNS_GETUSRID);
				putString(usrName);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				int s = respBuffer.getInt();
				if(respBuffer.hasRemaining()){
					throw new IOException("getUsrByName: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return s;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	String getUsrByUid(int uid) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				if (uid == 0) {
					return "root";
				}
				preparePacket(CNS_MAGIC, CNS_GETUSRNAM);
				sendBuf.putShort((short) 0);
				sendBuf.putShort((short) uid);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				String user = getString(respBuffer);
				if(respBuffer.hasRemaining()){
					throw new IOException("getUsrByUid: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return user;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	/**
	 * Test if a specific access is allowed.
	 * 
	 * @param path
	 *            path that has to be tested
	 * @param accessType
	 *            The access to test
	 * @throws IOException
	 *             if something wrong occurs
	 * @throws ReceiveException
	 *             if received an error message from the LFC
	 * @throws TimeoutException 
	 */
	void access(String path, AccessType accessType) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_ACCESS);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(path);
				sendBuf.putInt(accessType.getValue());
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}
	
	/**
	 * 
	 * /!\ SESSIONS NOT SUPPORTED /!\
	 * 
	 */
	long opendir(String path, String guid) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				if (guid == null) {
					preparePacket(CNS_MAGIC, CNS_OPENDIR);
				} else {
					preparePacket(CNS_MAGIC2, CNS_OPENDIR);
				}
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				putString(guid);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				long fileId = respBuffer.getLong();
				if(respBuffer.hasRemaining()){
					throw new IOException("opendir: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
				}
				return fileId;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				//CNS_OPENDIR, CNS_READDIR and CNS_CLOSEDIR must be done with the same connection...
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}
	
	/**
	 * 
	 * /!\ SESSIONS NOT SUPPORTED /!\
	 * 
	 */
	void closedir() throws IOException, ReceiveException, LFCBrokenPipeException {
		preparePacket(CNS_MAGIC, CNS_CLOSEDIR);
		sendAndReceive(true);
	}
	
	/**
	 * Read a directory entry
	 * 
	 * 
	 * /!\ SESSIONS NOT SUPPORTED /!\
	 * 
	 * 
	 * @param fileID
	 *            The id of the directory
	 * @return A collection of {@link NSFile} which are inside the directory
	 * @throws IOException
	 *             if something wrong occurs
	 * @throws TimeoutException 
	 */
	/**
	 * @param fileID	The id of the directory
	 * @param bod		Beginning of directory (0 or 1)
	 * @param eod		End of directory: This has to be checked by the caller of the method. Here we use a table because we cannot
	 * 					pass a Short/short by reference. The table must have a length of at least 1 (it should be one nothing else)!! 
	 * @return
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws LFCBrokenPipeException
	 */
	Collection<NSFile> readdir(long fileID, short bod, short[] eod) throws IOException, ReceiveException, LFCBrokenPipeException {
		preparePacket(CNS_MAGIC, CNS_READDIR);
		addIDs();
		sendBuf.putShort((short) 1); // 1 = full list (w/o comments), 0 = names only
		sendBuf.putShort((short) 0);
		sendBuf.putLong(fileID);
		sendBuf.putShort((short) bod); // beginning of directory
		ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
		
		short count = respBuffer.getShort();
		Collection<NSFile> lfcFiles = new ArrayList<NSFile>(count);
		for (short i = 0; i < count; i++) {
			NSFile file = new NSFile(respBuffer, true, false, false, false);
			lfcFiles.add(file);
		}		
		eod[0] = respBuffer.getShort();
		if(respBuffer.hasRemaining()){
			throw new IOException("readdir: Something remains to be read ("+respBuffer.remaining()+" byte(s))");
		}
		return lfcFiles;
	}

	/**
	 * Use UNLINK command instead of DELETE to remove files. DELETE command is
	 * for CASTOR entries only
	 * @throws TimeoutException 
	 */
	int delete(String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_DELETE);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(path);
				sendAndReceive(true);
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	/**
	 * Use this command instead of DELETE to remove files. DELETE command is for
	 * CASTOR entries only
	 * @throws TimeoutException 
	 */
	void unlink(String path) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_UNLINK);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	/**
	 * Set the file size
	 * @throws TimeoutException 
	 */
	void setfsize(String path, long fileSize) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC2, CNS_SETFSIZE);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				sendBuf.putLong(0L);
				putString(path);
				sendBuf.putLong(fileSize);
				putString(null);
				putString(null);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	/**
	 * Rename a file or a directory
	 * @throws TimeoutException 
	 */
	void rename(String oldPath, String newPath) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_RENAME);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(oldPath);
				putString(newPath);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	/**
	 * Change the permissions of a file or a directory. If the path represent a
	 * symbolic link, the pointed file/directory will be modified, not the
	 * symbolic link itself. TODO: Check the symbolic link behavior.
	 * @throws TimeoutException 
	 */
	void chmod(String path, int mode) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_CHMOD);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				mode &= 07777;
				sendBuf.putInt(mode);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	/**
	 * Change the owner and the group of a file or a directory. If the path
	 * represent a symbolic link, the pointed file/directory will be modified,
	 * not the symbolic link itself.
	 * @throws TimeoutException 
	 */
	void chown(String path, int new_uid, int new_gid) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_CHOWN);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				sendBuf.putInt(new_uid);
				sendBuf.putInt(new_gid);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	/**
	 * Change the owner and the group of a file or a directory. If the path
	 * represent a symbolic link, this later itself will be modified
	 * @throws TimeoutException 
	 */
	void lchown(String path, int new_uid, int new_gid) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_LCHOWN);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				sendBuf.putInt(new_uid);
				sendBuf.putInt(new_gid);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	/**
	 * List all the file replicas
	 * 
	 * /!\ SESSIONS NOT SUPPORTED /!\
	 * 
	 * @param path
	 * @param guid
	 * @param flag	{@link NSConnection#CNS_LIST_BEGIN} or {@link NSConnection#CNS_LIST_CONTINUE} or {@link NSConnection#CNS_LIST_END}
	 * @param eol	End of list: This has to be checked by the caller of the method. Here we use a table because we cannot
	 * 				pass a Short/short by reference. The table must have a length of at least 1 (it should be one nothing else)!! 
	 * @return
	 * @throws IOException
	 * @throws ReceiveException
	 * @throws LFCBrokenPipeException
	 */
	Collection<NSReplica> listReplica(String path, String guid, short flag, short[] eol) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC2, CNS_LISTREPLICA);
				addIDs();
				sendBuf.putShort((short) 0); // Size of nbentry
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				putString(guid);
				sendBuf.putShort(flag); // CNS_LIST_BEGIN, CNS_LIST_CONTINUE, CNS_LIST_END
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
		
				short count = respBuffer.getShort();
		
				Collection<NSReplica> replicas = new ArrayList<NSReplica>(count);
				for (short i = 0; i < count; i++) {
					NSReplica replica = new NSReplica(respBuffer);
					replicas.add(replica);
				}
				eol[0] = respBuffer.getShort(); //End of list
				if(respBuffer.hasRemaining()){
					throw new IOException("listReplica: Something remains to be read ("+respBuffer.remaining()+")");
				}
				return replicas;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}
	
	boolean delFiles(String[] guids, boolean force) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_DELFILES);
				addIDs();
				final short argtype = 0;
				final short sforce;
				if (force) {
					sforce = 1;
				} else {
					sforce = 0;
				}
				sendBuf.putShort(argtype);
				sendBuf.putShort(sforce);
				sendBuf.putInt(guids.length); // nbguids
				for (int i = 0; i < guids.length; i++) {
					putString(guids[i]);
				}
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				int nbstatuses = respBuffer.getInt();
				return nbstatuses == 0;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	void rmdir(String path) throws IOException, ReceiveException , LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				long cwd = 0L;
				preparePacket(CNS_MAGIC, CNS_RMDIR);
				addIDs();
				sendBuf.putLong(cwd);
				putString(path);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	long creat(String path, String guid) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				short mask = 0;
				long cwd = 0L;
				int mode = 0666;
				preparePacket(CNS_MAGIC2, CNS_CREAT);
				addIDs();
				sendBuf.putShort(mask);
				sendBuf.putLong(cwd);
				putString(path);
				sendBuf.putInt(mode);
				putString(guid);
				ByteBuffer respBuffer = sendAndReceive(true).getDataRespBuffer();
				return respBuffer.getLong();
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	void mkdir(String path, String guid) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				short mask = 0;
				long cwd = 0L;
				int mode = 0777;
				preparePacket(CNS_MAGIC2, CNS_MKDIR);
				addIDs();
				sendBuf.putShort(mask);
				sendBuf.putLong(cwd);
				putString(path);
				sendBuf.putInt(mode);
				putString(guid);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	/**
	 * It removes replica information from catalog, data stored on Storage
	 * Element stays intact
	 * @throws TimeoutException 
	 */
	void delReplica(String guid, String replicaUri) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				long id = 0L;
				preparePacket(CNS_MAGIC, CNS_DELREPLICA);
				addIDs();
				sendBuf.putLong(id);
				putString(guid);
				putString(replicaUri);
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	void addReplica(String guid, URI replicaUri) throws IOException, ReceiveException, LFCBrokenPipeException {
		for (int z = 0; z <= MAX_RETRY_IF_BROKEN_PIPE; z++) {
			try{
				preparePacket(CNS_MAGIC4, CNS_ADDREPLICA);
				this.addIDs();
				sendBuf.putLong(0L); 
				this.putString(guid); // uniqueId
				this.putString(replicaUri.getHost() == null ? "UNKNOWN" : replicaUri.getHost());
				this.putString(replicaUri.toString());
				sendBuf.put((byte) '-'); // status;
				sendBuf.put((byte) 'P'); // file type
				this.putString(null); // pool name
				this.putString(null); // fs
				sendBuf.put((byte) 'P'); // r_type
				this.putString(null); // setname
				sendAndReceive(true);
				break;
			}catch (LFCBrokenPipeException e) {
				if(z == MAX_RETRY_IF_BROKEN_PIPE ){
					throw e;
				}
				init();
			}
		}
	}

	


	/**
	 * Exception that can be thrown by the {@link NSConnection#receive()}
	 * function
	 */
	public final class ReceiveException extends Exception {
		private static final long serialVersionUID = 1L;
		private int error = -1;
		private NSError nSError = NSError.UNKOWN_ERROR;
		private String executedCmd = null;

		public ReceiveException(int error, String s) {
			super(s);
			this.error = error;
			NSError m_lfcError = NSError.UNKOWN_ERROR;
			m_lfcError.message = s;
			m_lfcError.error = error;
			this.nSError = m_lfcError;
		}
		
		public ReceiveException(NSError nSError) {
			super(nSError.getMessage());
			this.error = nSError.getError();
			this.nSError = nSError;
		}

		public void setExecutedCmd(String executedCmd) {
			if(this.executedCmd == null){
				this.executedCmd = executedCmd;
			}
		}
		
		public String getExecutedCmd() {
			return this.executedCmd;
		}
		
		@Override
		public String getMessage() {
			return executedCmd+": "+nSError.toString();
		}
		
		/**
		 * 
		 * @return The LFC error code
		 */
		public int getLFCErrorNumber() {
			return error;
		}

		/**
		 * @return The {@link NSError} object representing the error or <code>null</code> if this is an unknown error.
		 */
		public NSError getLFCError() {
			return nSError;
		}
	}

	/**
	 * Try to close the connection to free resources.
	 * @throws IOException If a problem occurs
	 */
	void close() throws IOException {
		try {
			this.channel.close();
		} catch (IOException e) {
			s_logger.warn(e.toString());
			throw e;
		}
	}
	
	/**
	 * Specific Exception thrown if the connction with the LFC
	 * throws a timeout.
	 *
	 */
	public class LFCBrokenPipeException extends Exception {

		private static final long serialVersionUID = 1L;
		public LFCBrokenPipeException(String message) {
			super(message);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} catch (IOException e) {
			// ignore
		}
		super.finalize();
	}
}