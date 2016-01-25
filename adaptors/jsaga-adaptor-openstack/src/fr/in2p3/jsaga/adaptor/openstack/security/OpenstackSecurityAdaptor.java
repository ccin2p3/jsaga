package fr.in2p3.jsaga.adaptor.openstack.security;

import java.util.Map;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.identity.Token;
import org.openstack4j.openstack.OSFactory;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UHidden;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.openstack.util.OpenstackRESTClient;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

public class OpenstackSecurityAdaptor implements SecurityAdaptor {

    public final static String PARAM_TENANT = "Tenant";
    @Override
    public String getType() {
        return "openstack";
    }

    @Override
    public Usage getUsage() {
        return new UAnd.Builder()
        .and(new U(Context.USERID))
        .and(new U(PARAM_TENANT))
        .and(new UHidden(Context.USERPASS))
        .build();
    }

    @Override
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
                new Default(Context.USERID, System.getProperty("user.name"))
        };
    }

    @Override
    public Class getSecurityCredentialClass() {
        return OpenstackSecurityCredential.class;
    }

    @Override
    public SecurityCredential createSecurityCredential(int usage,
            Map attributes, String contextId) throws IncorrectStateException,
            TimeoutException, NoSuccessException {
        return new OpenstackSecurityCredential((String)attributes.get(Context.USERID), 
                (String)attributes.get(Context.USERPASS), (String)attributes.get(PARAM_TENANT));
    }
}
