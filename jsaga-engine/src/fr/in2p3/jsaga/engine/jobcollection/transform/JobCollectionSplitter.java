package fr.in2p3.jsaga.engine.jobcollection.transform;

import fr.in2p3.jsaga.adaptor.evaluator.Evaluator;
import fr.in2p3.jsaga.engine.schema.jsdl.extension.*;
import fr.in2p3.jsaga.impl.job.description.XJSDLJobDescriptionImpl;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionSplitter
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionSplitter {
    private JobCollectionDescription m_jobCollectionBean;
    private XJSDLJobDescriptionImpl[] m_individualJobArray;

    public JobCollectionSplitter(byte[] jcBytes, Evaluator evaluator) throws NoSuccess {
        try {
            // marshall <JobCollection>
            InputSource jcSource = new InputSource(new ByteArrayInputStream(jcBytes));
            JobCollection jcBean = (JobCollection) Unmarshaller.unmarshal(JobCollection.class, jcSource);

            // set m_jobCollectionBean
            m_jobCollectionBean = jcBean.getJobCollectionDescription();

            // set collectionName
            String collectionName = m_jobCollectionBean.getJobCollectionIdentification().getJobCollectionName();

            // set jobNameTemplate
            String jobNameTemplate = jcBean.getJob(0).getJobDefinition().getJobDescription().getJobIdentification().getJobName();

            // set jobTemplate
            if (jcBean.getJobCount() == 0) {
                throw new NoSuccess("Found no <Job> in description");
            } else if (jcBean.getJobCount() > 1) {
                throw new NoSuccess("Several <Job> in description is not supported");
            }
            Job jobBean = jcBean.getJob(0);
            ByteArrayOutputStream jobBytes = new ByteArrayOutputStream();
            Marshaller m = new Marshaller(new OutputStreamWriter(jobBytes));
            m.setNamespaceMapping("ext", "http://www.in2p3.fr/jsdl-extension");
            m.setNamespaceMapping(null, "http://schemas.ggf.org/jsdl/2005/11/jsdl");
            m.marshal(jobBean);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document jobTemplate = factory.newDocumentBuilder().parse(new ByteArrayInputStream(jobBytes.toByteArray()));

            // split parametric job
            Parametric p = m_jobCollectionBean.getParametric();
            m_individualJobArray = new XJSDLJobDescriptionImpl[p.getCount()];
            for (int i=0; i<p.getCount(); i++) {
                int index = p.getStart() + i*p.getStep();

                // clone job
                Document clone = (Document) jobTemplate.cloneNode(true);

                // evaluate expressions
                evaluator.init(index);
                String jobName = evaluate(jobNameTemplate, evaluator);
                evaluate(clone.getDocumentElement(), evaluator);

                // add to array
                m_individualJobArray[i] = new XJSDLJobDescriptionImpl(collectionName, jobName, clone);
            }
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    public JobCollectionDescription getJobCollectionBean() {
        return m_jobCollectionBean;
    }

    public XJSDLJobDescriptionImpl[] getIndividualJobArray() {
        return m_individualJobArray;
    }

    private static void evaluate(Element current, Evaluator evaluator) throws BadParameter {
        NodeList list = current.getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            Node n = list.item(i);
            switch(n.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element elem = (Element) n;
                    evaluate(elem, evaluator);
                    NamedNodeMap attrs = elem.getAttributes();
                    for (int a=0; a<attrs.getLength(); a++) {
                        Attr attr = (Attr) attrs.item(a);
                        String newValue = evaluate(attr.getValue(), evaluator);
                        if (newValue != null) {
                            attr.setValue(newValue);
                        }
                    }
                    break;
                case Node.TEXT_NODE:
                    Text text = (Text) n;
                    String newValue = evaluate(text.getData(), evaluator);
                    if (newValue != null) {
                        text.setData(newValue);
                    }
            }
        }
    }

    private static String evaluate(String value, Evaluator evaluator) throws BadParameter {
        int start = value.indexOf("@{");
        if (start > -1) {
            int end = value.indexOf("}", start);
            if (end > -1) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(value.substring(0, start));
                buffer.append(evaluator.evaluate(value.substring(start+2, end)));
                String rest = evaluate(value.substring(end+1), evaluator);
                if (rest != null) {
                    buffer.append(rest);
                } else {
                    buffer.append(value.substring(end+1));
                }
                return buffer.toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
