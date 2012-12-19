//package fr.in2p3.jsaga.adaptor.security;
//
//import java.io.IOException;
//import java.util.Set;
//
//import org.bouncycastle.asn1.ASN1InputStream;
//import org.bouncycastle.asn1.x509.AttributeCertificate;
//import org.italiangrid.voms.VOMSError;
//import org.italiangrid.voms.request.VOMSACRequest;
//import org.italiangrid.voms.request.VOMSACService;
//import org.italiangrid.voms.request.VOMSESLookupStrategy;
//import org.italiangrid.voms.request.VOMSProtocolError;
//import org.italiangrid.voms.request.VOMSProtocolListener;
//import org.italiangrid.voms.request.VOMSRequestListener;
//import org.italiangrid.voms.request.VOMSResponse;
//import org.italiangrid.voms.request.VOMSServerInfo;
//import org.italiangrid.voms.request.VOMSServerInfoStore;
//import org.italiangrid.voms.request.VOMSServerInfoStoreListener;
//import org.italiangrid.voms.request.impl.AbstractVOMSProtocol;
//import org.italiangrid.voms.request.impl.DefaultVOMSServerInfoStore;
//import org.italiangrid.voms.request.impl.LegacyProtocol;
//import org.italiangrid.voms.request.impl.RESTProtocol;
//
//import eu.emi.security.authn.x509.X509CertChainValidatorExt;
//import eu.emi.security.authn.x509.X509Credential;
//
///** 
// * An implementation of the {@link VOMSACService} which fix an issue
// * on old VOMS servers where there is no REST service. 
// *
// */
//public class MyVOMSACService implements VOMSACService {
//
//	/**
//	 * The listener that will be informed about request events
//	 */
//	private VOMSRequestListener requestListener;
//		
//	/**
//	 * The listener that will be informed about  low-level protocol details
//	 */
//	private VOMSProtocolListener protocolListener;
//	
//	/**
//	 * The validator used for the SSL handshake
//	 */
//	private X509CertChainValidatorExt validator;
//	
//	/**
//	 * The store used to keep VOMS server contact information.
//	 */
//	private VOMSServerInfoStore serverInfoStore;
//	
//	/**
//	 * The connect timeout value 
//	 */
//	private int connectTimeout = AbstractVOMSProtocol.DEFAULT_CONNECT_TIMEOUT;
//	
//	/** 
//	 * The read timeout used 
//	 */
//	private int readTimeout = AbstractVOMSProtocol.DEFAULT_READ_TIMEOUT;
//
//	
//	public MyVOMSACService(X509CertChainValidatorExt validator,
//			VOMSRequestListener listener, 
//			VOMSESLookupStrategy lookupStrategy,
//			VOMSServerInfoStoreListener serverInfoStoreListener,
//			VOMSProtocolListener protocolListener) {
//
//		this.requestListener = listener;
//		this.validator = validator;
//		this.protocolListener = protocolListener;
//		serverInfoStore = new DefaultVOMSServerInfoStore.Builder().lookupStrategy(lookupStrategy).storeListener(serverInfoStoreListener).build();
//	}
//	
//	public void addVOMSServerInfo(VOMSServerInfo vomsServerInfo) {
//		this.serverInfoStore.addVOMSServerInfo(vomsServerInfo);
//	}
//	
//	/**
//	 * Extracts an AC from a VOMS response
//	 * 
//	 * @param request the request
//	 * @param response the received response
//	 * @return a possibly <code>null</code> {@link AttributeCertificate} object
//	 */
//	protected AttributeCertificate getACFromResponse(VOMSACRequest request, VOMSResponse response){
//		byte[] acBytes = response.getAC();
//
//		if (acBytes == null)
//			return null;
//		
//		ASN1InputStream asn1InputStream = new ASN1InputStream(acBytes);
//
//		AttributeCertificate attributeCertificate = null;
//
//		try {
//			
//			attributeCertificate = AttributeCertificate
//					.getInstance(asn1InputStream.readObject());
//
//			asn1InputStream.close();
//			return attributeCertificate;
//
//		} catch (IOException e) {
//
//			requestListener.notifyVOMSRequestFailure(request, null, e);
//			return null;
//		}
//	}
//	
//	/**
//	 * Executes the request using the VOMS REST protocol
//	 * 
//	 * @param request the request
//	 * @param serverInfo the VOMS server endpoint information
//	 * @param credential the credentials used to authenticate to the server
//	 * @return a {@link VOMSResponse}
//	 */
//	protected VOMSResponse doRESTRequest(VOMSACRequest request, VOMSServerInfo serverInfo, X509Credential credential){
//		
//		RESTProtocol restProtocol = new RESTProtocol(serverInfo, validator, protocolListener, connectTimeout, readTimeout);
//		
//		VOMSResponse response = null;
//		
//		try {
//			response = restProtocol.doRequest(credential, request);
//		
//		}catch(VOMSProtocolError e){
//			requestListener.notifyVOMSRequestFailure(request, serverInfo, e);
//		}catch(VOMSError error){
//			// REST function not available on old VOMS servers.
//			if("Unexpected end of file from server".equals(error.getMessage())){
//				return null;
//			}else{
//				throw error;
//			}
//		}
//		return response;
//		
//	}
//	
//	/**
//	 * Executes the request using the VOMS legacy protocol
//	 * @param request the request
//	 * @param serverInfo the VOMS server endpoint information
//	 * @param credential the credentials used to authenticate to the server
//	 * @return a {@link VOMSResponse}
//	 */
//	protected VOMSResponse doLegacyRequest(VOMSACRequest request, VOMSServerInfo serverInfo, X509Credential credential){
//		VOMSResponse response = null;
//		
//		LegacyProtocol legacyProtocol = new LegacyProtocol(serverInfo, validator, protocolListener, connectTimeout, readTimeout);
//		
//		try{
//			response = legacyProtocol.doRequest(credential, request);
//		
//		}catch (VOMSProtocolError e) {
//			requestListener.notifyVOMSRequestFailure(request, serverInfo, e);
//		}
//		
//		return response;
//	}
//	
//	/**
//	 * Handles errors included in the VOMS response
//	 * @param request the request
//	 * @param si the VOMS server endpoint information
//	 * @param response the received {@link VOMSResponse}
//	 */
//	protected void handleErrorsInResponse(VOMSACRequest request, VOMSServerInfo si, VOMSResponse response){
//		
//		if (response.hasErrors())
//			requestListener.notifyErrorsInVOMSReponse(request, si, response.errorMessages());		
//		
//	}
//	/**
//	 * Handles warnings included in the VOMS response
//	 * @param request the request
//	 * @param si the VOMS server endpoint information
//	 * @param response the received {@link VOMSResponse}
//	 */
//	protected void handleWarningsInResponse(VOMSACRequest request, VOMSServerInfo si, VOMSResponse response){
//		if (response.hasWarnings())
//			requestListener.notifyWarningsInVOMSResponse(request, si, response.warningMessages());
//	}
//	
//	public AttributeCertificate getVOMSAttributeCertificate(
//			X509Credential credential, VOMSACRequest request) {
//
//		Set<VOMSServerInfo> vomsServerInfos = getVOMSServerInfos(request);
//		
//		if (vomsServerInfos.isEmpty())
//			throw new VOMSError("VOMS server for VO "+request.getVoName()+" is not known! Check your vomses configuration.");
//		
//		VOMSResponse response = null;
//
//		for (VOMSServerInfo vomsServerInfo : vomsServerInfos) {
//
//			requestListener.notifyVOMSRequestStart(request,  vomsServerInfo);
//			
//			response = doRESTRequest(request, vomsServerInfo, credential);
//
//			if (response == null)
//				response = doLegacyRequest(request, vomsServerInfo, credential);
//
//			if (response != null){
//				requestListener.notifyVOMSRequestSuccess(request, vomsServerInfo);
//				
//				handleErrorsInResponse(request, vomsServerInfo, response);
//				handleWarningsInResponse(request, vomsServerInfo, response);
//				
//				break;
//			}
//			
//			requestListener.notifyVOMSRequestFailure(request, vomsServerInfo, new VOMSError("REST and legacy VOMS endpoints failed."));
//		}
//
//		if (response == null) {
//			requestListener.notifyVOMSRequestFailure(request, null, null);
//			return null;
//		}
//		
//		return getACFromResponse(request, response);
//	}
//
//	/**
//	 * Get VOMS server endpoint information that matches with the {@link VOMSACRequest} passed
//	 * as argument
//	 * @param request the request
//	 * @return a possibly empty {@link Set} of {@link VOMSServerInfo} objects
//	 */
//	protected Set<VOMSServerInfo> getVOMSServerInfos(VOMSACRequest request) {
//
//		Set<VOMSServerInfo> vomsServerInfos = serverInfoStore
//				.getVOMSServerInfo(request.getVoName());
//
//		return vomsServerInfos;
//	}
//
//	public void setConnectTimeout(int timeout) {
//		connectTimeout = timeout;
//		
//	}
//
//	public void setReadTimeout(int timeout) {
//		readTimeout = timeout;
//		
//	}
//}