package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.UOptionalBoolean;
import fr.in2p3.jsaga.adaptor.base.usage.UOptionalInteger;
import fr.in2p3.jsaga.adaptor.base.usage.UOr;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityCredential;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.IncorrectStateException;

import java.io.File;
import java.util.Map;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobAdaptorAbstract
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************
/**
 *
 */
public abstract class WMSJobAdaptorAbstract implements ClientAdaptor {
    protected GSSCredential m_credential;
    protected File m_certRepository;
    protected static final String DEFAULT_JDL_FILE = "DefaultJdlFile";
        
    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{GSSCredentialSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
        GSSCredentialSecurityCredential proxyAdaptor = (GSSCredentialSecurityCredential) credential;
        m_credential = proxyAdaptor.getGSSCredential();
        m_certRepository = proxyAdaptor.getCertRepository();
    }

    public Usage getUsage() {
        return new UOr.Builder()
                       .or(new UOptional(DEFAULT_JDL_FILE))
                       .or(new UOptional("LBAddress"))
                       .or(new UOptional("requirements"))
                       .or(new UOptional("rank"))
                       .or(new UOptional("virtualorganisation"))
                       .or(new UOptionalInteger("RetryCount"))
                       .or(new UOptionalInteger("ShallowRetryCount"))
                       .or(new UOptional("OutputStorage"))
                       .or(new UOptionalBoolean("AllowZippedISB"))
                       .or(new UOptionalBoolean("PerusalFileEnable"))
                       .or(new UOptional("ListenerStorage"))
                       .or(new UOptional("MyProxyServer"))
                       .or(new UOptional("DelegationID"))
                       .build();
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
                    // JDL attributes
                    new Default("requirements", "(other.GlueCEStateStatus==\"Production\")"),
                    new Default("rank", "(-other.GlueCEStateEstimatedResponseTime)")
                };
    }

}
