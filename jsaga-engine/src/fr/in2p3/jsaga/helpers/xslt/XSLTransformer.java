package fr.in2p3.jsaga.helpers.xslt;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.helpers.XMLFileParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   XSLTransformer
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   6 mai 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class XSLTransformer {
    private Transformer m_transformer;
    private File m_debugFile;

    public XSLTransformer(Transformer transformer, File debugFile) {
        m_transformer = transformer;
        m_debugFile = debugFile;
    }

    public byte[] transform(Element xmlInput) throws TransformerException, IOException {
        return transform(new DOMSource(xmlInput));
    }

    public byte[] transform(byte[] xmlInput) throws TransformerException, IOException {
        return transform(new StreamSource(new ByteArrayInputStream(xmlInput)));
    }

    public Document transformToDOM(Document xmlInput) throws TransformerException, IOException {
        return transformToDOM(new DOMSource(xmlInput));
    }

    public Document transformToDOM(byte[] xmlInput) throws TransformerException, IOException {
        return transformToDOM(new StreamSource(new ByteArrayInputStream(xmlInput)));
    }

    private byte[] transform(Source source) throws TransformerException, IOException {
        // transform
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Result result = new StreamResult(out);
        m_transformer.transform(source, result);
        byte[] outBytes = out.toByteArray();

        // debug
        if (Base.DEBUG && m_debugFile!=null) {
            if (outBytes!=null) {
                OutputStream f = new FileOutputStream(m_debugFile);
                f.write(outBytes);
                f.close();
            }
        }

        // return
        return outBytes;
    }

    private Document transformToDOM(Source source) throws TransformerException, IOException {
        // transform
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new TransformerException(e);
        }
        Result result = new DOMResult(doc);
        m_transformer.transform(source, result);

        // debug
        if (Base.DEBUG && m_debugFile!=null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLFileParser.dump(doc, out);
            byte[] outBytes = out.toByteArray();
            if (outBytes!=null) {
                OutputStream f = new FileOutputStream(m_debugFile);
                f.write(outBytes);
                f.close();
            }
        }

        // return
        return doc;
    }
}
