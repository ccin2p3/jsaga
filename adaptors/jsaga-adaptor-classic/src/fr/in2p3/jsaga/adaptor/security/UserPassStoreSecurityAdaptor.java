package fr.in2p3.jsaga.adaptor.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassStoreSecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserPassStoreSecurityAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   2 mai 2011
* ***************************************************/

public class UserPassStoreSecurityAdaptor implements SecurityAdaptor {

    private final String STOREFILE = "StoreFile";
    public final String HOST_TOKEN = "machine";
    public final String USER_TOKEN = "login";
    public final String PASSWORD_TOKEN = "password";
    public final String COMMENT_TOKEN = "#";
    public final String DEFAULT_HOST_TOKEN = "default";
    
    public String getType() {
        return "UserPassStore";
    }

    public Usage getUsage() {
        return new UFile(STOREFILE);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
                new Default(STOREFILE, new File[]{
                        new File(System.getProperty("user.home"), ".netrc")}),
        };
    }

    public Class getSecurityCredentialClass() {
        return UserPassStoreSecurityCredential.class;
    }

    public SecurityCredential createSecurityCredential(int usage,
            Map attributes, String contextId) throws IncorrectStateException,
            TimeoutException, NoSuccessException {
        UserPassStoreSecurityCredential upsc = new UserPassStoreSecurityCredential();
        BufferedReader br;
        String netrcFile = (String) attributes.get(STOREFILE);
        try {
            br = new BufferedReader(new FileReader(new File(netrcFile)));
        } catch (FileNotFoundException e) {
            throw new NoSuccessException("File not found: "+new File(netrcFile).toString());
        }
        
        // TODO : check that password file is readable by user only 
        /*
        try {
            Context _context = ContextFactory.createContext("None");
            Session _session = SessionFactory.createSession(false);
            _session.addContext(_context);
            URL storeFileURL = URLFactory.createURL("file:"+netrcFile);
            NSEntry storeFileEntry = NSFactory.createNSEntry(_session, storeFileURL);
            boolean anyCanReadOrWrite = false, groupCanReadOrWrite = false;
            try {
                String gr = storeFileEntry.getGroup();
                groupCanReadOrWrite = !storeFileEntry.permissionsCheck("group-" + gr, Permission.NONE.getValue());
            } catch (NotImplementedException e) {
                throw new NoSuccessException("Could not check group permissions on file " + netrcFile, e);
            }
            try {
                anyCanReadOrWrite = storeFileEntry.permissionsCheck("*", Permission.READ.or(Permission.WRITE));
            } catch (NotImplementedException e) {
                throw new NoSuccessException("Could not check world permissions on file " + netrcFile, e);
            }
            if (groupCanReadOrWrite || anyCanReadOrWrite) {
                throw new NoSuccessException("File " + netrcFile + " must be readable by user only");
            }
            storeFileEntry.close();
        } catch (IncorrectURLException e1) {
            throw new NoSuccessException(e1);
        } catch (AuthenticationFailedException e1) {
            throw new NoSuccessException(e1);
        } catch (AuthorizationFailedException e1) {
            throw new NoSuccessException(e1);
        } catch (PermissionDeniedException e1) {
            throw new NoSuccessException(e1);
        } catch (BadParameterException e1) {
            throw new NoSuccessException(e1);
        } catch (DoesNotExistException e1) {
            throw new NoSuccessException(e1);
        } catch (AlreadyExistsException e1) {
            throw new NoSuccessException(e1);
        } catch (NotImplementedException e1) {
            throw new NoSuccessException(e1);
        }
        */
        String line;
        try {
            String net_machine = null;
            String net_user = null;
            String net_password = null;
            while ((line= br.readLine()) != null) {
                if (!line.startsWith(this.COMMENT_TOKEN)) {
                    StringTokenizer st = new StringTokenizer(line);
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (token.equals(this.HOST_TOKEN) || token.equals(this.DEFAULT_HOST_TOKEN)) {
                            // store only if user and password and host != null
                            if (net_machine != null && net_user != null && net_password != null) {
                                upsc.addUserPassCredential(net_machine, net_user, net_password);
                            }
                            if (token.equals(this.HOST_TOKEN)) {
                                net_machine = st.nextToken();
                            } else {
                                net_machine = this.DEFAULT_HOST_TOKEN;
                            }
                        } else if (token.equals(this.USER_TOKEN)) {
                            net_user = st.nextToken();
                        } else if (token.equals(this.PASSWORD_TOKEN))
                            net_password = st.nextToken();
                    }
                }
            }
            // store last record
            if (net_machine != null && net_user != null && net_password != null) {
                upsc.addUserPassCredential(net_machine, net_user, net_password);
            }
        } catch (IOException e) {
            throw new NoSuccessException("Error reading file: "+new File(netrcFile).toString());
        }
        return upsc;
    }

}
