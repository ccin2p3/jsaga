package fr.in2p3.jsaga.engine.jobcollection.transform;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.engine.jobcollection.preprocess.XMLDocument;
import fr.in2p3.jsaga.engine.jobcollection.preprocess.XPathSelector;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.ogf.saga.error.NoSuccess;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.UUID;

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
    private static final String XML_JOBS_DESCRIPTION = "index.xml";
    private static final String XML_PRESTAGE_GRAPH = "prestage-graph.xml";
    private static final String XML_STAGE_GRAPH = "stage-graph.xml";

    private static final String XSL_1_ADD_DEFAULTS = "xsl/execution/collec_1-add-defaults.xsl";
    private static final String XSL_2_GENERATE_PRESTAGE = "xsl/execution/collec_2-generate-prestage.xsl";
    private static final String XSL_3_SPLIT_PARAMETRIC = "xsl/execution/collec_3-split-parametric.xsl";
    private static final String XSL_UPDATE_GRAPH = "xsl/execution/graph_1-update.xsl";

    private Document m_jsdl;
    private XSLTransformerFactory m_t;

    private XMLDocument m_jobsDescription;
    private XMLDocument m_prestageGraph;
    private XMLDocument m_stageGraph;

    public JobCollectionPreprocessor(Document jobsDescription) throws NoSuccess {
        // Job description DOM tree
        m_jsdl = jobsDescription;

        // Get job collection identifier
        String id;
        try {
            id = XPathSelector.select(m_jsdl, "/ext:JobCollection/ext:JobCollectionDescription/ext:JobCollectionIdentification/ext:JobCollectionName/text()");
            if (id == null) {
                id = XPathSelector.select(m_jsdl, "/ext:JobCollection/ext:Job/jsdl:JobDefinition/jsdl:JobDescription/jsdl:JobIdentification/jsdl:JobName/text()");
                if (id == null) {
                    id = UUID.randomUUID().toString();
                }
            }
        } catch (XPathExpressionException e) {
            throw new NoSuccess(e);
        }
        File baseDir = new File(Base.JSAGA_VAR, "jobs");
        if(!baseDir.exists()) baseDir.mkdir();
        baseDir = new File(baseDir, id);
        if(!baseDir.exists()) baseDir.mkdir();

        // Transformer factory
        m_t = XSLTransformerFactory.getInstance();
        
        // XML files
        m_jobsDescription = new XMLDocument(new File(baseDir, XML_JOBS_DESCRIPTION));
        m_prestageGraph = new XMLDocument(new File(baseDir, XML_PRESTAGE_GRAPH));
        m_stageGraph = new XMLDocument(new File(baseDir, XML_STAGE_GRAPH));
    }

    public byte[] preprocess() throws NoSuccess {
        try {
            m_jobsDescription.set(m_t.getCached(XSL_1_ADD_DEFAULTS).transform(m_jsdl.getDocumentElement()));
            m_jobsDescription.set(m_t.getCached(XSL_2_GENERATE_PRESTAGE).transform(m_jobsDescription.get()));
            m_jobsDescription.save();
            return m_jobsDescription.get();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    public void allocate(byte[] resources) throws Exception {
        m_prestageGraph.set(m_t.getCached(XSL_UPDATE_GRAPH).transform(m_jobsDescription.get()));
    }
}