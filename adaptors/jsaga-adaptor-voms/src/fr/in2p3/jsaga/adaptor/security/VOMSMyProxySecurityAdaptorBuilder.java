package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.globus.util.Util;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.*;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VOMSMyProxySecurityAdaptorBuilder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VOMSMyProxySecurityAdaptorBuilder extends VOMSSecurityAdaptorBuilder implements ExpirableSecurityAdaptorBuilder {
    private static final int USAGE_RENEW_MEMORY_WITH_PASSPHRASE = 5; // 1 to 4 reserved by super class
    private static final int USAGE_RENEW_LOAD_WITH_PASSPHRASE = 6;
    private static final int DEFAULT_STORED_PROXY_LIFETIME = 7*12*3600;
    private static final int DEFAULT_DELEGATED_PROXY_LIFETIME = 12*3600;

    public String getType() {
        return "VOMSMyProxy";
    }

    public Class getSecurityAdaptorClass() {
        return VOMSMyProxySecurityAdaptor.class;
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{
                        new UAnd(new Usage[]{
                                new UOr(new Usage[]{
                                        new UFile(USAGE_INIT_PKCS12, VOMSContext.USERCERTKEY),
                                        new UAnd(USAGE_INIT_PEM, new Usage[]{new UFile(Context.USERCERT), new UFile(Context.USERKEY)})
                                }),
                                new UFilePath(Context.USERPROXY), new UHidden(Context.USERPASS),
                                new U(Context.SERVER), new U(Context.USERVO), new UOptional(VOMSContext.USERFQAN),
                                new UDuration(Context.LIFETIME) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                                    }
                                },
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
                                            if (!v.equalsIgnoreCase("old") && !v.equalsIgnoreCase("globus") && !v.equalsIgnoreCase("RFC820")) {
                                                throw new BadParameterException("Expected: old | globus | RFC820");
                                            }
                                        }
                                        return value;
                                    }
                                }
                        }),

                        // get proxy from server with passphrase
                        new UAnd(USAGE_RENEW_MEMORY_WITH_PASSPHRASE, new Usage[]{
                                new UNoPrompt(VOMSContext.USERPROXYOBJECT),
                                new UDuration(VOMSContext.DELEGATIONLIFETIME)
                        }),
                        new UAnd(USAGE_RENEW_LOAD_WITH_PASSPHRASE, new Usage[]{
                                new U(Context.USERPROXY),
                                new UDuration(VOMSContext.DELEGATIONLIFETIME)
                        }),

                        // local proxy
                        new UNoPrompt(USAGE_MEMORY, VOMSContext.USERPROXYOBJECT),
                        new UFile(USAGE_LOAD, Context.USERPROXY)

                        // get proxy from server with old proxy
