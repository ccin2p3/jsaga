package org.ogf.saga.resource;

import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractJobTest
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr
* Date:   25 JAN 2016
* ***************************************************
* Description:                                      */

public abstract class ResourceBaseTest extends JSAGABaseTest {
	
    // configuration
    protected URL m_resourcemanager;
    protected Session m_session;
    protected ResourceManager m_rm;
    
    protected ResourceBaseTest(String resourceprotocol) throws Exception {
        super();

        // configure
        m_resourcemanager = URLFactory.createURL(getRequiredProperty(resourceprotocol, CONFIG_RM_URL));
        m_session = SessionFactory.createSession(true);
        
    }

    @Before
    public void setUp() throws NotImplementedException, BadParameterException, IncorrectURLException, 
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        m_rm = ResourceFactory.createResourceManager(m_session, m_resourcemanager);
    }
    
    ////////////
    // Templates
    ////////////
    @Test(expected = DoesNotExistException.class)
    public void unknownTemplate() throws NotImplementedException, BadParameterException, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException {
        m_rm.getTemplate("[" + m_resourcemanager.getString() + "]-[thisTemplateDoesNotExists]");
    }

    @Test
    public void listComputeTemplates() throws NotImplementedException, TimeoutException, NoSuccessException  {
        assertNotNull(m_rm.listTemplates(Type.COMPUTE));
    }

    @Test
    public void listStorageTemplates() throws NotImplementedException, TimeoutException, NoSuccessException  {
        assertNotNull(m_rm.listTemplates(Type.STORAGE));
    }

    @Test
    public void listNetworkTemplates() throws NotImplementedException, TimeoutException, NoSuccessException  {
        assertNotNull(m_rm.listTemplates(Type.NETWORK));
    }

    ////////////
    // Resources
    ////////////
    @Test
    public void listComputeResources() throws NotImplementedException, TimeoutException, 
        NoSuccessException, AuthenticationFailedException, AuthorizationFailedException  {
        assertNotNull(m_rm.listResources(Type.COMPUTE));
    }

    @Test
    public void listStorageResources() throws NotImplementedException, TimeoutException, 
        NoSuccessException, AuthenticationFailedException, AuthorizationFailedException  {
        assertNotNull(m_rm.listResources(Type.STORAGE));
    }

    @Test
    public void listNetworkResources() throws NotImplementedException, TimeoutException, 
        NoSuccessException, AuthenticationFailedException, AuthorizationFailedException  {
        assertNotNull(m_rm.listResources(Type.NETWORK));
    }

}
