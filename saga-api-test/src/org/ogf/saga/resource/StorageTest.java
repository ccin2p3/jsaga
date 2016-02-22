package org.ogf.saga.resource;

import org.junit.Test;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.resource.instance.Storage;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

public abstract class StorageTest extends ResourceBaseTest {

    public StorageTest(String resourceprotocol) throws Exception {
        super(resourceprotocol, Type.STORAGE);
    }
    
    @Override
    protected Resource acquire(ResourceDescription rd) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        return m_rm.acquireStorage((StorageDescription) rd);
    }

    //////////////////
    // acquire storage
    //////////////////
    @Test 
    public void createWithSize() throws Exception {
        StorageDescription nd = (StorageDescription) ResourceFactory.createResourceDescription(m_type);
        nd.setAttribute(StorageDescription.SIZE, Integer.toString(1024*1024));
        m_currentResource = (Storage) this.acquireResourceFromDescReadyForUse(nd);
        assertEquals(Integer.toString(1024*1024), 
                ((StorageDescription)m_currentResource.getDescription()).getAttribute(StorageDescription.SIZE));
    }
    
    @Test
    public void createAndMkdirAndDeleteStorageArea() throws Exception {
        m_currentResource = (Storage) this.acquireResourceReadyForUse();
        URL baseUrl = URLFactory.createURL(m_currentResource.getAccess()[0]);
        URL m_dirUrl = createURL(baseUrl, "dir/");
        NSDirectory m_dir = NSFactory.createNSDirectory(m_session, m_dirUrl, Flags.CREATE.or(Flags.EXCL));
        m_dir.close();
    }
}
