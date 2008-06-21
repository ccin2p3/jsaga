package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.engine.jobcollection.preprocess.XMLDocument;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.apache.log4j.Logger;
import org.ogf.saga.URL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionCleaner
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 juin 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionCleaner {
    private static final String XSL_1_GENERATE_CLEANUP = "xsl/execution/cleanup_1-generate.xsl";
    private static Logger s_logger = Logger.getLogger(JobCollectionCleaner.class);
    private Session m_session;
    private List<URL> m_files;
    private List<URL> m_directories;
    private File m_baseDir;

    /** constructor */
    public JobCollectionCleaner(Session session, String jobCollectionName) throws Exception {
        m_session = session;
        m_files = new ArrayList<URL>();
        m_directories = new ArrayList<URL>();
        m_baseDir = new File(new File(Base.JSAGA_VAR, "jobs"), jobCollectionName);
        if (m_baseDir.exists()) {
            // Transform to properties
            XSLTransformerFactory t = XSLTransformerFactory.getInstance();
            XMLDocument cleanupContainer = new XMLDocument(new File(m_baseDir, "tobecleaned.properties"));
            try {
                File statusFile = JobCollectionImpl.statusFile(jobCollectionName);
                cleanupContainer.set(t.getCached(XSL_1_GENERATE_CLEANUP).transform(new StreamSource(statusFile)));
                //cleanupContainer.save();
            } catch (Exception e) {
                throw new NoSuccess(e);
            }

            // Load generated properties
            Properties toBeCleaned = new Properties();
            toBeCleaned.load(new ByteArrayInputStream(cleanupContainer.get()));

            // Add properties to lists
            for (Enumeration<String> e= (Enumeration<String>) toBeCleaned.propertyNames(); e.hasMoreElements(); ) {
                String action = e.nextElement();
                String urlString = toBeCleaned.getProperty(action);
                URL url = new URL(urlString);
                if (action.startsWith("delete.")) {
                    m_files.add(url);
                } else if (action.startsWith("rmdir.")) {
                    m_directories.add(url);
                }
            }
        }
    }

    public void cleanup() {
        if (m_baseDir.exists()) {
            // Remove intermediary files
            for (Iterator<URL> it=m_files.iterator(); it.hasNext(); ) {
                URL url = it.next();
                try {
                    NSEntry entry = NSFactory.createNSEntry(m_session, url);
                    entry.remove();
                } catch(Exception e) {
                    s_logger.warn("Failed to cleanup file: "+url.toString(), e);
                }
            }

            // Remove intermediary directories
            for (Iterator<URL> it=m_directories.iterator(); it.hasNext(); ) {
                URL url = it.next();
                try {
                    NSDirectory directory = NSFactory.createNSDirectory(m_session, url);
                    directory.remove();
                } catch(Exception e) {
                    s_logger.warn("Failed to cleanup directory: "+url.toString(), e);
                }
            }

            // Remove monitoring files
            File[] files = m_baseDir.listFiles();
            for (int i=0; i<files.length; i++) {
                files[i].delete();
            }
            m_baseDir.delete();
        }
    }
}
