package fr.in2p3.jsaga.adaptor.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

import org.globus.common.CoGProperties;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.Util;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.italiangrid.voms.asn1.VOMSACUtils;
import org.italiangrid.voms.clients.impl.DefaultVOMSCommandsParser;
import org.italiangrid.voms.clients.strategies.ProxyInitStrategy;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UDuration;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.UFilePath;
import fr.in2p3.jsaga.adaptor.base.usage.UHidden;
import fr.in2p3.jsaga.adaptor.base.usage.UNoPrompt;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.UOr;
import fr.in2p3.jsaga.adaptor.base.usage.UProxyValue;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityCredential;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   VOMSSecurityAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Author: lionel.schwarz@in2p3.fr
 * Date:   27 nov 2013
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class VOMSSecurityAdaptor implements ExpirableSecurityAdaptor {
    protected static final int USAGE_INIT_PKCS12 = 1;
    protected static final int USAGE_INIT_PEM = 2;
    protected static final int USAGE_MEMORY = 3;
    protected static final int USAGE_LOAD = 4;
    protected static final int USAGE_INIT_PROXY = 5;
    public static final String DEFAULT_LIFETIME = "PT12H";

    public String getType() {
        return "VOMS";
    }

    public Class getSecurityCredentialClass() {
        return VOMSSecurityCredential.class;
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{
                        new UAnd(new Usage[]{
                                new UOr(new Usage[]{
                                        new UFilePath(USAGE_INIT_PKCS12, VOMSContext.USERCERTKEY),
                                        new UAnd(USAGE_INIT_PEM, new Usage[]{new UFile(Context.USERCERT), new UFile(Context.USERKEY)})
                                }),
                                new UFilePath(Context.USERPROXY), new UHidden(Context.USERPASS),
                                new U(Context.SERVER), new U(Context.USERVO), new UOptional(VOMSContext.USERFQAN),
                                new UDuration(Context.LIFETIME),
                                new UOptional(VOMSContext.DELEGATION) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        if (super.throwExceptionIfInvalid(value) != null) {
                                            String v = (String) value;
                                            if (!v.equalsIgnoreCase("none") && !v.equalsIgnoreCase("limited") && !v.equalsIgnoreCase("full")) {
                                                throw new BadParameterException("Expected: none | limited | full");
                                            }
                                        }
                                        return value;
                                    }
                                },
                                new UOptional(VOMSContext.PROXYTYPE) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        if (super.throwExceptionIfInvalid(value) != null) {
                                            String v = (String) value;
                                            if (!v.equalsIgnoreCase("old") && !v.equalsIgnoreCase("globus") && !v.equalsIgnoreCase("RFC3820")) {
                                                throw new BadParameterException("Expected: old | globus | RFC3820");
                                            }
                                        }
                                        return value;
                                    }
                                }
                        }),
                        new UNoPrompt(USAGE_MEMORY, VOMSContext.USERPROXYOBJECT),
                        new UOr(new Usage[]{
                                new UFilePath(USAGE_INIT_PROXY, VOMSContext.INITIALPROXY),
                        		new UFile(USAGE_LOAD, Context.USERPROXY),
                        		new UProxyValue(USAGE_LOAD,  Context.USERPROXY)
                        })
                }),
                new UFile(Context.CERTREPOSITORY),
                new UFile(VOMSContext.VOMSDIR),
                new UFile(VOMSContext.VOMSES)
        });
    }

    public Default[] getDefaults(Map map) throws IncorrectStateException {
        EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
                new Default(Context.USERPROXY, new String[]{
                        env.getProperty("X509_USER_PROXY"),
                        System.getProperty("java.io.tmpdir")+System.getProperty("file.separator")+"x509up_u"+
                                (System.getProperty("os.name").toLowerCase().startsWith("windows")
                                        ? "_"+System.getProperty("user.name").toLowerCase()
                                        : (env.getProperty("UID")!=null
                                                ? env.getProperty("UID")
                                                : getUnixUID()
                                          )
                                )}),
                new Default(Context.USERCERT, new File[]{
                        new File(env.getProperty("X509_USER_CERT")+""),
                        new File(System.getProperty("user.home")+"/.globus/usercert.pem")}),
                new Default(Context.USERKEY, new File[]{
                        new File(env.getProperty("X509_USER_KEY")+""),
                        new File(System.getProperty("user.home")+"/.globus/userkey.pem")}),
                new Default(Context.CERTREPOSITORY, new File[]{
                        new File(env.getProperty("CADIR")+""),
                        new File(env.getProperty("X509_CERT_DIR")+""),
                        new File(System.getProperty("user.home")+"/.globus/certificates/"),
                        new File("/etc/grid-security/certificates/")}),
                new Default(VOMSContext.VOMSDIR, new File[]{
                        new File(env.getProperty("X509_VOMS_DIR")+""),
                        new File(System.getProperty("user.home")+"/.globus/vomsdir/"),
                        new File("/etc/grid-security/vomsdir/")}),
                new Default(VOMSContext.VOMSES, new File[]{
                        new File(System.getProperty("user.home")+"/.glite/vomses/"),
                        new File("/etc/vomses/")}),
                // TODO: remove VomsesFile
                new Default(Context.SERVER, new VomsesFile().getDefaultServer()),
                new Default(Context.USERVO, new VomsesFile().getDefaultVO()),
                new Default(Context.LIFETIME, DEFAULT_LIFETIME),
                // TODO: use constants
                new Default(VOMSContext.PROXYTYPE, "RFC3820")
        };
    }
    protected static String getUnixUID() throws IncorrectStateException {
        try {
            Process p = Runtime.getRuntime().exec("id -u");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String uid = reader.readLine();
            reader.close();
            return uid;
        } catch (IOException e) {
            throw new IncorrectStateException(e);
        }
    }

    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            System.out.println("Usage: " + usage);
            switch(usage) {
                case USAGE_INIT_PKCS12:
                case USAGE_INIT_PEM:
                case USAGE_INIT_PROXY:
                {
                    JSAGAProxyInitParams params = new JSAGAProxyInitParams();
                    if (usage == USAGE_INIT_PROXY) {
                        params.setCertFile((String)attributes.get(VOMSContext.INITIALPROXY));
                        params.setNoRegen(true);
                    } else {
                        params.setNoRegen(false);
                        params.setUserPass((String)attributes.get(Context.USERPASS));
                        if (usage == USAGE_INIT_PKCS12) {
                            params.setCertFile((String)attributes.get(VOMSContext.USERCERTKEY));
                        } else if (usage == USAGE_INIT_PEM) {
                            params.setCertFile((String)attributes.get(Context.USERCERT));
                            params.setKeyFile((String)attributes.get(Context.USERKEY));
                        }
                    }
                    
                    params.setGeneratedProxyFile((String)attributes.get(Context.USERPROXY));
                    // TODO: handle Proxy Type
//                  params.setProxyType(ProxyType.RFC3820);
                    params.setVomsdir((String) attributes.get(VOMSContext.VOMSDIR));
                    params.setTrustAnchorsDir((String) attributes.get(Context.CERTREPOSITORY));
                    
                    // TODO: is this useful?
                    params.setVerifyAC(true);
                    params.setVomsCommands(Arrays.asList((String) attributes.get(Context.USERVO)));
                    params.setVomsesLocations(Arrays.asList((String) attributes.get(VOMSContext.VOMSES)));
                    VOMSProxyListener creation_listener = new VOMSProxyListener();

                    ProxyInitStrategy proxyInitBehaviour = 
                            new JSAGAVOMSProxyInitBehaviour(new DefaultVOMSCommandsParser(), creation_listener);
                    proxyInitBehaviour.initProxy(params);

                    return this.createSecurityAdaptor(creation_listener.getProxy(), attributes);
                }
                case USAGE_MEMORY:
                {
                    String base64 = (String) attributes.get(VOMSContext.USERPROXYOBJECT);
                    GSSCredential cred = InMemoryProxySecurityCredential.toGSSCredential(base64);
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_LOAD:
                {
                    CoGProperties.getDefault().setCaCertLocations((String) attributes.get(Context.CERTREPOSITORY));
                    String userProxy = (String) attributes.get(Context.USERPROXY);
                    GSSCredential cred = null;
                    if(userProxy.startsWith("-----")){
                    	//Proxy value
                    	cred = load(userProxy);
                    }else{
                    	//Proxy File
                    	File proxyFile = new File(userProxy);
                        cred = load(proxyFile);
                    }
                    return this.createSecurityAdaptor(cred, attributes);
                }
                default:
                    throw new NoSuccessException("INTERNAL ERROR: unexpected exception");
            }
        } catch(IncorrectStateException e) {
            throw e;
        } catch(NoSuccessException e) {
            throw e;
        } catch(Exception e) {
            throw new NoSuccessException(e);
        }
    }
    protected SecurityCredential createSecurityAdaptor(GSSCredential cred, Map attributes) throws IncorrectStateException {
        if (cred instanceof GlobusGSSCredentialImpl) {
            X509Credential globusProxy = ((GlobusGSSCredentialImpl)cred).getX509Credential();
	        try {
                if (!VOMSACUtils.getACsFromCertificate(globusProxy.getCertificateChain()[0]).isEmpty()) {
				    return new VOMSSecurityCredential(cred, attributes);
				} else {
				    throw new IncorrectStateException("Security context is not of type: "+this.getType());
				}
			} catch (IOException e) {
				throw new IncorrectStateException("Unable to determine if the provided GSSCredentialis a VOMS certificate or not", e);
			}
        }else{
        	throw new IncorrectStateException("The provided GSSCredential is not instance of GlobusGSSCredentialImpl");
        }
    }

    public void destroySecurityAdaptor(Map attributes, String contextId) throws Exception {
        String proxyFile = (String) attributes.get(Context.USERPROXY);
        Util.destroy(proxyFile);
    }

    protected static GSSCredential load(String proxyValue) throws IOException, GSSException {
        return realLoad(proxyValue.getBytes());
    }
    
    protected static GSSCredential load(File proxyFile) throws IOException, GSSException {
        byte [] proxyBytes = new byte[(int) proxyFile.length()];
        FileInputStream in = new FileInputStream(proxyFile);
        in.read(proxyBytes);
        in.close();
        return realLoad(proxyBytes);
    }
        
   private static GSSCredential realLoad(byte[] proxyBytes) throws IOException, GSSException {
        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        return manager.createCredential(
                proxyBytes,
                ExtendedGSSCredential.IMPEXP_OPAQUE,
                GSSCredential.DEFAULT_LIFETIME,
                null, // use default mechanism: GSI
                GSSCredential.INITIATE_AND_ACCEPT);
    }
}
