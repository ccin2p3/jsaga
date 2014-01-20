package fr.in2p3.jsaga.adaptor.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.bouncycastle.openssl.PasswordFinder;
import org.italiangrid.voms.VOMSError;
import org.italiangrid.voms.VOMSValidators;
import org.italiangrid.voms.ac.VOMSACValidator;
import org.italiangrid.voms.ac.ValidationResultListener;
import org.italiangrid.voms.ac.impl.DefaultVOMSValidator;
import org.italiangrid.voms.clients.ProxyInitParams;
import org.italiangrid.voms.clients.impl.InitListenerAdapter;
import org.italiangrid.voms.clients.impl.LoadProxyCredential;
import org.italiangrid.voms.clients.impl.LoadUserCredential;
import org.italiangrid.voms.clients.impl.ProxyCreationListener;
import org.italiangrid.voms.clients.strategies.ProxyInitStrategy;
import org.italiangrid.voms.clients.strategies.VOMSCommandsParsingStrategy;
import org.italiangrid.voms.clients.util.VOMSProxyPathBuilder;
import org.italiangrid.voms.credential.LoadCredentialsEventListener;
import org.italiangrid.voms.credential.LoadCredentialsStrategy;
import org.italiangrid.voms.credential.VOMSEnvironmentVariables;
import org.italiangrid.voms.credential.impl.DefaultLoadCredentialsStrategy;
import org.italiangrid.voms.request.VOMSACRequest;
import org.italiangrid.voms.request.VOMSACService;
import org.italiangrid.voms.request.VOMSESLookupStrategy;
import org.italiangrid.voms.request.VOMSProtocolListener;
import org.italiangrid.voms.request.VOMSRequestListener;
import org.italiangrid.voms.request.VOMSServerInfoStoreListener;
import org.italiangrid.voms.request.impl.BaseVOMSESLookupStrategy;
import org.italiangrid.voms.request.impl.DefaultVOMSACRequest;
import org.italiangrid.voms.request.impl.DefaultVOMSACService;
import org.italiangrid.voms.request.impl.DefaultVOMSESLookupStrategy;
import org.italiangrid.voms.request.impl.DefaultVOMSServerInfo;
import org.italiangrid.voms.request.impl.DefaultVOMSServerInfoStore;
import org.italiangrid.voms.store.VOMSTrustStore;
import org.italiangrid.voms.store.VOMSTrustStoreStatusListener;
import org.italiangrid.voms.store.impl.DefaultVOMSTrustStore;
import org.italiangrid.voms.util.CertificateValidatorBuilder;
import org.italiangrid.voms.util.CredentialsUtils;
import org.italiangrid.voms.util.FilePermissionHelper;
import org.italiangrid.voms.util.VOMSFQANNamingScheme;

import eu.emi.security.authn.x509.CrlCheckingMode;
import eu.emi.security.authn.x509.NamespaceCheckingMode;
import eu.emi.security.authn.x509.OCSPCheckingMode;
import eu.emi.security.authn.x509.StoreUpdateListener;
import eu.emi.security.authn.x509.ValidationErrorListener;
import eu.emi.security.authn.x509.ValidationResult;
import eu.emi.security.authn.x509.X509CertChainValidatorExt;
import eu.emi.security.authn.x509.X509Credential;
import eu.emi.security.authn.x509.helpers.proxy.ExtendedProxyType;
import eu.emi.security.authn.x509.helpers.proxy.ProxyHelper;
import eu.emi.security.authn.x509.proxy.ProxyCertificate;
import eu.emi.security.authn.x509.proxy.ProxyCertificateOptions;
import eu.emi.security.authn.x509.proxy.ProxyChainInfo;
import eu.emi.security.authn.x509.proxy.ProxyChainType;
import eu.emi.security.authn.x509.proxy.ProxyGenerator;
import eu.emi.security.authn.x509.proxy.ProxyPolicy;
import eu.emi.security.authn.x509.proxy.ProxyType;
import eu.emi.security.authn.x509.proxy.ProxyUtils;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JSAGAVOMSProxyInitBehaviour
 * Author: lionel.schwarz@in2p3.fr
 * Date:   27 nov 2013
 * ***************************************************
 * Description: This class is a clopy of DefaultVOMSProxyInitBehaviour
 * with only patches on lookupCredential() and strategyFromParams() */
