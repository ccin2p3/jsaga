package fr.in2p3.jsaga.adaptor.bes;

import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;
import java.io.StringWriter;
import javax.xml.namespace.QName;

public class BesUtils {
	public static String dumpBESMessage(Object BESMessage) {
		StringWriter writer = new StringWriter();
		writer.write("------->" + BESMessage.getClass().getName() + "\n");
		try {
			ObjectSerializer.serialize(writer, BESMessage, 
					new QName("http://schemas.ggf.org/bes/2006/08/bes-factory", BESMessage.getClass().getName()));
			return writer.toString();
		} catch (SerializationException e) {
			e.printStackTrace();
			return "";
		}
	}
}
