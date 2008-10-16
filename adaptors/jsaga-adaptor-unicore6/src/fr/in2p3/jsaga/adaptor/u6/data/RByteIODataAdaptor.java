package fr.in2p3.jsaga.adaptor.u6.data;

import com.intel.gpe.client2.common.i18n.Messages;
import com.intel.gpe.client2.common.i18n.MessagesKeys;
import com.intel.gpe.client2.common.requests.CreateDirectoryRequest;
import com.intel.gpe.client2.common.requests.DeleteFileRequest;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileExportImpl;
import com.intel.gpe.client2.common.transfers.byteio.RandomByteIOFileImportImpl;
import com.intel.gpe.client2.providers.FileProvider;
import com.intel.gpe.client2.transfers.*;
import com.intel.gpe.clients.api.StorageClient;
import com.intel.gpe.clients.api.exceptions.*;
import com.intel.gpe.gridbeans.GPEFile;
import com.intel.gpe.util.sets.Pair;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.data.BaseURL;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;
import fr.in2p3.jsaga.adaptor.u6.U6Abstract;
import org.apache.log4j.Logger;
import org.ogf.saga.error.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Exception;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   RByteIODataAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   5 mars 2008
* ***************************************************/
/**
 *
 */
public class RByteIODataAdaptor extends U6Abstract implements FileWriterPutter, FileReaderGetter {
	protected static final String SERVICE_NAME = "ServiceName";
    private static Logger s_logger = Logger.getLogger(RByteIODataAdaptor.class);
    private TargetSystemInfo targetSystemInfo ;
	private String m_serverFileSeparator ;
	private StorageClient m_client;
    private String rootDirectory = ".";
    private boolean m_isRetrying = false;
    
    public String getType() {
        return "rbyteio";
    }

    public BaseURL getBaseURL() throws IncorrectURL {
        return new BaseURL(8080);
    }

