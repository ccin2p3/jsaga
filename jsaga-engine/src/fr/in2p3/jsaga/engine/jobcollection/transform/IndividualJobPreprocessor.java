package fr.in2p3.jsaga.engine.jobcollection.transform;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.engine.jobcollection.preprocess.XMLDocument;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.Resource;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import fr.in2p3.jsaga.impl.job.description.XJSDLJobDescriptionImpl;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IndividualJobPreprocessor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 avr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IndividualJobPreprocessor {
    private static final String XML_STAGE_GRAPH = "stage-graph.xml";

    private static final String XSL_1_ADD_FILESYSTEMS = "xsl/execution/job_1-add-filesystems.xsl";
    private static final String XSL_2_RESOLVE_PARENT_FS = "xsl/execution/job_2-resolve-parent-fs.xsl";
    private static final String XSL_3_GENERATE_STAGE = "xsl/execution/job_3-generate-stage.xsl";
    private static final String XSL_4_RESOLVE_FS = "xsl/execution/job_4-resolve-fs.xsl";
    private static final String XSL_UPDATE_GRAPH = "xsl/execution/graph_1-update.xsl";

    private Document m_effectiveJob;
    private byte[] m_stageGraph;

    public IndividualJobPreprocessor(XJSDLJobDescriptionImpl jobDesc, Resource rm) throws NotImplemented, NoSuccess {
        // Set stylesheet parameters
        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("resourceId", rm.getId());
        parameters.put("gridName", rm.getGrid());
        if(rm.getIntermediary()!=null) parameters.put("Intermediary", rm.getIntermediary());
        if(rm.hasStandaloneWorker()) parameters.put("StandaloneWorker", ""+rm.getStandaloneWorker());
        if(rm.getTagCount()>0) parameters.put("Tag", rm.getTag(0));
        parameters.put("collectionName", jobDesc.getCollectionName());

        // Set base directory
        File baseDir = new File(Base.JSAGA_VAR, "jobs");
        if(!baseDir.exists()) baseDir.mkdir();
        baseDir = new File(baseDir, jobDesc.getCollectionName());
        if(!baseDir.exists()) baseDir.mkdir();

        // Transform
        XSLTransformerFactory t = XSLTransformerFactory.getInstance();
        XMLDocument jobContainer = new XMLDocument(new File(baseDir, jobDesc.getJobName()+".xml"));
        XMLDocument stageContainer = new XMLDocument(new File(baseDir, XML_STAGE_GRAPH));
        try {
            jobContainer.set(t.getCached(XSL_1_ADD_FILESYSTEMS, parameters).transform(jobDesc.getAsDocument().getDocumentElement()));
            jobContainer.set(t.getCached(XSL_2_RESOLVE_PARENT_FS, parameters).transform(jobContainer.get()));
            jobContainer.set(t.getCached(XSL_3_GENERATE_STAGE, parameters).transform(jobContainer.get()));
            jobContainer.set(t.getCached(XSL_4_RESOLVE_FS, parameters).transform(jobContainer.get()));
            jobContainer.save();

            stageContainer.set(t.getCached(XSL_UPDATE_GRAPH).transform(jobContainer.get()));
            stageContainer.save();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            m_effectiveJob = factory.newDocumentBuilder().parse(new ByteArrayInputStream(jobContainer.get()));
            m_stageGraph = stageContainer.get();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    public Document getEffectiveJobDescription() {
        return m_effectiveJob;
    }

    public byte[] getStageGraph() {
        return m_stageGraph;
    }
}