/*
                        new UProxyObject(USAGE_RENEW_MEMORY_WITH_PROXY, GlobusContext.USERPROXYOBJECT, MIN_LIFETIME_FOR_RENEW),
                        new UProxyFile(USAGE_RENEW_LOAD_WITH_PROXY, Context.USERPROXY, MIN_LIFETIME_FOR_RENEW)
*/
                }),
                new UFile(Context.CERTREPOSITORY),
                new UFile(VOMSContext.VOMSDIR),
                new U(VOMSContext.MYPROXYSERVER),
                new U(VOMSContext.MYPROXYUSERID),
                new UHidden(VOMSContext.MYPROXYPASS),
        });
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return super.getDefaults(attributes);
    }

    public SecurityAdaptor createSecurityAdaptor(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
        try {
            switch(usage) {
                case USAGE_INIT_PKCS12:
                case USAGE_INIT_PEM:
                {
                    // create local temporary proxy
                    String tempFile = File.createTempFile("vomsmyproxy", "txt").getAbsolutePath();
                    String oldUserProxy = (String) attributes.put(Context.USERPROXY, tempFile);
                    String oldLifeTime = (String) attributes.put(Context.LIFETIME, "PT12H");
                    VOMSSecurityAdaptor adaptor = (VOMSSecurityAdaptor) super.createSecurityAdaptor(usage, attributes, contextId);
                    attributes.put(Context.USERPROXY, oldUserProxy);
                    attributes.put(Context.LIFETIME, oldLifeTime);
                    GSSCredential cred = adaptor.getGSSCredential();

                    // send it to MyProxy server
                    storeCredential(cred, attributes);

                    // destroy local temporary proxy
                    Util.destroy(tempFile);

                    // returns
                    return new VOMSMyProxySecurityAdaptor(cred);
                }
                case USAGE_MEMORY:
                case USAGE_LOAD:
                {
                    // creates a VOMSMyProxySecurityAdaptor
                    return super.createSecurityAdaptor(usage, attributes, contextId);
                }
                case USAGE_RENEW_MEMORY_WITH_PASSPHRASE:
                {
                    GSSCredential cred = renewCredential(null, attributes);
//                    GSSCredential resignedCred = new VOMSProxyFactory(attributes, cred).createProxy();
                    return new VOMSMyProxySecurityAdaptor(cred);
                }
                case USAGE_RENEW_LOAD_WITH_PASSPHRASE:
                {
                    GSSCredential cred = renewCredential(null, attributes);
//                    GSSCredential resignedCred = new VOMSProxyFactory(attributes, cred).createProxy();
                    save(new File((String) attributes.get(Context.USERPROXY)), cred);
                    return new VOMSMyProxySecurityAdaptor(cred);
                }
/*
                case USAGE_RENEW_MEMORY_WITH_PROXY:
                {
                    GSSCredential oldCred = (GSSCredential) attributes.get(GlobusContext.USERPROXYOBJECT);
                    GSSCredential cred = renewCredential(oldCred, attributes);
                    return new VOMSMyProxySecurityAdaptor(cred);
                }
                case USAGE_RENEW_LOAD_WITH_PROXY:
                {
                    GSSCredential oldCred = load(new File((String) attributes.get(Context.USERPROXY)));
                    GSSCredential cred = renewCredential(oldCred, attributes);
                    save(new File((String) attributes.get(Context.USERPROXY)), cred);
                    return new VOMSMyProxySecurityAdaptor(cred);
                }
*/
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
    protected SecurityAdaptor createSecurityAdaptor(GSSCredential cred) {
        return new VOMSMyProxySecurityAdaptor(cred);
    }

    public void destroySecurityAdaptor(Map attributes, String contextId) throws Exception {
        // get attributes
        GSSCredential cred = load(new File((String) attributes.get(Context.USERPROXY)));
        String myProxyServer = (String) attributes.get(VOMSContext.MYPROXYSERVER);
        String myProxyUserId = (String) attributes.get(VOMSContext.MYPROXYUSERID);
        String myProxyPass = (String) attributes.get(VOMSContext.MYPROXYPASS);

        // destroy remote proxy
        MyProxy server = getServer(myProxyServer);
        server.destroy(cred, myProxyUserId, myProxyPass);

        // destroy local proxy
        Util.destroy((String) attributes.get(Context.USERPROXY));
    }
    private static MyProxy getServer(String server) {
        String[] array = (server).split(":");
        String host = array[0];
        int port = (array.length>1 ? Integer.parseInt(array[1]) : MyProxy.DEFAULT_PORT);
        MyProxy myProxy = new MyProxy(host, port);
/*
        String subjectDN = null;
        if (subjectDN != null) {
            myProxy.setAuthorization(new IdentityAuthorization(subjectDN));
        }
*/
        return myProxy;
    }

    private static void storeCredential(GSSCredential newCred, Map attributes) throws ParseException, MyProxyException {
        String myProxyUserId = (String) attributes.get(VOMSContext.MYPROXYUSERID);
        String myProxyPass = (String) attributes.get(VOMSContext.MYPROXYPASS);
        int storedLifetime = attributes.containsKey(Context.LIFETIME)
                ? UDuration.toInt(attributes.get(Context.LIFETIME))
                : DEFAULT_STORED_PROXY_LIFETIME;  // default lifetime for stored proxies
        MyProxy server = getServer((String) attributes.get(VOMSContext.MYPROXYSERVER));
        server.put(newCred, myProxyUserId, myProxyPass, storedLifetime);
    }
    private static GSSCredential renewCredential(GSSCredential oldCred, Map attributes) throws ParseException, URISyntaxException, MyProxyException {
        String myProxyUserId = (String) attributes.get(VOMSContext.MYPROXYUSERID);
        String myProxyPass = (String) attributes.get(VOMSContext.MYPROXYPASS);
        int delegatedLifetime = attributes.containsKey(Context.LIFETIME)
                ? UDuration.toInt(attributes.get(Context.LIFETIME))
                : DEFAULT_DELEGATED_PROXY_LIFETIME;  // effective lifetime for delegated proxy
        MyProxy server = getServer((String) attributes.get(VOMSContext.MYPROXYSERVER));
        return server.get(oldCred, myProxyUserId, myProxyPass, delegatedLifetime);
    }

    private static void save(File proxyFile, GSSCredential cred) throws GSSException, IOException {
        byte[] proxyBytes = ((ExtendedGSSCredential) cred).export(ExtendedGSSCredential.IMPEXP_OPAQUE);
        FileOutputStream out = new FileOutputStream(proxyFile);
        out.write(proxyBytes);
        out.close();
    }
}
