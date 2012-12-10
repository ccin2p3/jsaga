package fr.in2p3.jsaga.adaptor.data;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.transport.http.SocketHolder;
import org.globus.axis.transport.GSIHTTPSender;

import java.io.IOException;

/**
 * This class allows to bypass the need of declaring the URL handler for the HTTPG protocol.
 * This is really useful if you use JSAGA within tomcat or an OGSi container.
 * 
 * @author Jerome Revillard
 *
 */
public class HTTPGHandler extends GSIHTTPSender {
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.apache.axis.transport.http.HTTPSender#invoke(org.apache.axis.MessageContext)
	 */
	public void invoke(MessageContext msgContext) throws AxisFault {
		String url = msgContext.getStrProp(MessageContext.TRANS_URL);
		if (!(url.startsWith("httpg://"))) {
			throw new AxisFault(GSIHTTPSender.class.getCanonicalName() + " can only be used with the httpg protocol!",
					new IOException("Invalid protocol"));
		}
		msgContext.setProperty(MessageContext.TRANS_URL,
				msgContext.getStrProp(MessageContext.TRANS_URL).replaceFirst("httpg", "http"));
		super.invoke(msgContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.globus.axis.transport.GSIHTTPSender#getSocket(org.apache.axis.transport.http.SocketHolder,
	 * org.apache.axis.MessageContext, java.lang.String, java.lang.String, int, int, java.lang.StringBuffer,
	 * org.apache.axis.components.net.BooleanHolder)
	 */
	@Override
	protected void getSocket(SocketHolder sockHolder, MessageContext msgContext, String protocol, String host,
			int port, int timeout, StringBuffer otherHeaders, BooleanHolder useFullURL) throws Exception {
		super.getSocket(sockHolder, msgContext, "httpg", host, port, timeout, otherHeaders, useFullURL);
	}
}
