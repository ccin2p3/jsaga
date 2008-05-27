package fr.in2p3.jsaga.adaptor.u6.job;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOSetter;
import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.PermissionDenied;
import org.ogf.saga.error.Timeout;

import com.intel.gpe.client2.common.i18n.Messages;
import com.intel.gpe.client2.common.i18n.MessagesKeys;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileExportImpl;
import com.intel.gpe.client2.providers.FileProvider;
import com.intel.gpe.client2.transfers.FileExport;
import com.intel.gpe.client2.transfers.TransferFailedException;
import com.intel.gpe.clients.api.JobClient;
import com.intel.gpe.clients.api.StorageClient;
import com.intel.gpe.clients.api.exceptions.GPEFileTransferProtocolNotSupportedException;
import com.intel.gpe.clients.api.exceptions.GPEMiddlewareRemoteException;
import com.intel.gpe.clients.api.exceptions.GPEMiddlewareServiceException;
import com.intel.gpe.clients.api.exceptions.GPEResourceUnknownException;
import com.intel.gpe.gridbeans.GPEFile;
import com.intel.gpe.util.sets.Pair;

import java.io.OutputStream;
import java.util.List;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   U6JobIOHandler
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   29 avril 2008
* ***************************************************/
public class U6JobIOHandler implements JobIOSetter {

	private TargetSystemInfo targetSystemInfo;
	private JobClient jobClient;
	private String jobId;

	public U6JobIOHandler(TargetSystemInfo targetSystemInfo,
			JobClient jobClient, String jobId) {
		this.targetSystemInfo = targetSystemInfo;
		this.jobClient = jobClient;
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setStderr(OutputStream err) throws PermissionDenied, Timeout,
			NoSuccess {
		getStream(err, "stderr");
	}

	public void setStdout(OutputStream out) throws PermissionDenied, Timeout,
			NoSuccess {
		getStream(out, "stdout");
	}
	
	public void getStream(OutputStream outputStream, String type) throws PermissionDenied, Timeout, NoSuccess {
		
		try {	
			// create tmp File
			FileProvider fileProvider = new FileProvider();
	        fileProvider.addFileGetter("RBYTEIO", RandomByteIOFileExportImpl.class);
    		StorageClient workingDirectory = jobClient.getWorkingDirectory();
    		Pair<GPEFile, String> outputFile = new Pair<GPEFile, String>(null, type);
            
            List<FileExport> getters = fileProvider.prepareGetters(workingDirectory);
            int i;
            for (i = 0; i < getters.size(); i++) {
                try {
                    getters.get(i).getFile(targetSystemInfo.getSecurityManager(), outputFile.getM2(), outputStream, null);
                }
                catch (GPEFileTransferProtocolNotSupportedException e) {
                    continue;
                }
                catch (TransferFailedException e) {
                	throw e;
                }
                break;
            }
            if (i == getters.size()) {
            	throw new Exception(Messages.getString(MessagesKeys.common_requests_GetFilesRequest_Cannot_fetch_file_from_remote_location__no_suitable_protocol_found));
            }
		} catch (GPEResourceUnknownException e) {
			throw new NoSuccess(e);
		} catch (GPEMiddlewareRemoteException e) {
			throw new NoSuccess(e);
		} catch (GPEMiddlewareServiceException e) {
			throw new NoSuccess(e);
		} catch (InstantiationException e) {
			throw new NoSuccess(e);
		} catch (IllegalAccessException e) {
			throw new NoSuccess(e);
		} catch (ClassNotFoundException e) {
			throw new NoSuccess(e);
		} catch (Exception e) {
			throw new NoSuccess(e);
		}
	}
}