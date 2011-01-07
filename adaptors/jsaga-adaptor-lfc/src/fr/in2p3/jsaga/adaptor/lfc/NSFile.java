package fr.in2p3.jsaga.adaptor.lfc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import fr.in2p3.jsaga.adaptor.lfc.NSConnection.LFCBrokenPipeException;
import fr.in2p3.jsaga.adaptor.lfc.NSConnection.ReceiveException;

/**
 * Representation of a File or a Directory in the file catalog
 * 
 * @author Jerome Revillard
 * 
 */
 public class NSFile extends NSObject{
	 private final boolean readName;
	 private final boolean readGuid;
	 private final boolean readCheckSum;
	 private final boolean readComment;
	 
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

	public NSFile(ByteBuffer byteBuffer, final boolean readName, final boolean readGuid, final boolean readCheckSum, final boolean readComment) throws IOException{
		super(byteBuffer);
		this.readName = readName;
		this.readGuid = readGuid;
		this.readCheckSum = readCheckSum;
		this.readComment = readComment;
		fillObject();
	}
	
	/* (non-Javadoc)
	 * @see org.pandora.gateway.dpm.datamanagementdaemon.ns.NSConnection.NSObject#fillObject(java.nio.ByteBuffer)
	 */
	@Override
	protected void fillObject() throws IOException {
		this.fileId = byteBuffer.getLong();
		if (readGuid) {
			this.guid = NSConnection.getString(byteBuffer);
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
			this.chksumType = NSConnection.getString(byteBuffer);
			this.chksumValue = NSConnection.getString(byteBuffer);
		}
		if (readName) {
			this.fileName = NSConnection.getString(byteBuffer);
		}
		if (readComment) {
			this.comment = NSConnection.getString(byteBuffer);
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
	
	public NSGroup group(NSConnection connection) throws IOException, ReceiveException, LFCBrokenPipeException {
		return new NSGroup(gid,connection.getGrpByGid(gid));
	}
	
	public NSUser owner(NSConnection connection) throws IOException, ReceiveException, LFCBrokenPipeException {
		return new NSUser(uid,connection.getUsrByUid(uid));
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
		return ((this.fileMode & NSConnection.S_IFMT) == NSConnection.S_IFREG);
	}

	public boolean isDirectory() {
		return ((this.fileMode & NSConnection.S_IFMT) == NSConnection.S_IFDIR);
	}

	public boolean isSymbolicLink() {
		return ((this.fileMode & NSConnection.S_IFMT) == NSConnection.S_IFLNK);
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

	/**
	 * @return The string representation of the NSFile object
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
 }
