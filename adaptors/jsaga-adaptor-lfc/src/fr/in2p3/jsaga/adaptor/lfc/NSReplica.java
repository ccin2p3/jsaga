package fr.in2p3.jsaga.adaptor.lfc;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Describes replica entries.
 * 
 * @author jerome / inspired by the gEclipse project
 * 
 */
public class NSReplica extends NSObject {

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

	public NSReplica(ByteBuffer byteBuffer) throws IOException {
		super(byteBuffer);
		fillObject();
	}
	
	/* (non-Javadoc)
	 * @see org.pandora.gateway.dpm.datamanagementdaemon.ns.NSConnection.NSObject#fillObject()
	 */
	@Override
	protected void fillObject() throws IOException {
		this.fileId = byteBuffer.getLong();
		this.nbaccesses = byteBuffer.getLong();

		this.aDate = byteBuffer.getLong() * 1000;
		this.pDate = byteBuffer.getLong() * 1000;
		this.status = byteBuffer.get();
		this.f_type = byteBuffer.get();

		this.poolName = NSConnection.getString(byteBuffer);
		this.host = NSConnection.getString(byteBuffer);
		this.fs = NSConnection.getString(byteBuffer);
		this.sfn = NSConnection.getString(byteBuffer);
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