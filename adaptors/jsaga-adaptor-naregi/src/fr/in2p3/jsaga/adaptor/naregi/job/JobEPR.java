package fr.in2p3.jsaga.adaptor.naregi.job;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReference;
import org.apache.axis.message.addressing.ReferencePropertiesType;
import org.apache.axis.types.URI;
import org.apache.axis.utils.XMLUtils;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;
import org.ogf.saga.error.NoSuccessException;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobEPR
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   9 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobEPR {
    private EndpointReference m_epr;

    public JobEPR(InputStream eprStream) throws NoSuccessException {
//        m_epr = (EndpointReference) ObjectDeserializer.deserialize(new InputSource(eprStream), EndpointReference.class);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            Document doc = factory.newDocumentBuilder().parse(eprStream);
            Element elem = doc.getDocumentElement();
            m_epr = new EndpointReference(elem);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    public JobEPR(String jobid) throws NoSuccessException {
        String part1 = jobid.substring(0, jobid.lastIndexOf('/'));
        String part2 = jobid.substring(jobid.lastIndexOf('/')+1);
        // create EPR
        try {
            m_epr = new EndpointReference(part1);
        } catch (URI.MalformedURIException e) {
            throw new NoSuccessException(e);
        }
        MessageElement msg = new MessageElement();
        msg.setName("resourceId");
        msg.setPrefix("nrl-wsa");
        msg.setNamespaceURI("http://www.naregi.org/nrl/ws/addressing");
        msg.setValue(part2);
        MessageElement[] any = new MessageElement[]{msg};
        ReferencePropertiesType prop = new ReferencePropertiesType();
        prop.set_any(any);
        m_epr.setProperties(prop);
    }

    public String getJobId() {
        return m_epr.getAddress().toString() + "/" + m_epr.getProperties().get_any()[0].getChildren().get(0);
    }

    public String getEPR() throws NoSuccessException {
        Element oldRoot;
        try {
            oldRoot = ObjectSerializer.toElement(m_epr, EndpointReference.getTypeDesc().getXmlType());
        } catch (SerializationException e) {
            throw new NoSuccessException(e);
        }
        Document factory = oldRoot.getOwnerDocument();
        Element newRoot = factory.createElementNS("http://schemas.xmlsoap.org/ws/2004/03/addressing", "EndpointReference");
        newRoot.setPrefix("ns1");
        while(oldRoot.getFirstChild() != null) {
            Node child = oldRoot.getFirstChild();
            oldRoot.removeChild(child);
            newRoot.appendChild(child);
        }
        return XMLUtils.ElementToString(newRoot);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: JobEPR <jobid>");
            System.exit(1);
        }
        Pattern pattern = Pattern.compile("\\[(.*)\\]-\\[(.*)\\]");
        Matcher matcher = pattern.matcher(args[0]);
        if (!matcher.find()) {
            throw new Exception("Job ID does not match regular expression: "+pattern.pattern());
        }
        String nativeJobId = matcher.group(2);
        String epr = new JobEPR(nativeJobId).getEPR();
        System.out.println(epr);
    }
}