public class JSAGAVOMSProxyInitBehaviour implements ProxyInitStrategy {

    private VOMSCommandsParsingStrategy commandsParser;
    private X509CertChainValidatorExt certChainValidator;
    private VOMSACValidator vomsValidator;
    
    
    private ValidationResultListener validationResultListener;
    private VOMSRequestListener requestListener;
    private ProxyCreationListener proxyCreationListener;
    private VOMSServerInfoStoreListener serverInfoStoreListener;
    private LoadCredentialsEventListener loadCredentialsEventListener;
    private ValidationErrorListener certChainValidationErrorListener;
    private VOMSTrustStoreStatusListener vomsTrustStoreListener;
    private StoreUpdateListener storeUpdateListener;
    private VOMSProtocolListener protocolListener;
    
//    public JSAGAVOMSProxyInitBehaviour(VOMSCommandsParsingStrategy commandsParser,
//            ValidationResultListener validationListener,
//            VOMSRequestListener requestListener,
//            ProxyCreationListener pxCreationListener,
//            VOMSServerInfoStoreListener serverInfoStoreListener,
//            LoadCredentialsEventListener loadCredEventListener,
//            ValidationErrorListener certChainListener,
//            VOMSTrustStoreStatusListener vomsTSListener,
//            StoreUpdateListener trustStoreUpdateListener,
//            VOMSProtocolListener protocolListener)
//            {
//        
//        this.commandsParser = commandsParser;
//        this.validationResultListener = validationListener;
//        this.requestListener = requestListener;
//        this.proxyCreationListener = pxCreationListener;
//        this.serverInfoStoreListener = serverInfoStoreListener;
//        this.loadCredentialsEventListener = loadCredEventListener;
//        this.certChainValidationErrorListener = certChainListener;
//        this.vomsTrustStoreListener = vomsTSListener;
//        this.storeUpdateListener = trustStoreUpdateListener;
//        this.protocolListener = protocolListener;
//    }
//
    public JSAGAVOMSProxyInitBehaviour(VOMSCommandsParsingStrategy commandsParser, InitListenerAdapter listenerAdapter){
        this.commandsParser = commandsParser;
        this.validationResultListener = listenerAdapter;
        this.requestListener = listenerAdapter;
        this.proxyCreationListener = listenerAdapter;
        this.serverInfoStoreListener = listenerAdapter;
        this.loadCredentialsEventListener = listenerAdapter;
        this.certChainValidationErrorListener = listenerAdapter;
        this.vomsTrustStoreListener = listenerAdapter;
        this.storeUpdateListener = listenerAdapter;
        this.protocolListener = listenerAdapter;
    }
    
    
    
    protected void validateUserCredential(ProxyInitParams params, X509Credential cred){
        
        ValidationResult result = certChainValidator.validate(cred.getCertificateChain());
        if (!result.isValid())
            throw new VOMSError("User credential is not valid!");
        
    }
    
    
    private void init(ProxyInitParams params){
        
        boolean hasVOMSCommands = params.getVomsCommands() != null 
                && !params.getVomsCommands().isEmpty();
        
        if (hasVOMSCommands)
            params.setValidateUserCredential(true);
        
        if (params.validateUserCredential() || hasVOMSCommands)            
            initCertChainValidator(params);
        
        if (params.verifyAC())
            initVOMSValidator(params);
            
    }
    
