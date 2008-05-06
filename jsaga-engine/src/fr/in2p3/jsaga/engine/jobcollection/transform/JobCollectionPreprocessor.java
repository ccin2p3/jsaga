package fr.in2p3.jsaga.engine.jobcollection.transform;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.engine.jobcollection.preprocess.XMLDocument;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.ogf.saga.error.NoSuccess;
import org.w3c.dom.Document;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    private static final String XSL_1_ADD_DEFAULTS = "xsl/execution/collec_1-add-defaults.xsl";
    private static final String XSL_2_GENERATE_PRESTAGE = "xsl/execution/collec_2-generate-prestage.xsl";

    private Document m_effectiveJobCollection;

    public JobCollectionPreprocessor(Document jobCollecDesc, String collectionName) throws NoSuccess {
        // Set stylesheet parameters
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
        try {
            collectionContainer.set(t.getCached(XSL_1_ADD_DEFAULTS).transform(jobCollecDesc.getDocumentElement()));
            collectionContainer.set(t.getCached(XSL_2_GENERATE_PRESTAGE, parameters).transform(collectionContainer.get()));
            collectionContainer.save();
            m_effectiveJobCollection = collectionContainer.getAsDocument();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    public Document getEffectiveJobCollection() {
        return m_effectiveJobCollection;
    }
}