    public Usage getUsage() {
    	return new UAnd(new Usage[]{new U(SERVICE_NAME), 
    								new U(APPLICATION_NAME)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
    	return new Default[]{
    			new Default(SERVICE_NAME, "Registry"), 
    			new Default(APPLICATION_NAME, "Bash shell")};
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
    	super.connect(userInfo, host, port, basePath, attributes);
    	
    	// get SERVICE_NAME
    	if(attributes.containsKey(SERVICE_NAME))
    		m_serviceName = (String) attributes.get(SERVICE_NAME);
    	    	
    	// get registry
    	if(basePath.indexOf(m_serviceName) == -1 ) {
    		throw new BadParameter("Invalid base path:"+basePath);
    	}
    	String registryPath = basePath.substring(0,basePath.indexOf(m_serviceName)+m_serviceName.length());
    	m_serverUrl = "https://"+host+":"+port+registryPath;
    	
    	// get storage name
    	if(basePath.indexOf(m_serviceName)+m_serviceName.length() >= basePath.length()) {
    		throw new BadParameter("Invalid storage name: the name cannot be empty '"+basePath+"'");
    	}
    	String storageName =  basePath.substring(basePath.indexOf(m_serviceName)+m_serviceName.length()+1,basePath.length());
    	if(storageName.indexOf("/") < 1) {
    		throw new BadParameter("invalid storage name, must contain '/': '"+storageName+"'.");
		}
    	storageName = storageName.substring(0,storageName.indexOf("/"));
    	
		// get client that talks to registry
        try {
        	  // find target system that supports the specific application
        	targetSystemInfo = findTargetSystem();
        	m_client = targetSystemInfo.getTargetSystem().getStorage(storageName);
        	if(m_client == null)
        		throw new NoSuccess("Unable to get storage:"+storageName);
        	m_serverFileSeparator = m_client.getFileSeparator();
        } catch (GPEMiddlewareRemoteException e) {
            if ("Client exception".equals(e.getMessage())) {
                if (!m_isRetrying) {
                    s_logger.warn("Retrying method connect()");
                    m_isRetrying = true;
                    this.connect(userInfo, host, port, basePath, attributes);
                    m_isRetrying = false;
                    return; //====================> EXIT OK
                }
                throw new NoSuccess("Retry failed", e);
            }
            throw new NoSuccess(e);
        } catch (Exception e) {
			throw new NoSuccess(e);
		}
    }

    public void disconnect() throws NoSuccess {    	
    	targetSystemInfo = null;
    	m_client = null;
    	super.disconnect();
    }
    
    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDenied, Timeout, NoSuccess {
    	// prepare path
		absolutePath = getEntryPath(absolutePath);
		// if absolutePath is the root
		if(absolutePath.equals(rootDirectory)) {
			return true;
		}
		try {
	        // check
            com.intel.gpe.clients.api.GridFile entry = m_client.listProperties(absolutePath);
            return (entry.getPath() != null);
		} catch (GPESecurityException e) {
			throw new PermissionDenied("Unable to check entry",e);
        } catch (GPEMiddlewareRemoteException e) {
            if ("Unknown exception".equals(e.getMessage())) {
                // occurs when parent entry is not a directory
                return false;
            } else if ("Client exception".equals(e.getMessage())) {
                if (!m_isRetrying) {
                    s_logger.warn("Retrying method exists()");
                    m_isRetrying = true;
                    boolean ret = this.exists(absolutePath, additionalArgs);
                    m_isRetrying = false;
                    return ret; //====================> EXIT OK
                }
                throw new NoSuccess("Retry failed", e);
            }
            throw new NoSuccess(e);
        } catch (Throwable e) {
            throw new NoSuccess(e);
		}
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        //prepare path and connection
        absolutePath = getEntryPath(absolutePath);
        try {
            // check
            com.intel.gpe.clients.api.GridFile entry = m_client.listProperties(absolutePath);
            if (entry.getPath() != null) {
                return new RByteIOFileAttributes(entry, m_serverFileSeparator);
            } else {
                throw new DoesNotExist("Entry does not exist: "+absolutePath);
            }
        } catch (DoesNotExist e) {
            throw e;
        } catch (GPESecurityException e) {
            throw new PermissionDenied("Unable to get entry attributes",e);
        } catch (GPEMiddlewareRemoteException e) {
            if ("Client exception".equals(e.getMessage())) {
                if (!m_isRetrying) {
                    s_logger.warn("Retrying method getAttributes()");
                    m_isRetrying = true;
                    FileAttributes ret = this.getAttributes(absolutePath, additionalArgs);
                    m_isRetrying = false;
                    return ret; //====================> EXIT OK
                }
                throw new NoSuccess("Retry failed", e);
            }
            throw new NoSuccess(e);
        } catch (Throwable e) {
            throw new NoSuccess(e);
        }
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {        
    	// prepare path
    	absolutePath = getEntryPath(absolutePath);

		try {
	        List<com.intel.gpe.clients.api.GridFile> directoryList = m_client.listDirectory(absolutePath);
	        Vector<RByteIOFileAttributes> entries = new Vector<RByteIOFileAttributes>();
	        for (com.intel.gpe.clients.api.GridFile file : directoryList) {
	        	entries.add(new RByteIOFileAttributes(file,m_serverFileSeparator));
	        }
	        FileAttributes[] list = new FileAttributes[entries.size()];
	        for (int i = 0; i < list.length; i++) {
				list[i] = entries.get(i);
			}
	        return list;
		} catch (GPESecurityException e) {
			throw new NoSuccess(e);
        } catch (GPEMiddlewareRemoteException e) {
            if ("Unknown exception".equals(e.getMessage())) {
                if(!exists(absolutePath, additionalArgs)) {
                    throw new DoesNotExist("The entry ["+absolutePath+"] does not exist.");
                }
            } else if ("Client exception".equals(e.getMessage())) {
                if (!m_isRetrying) {
                    s_logger.warn("Retrying method listAttributes()");
                    m_isRetrying = true;
                    FileAttributes[] ret = this.listAttributes(absolutePath, additionalArgs);
                    m_isRetrying = false;
                    return ret; //====================> EXIT OK
                }
                throw new NoSuccess("Retry failed", e);
            }
            throw new NoSuccess(e);
		} catch (Throwable e) {
			throw new NoSuccess(e);
		}
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
    	String absolutePath = getEntryPath(parentAbsolutePath + directoryName);
    	try {
    		// check parent here, else no exception returned during creation
    		if(!exists(parentAbsolutePath, additionalArgs)) {
                throw new ParentDoesNotExist("Parent directory does not exist: "+parentAbsolutePath);
    		}

        	// make
    		CreateDirectoryRequest request = new CreateDirectoryRequest(m_client, absolutePath);
			request.perform();
        } catch (ParentDoesNotExist e) {
            throw e;
        } catch (GPESecurityException e) {
			throw new PermissionDenied(e);
        } catch (GPEMiddlewareRemoteException e) {
            if ("Unknown exception".equals(e.getMessage())) {
                if (exists(absolutePath, additionalArgs)) {
                    throw new AlreadyExists("Entry already exists: "+absolutePath);
                } else {
                    throw new BadParameter("Parent entry is not a directory: "+parentAbsolutePath, e);
                }
            } else if ("Client exception".equals(e.getMessage())) {
                if (!m_isRetrying) {
                    s_logger.warn("Retrying method makeDir()");
                    m_isRetrying = true;
                    this.makeDir(parentAbsolutePath, directoryName, additionalArgs);
                    m_isRetrying = false;
                    return; //====================> EXIT OK
                }
                throw new NoSuccess("Retry failed", e);
            }
            throw new NoSuccess(e);
        } catch (Throwable e) {
			throw new NoSuccess(e);
		}
    }

    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
    	// prepare path
		String absolutePath = getEntryPath(parentAbsolutePath + directoryName) ;
		try {
            // get directory
            com.intel.gpe.clients.api.GridFile directory = m_client.listProperties(absolutePath);
            if (!directory.isDirectory()) {
                throw new BadParameter("Entry is not a directory: "+absolutePath);
            }

			// check children here, else no exception returned during deletion !			
			List<com.intel.gpe.clients.api.GridFile> directoryList = m_client.listDirectory(absolutePath);
	        if(directoryList.size() > 0) {
	        	throw new NoSuccess("Directory is not empty: "+absolutePath);
	        }

            // remove
            DeleteFileRequest requestRmFile = new DeleteFileRequest(m_client, directory);
            requestRmFile.perform();
        } catch (BadParameter e) {
            throw e;
        } catch (NoSuccess e) {
            throw e;
        } catch (GPESecurityException e) {
			throw new PermissionDenied(e);
        } catch (GPEMiddlewareRemoteException e) {
            if ("Unknown exception".equals(e.getMessage())) {
                if (!exists(absolutePath, additionalArgs)) {
                    throw new DoesNotExist("Entry does not exist: "+absolutePath);
                }
            } else if ("Client exception".equals(e.getMessage())) {
                if (!m_isRetrying) {
                    s_logger.warn("Retrying method removeDir()");
                    m_isRetrying = true;
                    this.removeDir(parentAbsolutePath, directoryName, additionalArgs);
                    m_isRetrying = false;
                    return; //====================> EXIT OK
                }
                throw new NoSuccess("Retry failed", e);
            }
            throw new NoSuccess(e);
        } catch (Throwable e) {
			throw new NoSuccess(e);
		}
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess {
    	//prepare path
		String absolutePath = getEntryPath(parentAbsolutePath + fileName);
		try {
            // get file
            com.intel.gpe.clients.api.GridFile file = m_client.listProperties(absolutePath);
            if (file.isDirectory()) {
                throw new BadParameter("Entry is a directory: "+absolutePath);
            }

            // remove
            DeleteFileRequest requestRmFile = new DeleteFileRequest(m_client, file);
            requestRmFile.perform();
        } catch (BadParameter e) {
            throw e;
        } catch (GPESecurityException e) {
			throw new PermissionDenied(e);
        } catch (GPEMiddlewareRemoteException e) {
            if ("Unknown exception".equals(e.getMessage())) {
                if (!exists(absolutePath, additionalArgs)) {
                    throw new DoesNotExist("Entry does not exist: "+absolutePath);
                }
            } else if ("Client exception".equals(e.getMessage())) {
                if (!m_isRetrying) {
                    s_logger.warn("Retrying method removeFile()");
                    m_isRetrying = true;
                    this.removeFile(parentAbsolutePath, fileName, additionalArgs);
                    m_isRetrying = false;
                    return; //====================> EXIT OK
                }
                throw new NoSuccess("Retry failed", e);
            }
            throw new NoSuccess(e);
        } catch (Throwable e) {
            throw new NoSuccess(e);
        }
    }

	public void putFromStream(String absolutePath, boolean append, String additionalArgs,
			InputStream stream) throws PermissionDenied,
			BadParameter, AlreadyExists, ParentDoesNotExist, Timeout, NoSuccess {
		if(append) {
			throw new NoSuccess("Append not supported.");
		}
		
		try {
    		// prepare path
    		absolutePath = getEntryPath(absolutePath);
    		
    		Pair<GPEFile, String> inputFile = new Pair<GPEFile, String>(null, absolutePath);
    		FileProvider fileProvider = new FileProvider();
	        fileProvider.addFilePutter("RBYTEIO", RandomByteIOFileImportImpl.class);	        
    		List<FileImport> putters = fileProvider.preparePutters(m_client);
        	int i;
			for (i = 0; i < putters.size(); i++) {
			    try {
			         putters.get(i).putFile(targetSystemInfo.getSecurityManager(), stream, inputFile.getM2());
			     }
			     catch (GPEFileTransferProtocolNotSupportedException e) {
			         continue;
			     }
			     break;
			}
			if (i == putters.size()) {
			     throw new Exception(
			             Messages.getString(MessagesKeys.common_requests_PutFilesRequest_Cannot_put_file_to_remote_location__no_suitable_protocol_found));
			}            
        } catch (Throwable e) {
			throw new NoSuccess("Unable to put file", e);
		}
	}

	public void getToStream(String absolutePath, String additionalArgs,
			OutputStream stream) throws PermissionDenied, BadParameter,
			DoesNotExist, Timeout, NoSuccess {
		try {
    		// prepare path
    		absolutePath = getEntryPath(absolutePath);
    		
    		// copy
            Pair<GPEFile, String> outputFile = new Pair<GPEFile, String>(null, absolutePath);
            FileProvider fileProvider = new FileProvider();
	        fileProvider.addFileGetter("RBYTEIO", RandomByteIOFileExportImpl.class);
	        List<FileExport> getters = fileProvider.prepareGetters(m_client);
            int i;
            for (i = 0; i < getters.size(); i++) {
                try {
                    getters.get(i).getFile(targetSystemInfo.getSecurityManager(), outputFile.getM2(), stream, null);
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
            
        } catch (Throwable e) {
			throw new NoSuccess("Unable to get file", e);
		}
	}

    private String getEntryPath(String path) {
        // the path must be like /DEMOSITE/services/<service name>/<storage name>/directory/file.txt
    	// if the request path is a full path, containing service name
    	if(path.indexOf(m_serviceName) > -1) {
        	path  = path.substring(path.indexOf(m_serviceName)+m_serviceName.length()+1,path.length());
        
            // remove the registry path and the storage path in the path name
			path = path.substring(path.indexOf(m_serverFileSeparator)+1 ,path.length());
        }
        // remove last file separator, a path cannot ends with a file separator 
        if(path.endsWith(m_serverFileSeparator)) {
            path = path.substring(0,path.length()-1);
        }
        // remove first file separator, a path cannot starts with a file separator 
        if(path.startsWith(m_serverFileSeparator)) {
            path = path.substring(1,path.length());
        }
        // replace root directory
        if(path.equals("")) {
            path = rootDirectory;
        }
        return path;
    }
}
