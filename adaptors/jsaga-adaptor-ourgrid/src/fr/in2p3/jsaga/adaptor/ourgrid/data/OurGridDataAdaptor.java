package fr.in2p3.jsaga.adaptor.ourgrid.data;

import java.io.File;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.http_socket.HttpDataAdaptorSocketBased;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterPutter;

public class OurGridDataAdaptor extends HttpDataAdaptorSocketBased implements FileWriterPutter{
	private final String TYPE_ADAPTOR = "ourgrid";
	private final int PORT_NUMBER = 80;
	private final String UPLOAD_SERVICE = "/upload/";

	/**
	 * @return Returns the adaptor type
	 */
	public String getType() {

		return TYPE_ADAPTOR;
	}

	/**
	 * @return Returns the port number
	 */
	public int getDefaultPort() {

		return PORT_NUMBER;
	}
	public void makeDir(String parentAbsolutePath, String directoryName,
			String additionalArgs) throws PermissionDeniedException,
			BadParameterException, AlreadyExistsException, ParentDoesNotExist,
			TimeoutException, NoSuccessException {
		throw new BadParameterException("MakeDir is not supported.");

	}

	public void removeDir(String parentAbsolutePath, String directoryName,
			String additionalArgs) throws PermissionDeniedException,
			BadParameterException, DoesNotExistException, TimeoutException,
			NoSuccessException {
		throw new BadParameterException("RemoveDir is not supported.");

	}

	public void removeFile(String parentAbsolutePath, String fileName,
			String additionalArgs) throws PermissionDeniedException,
			BadParameterException, DoesNotExistException, TimeoutException,
			NoSuccessException {
		throw new BadParameterException("RemoveFile is not supported.");

	}

	// TODO: remove this
//	public boolean exists(String absolutePath, String additionalArgs)
//			throws PermissionDeniedException, TimeoutException,
//			NoSuccessException {
//		if (absolutePath.equals(UPLOAD_SERVICE)) {
//			return true;
//		}
//		return false;
//	}
	
	public void putFromStream(String absolutePath, boolean append,
			String additionalArgs, InputStream stream) throws PermissionDeniedException,
			BadParameterException, AlreadyExistsException, ParentDoesNotExist,
			TimeoutException, NoSuccessException {
		
		try {
			
			InputStreamBody isb = new InputStreamBody(stream, new File(absolutePath).getName());
			
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("file", isb);

			HttpPost method = new HttpPost("http://"+m_baseUrl.getHost()+UPLOAD_SERVICE);

			method.setEntity(reqEntity);

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(method);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new TimeoutException(response.getStatusLine()
						.getReasonPhrase());
			}

		} catch (Exception e) {
			throw new TimeoutException(e);
		}
		
	}
	
}

