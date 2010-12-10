package fr.in2p3.jsaga.adaptor.lfc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.GlobusGSSManagerImpl;
import org.gridforum.jgss.ExtendedGSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

/**
 * Low-Level connection to an LFC server.
 * 
 * @author Max Berger
 * @author Jerome Revillard
 */
public class LfcConnection {
	private static Logger s_logger = Logger.getLogger(LfcConnection.class);
	
	public final static int MAX_RETRY_IF_TIMEOUT = 3;
	
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

	private static final int BUF_SIZE = 10240;

	private static final int CSEC_TOKEN_MAGIC_1 = 0xCA03;
	private static final int CSEC_TOKEN_TYPE_PROTOCOL_REQ = 0x1;
	private static final int CSEC_TOKEN_TYPE_PROTOCOL_RESP = 0x2;
	private static final int CSEC_TOKEN_TYPE_HANDSHAKE = 0x3;
	private static final int CSEC_TOKEN_TYPE_HANDSHAKE_FINAL = 0x5;
	// private static final int CSEC_TOKEN_TYPE_ERROR = 0x6;

	private static final int CNS_MAGIC = 0x030E1301;

	private static final int CNS_RESP_MSG_ERROR = 1;
	private static final int CNS_RESP_MSG_DATA = 2;
	private static final int CNS_RESP_RC = 3;
	private static final int CNS_RESP_IRC = 4;
	private static final int CNS_RESP_MSG_GROUPS = 10;
	private static final int CNS_RESP_MSG_SUMMARY = 11;

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
	// private static final int CNS_STARTTRANS = 46;
	// private static final int CNS_ENDTRANS = 47;
	// private static final int CNS_ABORTTRANS = 48;
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

	private static final int CNS_MAGIC2 = 0x030E1302;
	private static final int CNS_MAGIC4 = 0x030E1304;

	private static final byte REQUEST_GSI_TOKEN[] = { 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x47, 0x53, 0x49, 0x00, 0x49, 0x44, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01 };

	/**
	 * Messages that are returned when CNS_RESP_IRC or CNS_RESP_RC are received
	 */
	public enum LfcError{
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
		