    public void initProxy(ProxyInitParams params) {
        
        init(params);
        
        X509Credential cred = lookupCredential(params);
        if (cred == null)
            throw new VOMSError("No credentials found!");

        if (params.validateUserCredential())    
            validateUserCredential(params, cred);
        
        List<AttributeCertificate> acs = Collections.emptyList();
        
        if (params.getVomsCommands() != null && !params.getVomsCommands().isEmpty()){
            initCertChainValidator(params);
            acs = getAttributeCertificates(params, cred);
        }

        if (params.verifyAC() && !acs.isEmpty())
            verifyACs(params, acs);
        
        createProxy(params, cred, acs);
    }
    
    private void directorySanityChecks(String dirPath, String preambleMessage){
        
        File f = new File(dirPath);
        
        String errorTemplate = String.format("%s: '%s'", preambleMessage, dirPath);
        errorTemplate = errorTemplate +" (%s)";
        
        if (!f.exists()){
            Throwable t = new FileNotFoundException(String.format(errorTemplate, "file not found"));
            throw new VOMSError(t.getMessage(), t);
        }
        
        if (!f.isDirectory()){
            throw new VOMSError(String.format(errorTemplate, "not a directory"));
        }
        
        if (!f.canRead())
            throw new VOMSError(String.format(errorTemplate, "not readable"));            
    }
    
    private void initCertChainValidator(ProxyInitParams params){
        
        if (certChainValidator == null){
            String trustAnchorsDir = DefaultVOMSValidator.DEFAULT_TRUST_ANCHORS_DIR;
        
            if (System.getenv(VOMSEnvironmentVariables.X509_CERT_DIR) != null)
                trustAnchorsDir = System.getenv(VOMSEnvironmentVariables.X509_CERT_DIR);
            
            if (params.getTrustAnchorsDir()!=null)
                trustAnchorsDir = params.getTrustAnchorsDir();
            
            directorySanityChecks(trustAnchorsDir, "Invalid trust anchors location");
        
            certChainValidator = CertificateValidatorBuilder.buildCertificateValidator(trustAnchorsDir, 
                    certChainValidationErrorListener, storeUpdateListener, 0L, 
                    CertificateValidatorBuilder.DEFAULT_NS_CHECKS,
                    CrlCheckingMode.IF_VALID, 
                    CertificateValidatorBuilder.DEFAULT_OCSP_CHECKS);
            
        }
    }
    
    private VOMSACValidator initVOMSValidator(ProxyInitParams params){
        
        if (vomsValidator != null)
            return vomsValidator;
        
        String vomsdir = DefaultVOMSTrustStore.DEFAULT_VOMS_DIR;
        
        if (System.getenv(VOMSEnvironmentVariables.X509_VOMS_DIR) != null)
            vomsdir = System.getenv(VOMSEnvironmentVariables.X509_VOMS_DIR);
        
        if (params.getVomsdir() != null)
            vomsdir = params.getVomsdir();
        
        directorySanityChecks(vomsdir, "Invalid vomsdir location");
        
        VOMSTrustStore trustStore = new DefaultVOMSTrustStore(Arrays.asList(vomsdir)
                , vomsTrustStoreListener);
        
        vomsValidator = VOMSValidators.newValidator(trustStore, 
                certChainValidator, 
                validationResultListener); 
        
        return vomsValidator;
    }
    
    private void verifyACs(ProxyInitParams params, List<AttributeCertificate> acs) {
        
        VOMSACValidator acValidator = initVOMSValidator(params);
        acValidator.validateACs(acs);
        
    }

    // Why we have to do this nonsense?
    private ProxyType extendedProxyTypeAsProxyType(ExtendedProxyType pt){
        switch(pt){
        
        case DRAFT_RFC:
            return ProxyType.DRAFT_RFC;
            
        case LEGACY:
            return ProxyType.LEGACY;
            
        case RFC3820:
            return ProxyType.RFC3820;
            
        default:
            return null;
        }
    }
    
