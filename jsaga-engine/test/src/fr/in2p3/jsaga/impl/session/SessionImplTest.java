package fr.in2p3.jsaga.impl.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.engine.session.SessionConfiguration;
import fr.in2p3.jsaga.generated.session.Session;
import fr.in2p3.jsaga.impl.context.ContextImpl;

public class SessionImplTest {

    private SessionImpl m_session;
    private Mockery m_mockery = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
 
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        m_session = new SessionImpl();
    }
    
    @Test
    public void addInvalidContext() throws Exception {
        // the context must not have empty or null or "unknown" type
        for (String type: new String[]{null, ""}) {
            try {
                m_session.addContext(new ContextImpl(type, null, null));
                fail("expected NoSuccessException with type:" + type);
            } catch (NoSuccessException e) {
            }
        }
        final ContextImpl ctx = m_mockery.mock(ContextImpl.class, "context-unknown");
        m_mockery.checking(new Expectations() {{
            oneOf(ctx).getAttribute(Context.TYPE); will(returnValue("Unknown"));
        }});
        exception.expect(NoSuccessException.class);
        m_session.addContext(ctx);
        m_mockery.assertIsSatisfied();
    }

    @Test
    public void addAndRemoveContext() throws Exception {
        final ContextImpl ctx = m_mockery.mock(ContextImpl.class, "context");
        m_mockery.checking(new Expectations() {{
            exactly(2).of(ctx).getAttribute(Context.TYPE); will(returnValue("type"));
            oneOf(ctx).setUrlPrefix(with(any(Integer.class)));
            exactly(2).of(ctx).createCredential();
        }});
        m_session.addContext(ctx);
        assertEquals(1, m_session.listContexts().length);
        // add twice
        m_session.addContext(ctx);
        assertEquals(1, m_session.listContexts().length);
        // remove
        m_session.removeContext(ctx);
        assertEquals(0, m_session.listContexts().length);
        m_mockery.assertIsSatisfied();
    }

    @Test
    public void addContextBadCredential() throws Exception {
        final ContextImpl ctx = m_mockery.mock(ContextImpl.class, "context-nie");
        m_mockery.checking(new Expectations() {{
            oneOf(ctx).getAttribute(Context.TYPE); will(returnValue("type"));
            oneOf(ctx).setUrlPrefix(with(any(Integer.class)));
            oneOf(ctx).createCredential(); will(throwException(new NotImplementedException()));
        }});
        exception.expect(NoSuccessException.class);
        m_session.addContext(ctx);
        assertEquals(0, m_session.listContexts().length);
        m_mockery.assertIsSatisfied();
    }
    
    @Test
    public void conflict() throws Exception {
        EngineProperties.setProperty(EngineProperties.JSAGA_DEFAULT_CONTEXTS_CHECK_CONFLICTS, "True");
        final ContextImpl ctx = m_mockery.mock(ContextImpl.class, "context-conflict1");
        final ContextImpl ctx2 = m_mockery.mock(ContextImpl.class, "context-conflict2");
        m_mockery.checking(new Expectations() {{
            oneOf(ctx).getAttribute(Context.TYPE); will(returnValue("type1"));
            oneOf(ctx).setUrlPrefix(with(any(Integer.class)));
            oneOf(ctx).createCredential();
            oneOf(ctx2).getAttribute(Context.TYPE); will(returnValue("type2"));
            oneOf(ctx2).throwIfConflictsWith(ctx); will(throwException(new NoSuccessException()));
        }});
        m_session.addContext(ctx);
        assertEquals(1, m_session.listContexts().length);
        try {
            m_session.addContext(ctx2);
            fail("expected NoSuccessException");
        } catch (NoSuccessException e) {
            //OK
        }
        assertEquals(1, m_session.listContexts().length);
        m_mockery.assertIsSatisfied();
    }
    
    @Test
    public void cloning() throws Exception {
        final ContextImpl ctx = m_mockery.mock(ContextImpl.class, "context-clone");
        m_mockery.checking(new Expectations() {{
            oneOf(ctx).getAttribute(Context.TYPE); will(returnValue("type"));
            oneOf(ctx).setUrlPrefix(with(any(Integer.class)));
            oneOf(ctx).createCredential();
            oneOf(ctx).clone(); will(returnValue(ctx));
        }});
        m_session.addContext(ctx);
        assertEquals(1, ((SessionImpl)m_session.clone()).listContexts().length);
        m_mockery.assertIsSatisfied();
    }
    
    @Test(expected=DoesNotExistException.class) 
    public void removeDoesNotExist() throws Exception {
        final ContextImpl ctx = m_mockery.mock(ContextImpl.class, "context-remove");
        m_session.removeContext(ctx);
    }
    
    @Test 
    public void factorySessionFalse() throws Exception {
        final SessionConfiguration conf = m_mockery.mock(SessionConfiguration.class, "session-false");
        m_mockery.checking(new Expectations() {{
            never(conf).setDefaultSession(with(any(SessionImpl.class)));
        }});
        new SessionFactoryImpl(conf).doCreateSession(false);
        m_mockery.assertIsSatisfied();
    }

    @Test 
    public void factorySessionTrue() throws Exception {
        final SessionConfiguration conf = m_mockery.mock(SessionConfiguration.class, "session-true");
        m_mockery.checking(new Expectations() {{
            oneOf(conf).setDefaultSession(with(any(SessionImpl.class)));
        }});
        new SessionFactoryImpl(conf).doCreateSession(true);
        m_mockery.assertIsSatisfied();
    }
}
