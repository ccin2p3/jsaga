package org.ogf.saga.permissions;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.ogf.saga.context.Context;
import org.ogf.saga.namespace.abstracts.AbstractData;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PermissionsTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Author: lionel.schwarz@in2p3.fr
* Date:   5 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class PermTest extends AbstractData {
    private static Logger s_logger = Logger.getLogger(PermTest.class);

    public PermTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test
    public void test_ownerEXECPermissions() throws Exception {
    	test_permissions(null, Permission.EXEC);
    }
    
    @Test
    public void test_ownerREADPermissions() throws Exception {
    	test_permissions(null, Permission.READ);
    }
    
    @Test
    public void test_ownerWRITEPermissions() throws Exception {
    	test_permissions(null, Permission.WRITE);
    }
    
    @Test
    public void test_groupEXECPermissions() throws Exception {
    	test_permissions("group-" + m_file.getGroup(), Permission.EXEC);
    }
    
    @Test
    public void test_groupREADPermissions() throws Exception {
    	test_permissions("group-" + m_file.getGroup(), Permission.READ);
    }
    
    @Test
    public void test_groupWRITEPermissions() throws Exception {
    	test_permissions("group-" + m_file.getGroup(), Permission.WRITE);
    }
    
    @Test
    public void test_otherEXECPermissions() throws Exception {
    	test_permissions("*", Permission.EXEC);
    }
    
    @Test
    public void test_otherREADPermissions() throws Exception {
    	test_permissions("*", Permission.READ);
    }
    
    @Test
    public void test_otherWRITEPermissions() throws Exception {
    	test_permissions("*", Permission.WRITE);
    }
    
    private void test_permissions(String id, Permission permission)throws Exception {
    	boolean toreset = false;
    	if(m_file.permissionsCheck(id, permission.getValue())){
    		m_file.permissionsDeny(id, permission.getValue());
    		assertFalse(m_file.permissionsCheck(id, permission.getValue()));
    		toreset = true;
    	}
        m_file.permissionsAllow(id, permission.getValue());
        assertTrue(m_file.permissionsCheck(id, permission.getValue()));
        if(toreset){
        	m_file.permissionsDeny(id, permission.getValue());
    		assertFalse(m_file.permissionsCheck(id, permission.getValue()));
        }
    }

    @Test
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

    @Test
    public void test_getGroup() throws Exception {
        String group = m_file.getGroup();
        if (group==null || "*".equals(group)) {
            fail("Adaptor returned unexpected value: "+group);
        } else if ("".equals(group)) {
            s_logger.warn("Method getGroup not supported by adaptor: "+m_fileUrl.getScheme());
        }
    }
}
