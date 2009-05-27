package fr.in2p3.jsaga.impl.job.description;

import fr.in2p3.jsaga.adaptor.language.SAGALanguageAdaptor;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformer;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.JobDescription;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SAGAJobDescriptionImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SAGAJobDescriptionImpl extends AbstractJobDescriptionImpl implements JobDescription {
    private static SAGALanguageAdaptor s_adaptor = new SAGALanguageAdaptor();
    static {
        s_adaptor.initParser();
    }

    public Document getAsDocument() throws NoSuccessException {
        // build DOM
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = document.createElement("attributes");
            document.appendChild(root);
            String[] attributeNames = super.listAttributes();
            for (int i=0; i<attributeNames.length; i++) {
                String key = attributeNames[i];
                Element elem = document.createElement(key);
                if (super.isVectorAttribute(key)) {
                    String[] values = super.getVectorAttribute(key);
                    Element item = document.createElement("value");
                    for (int v=0; v<values.length; v++) {
                        item.appendChild(document.createTextNode(values[v]));
                    }
                    elem.appendChild(item);
                } else {
                    String value = super.getAttribute(key);
                    elem.setAttribute("value", value);
                }
                root.appendChild(elem);
            }
        } catch (NoSuccessException e) {
            throw e;
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }

        // transform to JSDL
        String stylesheet = s_adaptor.getTranslator();
        if (stylesheet == null) {
            throw new NoSuccessException("[INTERNAL ERROR] Stylesheet is null");
        }
        try {
            XSLTransformer t = XSLTransformerFactory.getInstance().getCached(stylesheet);
            return t.transformToDOM(document);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }
}
