package fr.in2p3.jsaga.adaptor.openstack.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;

public class OpenstackRESTClient {

    protected Logger m_logger = Logger.getLogger(this.getClass());
    private JSONObject m_getParams = new JSONObject();
    
    private String m_tokenId;
    
    public OpenstackRESTClient() {
        this(null);
    }

    public OpenstackRESTClient(String token) {
        this.m_tokenId = token;
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
    public JSONObject post(URL url, JSONObject data) throws NoSuccessException, AuthenticationFailedException, IncorrectURLException {
        return this.doRequest(url, "POST", data);
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
        return doRequest(url, HTTPType, null);
    }
    
    /**
     * 
     * @param url
     * @param HTTPType
     * @param data
     * @return
     * @throws NoSuccessException
     * @throws AuthenticationFailedException
     * @throws IncorrectURLException
     */
    private JSONObject doRequest(URL url, String HTTPType, JSONObject data) 
            throws NoSuccessException, AuthenticationFailedException, IncorrectURLException {
        InputStream stream;
        try {
            stream = getStream(url, HTTPType, data);
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
    public InputStream getStream(URL url, String HTTPType, JSONObject data) throws IOException, KeyStoreException, 
            NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException, IncorrectURLException  {

        m_logger.debug(HTTPType + " " + url.toString());
        HttpsURLConnection httpsConnection = (HttpsURLConnection)url.openConnection();
        httpsConnection.setRequestMethod(HTTPType);
        if (this.m_tokenId != null) {
            httpsConnection.addRequestProperty("X-Auth-Token", this.m_tokenId);
        }
        SSLContext m_sslContext = SSLContext.getInstance("SSL");
        TrustManager[] trustManager = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {return null;}
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
        }};
        m_sslContext.init(null, trustManager, null);

        httpsConnection.setSSLSocketFactory(m_sslContext.getSocketFactory());
        httpsConnection.setHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {return true;}
        });

        // set POST message
        if (data != null) {
            httpsConnection.setDoOutput(true);
            httpsConnection.setUseCaches(false);
            EntityBuilder eb = EntityBuilder.create();
            eb.setContentType(ContentType.APPLICATION_JSON);
            eb.setText(data.toJSONString());
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
//    private String buildQuery(JSONObject args) throws UnsupportedEncodingException {
//        String query = "";
//        for (Object key : args.keySet()) {
//            if (query.length() == 0) {
//                query = "?";
//            } else {
//                query = query + "&";
//            }
//            query = query + URLEncoder.encode((String)key, "UTF-8") + "=" + URLEncoder.encode((String)args.get(key), "UTF-8");
//        }
//        return query;
//    }
    

}
