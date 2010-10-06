package org.ogf.saga.context;

import org.ogf.saga.AbstractTest;

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
public abstract class ContextDestroyTest extends AbstractTest {
    private String m_contextId;

    protected ContextDestroyTest(String contextId) throws Exception {
        super();
        m_contextId = contextId;
    }

    public void test_destroy() throws Exception {
        // create context
        Context context = ContextFactory.createContext();
        context.setAttribute(Context.TYPE, m_contextId);

        // destroy context
        destroy(context);
    }

    private static void destroy(Context context) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method m = context.getClass().getMethod("destroy");
        if (m != null) {
            m.invoke(context);
        }
    }
}