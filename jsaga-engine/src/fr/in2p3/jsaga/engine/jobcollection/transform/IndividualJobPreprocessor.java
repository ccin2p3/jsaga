package fr.in2p3.jsaga.engine.jobcollection.transform;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.engine.jobcollection.preprocess.XMLDocument;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.Intermediary;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.Resource;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import fr.in2p3.jsaga.impl.job.description.XJSDLJobDescriptionImpl;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.w3c.dom.Document;

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
    private static final String XSL_1_ADD_FILESYSTEMS = "xsl/execution/job_1-add-filesystems.xsl";
    private static final String XSL_2_RESOLVE_PARENT_FS = "xsl/execution/job_2-resolve-parent-fs.xsl";
    private static final String XSL_3_GENERATE_STAGE = "xsl/execution/job_3-generate-stage.xsl";
    private static final String XSL_4_RESOLVE_FS = "xsl/execution/job_4-resolve-fs.xsl";
    private static final String XSL_1_WRAPPER_GENERATE = "xsl/execution/wrapper_1-generate.xsl";
    private static final String XSL_2_WRAPPER_SUBMIT = "xsl/execution/wrapper_2-submit.xsl";

    private Document m_effectiveJob;
    private File m_effectiveJobFile;
    private String m_wrapper;
    private File m_wrapperFile;

    public IndividualJobPreprocessor(XJSDLJobDescriptionImpl jobDesc, Resource rm) throws NotImplementedException, NoSuccessException {
        // Set stylesheet parameters
        Map<String,String> parameters = new HashMap<String,String>();
        parameters.put("resourceId", rm.getId());
        parameters.put("gridName", rm.getGrid());
        if(rm.getIntermediary()!=null) {
            Intermediary i = rm.getIntermediary();
            parameters.put("Intermediary", i.getContent());
            if(i.getWorkerMountPoint()!=null) parameters.put("workerMountPoint", i.getWorkerMountPoint());
        }
        if(rm.hasStandaloneWorker()) parameters.put("StandaloneWorker", ""+rm.getStandaloneWorker());
        if(rm.getTagCount()>0) parameters.put("Tag", rm.getTag(0));
        parameters.put("collectionName", jobDesc.getCollectionName());

        // Set base directory
        File baseDir = new File(new File(Base.JSAGA_VAR, "jobs"), jobDesc.getCollectionName());
        m_effectiveJobFile = new File(baseDir, jobDesc.getJobName()+".xml");
        m_wrapperFile = new File(baseDir, jobDesc.getJobName()+".sh");

        // Transform
        XSLTransformerFactory t = XSLTransformerFactory.getInstance();
        XMLDocument jobContainer = new XMLDocument(m_effectiveJobFile);
        XMLDocument jobWrapper = new XMLDocument(m_wrapperFile);
        try {
            jobContainer.set(t.getCached(XSL_1_ADD_FILESYSTEMS, parameters).transform(jobDesc.getAsDocument().getDocumentElement()));
            jobContainer.set(t.getCached(XSL_2_RESOLVE_PARENT_FS, parameters).transform(jobContainer.get()));
            jobContainer.set(t.getCached(XSL_3_GENERATE_STAGE, parameters).transform(jobContainer.get()));
            jobContainer.set(t.getCached(XSL_4_RESOLVE_FS, parameters).transform(jobContainer.get()));

            jobWrapper.set(t.getCached(XSL_1_WRAPPER_GENERATE, parameters).transform(jobContainer.get()));
            jobWrapper.set(new String(jobWrapper.get()).replaceAll("\\r\\n", "\n").getBytes());
            jobWrapper.save();
            m_wrapper = new String(jobWrapper.get());

            jobContainer.set(t.getCached(XSL_2_WRAPPER_SUBMIT, parameters).transform(jobContainer.get()));
            jobContainer.save();
            m_effectiveJob = jobContainer.getAsDocument();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    public Document getEffectiveJobDescription() {
        return m_effectiveJob;
    }

    public File getEffectiveJobFile() {
        return m_effectiveJobFile;
    }

    public String getWrapper() {
        return m_wrapper;
    }

    public File getWrapperFile() {
        return m_wrapperFile;
    }
}
