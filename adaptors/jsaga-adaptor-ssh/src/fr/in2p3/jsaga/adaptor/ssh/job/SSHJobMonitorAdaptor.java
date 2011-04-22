package fr.in2p3.jsaga.adaptor.ssh.job;

import java.io.InputStream;
import java.util.Map;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import fr.in2p3.jsaga.adaptor.ssh.SSHAdaptorAbstract;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHJobMonitorAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   11 avril 2008
* ***************************************************/
// TODO: implement ListableJobAdaptor

public class SSHJobMonitorAdaptor extends SSHAdaptorAbstract implements QueryIndividualJob {

	private ChannelSftp channelGet = null;
	
    public String getType() {
        return "ssh";
    }
    
    // Already connected
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
		try {
			channelGet = (ChannelSftp) session.openChannel("sftp");
			channelGet.connect();
		} catch (JSchException e) {
			throw new NoSuccessException(e);
		}
    }
    
    public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {

    	try {    	
			InputStream is = channelGet.get(SSHJobProcess.getRootDir() + "/" + nativeJobId + ".endcode");
	    	byte[] buf = new byte[4];
	    	int len = is.read(buf);
	    	is.close();
    		return new SSHJobStatus(nativeJobId, new Integer(new String(buf).substring(0, len-1)).intValue());
					
    	} catch (SftpException sftpe) {
			// if endcode does not exist, SSHJobStatus(nativeJobId, true, null);
    		return new SSHJobStatus(nativeJobId, SSHJobProcess.PROCESS_RUNNING);
    	} catch (Exception e) {
    		throw new NoSuccessException(e);
		}
    }        

    public void disconnect() throws NoSuccessException {
		channelGet.disconnect();
		super.disconnect();
    }
}
