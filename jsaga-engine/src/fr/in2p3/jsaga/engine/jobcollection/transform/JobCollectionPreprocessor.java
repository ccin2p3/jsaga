package fr.in2p3.jsaga.engine.jobcollection.transform;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.engine.jobcollection.preprocess.XMLDocument;
import fr.in2p3.jsaga.engine.jobcollection.preprocess.XPathSelector;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.ogf.saga.error.NoSuccess;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionPreprocessor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   1 mai 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionPreprocessor {
    private static final String XML_JOB_TEMPLATE = "job-template.xml";
    private static final String XML_PRESTAGE_GRAPH = "prestage-graph.xml";

    private static final String XSL_1_ADD_DEFAULTS = "xsl/execution/collec_1-add-defaults.xsl";
    private static final String XSL_2_GENERATE_PRESTAGE = "xsl/execution/collec_2-generate-prestage.xsl";
    private static final String XSL_UPDATE_GRAPH = "xsl/execution/graph_1-update.xsl";

    private byte[] m_effectiveJobCollection;
    private byte[] m_prestageGraph;

    public JobCollectionPreprocessor(Document jobCollecDesc) throws NoSuccess {
        // Get job collection identifier
        String collectionName;
        try {
            collectionName = XPathSelector.select(jobCollecDesc, "/ext:JobCollection/ext:JobCollectionDescription/ext:JobCollectionIdentification/ext:JobCollectionName/text()");
            if (collectionName == null) {
                collectionName = XPathSelector.select(jobCollecDesc, "/ext:JobCollection/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName/text()");
                if (collectionName == null) {
                    collectionName = UUID.randomUUID().toString();
                }
            }
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
        Map parameters = new HashMap();
        parameters.put("collectionName", collectionName);

        // Set base directory
        File baseDir = new File(Base.JSAGA_VAR, "jobs");
        if(!baseDir.exists()) baseDir.mkdir();
        baseDir = new File(baseDir, collectionName);
        if(baseDir.exists()) {
            throw new NoSuccess("Collection already exists: "+collectionName+", please clean it up first.");
        } else {
            baseDir.mkdir();
        }

        // Transform
        XSLTransformerFactory t = XSLTransformerFactory.getInstance();
        XMLDocument collectionContainer = new XMLDocument(new File(baseDir, XML_JOB_TEMPLATE));
        XMLDocument prestageContainer = new XMLDocument(new File(baseDir, XML_PRESTAGE_GRAPH));
        try {
            collectionContainer.set(t.getCached(XSL_1_ADD_DEFAULTS).transform(jobCollecDesc.getDocumentElement()));
            collectionContainer.set(t.getCached(XSL_2_GENERATE_PRESTAGE, parameters).transform(collectionContainer.get()));
            collectionContainer.save();

            prestageContainer.set(t.getCached(XSL_UPDATE_GRAPH).transform(collectionContainer.get()));
            prestageContainer.save();

            m_effectiveJobCollection = collectionContainer.get();
            m_prestageGraph = prestageContainer.get();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    public byte[] getEffectiveJobCollection() {
        return m_effectiveJobCollection;
    }

    public byte[] getPrestageGraph() {
        return m_prestageGraph;
    }
}