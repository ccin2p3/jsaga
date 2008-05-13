package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.Base;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GraphGenerator
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GraphGenerator {
    private String m_collectionName;
    private Document m_xmlStatus;

    public GraphGenerator(String collectionName, Document xmlStatus) {
        m_collectionName = collectionName;
        m_xmlStatus = xmlStatus;
    }

    public File generateStatusGraph() throws Exception {
        File baseDir = new File(Base.JSAGA_VAR, "status-graph/");
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
        File graph = new File(baseDir, m_collectionName+".gif");
        this.generateGraph("xsl/graphviz/jsaga-status-graph.xsl", graph);
        return graph;
    }

    public File generateStagingGraph() throws Exception {
        File baseDir = new File(Base.JSAGA_VAR, "staging-graph/");
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }
        File graph = new File(baseDir, m_collectionName+".gif");
        this.generateGraph("xsl/graphviz/jsaga-staging-graph.xsl", graph);
        return graph;
    }

    private void generateGraph(String stylesheet, File graph) throws Exception {
        ByteArrayOutputStream dotStream = new ByteArrayOutputStream();
        TransformerFactory.newInstance().newTransformer(
                new StreamSource(GraphGenerator.class.getClassLoader().getResourceAsStream(stylesheet))
        ).transform(
                new DOMSource(m_xmlStatus),
                new StreamResult(dotStream));
        File dot;
        try {
            dot = getDot(Base.JSAGA_HOME);
        } catch(FileNotFoundException e) {
            dot = getDot(new File("externals/graphviz/config"));
        }
        //Process p = Runtime.getRuntime().exec("f:/cygwin/bin/cat.exe");
        Process p = Runtime.getRuntime().exec(dot.getAbsolutePath()+" -Tgif -o\""+graph.getAbsolutePath()+"\"");
        OutputStream stdin = p.getOutputStream();
        dotStream.writeTo(stdin);
        stdin.close();
        InputStream stdout = p.getInputStream();
        copy(stdout, System.out);
        stdout.close();
        if (p.waitFor() != 0) {
            ByteArrayOutputStream error = new ByteArrayOutputStream();
            InputStream stderr = p.getErrorStream();
            copy(stderr, error);
            stderr.close();
            throw new Exception(error.toString());
        }
    }

    private static File getDot(File baseDir) throws FileNotFoundException {
        File dotFile;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("windows") != -1) {
            dotFile = new File(baseDir, "lib/win32/dot.exe");
        } else {
            dotFile = new File(baseDir, "lib/linux/dot");
        }
        if (dotFile.exists()) {
            return dotFile;
        } else {
            throw new FileNotFoundException("You must install the Graphviz module to use this option");
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        for (int len; (len=in.read(buffer))>-1; ) {
            out.write(buffer, 0, len);
        }
    }
}
