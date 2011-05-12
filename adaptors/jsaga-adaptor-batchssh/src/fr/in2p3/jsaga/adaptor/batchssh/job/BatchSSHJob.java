package fr.in2p3.jsaga.adaptor.batchssh.job;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.ogf.saga.error.NoSuccessException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BatchSSHJob
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   10 mai 2011
* ***************************************************
* Description:                                      */

public class BatchSSHJob {

	private String m_id;
	private HashMap<String, String> m_attributes;
	public static String ATTR_EXIT_STATUS = "EXIT_STATUS";
	public static String ATTR_JOB_STATE = "JOB_STATE";
	public static String ATTR_CREATE_TIME = "CTIME";
	public static String ATTR_START_TIME = "START_TIME";
	public static String ATTR_END_TIME = "MTIME";
	public static String ATTR_EXEC_HOST = "EXEC_HOST";
	
	public BatchSSHJob(String nativeJobId) {
		this.m_id = nativeJobId;
		this.m_attributes = new HashMap<String, String>();
	}
	
	public String getId() {
		return this.m_id;
	}
	
	public void setAttribute(String key, String value) {
		this.m_attributes.put(key, value);
	}
	
	public String getAttribute(String key) throws NoSuccessException {
		if (!this.m_attributes.containsKey(key))
			throw new NoSuccessException("Could not get " + key + " attribute");
		return this.m_attributes.get(key);
	}
	
	public Date getDateAttribute(String key) throws NoSuccessException {
		DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);
		try {
			return df.parse(this.m_attributes.get(key));
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}		
	}
	
	public int getExitCode() throws NoSuccessException {
		return new Integer(this.m_attributes.get(ATTR_EXIT_STATUS));
	}
	
	public BatchSSHJobStatus getJobStatus() throws NoSuccessException {
		if (this.m_attributes.containsKey(ATTR_EXIT_STATUS)) {
			return new BatchSSHJobStatus(m_id, this.m_attributes.get(ATTR_JOB_STATE), this.getExitCode());
		} else {
			return new BatchSSHJobStatus(m_id, this.m_attributes.get(ATTR_JOB_STATE));
		}
	}
}
