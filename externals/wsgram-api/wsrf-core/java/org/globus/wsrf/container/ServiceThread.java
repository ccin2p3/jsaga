/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.container;

import org.globus.wsrf.utils.PerformanceLog;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.config.ContainerConfig;
import org.globus.util.I18n;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPFault;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.NonBlockingBufferedInputStream;
import org.apache.axis.transport.http.ChunkedInputStream;
import org.apache.axis.transport.http.ChunkedOutputStream;
import org.apache.axis.utils.XMLUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

import javax.xml.namespace.QName;

/**
 * This class is responsible for reading the request from the request queue,
 * format it, and pass it through the Axis engine.
 */
public class ServiceThread extends Thread {

    private static Log logger =
        LogFactory.getLog(ServiceThread.class.getName());

    private static I18n i18n =
        I18n.getI18n(Resources.class.getName());

    private ServiceRequestQueue queue;
    private ServiceThreadPool threadPool;
    private AxisEngine engine;
    protected MessageContext msgContext;
    private NonBlockingBufferedInputStream is;
    private Message responseMsg;

    private StringBuffer soapAction;
    private StringBuffer fileName;
    private int httpRequest;
    private boolean sendContentLength = true;
    protected boolean http11;
    private boolean chunked;

    static PerformanceLog performanceLogger =
        new PerformanceLog(ServiceThread.class.getName() + ".performance");

    static PerformanceLog performanceProcessLogger =
        new PerformanceLog(
            ServiceThread.class.getName() + ".performance.process"
        );

    public static final String SEND_CONTENT_LENGTH =
        "org.globus.wsrf.container.sendContentLength";

    // HTTP prefix
    protected static final byte[] HTTP_10 = "HTTP/1.0 ".getBytes();
    protected static final byte[] HTTP_11 = "HTTP/1.1 ".getBytes();

    // HTTP status codes
    private static final byte[] OK =
        "200 OK".getBytes();

    protected static final byte[] UNAUTH =
        "401 Unauthorized".getBytes();

    private static final byte[] ISE =
        "500 Internal server error".getBytes();

    private static final String FILE_NOT_FOUND = "404 ";
    private static final byte[] FILE_NOT_FOUND_MSG =
        ("<html><body>File not found</body></html>").getBytes();

    // Standard MIME headers for XML payload
    protected static final byte[] XML_MIME_STUFF =
        ("\r\nContent-Type: text/xml; charset=utf-8").getBytes();

    // Standard MIME headers for HTML payload
    protected static final byte[] HTML_MIME_STUFF =
        ("\r\nContent-Type: text/html; charset=utf-8").getBytes();
    protected static final byte[] JNLP_MIME_STUFF =
        ("\r\nContent-Type: application/x-java-jnlp-file").getBytes();
    protected static final byte[] JAR_MIME_STUFF =
        ("\r\nContent-Type: application/java-archive").getBytes();

    protected static final String CONTENT_TYPE =
        "\r\n" + HTTPConstants.HEADER_CONTENT_TYPE + ": ";

    protected static final byte[] CONTENT_LENGTH =
        ("\r\n" + HTTPConstants.HEADER_CONTENT_LENGTH + ": ").getBytes();

    private static final byte[] CONNECTION_CLOSE =
        "\r\nConnection: close".getBytes();

    private static final byte[] TRANSFER_ENCODING_CHUNKED =
        "\r\nTransfer-Encoding: chunked".getBytes();

    // Mime/Content separator
    protected static final byte[] SEPARATOR = "\r\n\r\n".getBytes();

    // ASCII character mapping to lower case
    private static final byte[] toLower = new byte[256];

    static {
        for (int i = 0; i < 256; i++) {
            toLower[i] = (byte) i;
        }

        for (int lc = 'a'; lc <= 'z'; lc++) {
            toLower[(lc + 'A') - 'a'] = (byte) lc;
        }
    }

    // mime header for content length
    private static final byte[] LENGTH_HEADER = "content-length: ".getBytes();

    // mime header for soap action
    private static final byte[] ACTION_HEADER = "soapaction: ".getBytes();

    // mime header for GET
    private static final byte[] GET_HEADER = "GET".getBytes();

    // mime header for HEAD
    private static final byte[] HEAD_HEADER = "HEAD".getBytes();

    // mime header for POST
    private static final byte[] POST_HEADER = "POST".getBytes();

    // header ender
    private static final byte[] HEADER_ENDER = ": ".getBytes();

