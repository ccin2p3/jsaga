package fr.in2p3.jsaga.helpers;

import fr.in2p3.jsaga.Base;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   XMLFileParser
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 avr. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class XMLFileParser {
    /**
     * Note: DocumentBuilderFactory is thread-safe, while DocumentBuilder is not.
     */
    private DocumentBuilderFactory m_parser;

    public XMLFileParser(String[] schemaResourcePaths) throws IOException {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.jaxp.SAXParserFactoryImpl");
        System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");
        m_parser = DocumentBuilderFactory.newInstance();
        m_parser.setNamespaceAware(true);
        if (schemaResourcePaths!=null && schemaResourcePaths.length>0) {
            extractSchemaFiles(schemaResourcePaths);
            m_parser.setValidating(true);
            m_parser.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema");
            m_parser.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
                    new File(Base.JSAGA_VAR, schemaResourcePaths[0]).getAbsolutePath());
        } else {
            m_parser.setValidating(false);
        }
    }

    public XMLFileParser() throws IOException {
        this(null);
    }

    /**
     * Note: included files are relative to the parent directory of this file.
     */
    public byte[] xinclude(File xmlFile) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        ByteArrayOutputStream dump = new ByteArrayOutputStream();
        dump(m_parser.newDocumentBuilder().parse(xmlFile), dump);
        return dump.toByteArray();
    }

    /**
     * Note: included files are relative to the working directory of the application.
     */
    public Document parse(InputStream xmlStream, File dumpMergedFile) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        Document doc;
        if (m_parser.isValidating()) {
            DocumentBuilder builder = m_parser.newDocumentBuilder();
            XMLFileParserExceptionHandler handler = new XMLFileParserExceptionHandler();
            builder.setErrorHandler(handler);
            doc = builder.parse(xmlStream);
            if(handler.getSAXParseException() != null) {
                SAXParseException e = handler.getSAXParseException();
                String filename;
                if (e.getSystemId() != null) {
                    filename = new File(e.getSystemId()).getName();
                } else {
                    filename = dumpMergedFile.getName();
                    if (dumpMergedFile.getParentFile().mkdirs()) {
                        dump(doc, new FileOutputStream(dumpMergedFile));
                    }
                }
                throw new SAXException("["+filename+": "+e.getLineNumber()+"] "+e.getMessage());
            }
        } else {
            DocumentBuilder builder = m_parser.newDocumentBuilder();
            doc = builder.parse(xmlStream);
        }
        return doc;
    }

    public static void dump(Document doc, OutputStream out) throws TransformerException {
        dump(doc.getDocumentElement(), out);
    }
    public static void dump(Element doc, OutputStream out) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty("{http://xml.apache.org/xalan/}indent-amount", "4");
        transformer.transform(new DOMSource(doc), new StreamResult(out));
    }

    private void extractSchemaFiles(String[] schemaResourcePaths) throws IOException {
        for (int i=0; schemaResourcePaths!=null && i<schemaResourcePaths.length; i++) {
            String resourcePath = schemaResourcePaths[i];
            File schemaFile = new File(Base.JSAGA_VAR, resourcePath);
            if (!schemaFile.exists()) {
                InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (in == null) {
                    throw new IOException("Schema resource not found: "+resourcePath);
                }
                schemaFile.getParentFile().mkdirs();
                copyBytes(in, new FileOutputStream(schemaFile));
            }
        }
    }

    private void copyBytes(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while((len=in.read(buffer))>0) {
            out.write(buffer, 0, len);
        }
        out.close();
        in.close();
    }
}
