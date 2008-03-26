package fr.in2p3.jsaga.adaptor.data.impl;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.schema.data.emulator.*;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.*;
import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;

import java.io.*;
import java.util.Properties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataEmulatorGrid
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DataEmulatorGrid {
    private static final java.io.File FILE = new java.io.File(Base.JSAGA_VAR, "data-emulator.xml");
    private static DataEmulatorGrid _instance = null;
    private DataEmulator m_gridRoot;

    ////////////////////////////////////////// friend methods /////////////////////////////////////////

    static synchronized DataEmulatorGrid getInstance() throws FileNotFoundException, ValidationException, MarshalException {
        if (_instance == null) {
            _instance = new DataEmulatorGrid();
        }
        return _instance;
    }
    protected DataEmulatorGrid() throws FileNotFoundException, ValidationException, MarshalException {
        if (FILE.exists()) {
            m_gridRoot = (DataEmulator) Unmarshaller.unmarshal(DataEmulator.class, new InputStreamReader(new FileInputStream(FILE)));
        } else {
            m_gridRoot = new DataEmulator();
        }
    }

    /**
     * commit modification made on server
     */
    void commit() throws IOException, ValidationException, MarshalException {
        // marshal
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Properties prop = LocalConfiguration.getInstance().getProperties();
        prop.setProperty("org.exolab.castor.indent", "true");
        Marshaller.marshal(m_gridRoot, new OutputStreamWriter(buffer));
        // dump to file if marshaling did not throw any exception
        OutputStream out = new FileOutputStream(FILE);
        out.write(buffer.toByteArray());
        out.close();
    }

    /**
     * find the server
     */
    Server connect(String protocol, String host, int port) {
        return findServer(protocol, host, port);
    }

    /**
     * find the secure server
     */
    SecureServer connect(String protocol, String host, int port, UserPassSecurityAdaptor security) throws AuthenticationFailed, AuthorizationFailed {
        if (security == null) {
            throw new AuthenticationFailed("No security context found");
        }
        SecureServer s = findSecureServer(protocol, host, port);
        String login = security.getUserID();
        String password = security.getUserPass();
        for (int i=0; i<s.getUserCount(); i++) {
            if (s.getUser(i).getLogin().equals(login)) {
                if (s.getUser(i).getPassword().equals(password)) {
                    return s;    
                } else {
                    throw new AuthorizationFailed("Bad password for user: "+login);
                }
            }
        }
        throw new AuthorizationFailed("Unkown user: "+login);
    }

    private Server findServer(String protocol, String host, int port) {
        for (int i=0; i<m_gridRoot.getServerCount(); i++) {
            Server s = m_gridRoot.getServer(i);
            if (equals(s, protocol, host, port)) {
                return s;
            }
        }
        return createTestServer(protocol, host, port);
    }
    private SecureServer findSecureServer(String protocol, String host, int port) {
        for (int i=0; i<m_gridRoot.getSecureServerCount(); i++) {
            SecureServer s = m_gridRoot.getSecureServer(i);
            if (equals(s, protocol, host, port)) {
                return s;
            }
        }
        return createTestSecureServer(protocol, host, port);
    }
    private boolean equals(ServerType s, String protocol, String host, int port) {
        return equals(s.getProtocol(),protocol) && equals(s.getHost(),host) && equals(s.getPort(),port);
    }
    private boolean equals(String s1, String s2) {
        return (s1!=null && s1.equals(s2)) || (s1==null && s2==null);
    }
    private boolean equals(int s1, int s2) {
        return (s1!=0 && s1==s2) || (s1==0 && s2==0);
    }

    void disconnect(Server server) {
        releaseTestServer(server);
    }

    void disconnect(SecureServer server) {
        releaseTestSecureServer(server);
    }

    ////////////////////////////////////////// for unit test //////////////////////////////////////////

    /**
     * may temporarily create server
     */
    private Server createTestServer(String protocol, String host, int port) {
        if (host.endsWith(".test.org")) {
            Server server = new Server();
            server.setProtocol(protocol);
            server.setHost(host);
            server.setPort(port);
            server.setName("/");
            m_gridRoot.addServer(server);
            return server;
        } else {
            return null;
        }
    }

    /**
     * may temporarily create secure server
     */
    private SecureServer createTestSecureServer(String protocol, String host, int port) {
        if (host.endsWith(".test.org")) {
            SecureServer server = new SecureServer();
            server.setProtocol(protocol);
            server.setHost(host);
            server.setPort(port);
            server.setName("/");
            if (protocol.startsWith("s")) {
                User user = new User();
                user.setLogin("anonymous");
                user.setPassword("anon");
                server.addUser(user);
            }
            m_gridRoot.addSecureServer(server);
            return server;
        } else {
            return null;
        }
    }

    /**
     * may remove temporarily created server
     */
    private void releaseTestServer(Server server) {
        if (server.getHost().endsWith(".test.org")) {
            m_gridRoot.removeServer(server);
        }
    }

    /**
     * may remove temporarily created secure server
     */
    private void releaseTestSecureServer(SecureServer server) {
        if (server.getHost().endsWith(".test.org")) {
            m_gridRoot.removeSecureServer(server);
        }
    }
}
