package org.ogf.saga.job;

import org.ogf.saga.AbstractTest;
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
@Deprecated
public abstract class JobListTest extends AbstractTest {
    protected Session m_session;
    protected URL m_url;

    protected JobListTest(String jobprotocol) throws Exception {
        super();
        m_session = SessionFactory.createSession(true);
        m_url = URLFactory.createURL(getRequiredProperty(jobprotocol, CONFIG_JOBSERVICE_URL));
    }

    public void test_list() throws Exception {
        JobService service = JobFactory.createJobService(m_session, m_url);
        List<String> list = service.list();
        for (Iterator it=list.iterator(); it.hasNext(); ) {
            System.out.println(it.next());
        }
    }
}
