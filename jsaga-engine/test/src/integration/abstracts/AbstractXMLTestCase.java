package integration.abstracts;

import org.custommonkey.xmlunit.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Arrays;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractXMLTestCase
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractXMLTestCase extends XMLTestCase {
    public AbstractXMLTestCase() {
        super();

        // configure XMLUnit
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);        
    }

    protected void assertXMLSimilarDetailed(InputStream expectedStream, Document checkedDOM) throws IOException, SAXException {
        assertXMLSimilarDetailed(null, expectedStream, checkedDOM);
    }
    protected void assertXMLSimilarDetailed(String jobName, InputStream expectedStream, Document checkedDOM) throws IOException, SAXException {
        if (expectedStream == null) {
            return;
        }
        String header = (jobName!=null ? "* "+jobName+" *"+System.getProperty("line.separator") : "");
        Document expectedDOM = XMLUnit.buildControlDocument(new InputSource(expectedStream));
        removeEmptyTextNodes(expectedDOM.getDocumentElement());
        removeEmptyTextNodes(checkedDOM.getDocumentElement());
        DetailedDiff myDiff = new DetailedDiff(compareXML(expectedDOM, checkedDOM));
        myDiff.overrideDifferenceListener(createDifferenceListener());
        List allDifferences = myDiff.getAllDifferences();
        assertEquals(header+myDiff.toString(), 0, allDifferences.size());
    }

    protected void assertXMLSimilar(InputStream expectedStream, Document checkedDOM) throws IOException, SAXException {
        assertXMLSimilar(null, expectedStream, checkedDOM);
    }
    protected void assertXMLSimilar(String jobName, InputStream expectedStream, Document checkedDOM) throws IOException, SAXException {
        if (expectedStream == null) {
            return;
        }
        String header = (jobName!=null ? "* "+jobName+" *"+System.getProperty("line.separator") : "");
        Document expectedDOM = XMLUnit.buildControlDocument(new InputSource(expectedStream));
        removeEmptyTextNodes(expectedDOM.getDocumentElement());
        removeEmptyTextNodes(checkedDOM.getDocumentElement());
        Diff myDiff = new Diff(expectedDOM, checkedDOM);
        myDiff.overrideDifferenceListener(createDifferenceListener());
        assertTrue(header+myDiff.toString(), myDiff.similar());
    }

    protected void assertXMLAlmostSimilar(InputStream expectedStream, Document checkedDOM) throws Exception {
        assertXMLAlmostSimilar(null, expectedStream, checkedDOM);
    }
    protected void assertXMLAlmostSimilar(String jobName, InputStream expectedStream, Document checkedDOM) throws Exception {
        if (expectedStream == null) {
            return;
        }
        String header = (jobName!=null ? "* "+jobName+" *"+System.getProperty("line.separator") : "");
        Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new ByteArrayInputStream((
"<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"+
"    <xsl:output method='xml' indent='yes'/>"+
"    <xsl:template match='/*'>"+
"        <xsl:element name='{name()}'>"+
"            <xsl:copy-of select='@*'/><xsl:apply-templates select='*'><xsl:sort select='@name'/></xsl:apply-templates>"+
"        </xsl:element>"+
"    </xsl:template>"+
"    <xsl:template match='*'>"+
"        <xsl:element name='{name()}'><xsl:copy-of select='@*'/><xsl:apply-templates/></xsl:element>"+
"    </xsl:template>"+
"</xsl:stylesheet>").getBytes())));
        Document expectedSorted = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Document checkedSorted = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        transformer.transform(new StreamSource(expectedStream), new DOMResult(expectedSorted));
        transformer.transform(new DOMSource(checkedDOM), new DOMResult(checkedSorted));
        Diff myDiff = new Diff(expectedSorted, checkedSorted);
        assertTrue(header+myDiff.toString(), myDiff.similar());
    }

    private static final int[] IGNORED_DIFFERENCES = new int[] {
        DifferenceConstants.NAMESPACE_PREFIX_ID
    };
    static {
        Arrays.sort(IGNORED_DIFFERENCES);
    }
    private static DifferenceListener createDifferenceListener() {
        return new DifferenceListener() {
            public int differenceFound(Difference difference) {
                return Arrays.binarySearch(IGNORED_DIFFERENCES, difference.getId()) >= 0
                    ? RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL
                    : RETURN_ACCEPT_DIFFERENCE;
            }
            public void skippedComparison(Node control, Node test) {
            }
        };
    }

    private static void removeEmptyTextNodes(Element elem) {
        NodeList list = elem.getChildNodes();
        for (int i=0; i<list.getLength(); ) {
            Node n = list.item(i);
            if (n.getNodeType()==Node.TEXT_NODE && n.getNodeValue().trim().equals("")) {
                elem.removeChild(n);
            } else {
                if (n.getNodeType()==Node.ELEMENT_NODE) {
                    removeEmptyTextNodes((Element) n);
                }
                i++;
            }
        }
    }

    public static void dump(Document doc) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty("{http://xml.apache.org/xalan/}indent-amount", "4");
        transformer.transform(new DOMSource(doc), new StreamResult(System.out));
    }
}
