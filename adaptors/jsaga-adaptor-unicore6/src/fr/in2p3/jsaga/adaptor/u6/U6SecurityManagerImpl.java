package fr.in2p3.jsaga.adaptor.u6;

import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.intel.gpe.client2.StandaloneClient;
import com.intel.gpe.client2.adapters.MessageAdapter;
import com.intel.gpe.client2.defaults.IPreferences;
import com.intel.gpe.client2.security.GPESecurityManager;
import com.intel.gpe.clients.api.ClientFactory;
import com.intel.gpe.clients.api.GridBeanClient;
import com.intel.gpe.clients.api.RegistryClient;
import com.intel.gpe.clients.api.TargetSystemFactoryClient;
import com.intel.gpe.clients.api.WSRFClient;
import com.intel.gpe.clients.api.cache.ICache;
import com.intel.gpe.clients.api.gpe.GPETargetSystemFactoryClient;
import com.intel.gpe.clients.api.virtualworkspace.GPEVirtualTargetSystemFactoryClient;
import com.intel.gpe.clients.gpe.Cache;
import com.intel.gpe.clients.gpe.DefaultProperties;
import com.intel.gpe.clients.gpe4gtk.rms.ResourceManagementServiceClient;
import com.intel.gpe.clients.impl.HandlerProvider;
import com.intel.gpe.clients.impl.WSRFClientImpl;
import com.intel.gpe.clients.impl.fts.RandomByteIOFileTransferClientImpl;
import com.intel.gpe.clients.impl.gbs.GridBeanClientImpl;
import com.intel.gpe.clients.impl.jms.workflow.GPERandomByteIODataStagingSetup;
import com.intel.gpe.clients.impl.registry.RegistryClientImpl;
import com.intel.gpe.clients.impl.tsf.TargetSystemFactoryClientImpl;
import com.intel.gpe.wsclient.Handler;
import com.intel.gpe.wsclient.MessageDumpHandler;
import com.intel.gui.controls2.configurable.IConfigurable;

public class U6SecurityManagerImpl implements GPESecurityManager {

    private ICache cache;
    private DefaultProperties props;
    private U6KeystoreSecurityProvider securityProvider;

    public U6SecurityManagerImpl() {
        cache = new Cache();
        props = new DefaultProperties();

        props.putImportTransferClient("RBYTEIO", RandomByteIOFileTransferClientImpl.class.getName());
        props.putExportTransferClient("RBYTEIO", RandomByteIOFileTransferClientImpl.class.getName());
        props.putDataStagingSetup("RBYTEIO", GPERandomByteIODataStagingSetup.class.getName());
    }

    public GPETargetSystemFactoryClient getTargetSystemFactoryClient(String arg0) throws Exception {
        return null;
    }

    public RegistryClient getRegistryClient(String url) throws Exception {
        final Handler requestDebugHandler = new MessageDumpHandler("REQUEST");
        final Handler responseDebugHandler = new MessageDumpHandler("RESPONSE");
        return new RegistryClientImpl(url, securityProvider, new HandlerProvider() {

            public List<Handler> getRequestHandlers() {
                return Arrays.asList(new Handler[] {
                    requestDebugHandler });
            }

            public List<Handler> getResponseHandlers() {
                return Arrays.asList(new Handler[] {
                    responseDebugHandler });
            }

        }, props, null, cache);
    }

    public GridBeanClient getGBSClient(URL url) {
        final Handler requestDebugHandler = new MessageDumpHandler("REQUEST");
        final Handler responseDebugHandler = new MessageDumpHandler("RESPONSE");
        return new GridBeanClientImpl(url.toString(), securityProvider, new HandlerProvider() {

            public List<Handler> getRequestHandlers() {
                return Arrays.asList(new Handler[] {
                    requestDebugHandler });
            }

            public List<Handler> getResponseHandlers() {
                return Arrays.asList(new Handler[] {
                    responseDebugHandler });
            }

        }, props, null, cache);
    }

    public void applyPreferences(IPreferences prefs) throws Exception {
    }

    public void configure(MessageAdapter messageAdapter, IPreferences prefs) throws Exception {
    }

    public void init(MessageAdapter messageAdapter, IPreferences prefs, StandaloneClient standaloneClient,
            IConfigurable parent) throws Exception {
    }

    public String getIdentity() {
        return null;
    }

    public GPEVirtualTargetSystemFactoryClient getVirtualTargetSystemFactoryClient(String arg0) throws Exception {
        return null;
    }

    public ResourceManagementServiceClient getRMSClient(String rmsName, String servicesURL) {
        return null;
    }

    public ClientFactory<TargetSystemFactoryClient> getTargetSystemFactoryBuilder() {
        return new ClientFactory<TargetSystemFactoryClient>() {
            public TargetSystemFactoryClient createClient(WSRFClient client) {
                return new TargetSystemFactoryClientImpl((WSRFClientImpl) client);
            }
        };
    }

	public void init(Vector<X509Certificate> caCertificates,
			X509Certificate userCertificate, PrivateKey privateKey) {
		securityProvider = new U6KeystoreSecurityProvider();
		securityProvider.init(caCertificates, userCertificate, privateKey);
	}

}