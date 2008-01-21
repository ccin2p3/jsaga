package fr.in2p3.jsaga.adaptor.language.abstracts;

import fr.in2p3.jsaga.adaptor.language.LanguageAdaptor;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractLanguageAdaptorProperties
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractLanguageAdaptorProperties implements LanguageAdaptor {
    private Set m_requiredNames;
    private Set m_propertyNames;
    private Set m_vectorPropertyNames;
    private String m_vectoryPropertySeparator;

    protected void _initParser(String[] requiredPropertyNames, String[] optionalPropertyNames,
                     String[] requiredVectorPropertyNames, String[] optionalVectorPropertyNames, String vectorPropertySeparator)
            throws IOException, TransformerConfigurationException
    {
        m_requiredNames = new HashSet();
        addAll(m_requiredNames, requiredPropertyNames);
        addAll(m_requiredNames, requiredVectorPropertyNames);

        m_propertyNames = new HashSet();
        addAll(m_propertyNames, requiredPropertyNames);
        addAll(m_propertyNames, optionalPropertyNames);

        m_vectorPropertyNames = new HashSet();
        addAll(m_vectorPropertyNames, requiredVectorPropertyNames);
        addAll(m_vectorPropertyNames, optionalVectorPropertyNames);

        m_vectoryPropertySeparator = (vectorPropertySeparator!=null ? vectorPropertySeparator : ",");
    }

    public Document parseJobDescription(InputStream jobDescStream) throws BadParameter, NoSuccess {
        // load properties
        Properties prop = new Properties();
        try {
            prop.load(jobDescStream);
        } catch (IOException e) {
            throw new NoSuccess(e);
        }

        // check if required elements are present
        for (Iterator it=m_requiredNames.iterator(); it.hasNext(); ) {
            String name = (String) it.next();
            String value = prop.getProperty(name);
            if (value == null) {
                throw new BadParameter("Missing required attribute: "+name);
            }
        }

        // build DOM
        Document jobDescDOM;
        try {
            jobDescDOM = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new NoSuccess(e);
        }
        Element root = jobDescDOM.createElement("attributes");
        jobDescDOM.appendChild(root);
        for (Iterator it=prop.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (this.isProperty(name)) {
                Element attribute = jobDescDOM.createElement("attribute");
                attribute.setAttribute("name", name);
                attribute.setAttribute("value", value);
                root.appendChild(attribute);
            } else if (this.isVectoryProperty(name)) {
                Element vectorAttribute = jobDescDOM.createElement("vectorAttribute");
                vectorAttribute.setAttribute("name", name);
                String[] values = value.split(m_vectoryPropertySeparator);
                for (int i=0; i<values.length; i++) {
                    Element item = jobDescDOM.createElement("value");
                    item.appendChild(jobDescDOM.createTextNode(values[i]));
                    vectorAttribute.appendChild(item);
                }
                root.appendChild(vectorAttribute);
            } else {
                throw new BadParameter("Unexpected attribute name: "+name);
            }
        }

        // returns
        return jobDescDOM;
    }

    public boolean isProperty(String name) {
        return m_propertyNames.contains(name);
    }

    public boolean isVectoryProperty(String name) {
        return m_vectorPropertyNames.contains(name);
    }

    private static void addAll(Set set, String[] array) {
        for (int i=0; array!=null && i<array.length; i++) {
            set.add(array[i]);
        }
    }
}
