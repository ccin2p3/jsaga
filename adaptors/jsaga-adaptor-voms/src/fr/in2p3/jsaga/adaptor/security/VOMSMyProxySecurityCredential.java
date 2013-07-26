package fr.in2p3.jsaga.adaptor.security;

import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;
import org.italiangrid.voms.VOMSAttribute;
import org.italiangrid.voms.VOMSValidators;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import fr.in2p3.jsaga.adaptor.base.usage.UDuration;

import java.io.File;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.globus.gsi.X509Credential;
import org.globus.gsi.util.ProxyCertificateUtil;

/*
 * ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) *** *** http://cc.in2p3.fr/
 * *** *************************************************** File:
 * VOMSMyProxySecurityCredential Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date: 26 janv. 2009 ***************************************************
 * Description:
 */
/**
 *
 */
public class VOMSMyProxySecurityCredential extends VOMSSecurityCredential {

    protected String _genuineLifeTime;
    protected String _localLifeTime;

    public VOMSMyProxySecurityCredential(GSSCredential proxy, Map attributes) {
        super(proxy, attributes);
        this._genuineLifeTime = (String) attributes.get(Context.LIFETIME);
        this._localLifeTime = (String) attributes.get(VOMSContext.DELEGATIONLIFETIME);
        if (_localLifeTime == null) {
            _localLifeTime = VOMSProxyFactory.DEFAULTLIFE_TIME;
        }
    }

    /**
     * override super.getAttribute()
     */
    public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
        // get attribute
        if (Context.LIFETIME.equals(key) || VOMSContext.DELEGATIONLIFETIME.equals(key)) {
            // same as globus
            X509Credential globusProxy;
            if (m_proxy instanceof GlobusGSSCredentialImpl) {
                globusProxy = ((GlobusGSSCredentialImpl) m_proxy).getX509Credential();
            } else {
                throw new NoSuccessException("Not a globus proxy");
            }
            List<VOMSAttribute> v = VOMSValidators.newValidator().validate(globusProxy.getCertificateChain());
            VOMSAttribute attr = (VOMSAttribute) v.get(0);
            try {
                long timeleft = (attr.getNotAfter().getTime() - System.currentTimeMillis()) / 1000;
                if (Context.LIFETIME.equals(key)) {
                    timeleft = timeleft + UDuration.toInt(this._genuineLifeTime) - UDuration.toInt(_localLifeTime);
                }
                return "" + (timeleft > 0 ? timeleft : 0);
            } catch (ParseException e) {
                throw new NoSuccessException(e);
            }
        } else {
            return super.getAttribute(key);
        }

    }

    /**
     * override super.dump()
     */
    public void dump(PrintStream out) throws Exception {
        // same as globus
        X509Credential globusProxy;
        if (m_proxy instanceof GlobusGSSCredentialImpl) {
            globusProxy = ((GlobusGSSCredentialImpl) m_proxy).getX509Credential();
        } else {
            throw new Exception("Not a globus proxy");
        }
        out.println("  subject  : " + globusProxy.getCertificateChain()[0].getSubjectDN());
        out.println("  issuer   : " + globusProxy.getCertificateChain()[0].getIssuerDN());
        out.println("  identity : " + globusProxy.getIdentity());
        out.println("  type     : " + ProxyCertificateUtil.getProxyTypeAsString(globusProxy.getProxyType()));
        out.println("  strength : " + globusProxy.getStrength() + " bits");
        // the genuine lifetime is the one on the server (set by the user config)
        out.println("  timeleft : " + Util.formatTimeSec(globusProxy.getTimeLeft() + UDuration.toInt(this._genuineLifeTime) - UDuration.toInt(_localLifeTime)));

        // VOMS specific
        List<VOMSAttribute> v = VOMSValidators.newValidator().parse(globusProxy.getCertificateChain());
        for (int i = 0; i < v.size(); i++) {
            VOMSAttribute attr = (VOMSAttribute) v.get(i);
            out.println("  === VO " + attr.getVO() + " extension information ===");
            out.println("  VO        : " + attr.getVO());
            out.println("  subject   : " + globusProxy.getIdentity());
            out.println("  issuer    : " + attr.getIssuer().getName());
            for (Iterator<String> it = attr.getFQANs().iterator(); it.hasNext();) {
                out.println("  attribute : " + it.next());
            }
            long timeleft = (attr.getNotAfter().getTime() - System.currentTimeMillis()) / 1000;
            // the genuine lifetime is the one on the server (set by the user config)
            timeleft = timeleft + UDuration.toInt(this._genuineLifeTime) - UDuration.toInt(_localLifeTime);
            if (timeleft < 0) {
                timeleft = 0;
            }
            out.println("  timeleft  : " + Util.formatTimeSec(timeleft));
        }
    }
}