    // transfer-encoding type
    private static final byte[] ENCODING_HEADER =
        "transfer-encoding: ".getBytes();

    // transfer-encoding type chunked
    private static final byte[] CHUNKED =
        "chunked".getBytes();

    private static final byte[] HTTP_BASE_VERSION =
        "http/".getBytes();

    private static final int HTTP_GET = 1;
    private static final int HTTP_HEAD = 2;
    private static final int HTTP_POST = 3;

    // buffer for IO
    private static final int BUFSIZ = 4096;
    private byte[] buf = new byte[BUFSIZ];
    private String webRootPath;
    private boolean webStartEnabled = false;
    private String configRootPath;
    private String baseURL;

    public ServiceThread(ServiceRequestQueue queue,
                         ServiceThreadPool pool,
                         AxisEngine engine) {
        logger.debug("Starting up thread");
        this.queue = queue;
        this.threadPool = pool;

        String webStart =
            System.getProperty(ContainerConfig.WEB_START_PROPERTY);
        this.webStartEnabled = (webStart != null) &&
            (webStart.equalsIgnoreCase("enable"));

        try {
            this.webRootPath = getWebRootPath(engine);
        } catch (Exception e) {
            throw new RuntimeException(i18n.getMessage("invalidWebRoot"));
        }

        this.configRootPath = getConfigRootPath(engine);

        // create an Axis server
        this.engine = engine;

        // Reusuable, buffered, content length controlled, InputStream
        this.is = new NonBlockingBufferedInputStream();

        // buffers for the headers we care about
        this.soapAction = new StringBuffer();
        this.fileName = new StringBuffer();

        String prop = System.getProperty(SEND_CONTENT_LENGTH);
        if (prop != null && prop.equalsIgnoreCase("false")) {
            this.sendContentLength = false;
        }

        String host = "localhost";
        try {
            host = getHost(engine);
        } catch (Exception e) {
            logger.warn(i18n.getMessage("noHostname"), e);
        }
        this.baseURL = getProtocol() + "://" + host + ":";
    }

    private static String getHost(AxisEngine engine) throws IOException {
        URL url = new URL("http", ServiceHost.getHost(engine), 80, "/");
        return url.getHost();
    }

    public static String getWebRootPath(AxisEngine engine)
        throws IOException {
        ContainerConfig config = ContainerConfig.getConfig(engine);
        String webRoot = config.getInternalWebRoot();

        if (webRoot == null) {
            webRoot = "." + File.separator;
        } else if (!webRoot.equals("")  &&
                   !webRoot.endsWith(File.separator)) {
            webRoot = webRoot + File.separator;
        }
        return new File(webRoot).getCanonicalPath();
    }

    public static String getConfigRootPath(AxisEngine engine) {
        return ContainerConfig.getGlobusLocation();
    }

    private void reset() {
        // clear the Call object associated with the current thread
        //Service.clearCall();

        // dispose of the previous message context
        if (this.msgContext != null) {
            this.msgContext.reset();
            this.msgContext.dispose();
        }

        // create and initialize a new message context
        this.msgContext = new MessageContext(engine);
        
        // reset previous message
        this.responseMsg = null;
    }

    // very basic OutOfMem error handling
    private void handleOutOfMemoryError() {
        reset();
        
        // request finalization & GC
        System.runFinalization();
        System.gc();
    }

