package fr.in2p3.jsaga.impl.permissions;

import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.permissions.Permission;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptor;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptorBasic;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptorFull;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;

public class DataPermissionsImplTest {
    private static String PATH = "/path/to/file";
    private static Session m_session;
    private static URL m_url;
    private FakePermissions m_perm;
    private Mockery m_mockery = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
 
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void init() throws BadParameterException, NoSuccessException {
        m_session = SessionFactory.createSession(false);
        m_url = URLFactory.createURL("scheme://dummy" + PATH);
    }
    
    @Before
    public void setUp() throws Exception {
    }
    
    @Test
    public void allowNotSupportedScope() throws Exception {
        final PermissionAdaptorFull adaptor = m_mockery.mock(PermissionAdaptorFull.class, "adaptor-allow-nss");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("type"));
            allowing(adaptor).getSupportedScopes(); will(returnValue(new int[]{PermissionAdaptor.SCOPE_USER}));
        }});
        m_perm = new FakePermissions(m_session, m_url, adaptor);
        exception.expect(BadParameterException.class);
        m_perm.permissionsAllow("group-group1", Permission.NONE.getValue());
    }

    @Test
    public void allowAnyOwner() throws Exception {
        final PermissionAdaptorFull adaptor = m_mockery.mock(PermissionAdaptorFull.class, "adaptor-allow-ao");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("type"));
            allowing(adaptor).getSupportedScopes(); will(returnValue(new int[]{PermissionAdaptor.SCOPE_ANY}));
        }});
        m_perm = new FakePermissions(m_session, m_url, adaptor);
        exception.expect(BadParameterException.class);
        m_perm.permissionsAllow("*", Permission.OWNER.getValue());
    }

    @Test
    public void allowUserOwner() throws Exception {
        final PermissionAdaptorFull adaptor = m_mockery.mock(PermissionAdaptorFull.class, "adaptor-allow-uo");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("type"));
            allowing(adaptor).getSupportedScopes(); will(returnValue(new int[]{PermissionAdaptor.SCOPE_USER}));
            oneOf(adaptor).setOwner(PATH, "me");
            oneOf(adaptor).permissionsAllow(with(any(String.class)), with(any(Integer.class)), 
                    with(any(PermissionBytes.class)), with(any(String.class)));
        }});
        m_perm = new FakePermissions(m_session, m_url, adaptor);
        m_perm.permissionsAllow("user-me", Permission.OWNER.getValue());
        m_mockery.assertIsSatisfied();
    }

    @Test
    public void allowGroupOwner() throws Exception {
        final PermissionAdaptorFull adaptor = m_mockery.mock(PermissionAdaptorFull.class, "adaptor-allow-go");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("type"));
            allowing(adaptor).getSupportedScopes(); will(returnValue(new int[]{PermissionAdaptor.SCOPE_GROUP}));
            oneOf(adaptor).setGroup(PATH, "mygroup");
            oneOf(adaptor).permissionsAllow(with(any(String.class)), with(any(Integer.class)), 
                    with(any(PermissionBytes.class)), with(any(String.class)));
        }});
        m_perm = new FakePermissions(m_session, m_url, adaptor);
        m_perm.permissionsAllow("group-mygroup", Permission.OWNER.getValue());
        m_mockery.assertIsSatisfied();
    }

    // setOwner cannot throw DoesNotExistException, it is not defined in PermissionAdaptorFull ITF
    @Test
    public void allowUserOwnerOnFileDoesNotExist() throws Exception {
        final PermissionAdaptorFull adaptor = m_mockery.mock(PermissionAdaptorFull.class, "adaptor-allow-uo-dne");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("type"));
            allowing(adaptor).getSupportedScopes(); will(returnValue(new int[]{PermissionAdaptor.SCOPE_USER}));
            oneOf(adaptor).setOwner(PATH, "me"); will(throwException(new NoSuccessException()));
        }});
        m_perm = new FakePermissions(m_session, m_url, adaptor);
        try {
            m_perm.permissionsAllow("user-me", Permission.OWNER.getValue());
            fail("Expected NoSuccessException");
        } catch (NoSuccessException e) {
            // OK
        }
        m_mockery.assertIsSatisfied();
    }

    @Test
    public void allowGroupOwnerOnFileDoesNotExist() throws Exception {
        final PermissionAdaptorFull adaptor = m_mockery.mock(PermissionAdaptorFull.class, "adaptor-allow-go-dne");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("type"));
            allowing(adaptor).getSupportedScopes(); will(returnValue(new int[]{PermissionAdaptor.SCOPE_GROUP}));
            oneOf(adaptor).setGroup(PATH, "mygroup"); will(throwException(new DoesNotExistException()));
        }});
        m_perm = new FakePermissions(m_session, m_url, adaptor);
        try {
            m_perm.permissionsAllow("group-mygroup", Permission.OWNER.getValue());
            fail("Expected NoSuccessException");
        } catch (NoSuccessException e) {
            // OK
        }
        m_mockery.assertIsSatisfied();
    }

    @Test
    public void allowUserOwnerBasic() throws Exception {
        final PermissionAdaptorBasic adaptor = m_mockery.mock(PermissionAdaptorBasic.class, "adaptor-allow-uo-basic");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("type"));
            allowing(adaptor).getSupportedScopes(); will(returnValue(new int[]{PermissionAdaptor.SCOPE_USER}));
        }});
        m_perm = new FakePermissions(m_session, m_url, adaptor);
        exception.expect(BadParameterException.class);
        m_perm.permissionsAllow("user-me", Permission.OWNER.getValue());
    }

    @Test
    public void allowGroupOwnerBasicNotReader() throws Exception {
        final PermissionAdaptorBasic adaptor = m_mockery.mock(PermissionAdaptorBasic.class, "adaptor-allow-go-basic-nr");
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("type"));
            allowing(adaptor).getSupportedScopes(); will(returnValue(new int[]{PermissionAdaptor.SCOPE_GROUP}));
            oneOf(adaptor).setGroup(PATH, "mygroup");
        }});
        m_perm = new FakePermissions(m_session, m_url, adaptor);
        exception.expect(NotImplementedException.class);
        m_perm.permissionsAllow("group-mygroup", Permission.OWNER.getValue());
    }

    @Test
    public void allowGroupOwnerBasic() throws Exception {
        final DataReaderBasicPermissionsAdaptor adaptor = m_mockery.mock(DataReaderBasicPermissionsAdaptor.class, "adaptor-allow-go-basic");
        final FileAttributes attrs = m_mockery.mock(FileAttributes.class);
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("type"));
            allowing(adaptor).getSupportedScopes(); will(returnValue(new int[]{PermissionAdaptor.SCOPE_GROUP}));
            oneOf(adaptor).setGroup(PATH, "mygroup");
            oneOf(adaptor).getAttributes(PATH, null); will(returnValue(attrs));
            oneOf(adaptor).permissionsAllow(with(any(String.class)), with(any(Integer.class)), 
                    with(any(PermissionBytes.class)));
        }});
        m_perm = new FakePermissions(m_session, m_url, adaptor);
        m_perm.permissionsAllow("group-mygroup", Permission.OWNER.getValue());
        m_mockery.assertIsSatisfied();
    }

    @Test
    public void getOwnerAndGroup() throws Exception {
        final DataReaderBasicPermissionsAdaptor adaptor = m_mockery.mock(DataReaderBasicPermissionsAdaptor.class, "adaptor-getownerandgroup");
        final FileAttributes attrs = m_mockery.mock(FileAttributes.class);
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("type"));
            oneOf(adaptor).getAttributes(PATH, null); will(returnValue(attrs));
            oneOf(attrs).getOwner(); will(returnValue("me"));
            oneOf(attrs).getGroup(); will(returnValue("mygroup"));
        }});
        m_perm = new FakePermissions(m_session, m_url, adaptor);
        assertEquals("me", m_perm.getOwner());
        assertEquals("mygroup", m_perm.getGroup());
        m_mockery.assertIsSatisfied();
    }

    
    
    interface DataReaderBasicPermissionsAdaptor extends PermissionAdaptorBasic, DataReaderAdaptor{};
    
    private class FakePermissions extends AbstractDataPermissionsImpl {

        public FakePermissions(Session session, URL url, DataAdaptor adaptor)
                throws NotImplementedException, IncorrectURLException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, BadParameterException,
                DoesNotExistException, TimeoutException, NoSuccessException {
            super(session, url, adaptor);
        }

        @Override
        public URL getURLSync() throws NotImplementedException,
                IncorrectStateException, TimeoutException, NoSuccessException {
            return null;
        }

        @Override
        public URL getCWDSync() throws NotImplementedException,
                IncorrectStateException, TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public URL getNameSync() throws NotImplementedException,
                IncorrectStateException, TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isDirSync() throws NotImplementedException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, IncorrectStateException,
                TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isEntrySync() throws NotImplementedException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, IncorrectStateException,
                TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isLinkSync() throws NotImplementedException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, IncorrectStateException,
                TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public URL readLinkSync() throws NotImplementedException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, IncorrectStateException,
                TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getMTimeSync() throws NotImplementedException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, IncorrectStateException,
                TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void copySync(URL target, int flags)
                throws NotImplementedException, AuthenticationFailedException,
                AuthorizationFailedException, PermissionDeniedException,
                BadParameterException, IncorrectStateException,
                AlreadyExistsException, DoesNotExistException,
                TimeoutException, NoSuccessException, IncorrectURLException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void copySync(URL target) throws NotImplementedException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, BadParameterException,
                IncorrectStateException, AlreadyExistsException,
                DoesNotExistException, TimeoutException, NoSuccessException,
                IncorrectURLException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void linkSync(URL target, int flags)
                throws NotImplementedException, AuthenticationFailedException,
                AuthorizationFailedException, PermissionDeniedException,
                BadParameterException, DoesNotExistException,
                IncorrectStateException, AlreadyExistsException,
                TimeoutException, NoSuccessException, IncorrectURLException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void linkSync(URL target) throws NotImplementedException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, BadParameterException,
                DoesNotExistException, IncorrectStateException,
                AlreadyExistsException, TimeoutException, NoSuccessException,
                IncorrectURLException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void moveSync(URL target, int flags)
                throws NotImplementedException, AuthenticationFailedException,
                AuthorizationFailedException, PermissionDeniedException,
                BadParameterException, IncorrectStateException,
                AlreadyExistsException, DoesNotExistException,
                TimeoutException, NoSuccessException, IncorrectURLException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void moveSync(URL target) throws NotImplementedException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, BadParameterException,
                IncorrectStateException, AlreadyExistsException,
                DoesNotExistException, TimeoutException, NoSuccessException,
                IncorrectURLException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void removeSync(int flags) throws NotImplementedException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, BadParameterException,
                IncorrectStateException, TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void removeSync() throws NotImplementedException,
                AuthenticationFailedException, AuthorizationFailedException,
                PermissionDeniedException, BadParameterException,
                IncorrectStateException, TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void permissionsAllowSync(String id, int permissions, int flags)
                throws NotImplementedException, AuthenticationFailedException,
                AuthorizationFailedException, PermissionDeniedException,
                IncorrectStateException, BadParameterException,
                TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void permissionsDenySync(String id, int permissions, int flags)
                throws NotImplementedException, AuthenticationFailedException,
                AuthorizationFailedException, IncorrectStateException,
                PermissionDeniedException, BadParameterException,
                TimeoutException, NoSuccessException {
            // TODO Auto-generated method stub
            
        }
        
    }
}
