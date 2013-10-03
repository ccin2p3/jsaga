package fr.in2p3.jsaga.adaptor.dirac.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;

import fr.in2p3.jsaga.adaptor.security.impl.X509SecurityCredential;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DiracRESTClient
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   30 sept 2013
 * ***************************************************/

// TODO: make this class an internal of AbstractAdaptor
public class DiracRESTClient {

//	private String m_cert = null;
	private X509SecurityCredential m_x509 = null;
//	private String m_passPhrase = null;
	private JSONObject m_getParams = new JSONObject();
	private StringBuffer m_postData = null;
	Logger m_logger = Logger.getLogger(this.getClass());
	
	/**
	 * Constructs a client that will connect with a "access_token"
	 * @param token
	 */
	public DiracRESTClient(X509SecurityCredential credential, String token) {
		this(credential);
		this.addParam(DiracConstants.DIRAC_GET_PARAM_ACCESS_TOKEN, token);
	}
	
	/**
	 * Constructs a client that will connect with a X509 certificate
	 * @param m_credential
	 */
	public DiracRESTClient(X509SecurityCredential credential) {
		m_x509 = credential;
	}
	
	/**
	 * @deprecated
	 */
	public DiracRESTClient(String cert, String pass) {
	}
	

	public void addParam(JSONObject params) {
    	for (Object key : params.keySet()) {
    		addParam((String)key, (String)params.get(key));
      	}
		
	}
	public void addParam(String key, String value) {
		m_getParams.put(key, value);
	}
	
	public void addData(String data) {
		if (m_postData == null) {
			m_postData = new StringBuffer();
		}
		m_postData.append(data);
		m_postData.append("\n");
	}
	
	public JSONObject get(URL url) throws NoSuccessException, AuthenticationFailedException, IncorrectURLException  {
		return this.doRequest(url, "GET");
	}
	public JSONObject post(URL url) throws NoSuccessException, AuthenticationFailedException, IncorrectURLException {
		return this.doRequest(url, "POST");
	}
	public JSONObject delete(URL url) throws NoSuccessException, AuthenticationFailedException, IncorrectURLException {
		return this.doRequest(url, "DELETE");
	}
	
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
    
    private InputStream getStream(URL url, String HTTPType) throws IOException, KeyStoreException, NoSuchAlgorithmException, 
    															CertificateException, UnrecoverableKeyException, 
    															KeyManagementException, IncorrectURLException  {

    	String query = buildQuery(m_getParams);
//    	for (Object key : m_getParams.keySet()) {
//    		if (query.length() == 0) {
//    			query = "?";
//    		} else {
//    			query = query + "&";
//    		}
//    		query = query + URLEncoder.encode((String)key, "UTF-8") + "=" + URLEncoder.encode((String)m_getParams.get(key), "UTF-8");
//      	}
    	url = new URL(url + query);
    	m_logger.debug(HTTPType + " " + url.toString());
        HttpsURLConnection httpsConnection = (HttpsURLConnection)url.openConnection();
//        if (! (connection instanceof HttpsURLConnection)) {
//            throw new IncorrectURLException("Unexpected URL: "+url);
//        }
//        HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
        httpsConnection.setRequestMethod(HTTPType);

//            if (connection instanceof HttpsURLConnection) {
//                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                TrustManager[] trustManager = new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {return null;}
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                }};
                SSLContext sslContext = SSLContext.getInstance("SSL");
//                if (m_x509 != null) {
                	sslContext.init(m_x509.getKeyManager(), trustManager, new java.security.SecureRandom());
                	httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
//                }
                httpsConnection.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {return true;}
                });
//            } else {
//                throw new IncorrectURLException("Unexpected URL: "+url);
//            }

        // set POST message
        if (m_postData != null) {
        	m_logger.debug("data=" + m_postData.toString());
            httpsConnection.setDoOutput(true);
            OutputStream post = httpsConnection.getOutputStream();
            post.write(URLEncoder.encode(m_postData.toString(), "UTF-8").getBytes());
            post.close();
        }

        // open input stream
//        if (connection instanceof HttpURLConnection) {
//            HttpURLConnection httpConnection = (HttpURLConnection) connection;
//            httpConnection.setRequestMethod(HTTPType);
            httpsConnection.connect();
            InputStream stream = httpsConnection.getInputStream();
            if (httpsConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                stream.close();
                throw new IOException("Received error message: "+httpsConnection.getResponseMessage());
            }
            return stream;
//        } else {
//            throw new IncorrectURLException("Unexpected URL: "+url);
//        }
        
    }

    public static String buildQuery(JSONObject args) throws UnsupportedEncodingException {
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
