package fr.in2p3.jsaga.adaptor.ourgrid.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import com.sun.jersey.core.util.Base64;

import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;
import fr.in2p3.jsaga.adaptor.ourgrid.job.OurGridAbstract;
import fr.in2p3.jsaga.adaptor.ourgrid.job.OurGridConstants;

/* ***************************************************
 * ***  Distributed Systems Lab(LSD)-UFCG) ***
 * ***   http://www.lsd.ufcg.edu.br        ***
 * ***************************************************
 * File:   OurGridDataAdaptor
 * Author: Patricia Alanis (patriciaam@lsd.ufcg.edu.br)
 * Date:   August 2012
 * ***************************************************/
/**
 * This class manages the data of the job such as input (required for the job execution)
 *  and output (errors and job outputs) files.
 * @author patriciaam
 * @since 1.1, 08/01/2012
 */
public class OurGridDataAdaptor extends OurGridAbstract implements 
FileReaderGetter, FileWriterPutter{

	private static final String DOWNLOAD_SERVICE = "/download/";
	private static final String UPLOAD_SERVICE = "/upload/";
	private static final String REMOVE_SERVICE = "/remove/";
	private final String REMOVE_DIR_MESSAGE = "RemoveDir is not supported"; 
	private final String MAKE_DIR_MESSAGE = "MakeDir is not supported";
	private final String FILE = "file";
	private String host;
	private String authentication;

	/**
	 * Authentication consists of a encoded string used to authenticate to the server
	 * @param authentication username and a password encoded
	 */
	public void setAuthentication(String authentication) {

		this.authentication = authentication;
	}

	/**
	 * Gets the authentication to the server
	 * @return aurhenticathion username and a password encoded
	 */
	public String getAuthentication() {

		return authentication;
	}

	/**
	 * Returns the adaptor type 
	 * @return {@link OurGridConstants.TYPE_ADAPTOR} ourgrid
	 */
	public String getType() {

		return OurGridConstants.TYPE_ADAPTOR;
	}

	/**
	 * Returns the default server port
	 * @return {@link OurGridConstants.PORT} 8080
	 */
	public int getDefaultPort() {

		return OurGridConstants.PORT;
	}

	/**
	 * Sets the host name
	 * @param host server address
	 */
	public void setHost(String host) {

		this.host = host;
	}

	/**
	 * Returns the name of the host 
	 * @return host server address
	 */
	public String getHost() {

		return host;
	}


	/**
	 *  Connects to the server and initializes the connection with the provided attributes
	 *  @param userInfo the user login
	 *  @param host the server
	 *  @param port the port 
	 *  @param basePath the base path
	 *  @param attributes the provided attributes
	 */
	public void connect(String userInfo, String host, int port,String basePath,Map attributes)	
			throws NotImplementedException, AuthenticationFailedException,AuthorizationFailedException, 
			IncorrectURLException,BadParameterException, TimeoutException, NoSuccessException {

		setHost(host);
		setAuthentication(new String(Base64.encode(m_account + ":" + m_passPhrase)));
	}

	/**
	 *  Creates a new directory directoryName
	 *  @param parentAbsolutePath the parent directory
	 *  @param directoryName the directory to create
	 *  @param additionalArgs adaptor specific arguments
	 */
	public void makeDir(String parentAbsolutePath, String directoryName,String additionalArgs) 
			throws PermissionDeniedException,BadParameterException, AlreadyExistsException, ParentDoesNotExist,TimeoutException, NoSuccessException {

		throw new BadParameterException(MAKE_DIR_MESSAGE);

	}

	/**
	 * Removes the directory absolutePath
	 * @param parentAbsolutePath the parent directory
	 * @param directoryName the directory to create
	 * @param additionalArgs adaptor specific arguments
	 */
	public void removeDir(String parentAbsolutePath, String directoryName,String additionalArgs) 
			throws PermissionDeniedException,BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {

		throw new BadParameterException(REMOVE_DIR_MESSAGE);

	}

	/**
	 * Tests this entry for existing
	 * @param absolutePath the absolute path of the entry
	 * @param additionalArgs adaptor specific arguments
	 * @return true if the entry exists
	 */
	public boolean exists(String absolutePath, String additionalArgs)
			throws PermissionDeniedException, TimeoutException,NoSuccessException {

		if (absolutePath.equals(UPLOAD_SERVICE)) {

			return true;
		}

		if (absolutePath.startsWith(DOWNLOAD_SERVICE)) {

			return true;
		}

		return false;
	}

	/**
	 * Gets the file attributes of the entry absolutePath.
	 * @param absolutePath  the absolute path of the entry
	 * @param additionalArgs adaptor specific arguments
	 * @return Returns the file attributes
	 */
	public FileAttributes getAttributes(String absolutePath, String additionalArgs)
			throws PermissionDeniedException, DoesNotExistException,TimeoutException, NoSuccessException {

		return null;
	}

	/**
	 * Lists all the entries in the directory absolutePath
	 * @param absolutePath the directory containing entries to list
	 * @param additionalArgs adaptor specific arguments
	 * @return Returns the entry attributes
	 */
	public FileAttributes[] listAttributes(String absolutePath, String additionalArgs)
			throws PermissionDeniedException, BadParameterException,DoesNotExistException, TimeoutException, NoSuccessException {

		return null;
	}
	
	/**
	 * Removes the file absolutePath
	 * @param parentAbsolutePath the parent directory
	 * @param fileName the file to remove
	 * @param additionalArgs adaptor specific arguments
	 */
	public void removeFile(String parentAbsolutePath, String fileName, String additionalArgs) 
			throws PermissionDeniedException,BadParameterException, DoesNotExistException, TimeoutException,NoSuccessException {

		try {

			String authentication = OurGridConstants.BASIC + " " + getAuthentication();
			HttpDelete method = new HttpDelete(OurGridConstants.HTTP+ getHost()+REMOVE_SERVICE+ fileName);
			method.addHeader(OurGridConstants.AUTHORIZATION, authentication);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(method);

			if (response.getStatusLine().getStatusCode()== 401) { 

				throw new PermissionDeniedException(response.getStatusLine().getReasonPhrase());
			}

		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}
	
	/**
	 * Puts content of stream to absolutePath
	 * @param absolutePath  the path of the file to put
	 * @param append if true, append stream at the end of file
	 * @param additionalArgs adaptor specific arguments
	 * @param stream the input stream
	 * @throws PermissionDeniedException
	 */
	public void putFromStream(String absolutePath, boolean append, String additionalArgs, InputStream stream)
			throws PermissionDeniedException, BadParameterException,AlreadyExistsException, ParentDoesNotExist, TimeoutException,NoSuccessException {

		InputStreamBody isb = new InputStreamBody(stream, new File(absolutePath).getName());

		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart(FILE, isb);
		String authentication = OurGridConstants.BASIC + " " + getAuthentication();
		HttpPost method = new HttpPost(OurGridConstants.HTTP + getHost()+ UPLOAD_SERVICE);
		method.addHeader(OurGridConstants.AUTHORIZATION, authentication);
		method.setEntity(reqEntity);

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		try {
			response = httpclient.execute(method);
		

		if (response.getStatusLine().getStatusCode() == 401) {

			throw new PermissionDeniedException(response.getStatusLine().getReasonPhrase());
		}
		} catch (ClientProtocolException e) {
			
		} catch (IOException e) {
			
		}
	}

	/**
	 * Gets content of absolutePath to stream
	 * @param absolutePath the path of the file to get
	 * @param additionalArgs adaptor specific arguments
	 * @param stream the output stream
	 */
	public void getToStream(String absolutePath, String additionalArgs, OutputStream stream) 
			throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException,	NoSuccessException {

		HttpClient httpClient = new DefaultHttpClient();
		String authentication = OurGridConstants.BASIC + " " + getAuthentication();
		HttpGet getMethod = new HttpGet(OurGridConstants.HTTP + getHost() + absolutePath);
		getMethod.addHeader(OurGridConstants.AUTHORIZATION, authentication);
		HttpResponse response = null;

		try {
			response = httpClient.execute(getMethod);
			HttpEntity entity = response.getEntity();
			if (entity != null) {

				InputStream inputStream = entity.getContent();

				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = inputStream.read(bytes)) != -1) {
					stream.write(bytes, 0, read);
				}

				inputStream.close();
				stream.flush();
				stream.close();
			}
			
			if (response .getStatusLine().getStatusCode()==401){
				throw new PermissionDeniedException(response.getStatusLine().getReasonPhrase());
			}

		} catch (ClientProtocolException e) {
			throw new NoSuccessException(e);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		}

	}

}