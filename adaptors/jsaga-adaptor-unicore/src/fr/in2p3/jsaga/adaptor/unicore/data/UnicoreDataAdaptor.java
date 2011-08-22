package fr.in2p3.jsaga.adaptor.unicore.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.adaptor.unicore.UnicoreAbstract;

import org.apache.log4j.Logger;
import org.ogf.saga.error.*;
import org.unigrids.services.atomic.types.GridFileType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.client.SByteIOClient;
import de.fzj.unicore.uas.client.StorageClient;
import de.fzj.unicore.uas.client.StorageFactoryClient;
import de.fzj.unicore.wsrflite.xmlbeans.BaseFault;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreDataAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   22 aout 2011
* ***************************************************/
/**
 *
 */
public class UnicoreDataAdaptor extends UnicoreAbstract implements FileWriterPutter, FileReaderGetter {
    private static Logger s_logger = Logger.getLogger(UnicoreDataAdaptor.class);
    //private TargetSystemInfo targetSystemInfo ;
	//private StorageFactoryClient m_storageFactoryClient = null;
	private String m_serverFileSeparator ;
	private StorageClient m_client;
    private String rootDirectory = ".";
    public String getType() {
        return "unicore";
    }

    public Usage getUsage() {
    	return new UAnd(new Usage[]{new U(SERVICE_NAME), 
    								new U(APPLICATION_NAME)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default(SERVICE_NAME, "StorageManagement"), 
    			new Default(RES, "default_storage"),
    			new Default(TARGET, "DEMO-SITE"),
    			new Default(APPLICATION_NAME, "Bash shell")};
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
    	
    	// get SERVICE_NAME
    	/*if(attributes.containsKey(SERVICE_NAME))
    		m_serviceName = (String) attributes.get(SERVICE_NAME);*/
    	    	
    	// get registry
    	/*
    	if(basePath.indexOf(m_serviceName) == -1 ) {
    		throw new BadParameterException("Invalid base path:"+basePath);
    	}
    	*/
    	//String registryPath = basePath.substring(0,basePath.indexOf(m_serviceName)+m_serviceName.length());
    	//m_serverUrl = "https://"+host+":"+port+registryPath;
    	
    	// get storage name
    	/*
    	if(basePath.indexOf(m_serviceName)+m_serviceName.length() >= basePath.length()) {
    		throw new BadParameterException("Invalid storage name: the name cannot be empty '"+basePath+"'");
    	}
    	String storageName =  basePath.substring(basePath.indexOf(m_serviceName)+m_serviceName.length()+1,basePath.length());
    	if(storageName.indexOf("/") < 1) {
    		throw new BadParameterException("invalid storage name, must contain '/': '"+storageName+"'.");
		}
    	storageName = storageName.substring(0,storageName.indexOf("/"));
    	*/
    	try {
			/*StorageFactoryClient sfc=new StorageFactoryClient(m_serverUrl,m_epr,m_uassecprop);
		    for ( Iterator<EndpointReferenceType> flavoursIter = sfc.getStorages().iterator(); flavoursIter.hasNext(); ) {
		        System.out.println( flavoursIter.next().getAddress().stringValue() );
		      }*/


			m_client = new StorageClient(m_epr,m_uassecprop);
    		//m_client = sfc.createSMS();
        	//if(m_client == null)
        	//	throw new NoSuccessException("Unable to get storage:"+m_serverUrl);
			// TODO customize this
			m_serverFileSeparator = "/";
			//m_client.createDirectory("lszdir");
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
		// get client that talks to registry
        try {
        	  // find target system that supports the specific application
        	//TODO targetSystemInfo = findTargetSystem();
        	// TODOm_client = targetSystemInfo.getTargetSystem().getStorage(storageName);
        	// TODOm_serverFileSeparator = m_client.getFileSeparator();
/*        } catch (GPEMiddlewareRemoteException e) {
            if ("Client exception".equals(e.getMessage())) {
                if (!m_isRetrying) {
                    s_logger.warn("Retrying method connect()");
                    m_isRetrying = true;
                    this.connect(userInfo, host, port, basePath, attributes);
                    m_isRetrying = false;
                    return; //====================> EXIT OK
                }
                throw new NoSuccessException("Retry failed", e);
            }
            throw new NoSuccessException(e);*/
        } catch (Exception e1) {
			throw new NoSuccessException(e1);
		}
    }

    public void disconnect() throws NoSuccessException {
    	//targetSystemInfo = null;
    	m_client = null;
    	super.disconnect();
    }
    
    public boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException {
    	// prepare path
    	absolutePath = getEntryPath(absolutePath);
		// if absolutePath is the root
		if(absolutePath.equals(rootDirectory)) {
			return true;
		}
		try {
			GridFileType gft = m_client.listProperties(absolutePath);
		} catch (BaseFault e) {
			e.printStackTrace();
			throw new NoSuccessException(e);
		} catch (FileNotFoundException e) {
			return false;
		}
    	return true;
    }

    public FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        //prepare path and connection
        absolutePath = getEntryPath(absolutePath);
		try {
			GridFileType gft = m_client.listProperties(absolutePath);
			return new UnicoreFileAttributes(gft, m_serverFileSeparator);
		} catch (BaseFault e) {
			e.printStackTrace();
			throw new NoSuccessException(e);
		} catch (FileNotFoundException e) {
			throw  new DoesNotExistException(e);
		}
        /*
        try {
            // check
            com.intel.gpe.clients.api.GridFile entry = m_client.listProperties(absolutePath);
            if (entry.getPath() != null) {
                return new RByteIOFileAttributes(entry, m_serverFileSeparator);
            } else {
                throw new DoesNotExistException("Entry does not exist: "+absolutePath);
            }
        } catch (DoesNotExistException e) {
            throw e;
        } catch (GPESecurityException e) {
            throw new PermissionDeniedException("Unable to get entry attributes",e);
        } catch (GPEMiddlewareRemoteException e) {
            if ("Client exception".equals(e.getMessage())) {
                if (!m_isRetrying) {
                    s_logger.warn("Retrying method getAttributes()");
                    m_isRetrying = true;
                    FileAttributes ret = this.getAttributes(absolutePath, additionalArgs);
                    m_isRetrying = false;
                    return ret; //====================> EXIT OK
                }
                throw new NoSuccessException("Retry failed", e);
            }
            throw new NoSuccessException(e);
        } catch (Throwable e) {
            throw new NoSuccessException(e);
        }
        */
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
    	// prepare path
    	absolutePath = getEntryPath(absolutePath);
    	try {
    		GridFileType[] _list = m_client.listDirectory(absolutePath);
        	FileAttributes[] attrs = new FileAttributes[_list.length];
        	int i=0;
			for (GridFileType gft : _list) {
				attrs[i++] = new UnicoreFileAttributes(gft, m_serverFileSeparator);
			}
			return attrs;
		} catch (BaseFault e) {
			throw new NoSuccessException(e);
		}
    	/*
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
			throw new NoSuccessException(e);
        } catch (GPEMiddlewareRemoteException e) {
            if ("Unknown exception".equals(e.getMessage())) {
                if(!exists(absolutePath, additionalArgs)) {
                    throw new DoesNotExistException("The entry ["+absolutePath+"] does not exist.");
                }
            } else if ("Client exception".equals(e.getMessage())) {
                if (!m_isRetrying) {
                    s_logger.warn("Retrying method listAttributes()");
                    m_isRetrying = true;
                    FileAttributes[] ret = this.listAttributes(absolutePath, additionalArgs);
                    m_isRetrying = false;
                    return ret; //====================> EXIT OK
                }
                throw new NoSuccessException("Retry failed", e);
            }
            throw new NoSuccessException(e);
		} catch (Throwable e) {
			throw new NoSuccessException(e);
		}*/
    }

    public void makeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
    	String absolutePath = getEntryPath(parentAbsolutePath + directoryName);
		try {
			m_client.createDirectory(absolutePath);
		} catch (BaseFault e) {
			e.printStackTrace();
			throw new NoSuccessException(e);
		}
    }