    private void ensureProxyTypeIsCompatibleWithIssuingCredential(ProxyCertificateOptions options, 
            X509Credential issuingCredential,
            List<String> proxyCreationWarnings){
        
        if (ProxyUtils.isProxy(issuingCredential.getCertificateChain())){
            
            ProxyType issuingProxyType = extendedProxyTypeAsProxyType(ProxyHelper.getProxyType(issuingCredential.getCertificateChain()[0]));
            
            if (!issuingProxyType.equals(options.getType())){
                proxyCreationWarnings.add("forced "+issuingProxyType.name()+" proxy type to be compatible with the type of the issuing proxy.");
                options.setType(issuingProxyType);
            }
            
            try {
                
                boolean issuingProxyIsLimited = ProxyHelper.isLimited(issuingCredential.getCertificateChain()[0]);
                if (issuingProxyIsLimited && !options.isLimited()){
                    proxyCreationWarnings.add("forced the creation of a limited proxy to be compatible with the type of the issuing proxy.");
                    limitProxy(options);
                }
                
            } catch (IOException e) {
                throw new VOMSError(e.getMessage(),e);
            }
        }
    }
    private void checkMixedProxyChain(X509Credential issuingCredential){
        
        if (ProxyUtils.isProxy(issuingCredential.getCertificateChain())){
        
            ProxyChainInfo ci;
            try {
                ci = new ProxyChainInfo(issuingCredential.getCertificateChain());
                if (ci.getProxyType().equals(ProxyChainType.MIXED))
                    throw new VOMSError("Cannot generate a proxy certificate starting from a mixed type proxy chain.");
                
            } catch (CertificateException e) {
                throw new VOMSError(e.getMessage(), e);
            }
        }
    }
    
    private void ensureProxyLifetimeIsConsistentWithIssuingCredential(ProxyCertificateOptions options, 
            X509Credential issuingCredential,
            List<String> proxyCreationWarnings){
        
        Calendar cal = Calendar.getInstance();
        
        // LSZ set starttime 5 min before current time
        cal.add(Calendar.MINUTE, -5);
        Date proxyStartTime = cal.getTime();
        
        // LSZ reset cal to compute end date
        cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, options.getLifetime());
        
        Date proxyEndTime = cal.getTime();
        Date issuingCredentialEndTime = issuingCredential.getCertificate().getNotAfter();
        
        options.setValidityBounds(proxyStartTime, proxyEndTime);
        
