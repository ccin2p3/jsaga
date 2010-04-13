package org.ogf.saga.permissions;

import org.apache.log4j.Logger;
import org.ogf.saga.context.Context;
import org.ogf.saga.namespace.abstracts.AbstractNSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PermissionsTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class PermissionsTest extends AbstractNSEntryTest {
    private static Logger s_logger = Logger.getLogger(PermissionsTest.class);

    public PermissionsTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_enablePermissions() throws Exception {
        assertFalse(m_file.permissionsCheck(null, Permission.EXEC.getValue()));
        m_file.permissionsAllow(null, Permission.EXEC.getValue());
        assertTrue(m_file.permissionsCheck(null, Permission.EXEC.getValue()));
    }

    public void test_disablePermissions() throws Exception {
        assertTrue(m_file.permissionsCheck(null, Permission.READ.getValue()));
        m_file.permissionsDeny(null, Permission.READ.getValue());
        assertFalse(m_file.permissionsCheck(null, Permission.READ.getValue()));
    }

    public void test_getOwner() throws Exception {
        String owner = m_file.getOwner();
        if (owner==null || "*".equals(owner)) {
            fail("Adaptor returned unexpected value: "+owner);
        }
        for (Context ctx : m_session.listContexts()) {
            String userId = ctx.getAttribute(Context.USERID);
            if (owner.equals(userId)) {
                return; //==========> SUCCESS
            }
        }
        fail("Owner not found in security contexts: "+owner);
    }

    public void test_getGroup() throws Exception {
        String group = m_file.getGroup();
        if (group==null || "*".equals(group)) {
            fail("Adaptor returned unexpected value: "+group);
        } else if ("".equals(group)) {
            s_logger.warn("Method getGroup not supported by adaptor: "+m_fileUrl.getScheme());
        }
    }
}