    private void delete(String path) throws NoSuccessException{
		try {
			m_client.delete(path);
		} catch (BaseFault e) {
			e.printStackTrace();
			throw new NoSuccessException(e);
		}
    }
    
    public void removeDir(String parentAbsolutePath, String directoryName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
    	// prepare path
		delete(getEntryPath(parentAbsolutePath + directoryName)) ;
    }

    public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
    	//prepare path
		delete(getEntryPath(parentAbsolutePath + fileName));
    }

	public void putFromStream(String absolutePath, boolean append, String additionalArgs,
			InputStream stream) throws PermissionDeniedException,
            BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
		if(append) {
			throw new NoSuccessException("Append not supported.");
		}
		try {
			SByteIOClient io_client = m_client.getWriteStream(absolutePath);
			io_client.writeAllData(stream);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
		/*
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
			throw new NoSuccessException("Unable to put file", e);
		}*/ 
	}

	public void getToStream(String absolutePath, String additionalArgs,
			OutputStream stream) throws PermissionDeniedException, BadParameterException,
            DoesNotExistException, TimeoutException, NoSuccessException {
		try {
			SByteIOClient io_client = m_client.getReadStream(absolutePath);
			io_client.readAllData(stream);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}
		/*
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
			throw new NoSuccessException("Unable to get file", e);
		}
		*/
	}

    private String getEntryPath(String path) {
        // the path must be like /DEMOSITE/services/<service name>/<storage name>/directory/file.txt
    	// if the request path is a full path, containing service name
    	/*if(path.indexOf(m_serviceName) > -1) {
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
        }*/
        return path;
    }
}
