package fr.in2p3.jsaga.adaptor.openstack.security;

import java.io.PrintStream;
import java.util.Date;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;

public class OpenstackSecurityCredential extends UserPassSecurityCredential {

    private String m_tenant;
    
    public OpenstackSecurityCredential(String userId, String userPass, String tenantName) {
        super(userId, userPass);
        this.m_tenant = tenantName;
    }

    @Override
    public String getAttribute(String key) throws NotImplementedException,
            NoSuccessException {
        if (OpenstackSecurityAdaptor.PARAM_TENANT.equals(key)) {
            return this.m_tenant;
        }
        throw new NotImplementedException(key);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void dump(PrintStream out) throws Exception {
        super.dump(out);
        out.print("Tenant: " + m_tenant);
    }

}
