package fr.in2p3.jsaga.adaptor.ssh.job;

import java.io.FileOutputStream;
import java.util.Map;

import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;
import fr.in2p3.jsaga.adaptor.ssh.SSHAdaptorAbstract;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.Timeout;

import com.jcraft.jsch.ChannelExec;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHJobMonitorAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   11 avril 2008
* ***************************************************/

public class SSHJobMonitorAdaptor extends SSHAdaptorAbstract implements QueryIndividualJob {

    public String getType() {
        return "ssh";
    }
    
    // Already connected
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
    }
    
    public JobStatus getStatus(String nativeJobId) throws Timeout, NoSuccess {

    	try {    		
    		ChannelExec channel = (ChannelExec) SSHAdaptorAbstract.sessionMap.get(nativeJobId+"-id");

    		// TODO move to cleanup
    		if(channel.isClosed()) {
    			channel.disconnect();
    			FileOutputStream stdout = (FileOutputStream) SSHAdaptorAbstract.sessionMap.get(nativeJobId+"-stdout");
    			stdout.close();
    			FileOutputStream stderr = (FileOutputStream) SSHAdaptorAbstract.sessionMap.get(nativeJobId+"-stderr");
    			stderr.close();
    		}

    		return new SSHJobStatus(nativeJobId, channel.getExitStatus());
						
    	} catch (Exception e) {
    		throw new NoSuccess(e);
		}
    }        

}
