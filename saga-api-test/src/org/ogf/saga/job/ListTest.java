package org.ogf.saga.job;

import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.util.Iterator;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobListTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class ListTest extends JSAGABaseTest {
    protected Session m_session;
    protected URL m_url;

    protected ListTest(String jobprotocol) throws Exception {
        super();
        m_session = SessionFactory.createSession(true);
        m_url = URLFactory.createURL(getRequiredProperty(jobprotocol, CONFIG_JOBSERVICE_URL));
    }

    @Test
    public void test_list() throws Exception {
        JobService service = JobFactory.createJobService(m_session, m_url);
        List<String> list = service.list();
        for (Iterator it=list.iterator(); it.hasNext(); ) {
            System.out.println(it.next());
        }
    }
}
