package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.introspector.Introspector;
import fr.in2p3.jsaga.introspector.IntrospectorFactory;
import org.ogf.saga.error.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PostInstall
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class PostInstall {
    private static enum PostInstallType {security, data, job}
    private String m_scheme;                private static final String SCHEME = "scheme";
    private Introspector m_introspector;    private static final String TYPE = "type";
    private String m_osName;                private static final String OS_NAME = "os.name";
    private File[] m_preInstalledFiles;     private static final String PREINSTALLED_FILES = "pre-installed.files";
    private String m_preInstalledMessage;   private static final String PREINSTALLED_MESSAGE = "pre-installed.message";
    private boolean m_superUser;            private static final String SUPER_USER = "super-user";
    private URL m_script;                   private static final String SCRIPT = "script";
    private String[] m_arguments;           private static final String ARGUMENTS = "arguments";
    private File[] m_postInstalledFiles;    private static final String POSTINSTALLED_FILES = "post-installed.files";

    public static void main(String[] args) throws Exception {
        if (args.length > 1) {
            System.err.println("usage: jsaga-post-install [<plug-in>]");
            System.exit(1);
        }
        Map<String,PostInstall> map = new HashMap<String,PostInstall>();
        for (Enumeration e=PostInstall.class.getClassLoader().getResources("META-INF/post-install.properties"); e.hasMoreElements(); ) {
            PostInstall pi = new PostInstall((URL) e.nextElement());
            map.put(pi.m_scheme, pi);
        }
        switch(args.length) {
            case 0:
                for (PostInstall pi : map.values()) {
                    String message;
                    try {
                        if (pi.isPostInstalled()) {
                            message = "OK";
                        } else {
                            message = "To be post-installed";
                        }
                    } catch(Exception e) {
                        message = e.getMessage();
                    }
                    System.out.println(pi.m_scheme+"\t\t"+message);
                }
                break;
            case 1:
                PostInstall pi = map.get(args[0]);
                pi.dumpScript();
                break;
        }
    }

    public PostInstall(URL resource) throws Exception {
        Properties prop = new Properties();
        InputStream stream = resource.openStream();
        prop.load(stream);
        stream.close();
        m_scheme = prop.getProperty(SCHEME);
        m_introspector = getIntrospector(m_scheme, prop.getProperty(TYPE));
        m_osName = prop.getProperty(OS_NAME);
        m_preInstalledFiles = toFilesArray(prop.getProperty(PREINSTALLED_FILES));
        m_preInstalledMessage = prop.getProperty(PREINSTALLED_MESSAGE);
        m_superUser = "true".equalsIgnoreCase(prop.getProperty(SUPER_USER));
        m_script = PostInstall.class.getClassLoader().getResource(prop.getProperty(SCRIPT));
        m_arguments = evalAttributes(prop.getProperty(ARGUMENTS)).split(" ");
        m_postInstalledFiles = toFilesArray(evalAttributes(prop.getProperty(POSTINSTALLED_FILES)));
    }

    ///////////////////////////////////////////// public ////////////////////////////////////////////

    /**
     * @return true if is post-installed, else false
     * @throws Exception if can not be post-installed
     */
    public boolean isPostInstalled() throws Exception {
        if (exist(m_postInstalledFiles)) {
            return true;
        } else {
            // check if can be post-installed
            String os = System.getProperty("os.name");
            if (m_osName!=null && !os.startsWith(m_osName)) {
                throw new Exception("This plug-in only supports OS: "+m_osName);
            }
            if (! exist(m_preInstalledFiles)) {
                throw new Exception(m_preInstalledMessage);
            }
            if (m_superUser) {
                if ("Linux".equalsIgnoreCase(os) && !new File("/proc/kmsg").canRead()) {
                    throw new Exception("You must be a super-user (root) to post-install this plug-in");
                } else if ("Windows".equalsIgnoreCase(os) && !new File("C:\\MSDOS.SYS").canRead()) {
                    throw new Exception("You must be a super-user (Administrator) to post-install this plug-in");
                }
            }
            return false;
        }
    }

    public void dumpScript() throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(m_script.openStream()));
        while( (line=reader.readLine()) != null ) {
            for (int i=0; m_arguments!=null && i<m_arguments.length; i++) {
                line = line.replaceAll("\\$"+(i+1), m_arguments[i]);
            }
            System.out.println(line);
        }
        reader.close();
    }

    //////////////////////////////////////////// private ////////////////////////////////////////////

    private static final Pattern s_pattern = Pattern.compile("\\$\\{(.*)\\}");
    private String evalAttributes(String value) throws Exception {
        Matcher matcher = s_pattern.matcher(value);
        for (int i=0; matcher.find() && i<matcher.groupCount(); i++) {
            String attributeName = matcher.group(i+1);
            String attributeValue = m_introspector.getAttribute(attributeName);
            value = value.replaceFirst("\\$\\{"+attributeName+"\\}", attributeValue);
        }
        return value;
    }

    private static Introspector getIntrospector(String scheme, String type) throws NotImplementedException, DoesNotExistException, NoSuccessException {
        switch (PostInstallType.valueOf(type)) {
            case security:
                throw new NotImplementedException("Security contexts introspection not yet implemented"); //TODO
            case data:
                Introspector nsIntr = IntrospectorFactory.createNSIntrospector();
                return nsIntr.getChildIntrospector(scheme);
            case job:
                Introspector jobIntr = IntrospectorFactory.createJobIntrospector();
                return jobIntr.getChildIntrospector(scheme);
            default:
                throw new NoSuccessException("Unexpected type: "+type);
        }
    }

    private static File[] toFilesArray(String paths) {
        String[] pathsArray = paths.split(" ");
        File[] filesArray = new File[pathsArray.length];
        for (int i=0; i<filesArray.length; i++) {
            filesArray[i] = new File(pathsArray[i]);
        }
        return filesArray;
    }

    private static boolean exist(File[] files) {
        for (int i=0; files!=null && i<files.length; i++) {
            if (! files[i].exists()) {
                return false;
            }
        }
        return true;
    }
}
