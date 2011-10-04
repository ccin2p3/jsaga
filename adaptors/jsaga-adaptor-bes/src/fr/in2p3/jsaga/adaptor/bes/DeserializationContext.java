package fr.in2p3.jsaga.adaptor.bes;

import org.apache.axis.MessageContext;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.message.EnvelopeHandler;
import org.apache.axis.message.SOAPHandler;
import org.xml.sax.InputSource;

public class DeserializationContext extends org.apache.axis.encoding.DeserializationContext {

	private Deserializer topDeserializer = null;
	public DeserializationContext(Class clazz, MessageContext ctx, SOAPHandler initialHandler) {
		super(ctx, initialHandler);
        //msgContext.setEncodingStyle("");
        popElementHandler();
		topDeserializer = getDeserializer(clazz, getTypeMapping().getTypeQName(clazz));
        if (topDeserializer == null) {
            topDeserializer = getDeserializerForClass(clazz);
        }
		pushElementHandler(new EnvelopeHandler((SOAPHandler) topDeserializer));

	}
	
	public void setInputSource(InputSource is) {
		this.inputSource = is;
	}
    
	public Object getValue() {
        return (topDeserializer == null) ?
            null :
            topDeserializer.getValue();
    }

}
