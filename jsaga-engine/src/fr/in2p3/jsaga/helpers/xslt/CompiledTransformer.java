package fr.in2p3.jsaga.helpers.xslt;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.helpers.XMLFileParser;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import java.util.Iterator;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   CompiledTransformer
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 <plugins>
     <plugin>
         <groupId>org.codehaus.mojo</groupId>
         <artifactId>maven-plugin-xsltc</artifactId>
         <version>1.0-SNAPSHOT</version>
         <executions>
             <execution>
                 <id>xsltc</id>
                 <goals><goal>compile</goal></goals>
             </execution>
         </executions>
         <configuration>
             <stylesheets>
                 <stylesheet>${basedir}/resources/xsl/jsaga-default-contexts-merge.xsl</stylesheet>
                 <stylesheet>${basedir}/resources/xsl/jsaga-default-contexts.xsl</stylesheet>
             </stylesheets>
             <packageName>fr.in2p3.jsaga.generated.xsl</packageName>
             <outputDirectory>${project.build.directory}/generated-classes</outputDirectory>
         </configuration>
     </plugin>
 </plugins>
 <resources>
     <resource><directory>${project.build.directory}/generated-classes</directory></resource>
     <resource><directory>resources</directory></resource>
 </resources>
 */
public class CompiledTransformer {
    private Templates m_translet;

    public CompiledTransformer(String packageName, String transletName) throws TransformerConfigurationException, IOException {
        // load translet
        TransformerFactory tFactory;
        // thread-safe instanciation not supported by JDK 1.5
//        tFactory = TransformerFactory.newInstance("org.apache.xalan.xsltc.trax.TransformerFactoryImpl", CompiledTransformer.class.getClassLoader());
        synchronized (this) {
            // save transformer implementation
            final String TRANSFORMER_IMPL = "javax.xml.transform.TransformerFactory";
            String savImpl = System.getProperty(TRANSFORMER_IMPL);
            System.setProperty(TRANSFORMER_IMPL, "org.apache.xalan.xsltc.trax.TransformerFactoryImpl");

            // create transformer factory
            tFactory = TransformerFactory.newInstance();

            // restore transformer implementation
            if (savImpl != null) {
                System.setProperty(TRANSFORMER_IMPL, savImpl);
            } else {
                System.getProperties().remove(TRANSFORMER_IMPL);
            }
        }
        tFactory.setAttribute("debug", "true");
        tFactory.setAttribute("package-name", packageName);
        tFactory.setAttribute("translet-name", transletName);
        tFactory.setAttribute("use-classpath", "true");
        m_translet = tFactory.newTemplates(null);
    }

    public Document transformToDOM(Document xml, File debugFile) throws TransformerException, IOException {
        // create result container
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new TransformerException(e);
        }
        Result result = new DOMResult(doc);

        // create transformer
        Transformer transformer = m_translet.newTransformer();
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setURIResolver(new TransformerURIResolver());
        transformer.setErrorListener(new XSLLogger());
        for (Iterator it = EngineProperties.getProperties().entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            transformer.setParameter((String) entry.getKey(), entry.getValue());
        }

        // process
        try {
            transformer.transform(new DOMSource(xml), result);
        } catch(TransformerException e) {
            // throw the cause of exception
            transformer.getErrorListener().fatalError(e);
        }

        // debug
        if (Base.DEBUG && debugFile!=null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLFileParser.dump(doc, out);
            byte[] outBytes = out.toByteArray();
            if (outBytes!=null) {
                OutputStream f = new FileOutputStream(debugFile);
                f.write(outBytes);
                f.close();
            }
        }

        // return
        return doc;
    }
}