		private LfcError(int error, String message) {
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
		
		public static LfcError fromError(int error){
			LfcError[] lfcErrors = LfcError.values();
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

	private final ByteBuffer sendBuf = ByteBuffer.allocateDirect(BUF_SIZE);
	private ByteBuffer recvBuf = ByteBuffer.allocateDirect(BUF_SIZE);
	private SocketChannel channel;

	private final GSSCredential gssCredential;
	private final String host;
	private final int port;

	public LfcConnection(String host, int port, final GSSCredential gssCredential) throws IOException, ReceiveException, TimeoutException {
		this.host = host;
		this.port = port;
		this.gssCredential = gssCredential;
		init();
	}
		
	public void init()throws IOException, ReceiveException, TimeoutException{
		channel = SocketChannel.open(new InetSocketAddress(host, port));
		s_logger.debug("Establishing Connection with " + host + ":" + port);
		try{
			authenticate();
		}catch (IOException e) {
			throw e;
		}catch (ReceiveException e) {
			e.setExecutedCmd("initLFCConnection()");
			throw e;
		}
		s_logger.debug("Connection established with " + host + ":" + port);
	}

	private void preparePacket(int magic, int command) {
		sendBuf.clear();
		sendBuf.putInt(magic);
		sendBuf.putInt(command);
		sendBuf.mark();
		sendBuf.putInt(0);
	}

	private int sendAndReceive(boolean includeHeaderInLength) throws IOException, ReceiveException, TimeoutException {
		send(includeHeaderInLength);
		return receive();
	}

	private void send(boolean includeHeaderInLength) throws IOException, ReceiveException {
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
//			}catch (ClosedChannelException e) {
//				init();
//			}
//		}
	}

	private int receive() throws IOException, ReceiveException, TimeoutException {
		try{
			recvBuf.clear();
			int ret = channel.read(recvBuf);
			if(ret == -1){
				throw new TimeoutException("Broken pipe...The socket connection with the LFC server was closed unexpectedly.");
			}
			int timeout = 5;
		    int delay = 100;
			while ((recvBuf.position() < HEADER_SIZE) && timeout-- > 0) {
				try {
					ret = channel.read(recvBuf);
					if(ret == -1){
						throw new TimeoutException("Broken pipe...The socket connection with the LFC server was closed unexpectedly.");
					}
					s_logger.debug("\t HEAD: waiting " + delay + " [ms]... AVAIL=" + recvBuf.position() + ", EXP="+HEADER_SIZE);
					Thread.sleep(delay);
					delay *= 2;
				} catch (InterruptedException exc) {
					s_logger.error(exc);
				}
			}
		    if ( recvBuf.position() < HEADER_SIZE ) {
		      throw new TimeoutException( "Connection timeout during receiving header." );
		    }
//		    s_logger.debug("AVAIL=" + recvBuf.position() + ", EXP="+HEADER_SIZE);
			recvBuf.flip();
			int magic = recvBuf.getInt();
			int type = recvBuf.getInt();
			// For whatever reason, the reply never includes the size of the Header.
			int sizeOrError = recvBuf.getInt();
//			s_logger.debug("Received M/T/S: " + magic + " " + type + " " + sizeOrError);
	
			if (magic == CSEC_TOKEN_MAGIC_1) {
				if ((type != CSEC_TOKEN_TYPE_PROTOCOL_RESP) && (type != CSEC_TOKEN_TYPE_HANDSHAKE) && (type != CSEC_TOKEN_TYPE_HANDSHAKE_FINAL)) {
					throw new ReceiveException(sizeOrError, "Received invalid CSEC Type: " + type);
				}
			} else if (magic == CNS_MAGIC) {
				if ((type == CNS_RESP_IRC) || (type == CNS_RESP_RC)) {
					if (sizeOrError == 0)
						return 0;
					LfcError lfcError = LfcError.fromError(sizeOrError);
					if (lfcError == null){
						String errorMessage = "Unknown LFC error value: " + sizeOrError;
						throw new ReceiveException(sizeOrError, errorMessage);
					}
					throw new ReceiveException(lfcError);
				} else if (type == CNS_RESP_MSG_ERROR) {
					String errorMessage = getString();
					throw new ReceiveException(sizeOrError, errorMessage);
				} else if ((type != CNS_RESP_MSG_DATA) && (type != CNS_RESP_MSG_SUMMARY) && (type != CNS_RESP_MSG_GROUPS)) {
					throw new ReceiveException(sizeOrError, "Received invalid CNS Type: " + type);
				}
			} else {
				throw new ReceiveException(sizeOrError, "Recieved invalid Magic/Type: " + magic + "/" + type);
			}
	
//			s_logger.debug("Limit: " + recvBuf.limit() + ", Pos: " + recvBuf.position() + " MinContent: " + sizeOrError + " Avail " + recvBuf.remaining());
			while (recvBuf.remaining() < sizeOrError ) {
//				s_logger.debug("Reading once more: " + recvBuf.remaining() + " < " + sizeOrError);
				// TODO: There must be an easier method of reading more data.
				byte[] temp = new byte[recvBuf.remaining()];
				recvBuf.get(temp);
				if (recvBuf.capacity() < sizeOrError) {
					recvBuf = ByteBuffer.allocateDirect(sizeOrError);
				} else {
					recvBuf.clear();
				}
				recvBuf.put(temp);
				ret = channel.read(recvBuf);
				if(ret == -1){
					throw new TimeoutException("Broken pipe...The socket connection with the LFC server was closed unexpectedly.");
				}
				recvBuf.flip();
//				if(timeout < 4){
//					try {
//						Thread.sleep(delay);
//						delay *= 2;
//					} catch (InterruptedException exc) {
//						s_logger.error(exc);
//					}
//				}
			}
//			if ( recvBuf.remaining() < sizeOrError ) {
//			      throw new TimeoutException("Connection timeout during receiving data.");
//			}
			return sizeOrError;
		}catch (BufferUnderflowException e) {
			throw new ReceiveException(-9999, e.getMessage());
		}
	}

	private void addIDs() {
		int uid = 0;
		int gid = 0;
		sendBuf.putInt(uid);
		sendBuf.putInt(gid);
	}

	private void authenticate() throws IOException, ReceiveException, TimeoutException {
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
					int l = receive();
					recvToken = new byte[l];
					recvBuf.get(recvToken);
				}
			}			
		} catch (GSSException e) {
//			s_logger.warn(e.toString());
			throw new IOException("Error processing credential");
		}
//		s_logger.debug("Secure Context established!");
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