    public void run() {
        while (true) {
            reset();
            
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Thread " + getName() + " listening for requests");
            }

            try {
                ServiceRequest request = this.queue.dequeue();
                if (request == null) {
                    logger.debug("Stopping thread " + getName());
                    this.threadPool.removeThread(this);
                    break;
                }
                performanceProcessLogger.start();
                process(request);
                performanceProcessLogger.stop("process");
                yield();
            } catch (OutOfMemoryError e) {
                logger.error(i18n.getMessage("serverFault01"), e);
                handleOutOfMemoryError();
            } catch (Throwable e) {
                logger.error(i18n.getMessage("serverFault02"), e);
            }
        }
    }

    protected String getProtocol() {
        return "http";
    }

    protected void process(ServiceRequest request) {

        Socket socket = request.getSocket();
        OutputStream out = null;
        InputStream in = null;

        if (logger.isDebugEnabled()) {
            logger.debug("Thread " + getName() + " processing requests");
        }

        try {
            out = socket.getOutputStream();
            in = socket.getInputStream();

            // assume the best
            byte[] status = OK;

            // assume we're not getting WSDL
            boolean doWsdl = false;
            String filePart = "";

            is.setInputStream(in);
            try {
                // parse all headers into hashtable
                int contentLength =
                    parseHeaders(is, soapAction, fileName);

                if (contentLength >= 0) {
                    is.setContentLength(contentLength);
                }

                int paramIdx = fileName.toString().indexOf('?');

                if (paramIdx != -1) {
                    // Got params
                    String params = fileName.substring(paramIdx + 1);
                    fileName.setLength(paramIdx);

                    if (logger.isDebugEnabled()) {
                        logger.debug(i18n.getMessage("filename00",
                                                     fileName.toString()));
                        logger.debug(i18n.getMessage("params00",
                                                     params));
                    }

                    if ("wsdl".equalsIgnoreCase(params)) {
                        doWsdl = true;
                    }
                }

                filePart = fileName.toString();

                msgContext.setProperty(Constants.MC_REALPATH,
                                       filePart);
                msgContext.setProperty(Constants.MC_CONFIGPATH,
                                       this.configRootPath);
                msgContext.setProperty(Constants.MC_HOME_DIR,
                                       this.webRootPath);
                msgContext.setProperty(Constants.MC_REMOTE_ADDR,
                                       socket.getInetAddress().getHostAddress());
                // !!! Fix string concatenation
                ServerSocket serverSocket = request.getServerSocket();
                String url = this.baseURL + serverSocket.getLocalPort() +
                    "/" + filePart;
                msgContext.setProperty(MessageContext.TRANS_URL,
                                       url);

                // if get, then return simpleton document as response
                if (this.httpRequest == HTTP_GET) {
                    if (doWsdl) {
                        doWSDL(engine, msgContext, out, filePart);
                    } else {
                        doGet(engine, msgContext, out, filePart, true);
                    }
                    return;
                } else if (this.httpRequest == HTTP_HEAD) {
                    doGet(engine, msgContext, out, filePart, false);
                    return;
                }

                doPost(engine, msgContext);
            } catch (Exception e) {
                status = doFault(e, msgContext, filePart);
            }

            // if responseMsg is null let's assume
            // it is a one-way operation.
            // we can add better checking for one-way operations
            // later on
            if (this.responseMsg == null) {
                out.write(createHeaderReply(OK,
                                            XML_MIME_STUFF,
                                            0));
            } else {
                String contentType =
                    CONTENT_TYPE +
                    responseMsg.getContentType(msgContext.getSOAPConstants());

                if (this.chunked) {
                    out.write(createHeaderReply(status,
                                                contentType.getBytes(),
                                                -1));
                    out = new ChunkedOutputStream(out);
                } else {
                    out.write(createHeaderReply(status,
                                                contentType.getBytes(),
                                                responseMsg.getContentLength()));
                }
                this.responseMsg.writeTo(out);
            }
            out.flush();
        } catch (InterruptedIOException iie) {
            return;
        } catch (SocketException se) {
            // socket write error can be delivered if client shuts down
            // connection improperly
            logger.debug("Socket exception", se);
        } catch (Exception e) {
            logger.error(i18n.getMessage("general"), e);
        } finally {
            // close socket out
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                logger.debug(i18n.getMessage("errorClosingOutputStream"), e);
            }
            // close socket in
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                logger.debug(i18n.getMessage("errorClosingInputStream"), e);
            }
            // close socket in wrapper 
            // so hopefully it will release some data
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                logger.debug(i18n.getMessage("errorClosingInputStream"), e);
            }
            // close the actual socket
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                logger.debug(i18n.getMessage("errorClosingSocket"), e);
            }
        }
    }

    private byte[] createHeaderReply(byte[] code,
                                     byte[] contentType,
                                     long length)
        throws IOException {
        ByteArrayOutputStream out =
            new ByteArrayOutputStream();
        out.write( (this.http11) ? HTTP_11 : HTTP_10 );
        out.write(code);
        if (contentType != null) {
            out.write(contentType);
        }
        if (length < 0) {
            out.write(TRANSFER_ENCODING_CHUNKED);
        } else {
            if (this.sendContentLength) {
                out.write(CONTENT_LENGTH);
                out.write(String.valueOf(length).getBytes());
            }
        }
        if (this.http11) {
            out.write(CONNECTION_CLOSE);
        }
        out.write(SEPARATOR);
        return out.toByteArray();
    }

    protected void doWSDL(AxisEngine engine,
                          MessageContext msgContext,
                          OutputStream out,
                          String filePart)
        throws IOException {

        try {
            engine.generateWSDL(msgContext);
        } catch (Exception e) {
            byte [] status = ISE;
            byte [] msg = null;
            if (e instanceof AxisFault) {
                AxisFault fault = (AxisFault)e;
                if (fault.getFaultCode().equals(
                       Constants.QNAME_NO_SERVICE_FAULT_CODE)) {
                    status = (FILE_NOT_FOUND + filePart).getBytes();
                    msg = FILE_NOT_FOUND_MSG;
                }
            }
            if (msg == null) {
                logger.debug("Get wsdl error", e);
                StringWriter sWriter = new StringWriter();
                PrintWriter pWriter = new PrintWriter(sWriter);
                pWriter.println("<html><body>");
                pWriter.println("<h2>Error getting wsdl:</h2><pre>");
                e.printStackTrace(pWriter);
                pWriter.println("</pre></body></html>");
                pWriter.flush();
                msg = sWriter.toString().getBytes();
            }
            out.write(createHeaderReply(status,
                                        HTML_MIME_STUFF,
                                        msg.length));
            out.write(msg);
            out.flush();

            return;
        }

        Document doc = (Document) msgContext.getProperty("WSDL");

        if (doc != null) {
            String response = XMLUtils.DocumentToString(doc);
            byte[] respBytes = response.getBytes();
            out.write(createHeaderReply(OK,
                                        XML_MIME_STUFF,
                                        respBytes.length));
            out.write(respBytes);
            out.flush();
        }
    }

    protected void doGet(AxisEngine engine,
                         MessageContext msgContext,
                         OutputStream out,
                         String filePart,
                         boolean returnContent) {
        try {
            doGetSub(engine, msgContext, out, filePart, returnContent);
        } catch (FileNotFoundException e) {
            try {
                byte [] status = (FILE_NOT_FOUND + filePart).getBytes();
                out.write(createHeaderReply(status,
                                            HTML_MIME_STUFF,
                                            FILE_NOT_FOUND_MSG.length));
                out.write(FILE_NOT_FOUND_MSG);
                out.flush();
            } catch (IOException ee) {
                logger.debug(i18n.getMessage("errorWritingResponse"), e);
            }
        } catch (SocketException e) {
            // ignore - client probably closed too early
        } catch (IOException e) {
            try {
                out.write(createHeaderReply(ISE,
                                            HTML_MIME_STUFF,
                                            0));
                out.flush();
            } catch (IOException ee) {
                logger.debug(i18n.getMessage("errorWritingResponse"), e);
            }
        }
    }

    protected void doGetSub(AxisEngine engine,
                            MessageContext msgContext,
                            OutputStream out,
                            String filePart,
                            boolean returnContent)
        throws IOException {

        File getFile = new File(this.webRootPath, filePart);

        // read local file
        if (logger.isDebugEnabled()) {
            logger.debug("Trying to retrieve file from:" + getFile);
        }

        if (!getFile.getCanonicalPath().startsWith(this.webRootPath)) {
            throw new FileNotFoundException(filePart);
        }

        boolean xmlContent = filePart.endsWith(".xsd") ||
                             filePart.endsWith(".wsdl") ||
                             filePart.endsWith(".gwsdl");

        boolean jnlpContent = filePart.endsWith(".jnlp");
        boolean jarContent = filePart.endsWith(".jar");

        if ((!xmlContent && !jnlpContent && !jarContent) ||
            ((jnlpContent || jarContent) && !this.webStartEnabled)) {
            throw new FileNotFoundException(filePart);
        }

        FileInputStream getFileStream = new FileInputStream(getFile);

        try {
            byte [] type = null;
            if (xmlContent) {
                type = XML_MIME_STUFF;
            } else if (jnlpContent) {
                type = JNLP_MIME_STUFF;
            } else if (jarContent) {
                type = JAR_MIME_STUFF;
            }

            out.write(createHeaderReply(OK,
                                        type,
                                        getFile.length()));

            if (!returnContent) {
                return;
            }

            byte[] fileBuffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = getFileStream.read(fileBuffer, 0, 4096)) > 0) {
                out.write(fileBuffer, 0, bytesRead);
            }
        } finally {
            getFileStream.close();
        }
    }

    protected void doPost(AxisEngine engine,
                          MessageContext msgContext)
        throws AxisFault {
        // this may be "" if either SOAPAction: "" or if no SOAPAction at all.
        // for now, do not complain if no SOAPAction at all
        String soapActionString = soapAction.toString();

        if (soapActionString != null) {
            msgContext.setUseSOAPAction(true);
            msgContext.setSOAPActionURI(soapActionString);
        }

        InputStream in = this.is;
        if (this.chunked) {
            in = new ChunkedInputStream(this.is);
        }

        Message requestMsg = new Message(in);
        msgContext.setRequestMessage(requestMsg);

        // invoke the Axis engine
        performanceLogger.start();
        engine.invoke(msgContext);
        performanceLogger.stop("Post: Engine Invoke");

        // Retrieve the response from Axis
        this.responseMsg = msgContext.getResponseMessage();
    }

    protected byte[] doFault(Exception e,
                             MessageContext msgContext,
                             String filepart) {
        AxisFault af;
        byte[] status = OK;

        if (e instanceof AxisFault) {
            af = (AxisFault) e;
            if (af.getClass().equals(AxisFault.class) &&
                af.detail instanceof InvocationTargetException) {
                af = new AxisFault(
                  i18n.getMessage("serverFault00"), 
                  ((InvocationTargetException)af.detail).getTargetException());
            }
            logger.debug(i18n.getMessage("serverFault00"), af);
            QName faultCode = af.getFaultCode();
            if (faultCode != null &&
                "Server.Unauthorized".equals(faultCode.getLocalPart())) {
                status = UNAUTH; // SC_UNAUTHORIZED
            } else {
                status = ISE; // SC_INTERNAL_SERVER_ERROR
            }
        } else {
            if (e instanceof IOException) {
                status = (FILE_NOT_FOUND + filepart).getBytes();
            } else {
                status = ISE; // SC_INTERNAL_SERVER_ERROR
            }

            af = AxisFault.makeFault(e);
        }

        // There may be headers we want to preserve in the
        // response message - so if it's there, just add the
        // FaultElement to it.  Otherwise, make a new one.
        this.responseMsg = msgContext.getResponseMessage();

        if (this.responseMsg == null) {
            this.responseMsg = new Message(af);
        } else {
            try {
                SOAPEnvelope env = this.responseMsg.getSOAPEnvelope();
                env.clearBody();
                env.addBodyElement(new SOAPFault(af));
            } catch (AxisFault fault) {
                // Should never reach here!
                logger.error(i18n.getMessage("unexpectedError"), e);
            }
        }

        return status;
    }

    /**
    * Read a single line from the input stream
    * @param is        inputstream to read from
    * @param b         byte array to read into
    * @param off       starting offset into the byte array
    * @param len       maximum number of bytes to read
    */
    private int readLine(InputStream is,
                         byte[] b,
                         int off,
                         int len)
        throws IOException {
        int count = 0;
        int c;

        while ((c = is.read()) != -1) {
            b[off++] = (byte) c;
            count++;

            if ((c == '\n') || (count == len)) {
                break;
            }
        }

        return (count > 0) ? count : (-1);
    }

    private void parseRequest(StringBuffer fileName,
                              byte [] buf,
                              int n,
                              int offset)
        throws IOException {
        fileName.delete(0, fileName.length());
        int i;
        for (i = 0; i < (n - offset); i++) {
            char c = (char) (buf[i + offset] & 0x7f);

            if (c == ' ') {
                break;
            }

            fileName.append(c);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(i18n.getMessage("filename01",
                                         new String[] {"ServiceThread",
                                                       fileName.toString()}));
        }

        if (matches(buf, i+offset+1, HTTP_BASE_VERSION)) {
            int j = i+offset+1+HTTP_BASE_VERSION.length;
            if (j+3 > n) { // 3 is for <major digit>.<minor digit>
                throw new IOException(i18n.getMessage("malformedHTTPVersion"));
            }
            if (buf[j] != '1') {
                throw new IOException(i18n.getMessage("unsupportedHTTPMajor"));
            }
            if (buf[j+2] == '0') {
                this.http11 = false;
            } else if (buf[j+2] == '1') {
                this.http11 = true;
            } else {
                throw new IOException(i18n.getMessage("unsupportedHTTMinor"));
            }
        } else {
            throw new IOException(i18n.getMessage("malformedHTTPVersion"));
        }
    }

    /**
    * Read all mime headers, returning the value of Content-Length and
    * SOAPAction.
    * @param is         InputStream to read from
    * @param soapAction StringBuffer to return the soapAction into
    * @return Content-Length
    */
    private int parseHeaders(InputStream is,
                             StringBuffer soapAction,
                             StringBuffer fileName)
        throws IOException {

        // parse first line as GET or POST
        int n = readLine(is, buf, 0, buf.length);

        if (n < 0) {
            // nothing!
            throw new IOException(i18n.getMessage("unexpectedEOS00"));
        }

        soapAction.delete(0, soapAction.length());
        this.httpRequest = -1;
        this.chunked = false;

        if (buf[0] == GET_HEADER[0]) {
            this.httpRequest = HTTP_GET;
            parseRequest(fileName, buf, n, 5);
            return 0;
        } else if (buf[0] == HEAD_HEADER[0]) {
            this.httpRequest = HTTP_HEAD;
            parseRequest(fileName, buf, n, 5);
            return 0;
        } else if (buf[0] == POST_HEADER[0]) {
            this.httpRequest = HTTP_POST;
            parseRequest(fileName, buf, n, 6);
        } else {
            throw new IOException(i18n.getMessage("badRequest00"));
        }

        int len = -1;

        while ((n = readLine(is, buf, 0, buf.length)) > 0) {
            // if we are at the separator blank line, bail right now
            if ((n <= 2) && ((buf[0] == '\n') || (buf[0] == '\r'))) {
                break;
            }

            // RobJ gutted the previous logic; it was too hard to extend for more headers.
            // Now, all it does is search forwards for ": " in the buf,
            // then do a length / byte compare.
            // Hopefully this is still somewhat efficient (Sam is watching!).
            // First, search forwards for ": "
            int endHeaderIndex = 0;

            while (
                (endHeaderIndex < n) &&
                    (toLower[buf[endHeaderIndex]] != HEADER_ENDER[0])
            ) {
                endHeaderIndex++;
            }

            endHeaderIndex += 2;

            // endHeaderIndex now points _just past_ the ": ", and is
            // comparable to the various lenLen, actionLen, etc. values
            // convenience; i gets pre-incremented, so initialize it to one less
            int i = endHeaderIndex - 1;

            // which header did we find?
            if ((endHeaderIndex == LENGTH_HEADER.length) &&
                matches(buf, LENGTH_HEADER)) {
                len = 0;
                // parse content length
                while ((++i < n) && (buf[i] >= '0') && (buf[i] <= '9')) {
                    len = (len * 10) + (buf[i] - '0');
                }
            } else if ((endHeaderIndex == ACTION_HEADER.length) &&
                       matches(buf, ACTION_HEADER)) {
                // skip initial '"'
                i++;

                while ((++i < n) && (buf[i] != '"')) {
                    soapAction.append((char) (buf[i] & 0x7f));
                }
            } else if ((endHeaderIndex == ENCODING_HEADER.length) &&
                       matches(buf, ENCODING_HEADER)) {
                this.chunked = matches(buf, i+1, CHUNKED) && this.http11;
            }
        }

        return len;
    }

    /**
    * does tolower[buf] match the target byte array, up to the target's length?
    */
    public boolean matches(byte[] buf, byte[] target) {
        for (int i = 0; i < target.length; i++) {
            if (toLower[buf[i]] != target[i]) {
                return false;
            }
        }
        return true;
    }

    /**
    * Case-insensitive match of a target byte [] to a source byte [],
    * starting from a particular offset into the source.
    */
    public boolean matches(byte[] buf, int bufIdx, byte[] target) {
        for (int i = 0; i < target.length; i++) {
            if (toLower[buf[bufIdx + i]] != target[i]) {
                return false;
            }
        }
        return true;
    }

    /**
    * output an integer into the output stream
    * @param out       OutputStream to be written to
    * @param value     Integer value to be written.
    */
    private void putInt(OutputStream out, int value)
        throws IOException {
        int len = 0;
        int offset = buf.length;

        // negative numbers
        if (value < 0) {
            buf[--offset] = (byte) '-';
            value = -value;
            len++;
        }

        // zero
        if (value == 0) {
            buf[--offset] = (byte) '0';
            len++;
        }

        // positive numbers
        while (value > 0) {
            buf[--offset] = (byte) ((value % 10) + '0');
            value = value / 10;
            len++;
        }

        // write the result
        out.write(buf, offset, len);
    }
}
