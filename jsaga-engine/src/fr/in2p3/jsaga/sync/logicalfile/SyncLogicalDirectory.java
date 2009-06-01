package fr.in2p3.jsaga.sync.logicalfile;

import fr.in2p3.jsaga.sync.namespace.SyncNSDirectory;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.util.List;

/**
 * This interface represents a container for logical files in a logical file
 * name space.
 */
public interface SyncLogicalDirectory extends SyncNSDirectory {

    /**
     * Tests the name for being a logical file. Is an alias for
     * {@link SyncNSDirectory#isEntrySync}.
     *
     * @param name
     *            to be tested.
     * @return <code>true</code> if the name represents a non-directory entry.
     */
    public boolean isFileSync(URL name) throws NotImplementedException,
            IncorrectURLException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, DoesNotExistException,
            IncorrectStateException, TimeoutException, NoSuccessException;

    /**
     * Finds entries in the current directory and possibly below, with matching
     * names and matching meta data.
     *
     * @param namePattern
     *            pattern for names of entries to be found.
     * @param attrPattern
     *            pattern for meta data keys/values of entries to be found.
     * @param flags
     *            flags defining the operation modus.
     * @return the list of matching entries.
     */
    public List<URL> findSync(String namePattern, String[] attrPattern, int flags)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException;

    /**
     * Finds entries in the current directory and below, with matching names and
     * matching meta data.
     *
     * @param namePattern
     *            pattern for names of entries to be found.
     * @param attrPattern
     *            pattern for meta data keys/values of entries to be found.
     * @return the list of matching entries.
     */
    public List<URL> findSync(String namePattern, String[] attrPattern)
            throws NotImplementedException, AuthenticationFailedException,
            AuthorizationFailedException, PermissionDeniedException,
            BadParameterException, IncorrectStateException, TimeoutException,
            NoSuccessException;
}
