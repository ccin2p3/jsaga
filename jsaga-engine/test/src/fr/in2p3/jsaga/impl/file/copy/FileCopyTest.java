package fr.in2p3.jsaga.impl.file.copy;

import static org.junit.Assert.fail;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.ParentDoesNotExist;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderGetter;
import fr.in2p3.jsaga.impl.file.AbstractSyncFileImpl;

public class FileCopyTest {
    private static Mockery m_mockery;
    private static Session m_session;
    
    @Rule
    public ExpectedException m_exception = ExpectedException.none();
    
    @BeforeClass
    public static void init() throws Exception {
        m_mockery = new Mockery();
        m_session = SessionFactory.createSession(false);
    }
    
    @Test
    public void dataCopyDelegated() throws Exception {
        final DataCopyDelegated adaptor = m_mockery.mock(DataCopyDelegated.class, "adaptor-delegated");
        final URL sourceOk = URLFactory.createURL("adaptor://host/path/to/exists");
        final URL sourceNotExist = URLFactory.createURL("adaptor://host/path/to/NOTexists");
        final URL destOk = URLFactory.createURL("adaptor://host/path/to/newentry");
        final URL destAlreadyExist = URLFactory.createURL("adaptor://host/path/to/ALREADYexists");

        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).requestTransfer(sourceOk, destOk, false, null); will(returnValue(null));
            allowing(adaptor).requestTransfer(sourceNotExist, destOk, false, null); 
                will(throwException(new DoesNotExistException()));
            allowing(adaptor).requestTransfer(sourceOk, destAlreadyExist, false, null); 
                will(throwException(new AlreadyExistsException()));
        }});
        AbstractSyncFileImpl m_source;
        m_source = new DymmyFile(m_session, sourceOk, adaptor, 0);
        new FileCopy(m_session, m_source, adaptor).copy(destOk, 0, null);
 
        // target already exists
        try {
            new FileCopy(m_session, m_source, adaptor).copy(destAlreadyExist, 0, null);
            fail("Expected AlreadyExistsException");
        } catch (AlreadyExistsException aee) {
            // OK
        }
        
        // source does not exist
        m_source = new DymmyFile(m_session, sourceNotExist, adaptor, 0);
        try {
            new FileCopy(m_session,m_source, adaptor).copy(destOk, 0, null);
            fail("Expected IncorrectStateException");
        } catch (IncorrectStateException ise) {
            // OK
        }

        // different scheme => NotImplementedException
        try {
            new FileCopy(m_session, m_source, adaptor).copy(URLFactory.createURL("another://host/path/to/newentry"), 0, null);
            fail("Expected NotImplementedException");
        } catch (NotImplementedException nie) {
            // OK
        }
    }
    
    @Test
    public void dataCopy() throws Exception {
        final DataCopy adaptor = m_mockery.mock(DataCopy.class, "adaptor-datacopy");
        final URL sourceOk = URLFactory.createURL("adaptor://host/path/to/exists");
        final URL sourceNotExist = URLFactory.createURL("adaptor://host/path/to/NOTexists");
        final URL sourceParentNotExist = URLFactory.createURL("adaptor://host/path/to/parentNOTexists");
        final URL destOk = URLFactory.createURL("adaptor://host/path/to/newentry");
        final URL destAlreadyExist = URLFactory.createURL("adaptor://host/path/to/ALREADYexists");

        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).getDefaultPort(); will(returnValue(0));
            allowing(adaptor).copy(sourceOk.getPath(), sourceOk.getHost(), 0, destOk.getPath(), false, null, null); 
                will(returnValue(null));
            allowing(adaptor).copy(sourceNotExist.getPath(), sourceNotExist.getHost(), 0, destOk.getPath(), false, null, null); 
                will(throwException(new DoesNotExistException()));
            allowing(adaptor).copy(sourceParentNotExist.getPath(), sourceParentNotExist.getHost(), 0, destOk.getPath(), false, null, null); 
                will(throwException(new ParentDoesNotExist()));
            allowing(adaptor).copy(sourceOk.getPath(), sourceOk.getHost(), 0, destAlreadyExist.getPath(), false, null, null); 
                will(throwException(new AlreadyExistsException()));
        }});
        AbstractSyncFileImpl m_source;
        m_source = new DymmyFile(m_session, sourceOk, adaptor, 0);
        new FileCopy(m_session, m_source, adaptor).copy(destOk, 0, null);
 
        // target already exists
        try {
            new FileCopy(m_session, m_source, adaptor).copy(destAlreadyExist, 0, null);
            fail("Expected AlreadyExistsException");
        } catch (AlreadyExistsException aee) {
            // OK
        }

      // source does not exist
        m_source = new DymmyFile(m_session, sourceNotExist, adaptor, 0);
        try {
            new FileCopy(m_session,m_source, adaptor).copy(destOk, 0, null);
            fail("Expected IncorrectStateException");
        } catch (IncorrectStateException ise) {
            // OK
        }

        // parent source does not exist
        m_source = new DymmyFile(m_session, sourceParentNotExist, adaptor, 0);
        try {
            new FileCopy(m_session,m_source, adaptor).copy(destOk, 0, null);
            fail("Expected DoesNotExistException");
        } catch (DoesNotExistException dnee) {
            // OK
        }

        
        // different scheme => NotImplementedException
        try {
            new FileCopy(m_session, m_source, adaptor).copy(URLFactory.createURL("another://host/path/to/newentry"), 0, null);
            fail("Expected NotImplementedException");
        } catch (NotImplementedException nie) {
            // OK
        }
    }
    
    @Test @Ignore("Impossible to test as destination adaptor is discovered by engine and thus cannot be mocked")
    public void readerGetter() throws Exception {
        final FileReaderGetter adaptor = m_mockery.mock(FileReaderGetter.class, "adaptor-readergetter");
        final URL sourceOk = URLFactory.createURL("adaptor://host/path/to/exists");
        final URL destOk = URLFactory.createURL("dest://host/path/to/newentry");

        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getType(); will(returnValue("adaptor"));
            allowing(adaptor).exists(sourceOk.getPath(), null); will(returnValue(true));
        }});
        AbstractSyncFileImpl m_source;
        m_source = new DymmyFile(m_session, sourceOk, adaptor, 0);
        new FileCopy(m_session, m_source, adaptor).copy(destOk, 0, null);
 
    }
    
    
    
    
    private class DymmyFile extends AbstractSyncFileImpl {

        protected DymmyFile(Session session, URL url, DataAdaptor adaptor,
                int flags) throws NotImplementedException,
                IncorrectURLException, AuthenticationFailedException,
                AuthorizationFailedException, PermissionDeniedException,
                BadParameterException, AlreadyExistsException,
                DoesNotExistException, TimeoutException, NoSuccessException {
            super(session, url, adaptor, flags);
        }
        
    }
}
