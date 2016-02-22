package org.ogf.saga.resource;

import org.junit.Test;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.instance.Storage;
import org.ogf.saga.resource.task.State;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

public class StorageTest extends ResourceBaseTest {

    public StorageTest(String resourceprotocol) throws Exception {
        super(resourceprotocol);
    }
    protected Object[] typeToBeTested() {
        return new Object[][] {
                {Type.STORAGE}
        };
    }

    //////////////////
    // acquire storage
    //////////////////
    @Test
    public void createAndMkdirAndDeleteStorageArea() throws Exception {
        StorageDescription sd = (StorageDescription) ResourceFactory.createResourceDescription(Type.STORAGE);
        Storage storage = m_rm.acquireStorage(sd);
        storage.waitFor(120, State.ACTIVE);
        this.dumpResource(storage);
        URL baseUrl = URLFactory.createURL(storage.getAccess()[0]);
        URL m_dirUrl = createURL(baseUrl, "dir/");
        NSDirectory m_dir = NSFactory.createNSDirectory(m_session, m_dirUrl, Flags.CREATE.or(Flags.EXCL));
        m_dir.close();
        storage.release();
    }
}
