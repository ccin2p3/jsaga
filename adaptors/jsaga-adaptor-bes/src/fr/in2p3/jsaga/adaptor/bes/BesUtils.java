package fr.in2p3.jsaga.adaptor.bes;

import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.namespace.QName;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesUtils
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   4 jan 2011
* ***************************************************/

public class BesUtils {
	public static String dumpMessage(QName nameSpace, Object BESMessage) {
		if (BESMessage == null) return nameSpace.getNamespaceURI() + ": NULL";
		String dump = "-------> " + BESMessage.getClass().getName() + "\n";
		try {
			dump += BesUtils.serialize(nameSpace, BESMessage);
		} catch (Exception e) {
			dump += "Could not dump object " + BESMessage.getClass().getName();
		}
		return dump;
	}

	/**
	 * @deprecated
	 */
	public static String dumpMessage(String nameSpace, Object BESMessage) {
		return dumpMessage(new QName(nameSpace), BESMessage);
	}

	public static String dumpBESMessage(Object Message) {
		return dumpMessage(new QName("http://schemas.ggf.org/bes/2006/08/bes-factory"), Message);
	}

	public static String serialize(QName nameSpace, Object obj) throws Exception {
		MessageElement messageElement = new MessageElement();
        messageElement.setQName(nameSpace);
        messageElement.setObjectValue(obj);
        
        StringWriter writer = new StringWriter();

        MessageContext ctx = MessageContext.getCurrentContext();
        if (ctx == null) {
            ctx = new MessageContext(new AxisClient());
            ctx.setEncodingStyle("");
            ctx.setProperty(AxisClient.PROP_DOMULTIREFS, Boolean.FALSE);
        }

        SerializationContext context = new SerializationContext(writer, ctx);
        context.setPretty(true);
        context.setSendDecl(false);
        messageElement.output(context);
        writer.write('\n');
        writer.flush();
        return writer.toString();
	}
	
	/**
	 * @deprecated
	 */
	public static String serialize(String nameSpace, Object obj) throws Exception {
		return serialize(new QName(nameSpace, obj.getClass().getName().substring(obj.getClass().getName().lastIndexOf('.')+1)), obj);
	}
	
	public static Object deserialize(String s, Class clazz) throws SAXException {
        MessageContext ctx = MessageContext.getCurrentContext();
        if (ctx == null) {
            ctx = new MessageContext(new AxisClient());
            ctx.setEncodingStyle("");
            ctx.setProperty(AxisClient.PROP_DOMULTIREFS,
                            Boolean.FALSE);
        }
		DeserializationContext deserializer = new DeserializationContext(clazz, ctx, new SOAPHandler());
		deserializer.setInputSource(new InputSource(new StringReader(s)));
		deserializer.parse();
		return deserializer.getValue();

	}
}
