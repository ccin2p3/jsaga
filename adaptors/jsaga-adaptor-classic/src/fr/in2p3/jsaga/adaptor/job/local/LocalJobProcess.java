package fr.in2p3.jsaga.adaptor.job.local;

import java.io.*;
import java.util.Date;
import org.ogf.saga.error.NoSuccessException;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LocalJobProcess
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   15 avril 2011
* ***************************************************/

public class LocalJobProcess implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 442420832799282097L;
	private String m_pid;
	private String m_jobId;
	private String m_outfile;
	private String m_infile;
	private String m_errfile;
	private int m_returnCode;
	private Date m_created;
	
	private static final String _rootDir = "/tmp/jsaga/adaptor/local";
	
	public static final int PROCESS_DONE_OK = 0;
	public static final int PROCESS_RUNNING = -1;
	public static final int PROCESS_STOPPED = -2;

	public LocalJobProcess(String jobId) {
		m_jobId = jobId;
		m_outfile = getFile("out");
		m_infile = getFile("in");
		m_errfile = getFile("err");
		m_returnCode = -1;
		m_pid = null;
	}
	
    public static String getRootDir() {
    	return _rootDir;
    }
    public String getFile(String suffix) {
    	return getRootDir() + "/" + m_jobId + "." + suffix;
    }
    public String getJobId() {
    	return m_jobId;
    }

    public String getPid() throws NoSuccessException {
		if (m_pid != null) return m_pid;
    	File f = new File(getPidfile());
    	FileInputStream fis;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			throw new NoSuccessException(e);
		}
    	byte[] buf = new byte[(int)f.length()];
    	try {
			int len = fis.read(buf);
        	fis.close();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
		// Get PID reading xxx.pid file minus last character (carriage return)
		m_pid = new String(buf).substring(0, buf.length-1);
		return m_pid;
	}

	public String getInfile() {
		return m_infile;
	}

	public String getOutfile() {
		return m_outfile;
	}

	public String getErrfile() {
		return m_errfile;
	}
	public void setReturnCode(int returnCode) {
		this.m_returnCode = returnCode;
	}

	public int getReturnCode() throws NoSuccessException {
		if (m_returnCode >= 0) return m_returnCode; // final state
    	File f = new File(getEndcodefile());
    	FileInputStream fis;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			// PROCESS Is not finished
			return m_returnCode;
		}
    	byte[] buf = new byte[(int)f.length()];
    	try {
			int len = fis.read(buf);
        	fis.close();
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
		// Get return code reading xxx.endcode file minus last character (carriage return)
		m_returnCode = Integer.valueOf(new String(buf).substring(0, buf.length-1));
		return m_returnCode;
	}
	
	public JobStatus getJobStatus() throws NoSuccessException {
		int status = getReturnCode();
		if (status <0) { // either running or suspended
			status = getProcessStatus();
		}
		return new LocalJobStatus(m_jobId, status);						
	}
	
	
    public void setCreated(Date created) {
		this.m_created = created;
	}

	public Date getCreated() {
		return m_created;
	}

	public Date getStarted() throws NoSuccessException {
		long startTime = new File(this.getPidfile()).lastModified();
		if (startTime == 0) {
			throw new NoSuccessException("Could not get time of " + this.getPidfile());
		}
		return new Date(startTime);
	}

	public Date getFinished() throws NoSuccessException {
		long endTime = new File(this.getEndcodefile()).lastModified();
		if (endTime == 0) {
			throw new NoSuccessException("Could not get time of " + this.getEndcodefile());
		}
		return new Date(endTime);
	}
	
	public String getPidfile() {
    	return getFile("pid");
    }
    
    public String getEndcodefile() {
    	return getFile("endcode");
    }
    public String getSerializefile() {
    	return getFile("process");
    }
    
    public void clean() {
    	// TODO: uncomment when engine will cache this info before calling cleanUp()
		//new File(getPidfile()).delete();
		//new File(getInfile()).delete();
		//new File(getOutfile()).delete();
		//new File(getErrfile()).delete();
		//new File(getEndcodefile()).delete();
		//new File(getSerializefile()).delete();
    }

	public int getProcessStatus() throws NoSuccessException {
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/proc/"+getPid()+"/stat"))));
			String status = br.readLine().split(" ")[2];
			if (status.startsWith("S") || status.startsWith("R")) { return PROCESS_RUNNING;}
			if (status.startsWith("T")) { return PROCESS_STOPPED;}
			throw new NoSuccessException("Unknown status: "+status);
		} catch (FileNotFoundException e) {
			throw new NoSuccessException(e);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
	}
	

}
