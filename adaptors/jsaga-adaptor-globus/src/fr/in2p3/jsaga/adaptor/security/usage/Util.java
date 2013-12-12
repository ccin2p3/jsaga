package fr.in2p3.jsaga.adaptor.security.usage;

import org.ogf.saga.context.Context;

import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.UOr;
import fr.in2p3.jsaga.adaptor.security.GlobusContext;
import fr.in2p3.jsaga.adaptor.security.GlobusSecurityAdaptor;

public class Util {

    public static UOr buildCertsUsage() {
        return new UOr.Builder()
            .or(new UFile(GlobusSecurityAdaptor.USAGE_INIT_PKCS12, GlobusContext.USERCERTKEY))
            .or(new UAnd.Builder()
                .id(GlobusSecurityAdaptor.USAGE_INIT_PEM)
                .and(new UFile(Context.USERCERT))
                .and(new UFile(Context.USERKEY))
                .build()
                )
            .build();
    }
}
