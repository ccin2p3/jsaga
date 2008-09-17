package org.ogf.saga.context;

import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextDestroyTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 sept. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ContextDestroyTest extends AbstractTest {
    private String m_contextId;

    protected ContextDestroyTest(String contextId) throws Exception {
        super();
        m_contextId = contextId;
    }

    public void test_destroy() throws Exception {
        Session session = SessionFactory.createSession();

        // find context
        Context context = this.findContext(session);

        // destroy context
        destroy(context);

        session.close();
    }

    private Context findContext(Session session) throws Exception {
        Context[] contexts = session.listContexts();
        for (int i=0; i<contexts.length; i++) {
            Context context = contexts[i];
            String id = context.getAttribute(Context.TYPE);
            if (id.equals(m_contextId)) {
                return context;
            }
        }
        throw new DoesNotExist("Context does not exist: "+m_contextId);
    }

    private static void destroy(Context context) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method m = context.getClass().getMethod("destroy");
        if (m != null) {
            m.invoke(context);
        }
    }
}