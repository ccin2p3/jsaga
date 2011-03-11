package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.link.LinkAdaptor;
import fr.in2p3.jsaga.adaptor.data.link.NotLink;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionAdaptorBasic;
import fr.in2p3.jsaga.adaptor.data.permission.PermissionBytes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStreamFactory;
import org.ogf.saga.error.*;

import java.io.InputStream;
import java.io.OutputStream;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   WaitForEverDataAdaptorPhysical
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class WaitForEverDataAdaptorPhysical extends WaitForEverDataAdaptorAbstract implements FileReaderStreamFactory, FileWriterStreamFactory, LinkAdaptor, PermissionAdaptorBasic {
    public String getType() {
        return "waitforever";
    }

    //////////////////////////////////////// interfaces *StreamFactory ////////////////////////////////////////

    public InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        mayHang(additionalArgs);
        return new WaitForEverInputStream();
    }
    public OutputStream getOutputStream(String parentAbsolutePath, String fileName, boolean exclusive, boolean append, String additionalArgs) throws PermissionDeniedException, BadParameterException, AlreadyExistsException, ParentDoesNotExist, TimeoutException, NoSuccessException {
        mayHang(additionalArgs);
        return new WaitForEverOutputStream();
    }

    ////////////////////////////////////////// interface LinkAdaptor //////////////////////////////////////////

    public boolean isLink(String absolutePath) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        hang();
        return false;
    }
    public String readLink(String absolutePath) throws NotLink, PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        hang();
        return absolutePath;
    }
    public void link(String sourceAbsolutePath, String linkAbsolutePath, boolean overwrite) throws PermissionDeniedException, DoesNotExistException, AlreadyExistsException, TimeoutException, NoSuccessException {
        hang();
    }

    /////////////////////////////////////// interface PermissionAdaptor ///////////////////////////////////////

    public int[] getSupportedScopes() {
        return new int[]{SCOPE_USER,SCOPE_GROUP,SCOPE_ANY};
    }

    public String[] getGroupsOf(String id) throws BadParameterException, NoSuccessException {
        hang();
        return null;
    }

    public void permissionsAllow(String absolutePath, int scope, PermissionBytes permissions) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        hang();
    }

    public void permissionsDeny(String absolutePath, int scope, PermissionBytes permissions) throws PermissionDeniedException, TimeoutException, NoSuccessException {
        hang();
    }

    public void setGroup(String absolutePath, String id) throws PermissionDeniedException, TimeoutException, BadParameterException, NoSuccessException {
        hang();
    }
}