	private String getString() throws IOException {
		// TODO: This uses Latin-1!
		StringBuilder builder = new StringBuilder();
		byte b = recvBuf.get();
		while (b != 0) {
			builder.append((char) b);
			if (recvBuf.remaining() == 0) {
				recvBuf.clear();
				channel.read(recvBuf);
			}
			b = recvBuf.get();
		}
		return builder.toString();
	}

	
	public void startSession() throws IOException, ReceiveException, TimeoutException{
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_STARTSESS);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(null); //Session comment in the LFC logs.
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("startSession: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
	}
	
	public void closeSession() throws IOException, ReceiveException, TimeoutException{
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_ENDSESS);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("closeSession: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
	}
	
	public LFCFile stat(String path) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_STAT);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				sendBuf.putLong(0L); // 0
				putString(path);
				sendAndReceive(true);
				LFCFile file = new LFCFile(recvBuf, false, false, false, false);
				//FIXME:
				if(recvBuf.hasRemaining()){
					System.err.println("stat: Something remains to be read");
					//throw new IOException("stat: Something remains to be read");
				}
				return file;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}
	
	public LFCFile statg(String path, String guid) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_STATG);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(path);
				putString(guid);
				sendAndReceive(true);
				LFCFile file = new LFCFile(recvBuf, false, true, true, false);
				//FIXME:
				if(recvBuf.hasRemaining()){
					System.err.println("statg: Something remains to be read");
					//throw new IOException("statg: Something remains to be read");
				}
				return file;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}
	
	//sfn = physical path
	public LFCFile statr(String sfn) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_STATR);
				addIDs();
				putString(sfn);
				sendAndReceive(true);
				LFCFile file = new LFCFile(recvBuf, false, true, true, false);
				//FIXME:
				if(recvBuf.hasRemaining()){
					System.err.println("statr: Something remains to be read");
					//throw new IOException("statr: Something remains to be read");
				}
				return file;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}


	public LFCFile lstat(String path) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_LSTAT);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				sendBuf.putLong(0L); // 0
				putString(path);
				sendAndReceive(true);
				LFCFile file = new LFCFile(recvBuf, false, false, false, false);
				//FIXME:
				if(recvBuf.hasRemaining()){
					System.err.println("lstat: Something remains to be read");
					//throw new IOException("lstat: Something remains to be read");
				}
				return file;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}
	
	public String readlink(String link) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_READLINK);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(link);
				sendAndReceive(true);
				String path = getString();
				//FIXME:
				if(recvBuf.hasRemaining()){
					System.err.println("readlink: Something remains to be read");
					//throw new IOException("readlink: Something remains to be read");
				}
				return path;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}
	
	public void link(String path, String target) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_SYMLINK);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(path);
				putString(target);
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("link: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
	}

	public int getGrpByName(String grpName) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				if ("root".equals(grpName)) {
					return 0;
				}
				preparePacket(CNS_MAGIC, CNS_GETGRPID);
				putString(grpName);
				sendAndReceive(true);
				int s = recvBuf.getInt();
				//FIXME
				if(recvBuf.hasRemaining()){
					System.err.println("getGrpByName: Something remains to be read");
//					throw new IOException("getGrpByName: Something remains to be read");
				}
				return s;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	public String getGrpByGid(int gid) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				if (gid == 0) {
					return "root";
				}
				preparePacket(CNS_MAGIC, CNS_GETGRPNAM);
				sendBuf.putShort((short) 0);
				sendBuf.putShort((short) gid);
				sendAndReceive(true);
				String group = getString();
				//FIXME
				if(recvBuf.hasRemaining()){
					System.err.println("getGrpByGid: Something remains to be read");
//					throw new IOException("getGrpByGid: Something remains to be read");
				}
				return group;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	public Collection<String> getGrpByGids(int[] gids) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
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
				sendAndReceive(true);
		
				Collection<String> grpNames = new ArrayList<String>(gids.length);
				for (int i = 0; i < gids.length; i++) {
					if (rootGIDIndexes.contains(i)) {
						grpNames.add("root");
					} else {
						grpNames.add(getString());
					}
				}
				//FIXME
				if(recvBuf.hasRemaining()){
					System.err.println("getGrpByGids: Something remains to be read");
//					throw new IOException("getGrpByGids: Something remains to be read");
				}
				return grpNames;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	public int getUsrByName(String usrName) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				if ("root".equals(usrName)) {
					return 0;
				}
				preparePacket(CNS_MAGIC, CNS_GETUSRID);
				putString(usrName);
				long l = sendAndReceive(true);
				int s = recvBuf.getInt();
				if(l != 4){
					throw new IOException("getUsrByName: Something remains to be read ("+l+" bits)");
				}
				return s;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	public String getUsrByUid(int uid) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				if (uid == 0) {
					return "root";
				}
				preparePacket(CNS_MAGIC, CNS_GETUSRNAM);
				sendBuf.putShort((short) 0);
				sendBuf.putShort((short) uid);
				sendAndReceive(true);
				String user = getString();
				//FIXME
				if(recvBuf.hasRemaining()){
					System.err.println("getUsrByUid: Something remains to be read");
//					throw new IOException("getUsrByUid: Something remains to be read");
				}
				return user;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
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
	public void access(String path, AccessType accessType) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_ACCESS);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(path);
				sendBuf.putInt(accessType.getValue());
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("access: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
	}

	public long opendir(String path, String guid) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
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
				long l = sendAndReceive(true);
				long fileId = recvBuf.getLong();
				if(l != 8){
					throw new IOException("opendir: Something remains to be read ("+l+" bits)");
				}
				return fileId;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				//CNS_OPENDIR, CNS_READDIR and CNS_CLOSEDIR must be done with the same connection...
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	public void closedir() throws IOException, ReceiveException, TimeoutException {
//		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
//			try{
				preparePacket(CNS_MAGIC2, CNS_CLOSEDIR);
				long l = sendAndReceive(false);
				//JEROME
				if(l != 0){
					//FIXME:
					if(recvBuf.hasRemaining()){
						System.err.println("closedir: Something remains to be read");
						//throw new IOException("closedir: Something remains to be read");
					}
				}
//				break;
//			}catch (TimeoutException e) {
//				if(z == MAX_RETRY_IF_TIMEOUT ){
//					throw e;
//				}
//				//CNS_OPENDIR, CNS_READDIR and CNS_CLOSEDIR must be done with the same connection...
//				//init();
//			}
//		}
	}
	
	/**
	 * Read a directory entry
	 * 
	 * @param fileID
	 *            The id of the directory
	 * @return A collection of {@link LFCFile} which are inside the directory
	 * @throws IOException
	 *             if something wrong occurs
	 * @throws TimeoutException 
	 */
	public Collection<LFCFile> readdir(long fileID) throws IOException, ReceiveException, TimeoutException {
//		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
//			try{
				preparePacket(CNS_MAGIC, CNS_READDIR);
				addIDs();
				sendBuf.putShort((short) 1); // 1 = full list (w/o comments), 0 = names only
				sendBuf.putShort((short) 0);
				sendBuf.putLong(fileID);
				sendBuf.putShort((short) 1);
				int l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("readdir: Something remains to be read ("+l+" bits)");
				}
				
				// FIXME: Jerome: I don't know why I have to do that but at least it works....
				preparePacket(CNS_MAGIC, CNS_READDIR);
				l = sendAndReceive(true);
				
				
				short count = recvBuf.getShort();
				Collection<LFCFile> lfcFiles = new ArrayList<LFCFile>(count);
				for (short i = 0; i < count; i++) {
					LFCFile file = new LFCFile(recvBuf, true, false, false, false);
					lfcFiles.add(file);
				}
