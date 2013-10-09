package fr.in2p3.jsaga.adaptor.dirac;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.dirac.util.DiracConstants;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.X509SecurityCredential;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DiracJobAdaptorAbstract
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   30 sept 2013
 * ***************************************************/

public abstract class DiracAdaptorAbstract implements ClientAdaptor {

	protected Logger m_logger = Logger.getLogger(this.getClass());

	protected X509SecurityCredential m_credential;
	protected URL m_url;
	protected String m_accessToken;
	private SSLContext m_sslContext;
	
	public Class[] getSupportedSecurityCredentialClasses() {
		return new Class[]{X509SecurityCredential.class};
	}

	public void setSecurityCredential(SecurityCredential credential) {
		m_credential = (X509SecurityCredential) credential;
	}

	public int getDefaultPort() {
		// TODO default port
		return 0;
	}

	public void connect(String userInfo, String host, int port,
			String basePath, Map attributes) throws NotImplementedException,
			AuthenticationFailedException, AuthorizationFailedException,
			IncorrectURLException, BadParameterException, TimeoutException,
			NoSuccessException {
		try {
			m_url = new URL("https",host, port, "/");
		} catch (MalformedURLException e) {
			throw new NoSuccessException(e);
		}
        TrustManager[] trustManager = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {return null;}
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
        }};
        try {
			m_sslContext = SSLContext.getInstance("SSL");
	    	m_sslContext.init(m_credential.getKeyManager(), trustManager, new java.security.SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			throw new AuthenticationFailedException(e);
		} catch (KeyManagementException e) {
			throw new AuthenticationFailedException(e);
		}
	}

	public void disconnect() throws NoSuccessException {
		m_sslContext = null;
	}

	protected class DiracRESTClient {

		private JSONObject m_getParams = new JSONObject();
		private Hashtable<String, String> m_files = new Hashtable<String, String>();
		
		public DiracRESTClient() {
			if (m_accessToken != null) {
				this.addParam(DiracConstants.DIRAC_GET_PARAM_ACCESS_TOKEN, m_accessToken);
			}
		}

		/**
		 * Add a list of GET parmaeters
		 * @param params
		 */
		public void addParam(JSONObject params) {
	    	for (Object key : params.keySet()) {
	    		addParam((String)key, (String)params.get(key));
	      	}
			
		}
		
		/**
		 * Add a GET parameter
		 * @param key
		 * @param value
		 */
		public void addParam(String key, String value) {
			m_getParams.put(key, value);
		}

		/**
		 * Add a file to send as multipart
		 * @param filenameToSend		name that will be sent
		 * @param localFilenameToRead	name of the local file which content will be sent
		 */
		public void addFile(String filenameToSend, String localFilenameToRead) {
			m_files.put(filenameToSend, localFilenameToRead);
		}
		
		/**
		 * send a GET request
		 * @param url
		 * @return a JSONObject
		 * @throws NoSuccessException
		 * @throws AuthenticationFailedException
		 * @throws IncorrectURLException
		 */
		public JSONObject get(URL url) throws NoSuccessException, AuthenticationFailedException, IncorrectURLException  {
			return this.doRequest(url, "GET");
		}
		
		/**
		 * send a POST request
		 * @param url
		 * @return a JSONObject
		 * @throws NoSuccessException
		 * @throws AuthenticationFailedException
		 * @throws IncorrectURLException
		 */
		public JSONObject post(URL url) throws NoSuccessException, AuthenticationFailedException, IncorrectURLException {
			return this.doRequest(url, "POST");
		}
		
		/**
		 * send a DELETE request
		 * @param url
		 * @return a JSONObject
		 * @throws NoSuccessException
		 * @throws AuthenticationFailedException
		 * @throws IncorrectURLException
		 */
		public JSONObject delete(URL url) throws NoSuccessException, AuthenticationFailedException, IncorrectURLException {
			return this.doRequest(url, "DELETE");
		}
		
		/**
		 * send a HTTP request
		 * @param url
		 * @param HTTPType
		 * @return a JSONObject
		 * @throws NoSuccessException
		 * @throws AuthenticationFailedException
		 * @throws IncorrectURLException
		 */
	    private JSONObject doRequest(URL url, String HTTPType) 
	    		throws NoSuccessException, AuthenticationFailedException, IncorrectURLException {
	        InputStream stream;
			try {
				stream = getStream(url, HTTPType);
			} catch (UnrecoverableKeyException e) {
				throw new AuthenticationFailedException(e);
			} catch (KeyManagementException e) {
				throw new AuthenticationFailedException(e);
			} catch (KeyStoreException e) {
				throw new AuthenticationFailedException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new AuthenticationFailedException(e);
			} catch (CertificateException e) {
				throw new AuthenticationFailedException(e);
			} catch (IOException e) {
				throw new NoSuccessException(e);
			}
	        try {
	            JSONParser parser = new JSONParser();
	            return (JSONObject) parser.parse(new InputStreamReader(stream));
	        } catch (IOException e) {
				throw new NoSuccessException(e);
			} catch (ParseException e) {
				throw new NoSuccessException(e);
	        }
	    }
	    
	    /**
	     * get the result of a HTTP request
	     * @param url
	     * @param HTTPType
	     * @return the InputStream of the result
	     * @throws IOException
	     * @throws KeyStoreException
	     * @throws NoSuchAlgorithmException
	     * @throws CertificateException
	     * @throws UnrecoverableKeyException
	     * @throws KeyManagementException
	     * @throws IncorrectURLException
	     */
	    public InputStream getStream(URL url, String HTTPType) throws IOException, KeyStoreException, NoSuchAlgorithmException, 
	    															CertificateException, UnrecoverableKeyException, 
	    															KeyManagementException, IncorrectURLException  {

	    	String query = buildQuery(m_getParams);
	    	url = new URL(url + query);
	    	m_logger.debug(HTTPType + " " + url.toString());
	        HttpsURLConnection httpsConnection = (HttpsURLConnection)url.openConnection();
	        httpsConnection.setRequestMethod(HTTPType);

	    	httpsConnection.setSSLSocketFactory(m_sslContext.getSocketFactory());
	        httpsConnection.setHostnameVerifier(new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {return true;}
	        });

	        // set POST message
	        if (m_files.size() > 0) {
	        	httpsConnection.setDoOutput(true);
	        	httpsConnection.setUseCaches(false);
	        	httpsConnection.setRequestProperty("Connection", "Keep-Alive");


	        	Iterator<String> i=m_files.keySet().iterator();
	        	MultipartEntityBuilder meb = MultipartEntityBuilder.create(); 
	        	while (i.hasNext()) {
	        		String f = i.next();
	            	m_logger.debug("Adding multipart file: " + f);
	            	String local_f = m_files.get(f);
	            	File local_file;
	            	try {
	                	// try to read file as URL file:/
	            		local_file = new File(new URL(local_f).getPath());
	            	} catch (MalformedURLException e) {
	            		// local file
	            		local_file  = new File(local_f);
	            	}

	            	meb.addBinaryBody(f, local_file, ContentType.MULTIPART_FORM_DATA, f);
	        	}
	        	HttpEntity data = meb.build();
	        	httpsConnection.addRequestProperty("Content-length", data.getContentLength()+"");
	        	httpsConnection.addRequestProperty(data.getContentType().getName(), data.getContentType().getValue());
	        	DataOutputStream post = new DataOutputStream(httpsConnection.getOutputStream());
	        	data.writeTo(post);
	        	post.flush();
	        	post.close();
	        }
	        // open input stream
	        httpsConnection.connect();
	        InputStream stream = httpsConnection.getInputStream();
	        if (httpsConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
	            stream.close();
	            throw new IOException("Received error message: "+httpsConnection.getResponseMessage());
	        }
	        return stream;
	        
	    }

	    /**
	     * Transforms a list of params into a URL-encoded GET query
	     * @param args as a JSONObject
	     * @return a URL-encoded GET query
	     * @throws UnsupportedEncodingException
	     */
	    private String buildQuery(JSONObject args) throws UnsupportedEncodingException {
	    	String query = "";
	    	for (Object key : args.keySet()) {
	    		if (query.length() == 0) {
	    			query = "?";
	    		} else {
	    			query = query + "&";
	    		}
	    		query = query + URLEncoder.encode((String)key, "UTF-8") + "=" + URLEncoder.encode((String)args.get(key), "UTF-8");
	      	}
	    	return query;
	    }
	    
	}
	
}