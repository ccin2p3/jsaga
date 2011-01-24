package fr.in2p3.jsaga.adaptor.bes;

import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;
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
	public static String dumpMessage(String nameSpace, Object BESMessage) {
		StringWriter writer = new StringWriter();
		writer.write("------->" + BESMessage.getClass().getName() + "\n");
		try {
			ObjectSerializer.serialize(writer, BESMessage, 
					new QName(nameSpace, BESMessage.getClass().getName()));
		} catch (SerializationException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}

	public static String dumpBESMessage(Object Message) {
		return dumpMessage("http://schemas.ggf.org/bes/2006/08/bes-factory", Message);
	}
}