        if ( proxyEndTime.after(issuingCredentialEndTime) ){
            
            proxyCreationWarnings.add("proxy lifetime limited to issuing " +
                    "credential lifetime.");
            options.setValidityBounds(proxyStartTime, issuingCredentialEndTime);
        }    
    }
    
    private void limitProxy(ProxyCertificateOptions proxyOptions){
        
        proxyOptions.setLimited(true);
        
        if (proxyOptions.getType().equals(ProxyType.RFC3820)|| proxyOptions.getType().equals(ProxyType.DRAFT_RFC))
            proxyOptions.setPolicy(new ProxyPolicy(ProxyPolicy.LIMITED_PROXY_OID));
        
    }
    
    private void  createProxy(ProxyInitParams params,
            X509Credential credential, List<AttributeCertificate> acs) {
        
        List<String> proxyCreationWarnings = new ArrayList<String>();
        
        // LS does not work on windows
        // FIXME: remove when voms-api works on windows
        /*
        String proxyFilePath = VOMSProxyPathBuilder.buildProxyPath();
        
        String envProxyPath = System.getenv(VOMSEnvironmentVariables.X509_USER_PROXY);

        if (envProxyPath != null)
            proxyFilePath = envProxyPath;
        
        if (params.getGeneratedProxyFile() != null)
            proxyFilePath = params.getGeneratedProxyFile();
        */
        String proxyFilePath =params.getGeneratedProxyFile();
        // END LS fix me
        
        ProxyCertificateOptions proxyOptions = new ProxyCertificateOptions(credential.getCertificateChain());
        
        proxyOptions.setProxyPathLimit(params.getPathLenConstraint());
        
        proxyOptions.setLimited(params.isLimited());
        proxyOptions.setLifetime(params.getProxyLifetimeInSeconds());
        proxyOptions.setType(params.getProxyType());
        proxyOptions.setKeyLength(params.getKeySize());
        
        if (params.isEnforcingChainIntegrity()){
            
            checkMixedProxyChain(credential);
        
            ensureProxyTypeIsCompatibleWithIssuingCredential(proxyOptions, 
                    credential, proxyCreationWarnings);
        
            ensureProxyLifetimeIsConsistentWithIssuingCredential(proxyOptions, 
                    credential, proxyCreationWarnings);
        }
        
        if (params.isLimited())
            limitProxy(proxyOptions);
        
        if (acs != null && !acs.isEmpty())
            proxyOptions.setAttributeCertificates(acs.toArray(new AttributeCertificate[acs.size()]));
        
        try {
            
            ProxyCertificate proxy = ProxyGenerator.generate(proxyOptions, credential.getKey());
            
            // Lionel Schwarz 13/01: saveProxyPermissions not available on Windows
            // FIXME remove this bloc when voms-api works on windows
//            CredentialsUtils.saveProxyCredentials(proxyFilePath, proxy.getCredential());
            File f = new File(proxyFilePath);
            RandomAccessFile raf = new RandomAccessFile(f, "rws");
            FileChannel channel = raf.getChannel();
            if (!System.getProperty("os.name").startsWith("Windows")) {
                FilePermissionHelper.setProxyPermissions(proxyFilePath);
            }
            
            channel.truncate(0);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CredentialsUtils.saveProxyCredentials(baos, proxy.getCredential(), CredentialsUtils.DEFAULT_ENCONDING);
            
            baos.close();
            channel.write(ByteBuffer.wrap(baos.toByteArray()));
            
            channel.close();        
            raf.close();
            // End LS fix me
            proxyCreationListener.proxyCreated(proxyFilePath, proxy, proxyCreationWarnings);
        
        } catch (Throwable t) {
            
            throw new VOMSError("Error creating proxy certificate: "+t.getMessage(), t);
        }
    }

    protected List<String> sortFQANsIfRequested(ProxyInitParams params, List<String> unsortedFQANs){
        
        if (params.getFqanOrder() != null && !params.getFqanOrder().isEmpty()){
            
            Set<String> fqans = new LinkedHashSet<String>();
            for (String fqan: params.getFqanOrder()){
                
                if (VOMSFQANNamingScheme.isGroup(fqan))
                    fqans.add(fqan);
                
                if (VOMSFQANNamingScheme.isQualifiedRole(fqan) && unsortedFQANs.contains(fqan))
                    fqans.add(fqan);
                
            }
            
            fqans.addAll(unsortedFQANs);
            
            return new ArrayList<String>(fqans);
        }
        
        return unsortedFQANs;
    }

    protected VOMSESLookupStrategy getVOMSESLookupStrategyFromParams(ProxyInitParams params){
        
        if (params.getVomsesLocations() != null && ! params.getVomsesLocations().isEmpty())
            return new BaseVOMSESLookupStrategy(params.getVomsesLocations());
        else
            return new DefaultVOMSESLookupStrategy();
        
    }
    
    protected List<AttributeCertificate> getAttributeCertificates(
            ProxyInitParams params, X509Credential cred) {

        List<String> vomsCommands = params.getVomsCommands();

        if (vomsCommands == null || vomsCommands.isEmpty())
            return Collections.emptyList();

        Map<String, List<String>> vomsCommandsMap = commandsParser
                .parseCommands(params.getVomsCommands());

        List<AttributeCertificate> acs = new ArrayList<AttributeCertificate>();

        for (String vo : vomsCommandsMap.keySet()) {

            List<String> fqans = vomsCommandsMap.get(vo);
            
            VOMSACRequest request = new DefaultVOMSACRequest.Builder(vo)
                .fqans(sortFQANsIfRequested(params, fqans))
                .targets(params.getTargets())
                .lifetime(params.getAcLifetimeInSeconds())
                .build();
            
//            VOMSACService acService = new DefaultVOMSACService.Builder(certChainValidator)
//                    .requestListener(requestListener)
//                    .vomsesLookupStrategy(getVOMSESLookupStrategyFromParams(params))
//                    .serverInfoStoreListener(serverInfoStoreListener)
//                    .protocolListener(protocolListener)
//                    .connectTimeout((int)TimeUnit.SECONDS.toMillis(params.getTimeoutInSeconds()))
//                    .readTimeout((int)TimeUnit.SECONDS.toMillis(params.getTimeoutInSeconds()))
//                    .build();
            DefaultVOMSACService.Builder builder = new DefaultVOMSACService.Builder(certChainValidator)
                .requestListener(requestListener)
                .vomsesLookupStrategy(getVOMSESLookupStrategyFromParams(params))
                .serverInfoStoreListener(serverInfoStoreListener)
                .protocolListener(protocolListener)
                .connectTimeout((int)TimeUnit.SECONDS.toMillis(params.getTimeoutInSeconds()))
                .readTimeout((int)TimeUnit.SECONDS.toMillis(params.getTimeoutInSeconds()));
            // Patch from lionel.schwarz@in2p3.fr : support server name and VO in config
            if (((JSAGAProxyInitParams)params).getServer() != null){
                String serverUrl = ((JSAGAProxyInitParams)params).getServer();
                URI uri;
                try {
                    uri = new URI(serverUrl.replaceAll(" ", "%20"));
                } catch (URISyntaxException e) {
                    throw new VOMSError("Unable to build URI: " + serverUrl);
                }
                if (uri.getHost() == null) {
                    throw new VOMSError("Attribute Server has no host name: " + uri.toString());
                }
                DefaultVOMSServerInfo server = new DefaultVOMSServerInfo();
                server.setURL(uri);
                server.setVOMSServerDN(uri.getPath());
                server.setVoName(((JSAGAProxyInitParams)params).getVOName());
                DefaultVOMSServerInfoStore sis = new DefaultVOMSServerInfoStore.Builder().build();
                sis.addVOMSServerInfo(server);
                builder.serverInfoStore(sis);
            }
            VOMSACService acService = builder.build();
            
            AttributeCertificate ac = acService.getVOMSAttributeCertificate(cred, request);

            if (ac != null)
                acs.add(ac);
        }

        if (!vomsCommandsMap.keySet().isEmpty() && acs.isEmpty())
            throw new VOMSError("User's request for VOMS attributes could not be fulfilled.");
        
        return acs;
    }

    
    private LoadCredentialsStrategy strategyFromParams(ProxyInitParams params){
        
        // Patch from lionel.schwarz@in2p3.fr: need certFile
        if (params.isNoRegen() && params.getCertFile()!=null)
            // FIXME: use LoadProxyCredential when voms-api-java works on Windos
            return new JSAGALoadProxyCredential(loadCredentialsEventListener, params.getCertFile());
        
        if (params.getCertFile()!=null && params.getKeyFile() == null)
            // FIXME: use LoadUserCredential when voms-api-java works on Windos
            return new JSAGALoadUserCredential(loadCredentialsEventListener, params.getCertFile());
        
        if (params.getCertFile()!=null && params.getKeyFile()!=null)
            // FIXME: use LoadUserCredential when voms-api-java works on Windos
            return new JSAGALoadUserCredential(loadCredentialsEventListener, params.getCertFile(), params.getKeyFile());
        
        return new DefaultLoadCredentialsStrategy(System.getProperty(DefaultLoadCredentialsStrategy.HOME_PROPERTY),
                    DefaultLoadCredentialsStrategy.TMPDIR_PROPERTY,
                    loadCredentialsEventListener);
        
    }
    
    private X509Credential lookupCredential(ProxyInitParams params) {

        PasswordFinder pf = null;

        // Patch from lionel.schwarz@in2p3.fr: read password from config
        pf = ((JSAGAProxyInitParams)params).getPasswordFinder();
        LoadCredentialsStrategy loadCredStrategy = strategyFromParams(params);
        
        return loadCredStrategy.loadCredentials(pf);
    }

}