//				short eod = recvBuf.getShort();
//				int size = receive();
//				//FIXME:
//				while(recvBuf.hasRemaining()){
//					System.out.println("FIXME: readdir: Something remains to be read:"+getString());
//					getString();
//					//throw new IOException("readdir: Something remains to be read");
//				}
				return lfcFiles;
//			}catch (TimeoutException e) {
//				if(z == MAX_RETRY_IF_TIMEOUT ){
//					throw e;
//				}
//				//CNS_OPENDIR, CNS_READDIR and CNS_CLOSEDIR must be done with the same connection...
//				//init();
//			}
//		}
//		throw new RuntimeException("Must not be here. BUG");
	}

	/**
	 * Use UNLINK command instead of DELETE to remove files. DELETE command is
	 * for CASTOR entries only
	 * @throws TimeoutException 
	 */
	public int delete(String path) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_DELETE);
				addIDs();
				long cwd = 0L;
				sendBuf.putLong(cwd);
				putString(path);
				long l = sendAndReceive(true);
				int execResult = recvBuf.getInt();
				if(l != 4){
					throw new IOException("delete: Something remains to be read ("+l+" bits)");
				}
				return execResult;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
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
	public void unlink(String path) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_UNLINK);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("unlink: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
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
	public void setfsize(String path, long fileSize) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
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
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("setfsize: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
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
	public void rename(String oldPath, String newPath) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_RENAME);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(oldPath);
				putString(newPath);
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("rename: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
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
	public void chmod(String path, int mode) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_CHMOD);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				mode &= 07777;
				sendBuf.putInt(mode);
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("chmod: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
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
	public void chown(String path, int new_uid, int new_gid) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_CHOWN);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				sendBuf.putInt(new_uid);
				sendBuf.putInt(new_gid);
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("chown: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
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
	public void lchown(String path, int new_uid, int new_gid) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC, CNS_LCHOWN);
				addIDs();
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				sendBuf.putInt(new_uid);
				sendBuf.putInt(new_gid);
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("lchown: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
	}

	/**
	 * List all the file replicas
	 * @throws TimeoutException 
	 */
	public Collection<LFCReplica> listReplica(String path, String guid) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC2, CNS_LISTREPLICA);
				addIDs();
				sendBuf.putShort((short) 0); // Size of nbentry
				long cwd = 0L; // Current Working Directory
				sendBuf.putLong(cwd);
				putString(path);
				putString(guid);
				sendBuf.putShort((short) 1); // BOL = Beginning of List
				sendAndReceive(true);
		
				short count = recvBuf.getShort();
		
				Collection<LFCReplica> srms = new ArrayList<LFCReplica>(count);
				for (short i = 0; i < count; i++) {
					LFCReplica replica = new LFCReplica(recvBuf);
					srms.add(replica);
				}
				//FIXME: 
				if(recvBuf.hasRemaining()){
					System.err.println("listReplica: Something remains to be read");
				}
				return srms;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	/**
	 * Use UNLINK command instead of DELFILES to remove files.
	 * @throws TimeoutException 
	 */
	public boolean delFiles(String[] guids, boolean force) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
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
				long l = sendAndReceive(true);
				int nbstatuses = recvBuf.getInt();
				if(l != 4){
					throw new IOException("delFiles: Something remains to be read ("+l+" bits)");
				}
				return nbstatuses == 0;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	public void rmdir(String path) throws IOException, ReceiveException , TimeoutException{
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				long cwd = 0L;
				preparePacket(CNS_MAGIC, CNS_RMDIR);
				addIDs();
				sendBuf.putLong(cwd);
				putString(path);
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("rmdir: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
	}

	public long creat(String path, String guid) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
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
				long l = sendAndReceive(true);
				long file_id = recvBuf.getLong();
				if(l != 8){
					throw new IOException("creat: Something remains to be read ("+l+" bits)");
				}
				return file_id;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
		throw new RuntimeException("Must not be here. BUG");
	}

	public void mkdir(String path, String guid) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
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
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("mkdir: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
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
	public void delReplica(String guid, String replicaUri) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				long id = 0L;
				preparePacket(CNS_MAGIC, CNS_DELREPLICA);
				addIDs();
				sendBuf.putLong(id);
				putString(guid);
				putString(replicaUri);
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("delReplica: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
	}

	public void addReplica(String guid, URI replicaUri) throws IOException, ReceiveException, TimeoutException {
		for (int z = 0; z <= MAX_RETRY_IF_TIMEOUT; z++) {
			try{
				preparePacket(CNS_MAGIC4, CNS_ADDREPLICA);
				this.addIDs();
				sendBuf.putLong(0L); // uniqueId
				this.putString(guid);
				this.putString(replicaUri.getHost());
				this.putString(replicaUri.toString());
				sendBuf.put((byte) '-'); // status;
				sendBuf.put((byte) 'P'); // file type
				this.putString(null); // pool name
				this.putString(null); // fs
				sendBuf.put((byte) 'P'); // r_type
				this.putString(null); // setname
				long l = sendAndReceive(true);
				if(l != 0){
					throw new IOException("addReplica: Something remains to be read ("+l+" bits)");
				}
				break;
			}catch (TimeoutException e) {
				if(z == MAX_RETRY_IF_TIMEOUT ){
					throw e;
				}
				init();
			}
		}
	}

	/**
	 * Representation of a File or a Directory in the file catalog
	 * 
	 * @author Jerome Revillard
	 * 
	 */
	 public class LFCFile {
		private String guid; // global unique id
        private String fileName; // name of the file
		private String comment; // user comment of the file
		private String chksumType; // checksum type
		private String chksumValue; // checksum value

		private long aDate; // last access time
		private long mDate; // last modification
		private long cDate; // last meta-data modification

		private long fileId; // unique id

		private int nLink; // number of children
		private int uid; // user id
		private int gid; // group id
        private long fileSize;  // size of the file

		private short fileMode; // see description on the end of the file
		private short fileClass; // 1 = experiment, 2 = user (don't know what is
									// this for)
		private byte status; // '-' = online, 'm' = migrated (don't know what is
								// this for)

		public LFCFile(ByteBuffer byteBuffer, final boolean readName, final boolean readGuid, final boolean readCheckSum, final boolean readComment) throws IOException, ReceiveException, TimeoutException {
			this.fileId = byteBuffer.getLong();
			if (readGuid) {
				this.guid = getString();
			}
			this.fileMode = byteBuffer.getShort();
			this.nLink = byteBuffer.getInt();
			
			this.uid = byteBuffer.getInt();
			this.gid = byteBuffer.getInt();
			this.fileSize = byteBuffer.getLong();

			this.aDate = byteBuffer.getLong() * 1000;
			this.mDate = byteBuffer.getLong() * 1000;
			this.cDate = byteBuffer.getLong() * 1000;

			this.fileClass = (byteBuffer.getShort());
			this.status = (byteBuffer.get());
			if (readCheckSum) {
				this.chksumType = getString();
				this.chksumValue = getString();
			}
			if (readName) {
				this.fileName = getString();
			}
			if (readComment) {
				this.comment = getString();
			}
		}


		public String getGuid() {
			return guid;
		}

        public String getFileName() {
            return fileName;
        }

		public String getComment() {
			return comment;
		}

		public String getChksumType() {
			return chksumType;
		}

		public String getChksumValue() {
			return chksumValue;
		}

		public long getFileId() {
			return fileId;
		}

        public long getFileSize() {
            return fileSize;
        }

        public short getFileMode() {
            return fileMode;
        }

		public short getFileClass() {
			return fileClass;
		}

		public byte getStatus() {
			return status;
		}

		/**
		 * @return The string representation of the LFCFile object
		 */
		@Override
		public String toString() {

			String tostring = "";
			if (fileName != null) {
				tostring += "Name: " + fileName;
			}
			if (guid != null)
				tostring += (tostring.equals("") ? "" : " - ") + "guid: " + guid;
			if (comment != null)
				tostring += (tostring.equals("") ? "" : " - ") + "comment: " + comment;
			if (chksumType != null) {
				tostring += (tostring.equals("") ? "" : " - ") + "chksumType: " + chksumType;
				tostring += " - chksumValue: " + chksumValue;
			}

			tostring += (tostring.equals("") ? "" : " - ") + "aDate: " + aDate;
			tostring += " - mDate: " + mDate;
			tostring += " - cDate: " + cDate;

			tostring += " - fileId: " + fileId;
			tostring += " - fileSize: " + fileSize;
			tostring += " - nLink: " + nLink;
			tostring += " - uid: " + uid;
			tostring += " - gid: " + gid;

			tostring += " - fileMode: " + fileMode;
			tostring += " - fileClass: " + fileClass;
			tostring += " - status: " + status;

			return tostring;
		}

		public LFCGroup group() throws IOException, ReceiveException, TimeoutException {
			LfcConnection lfcConnection = new LfcConnection(host, port, gssCredential);
			String groupName = null;
			try {
				groupName = lfcConnection.getGrpByGid(gid);
			} finally {
				lfcConnection.close();
			}
			return new LFCGroup(gid,groupName);
		}
		
		public LFCUser owner() throws IOException, ReceiveException, TimeoutException {
			LfcConnection lfcConnection = new LfcConnection(host, port, gssCredential);
			String userName = null;
			try {
				userName = lfcConnection.getUsrByUid(uid);
			} finally {
				lfcConnection.close();
			}
			return new LFCUser(uid,userName);
		}

		public long creationTime() {
			return mDate;
		}

		public Object fileKey() {
			return null;
		}

		public boolean isOther() {
			return (!isRegularFile() && !isDirectory() && !isSymbolicLink());
		}

		public boolean isRegularFile() {
			return ((this.fileMode & S_IFMT) == S_IFREG);
		}

		public boolean isDirectory() {
			return ((this.fileMode & S_IFMT) == S_IFDIR);
		}

		public boolean isSymbolicLink() {
			return ((this.fileMode & S_IFMT) == S_IFLNK);
		}

		public long lastAccessTime() {
			return aDate;
		}

		public long lastModifiedTime() {
			return cDate;
		}

		public int linkCount() {
			return nLink;
		}

		public TimeUnit resolution() {
			return TimeUnit.MILLISECONDS;
		}

	}

	/**
	 * Describes replica entries.
	 * 
	 * @author jerome / inspired by the gEclipse project
	 * 
	 */
	public class LFCReplica {

		private String poolName; // name of the pool
		private String guid; // global unique id
		private String host; // storage element host
		private String fs; // filesystem type
		private String sfn; // sfn value

		private long aDate; // last access time
		private long pDate; // pin time

		private long fileId; // unique id
		private long nbaccesses; // number of accesses???

		private byte status; // '-' = online, 'm' = migrated (don't know what is
								// this for)
		private byte f_type; // 

		public LFCReplica(ByteBuffer byteBuffer) throws IOException {
			this.fileId = byteBuffer.getLong();
			this.nbaccesses = byteBuffer.getLong();

			this.aDate = byteBuffer.getLong() * 1000;
			this.pDate = byteBuffer.getLong() * 1000;
			this.status = byteBuffer.get();
			this.f_type = byteBuffer.get();

			this.poolName = getString();
			this.host = getString();
			this.fs = getString();
			this.sfn = getString();
		}

		/**
		 * @return global unique ID of the file/directory
		 */
		public String getGuid() {
			return this.guid;
		}

		/**
		 * @return fileId of the file/directory
		 */
		public long getFileId() {
			return this.fileId;
		}

		/**
		 * @return last access time
		 */
		public long getADate() {
			return this.aDate;
		}

		/**
		 * @return replica pin time
		 */
		public long getPDate() {
			return this.pDate;
		}

		/**
		 * @return '-' = online, 'm' = migrated (don't know what is this for)
		 */
		public byte getStatus() {
			return this.status;
		}

		/**
		 * @return the poolName
		 */
		public String getPoolName() {
			return this.poolName;
		}

		/**
		 * @return the host
		 */
		public String getHost() {
			return this.host;
		}

		/**
		 * @return the fs
		 */
		public String getFs() {
			return this.fs;
		}

		/**
		 * @return the sfn
		 */
		public String getSfn() {
			return this.sfn;
		}

		/**
		 * @return the nbaccesses
		 */
		public long getNbaccesses() {
			return this.nbaccesses;
		}

		/**
		 * @return the f_type
		 */
		public byte getF_type() {
			return this.f_type;
		}

		/**
		 * @return The string representation of the LFCReplica object
		 */
		@Override
		public String toString() {
			String replica = "";
			replica += "fileId: " + this.fileId;
			replica += " - nbaccesses: " + this.nbaccesses;

			replica += " - aDate: " + this.aDate;
			replica += " - pDate: " + this.pDate;
			replica += " - status: " + this.status;
			replica += " - f_type: " + this.f_type;

			replica += " - poolName: " + this.poolName;
			replica += " - host: " + this.host;
			replica += " - fs: " + this.fs;
			replica += " - sfn: " + this.sfn;
			return replica;
		}
	}

	/**
	 * Exception that can be thrown by the {@link LfcConnection#receive()}
	 * function
	 */
	public final class ReceiveException extends Exception {
		private static final long serialVersionUID = 1L;
		private int error = -1;
		private LfcError lfcError = LfcError.UNKOWN_ERROR;
		private String executedCmd = null;

		public ReceiveException(int error, String s) {
			super(s);
			this.error = error;
			LfcError m_lfcError = LfcError.UNKOWN_ERROR;
			m_lfcError.message = s;
			m_lfcError.error = error;
			this.lfcError = m_lfcError;
		}
		
		public ReceiveException(LfcError lfcError) {
			super(lfcError.getMessage());
			this.error = lfcError.getError();
			this.lfcError = lfcError;
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
			return executedCmd+": "+lfcError.toString();
		}
		
		/**
		 * 
		 * @return The LFC error code
		 */
		public int getLFCErrorNumber() {
			return error;
		}

		/**
		 * @return The {@link LfcError} object representing the error or <code>null</code> if this is an unknown error.
		 */
		public LfcError getLFCError() {
			return lfcError;
		}
	}

	/**
	 * Try to close the connection to free resources.
	 */
	public void close() {
		try {
			this.channel.close();
		} catch (IOException e) {
			s_logger.warn(e.toString());
		}
	}

//	/** {@inheritDoc} */
//	@Override
//	protected void finalize() throws Throwable {
//		try {
//			this.channel.close();
//		} catch (IOException e) {
//			// ignore
//		}
//		super.finalize();
//	}
}
