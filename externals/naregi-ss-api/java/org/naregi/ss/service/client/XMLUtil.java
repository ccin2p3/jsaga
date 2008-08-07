/*
 *
 * COPYRIGHT (C) 2003-2006 National Institute of Informatics, Japan
 *                         All Rights Reserved
 * COPYRIGHT (C) 2003-2006 Fujitsu Limited
 *                         All Rights Reserved
 * 
 * This file is part of the NAREGI Grid Super Scheduler software package.
 * For license information, see the docs/LICENSE file in the top level 
 * directory of the NAREGI Grid Super Scheduler source distribution.
 *
 *
 * Revision history:
 *      $Revision: 1.1 $
 *      $Id: XMLUtil.java,v 1.1 2008/08/07 15:24:32 sreynaud Exp $
 */
package org.naregi.ss.service.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utility for dealing with XML
 */
public class XMLUtil {

	/**
	 * Store the Document type XML as the file
	 * 
	 * @param xml XML specified in Document type
	 * @param file storing file name
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws TransformerException
	 */
	public static void saveFileFromDocument(Document xml, File file) 
		throws UnsupportedEncodingException, FileNotFoundException, TransformerException  {

		makeStreamFromDocument(xml, new FileOutputStream(file));
	}

	/**
	 * Transform the Document type XML to String type
	 * 
	 * @param xml XML specified in Document type
	 * @return transformed XML to String type
	 * @throws TransformerException
	 * @throws UnsupportedEncodingException
	 */
	public static String getStringFromDocument(Document xml) 
		throws TransformerException, UnsupportedEncodingException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		makeStreamFromDocument(xml, baos);
		String retVal =  new String( baos.toByteArray(), "UTF-8" );
		return retVal;
	}

	/**
	 * Transform the Document type XML to String type, and output to OutputStream type
	 * 
	 * @param xml XML specified in Document type
	 * @param os  Target OutputStream of XML contents
	 * @throws TransformerException
	 * @throws UnsupportedEncodingException
	 */
	private static void makeStreamFromDocument(Document xml, OutputStream os)
		throws TransformerException, UnsupportedEncodingException {

		try {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer tran;
			String retVal = null;
			tran = transFactory.newTransformer();
			Properties props = new Properties();
			props.put(OutputKeys.METHOD, "xml");
			tran.setOutputProperties(props);

			tran.transform(new DOMSource(xml), new StreamResult(os));
		} finally {
			try {
				os.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Read the XML formatted file as Document type
	 * 
	 * @param file file specified in XML format
	 * @return XML Document
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document loadFileToDocument(File file) 
		throws FileNotFoundException, ParserConfigurationException, SAXException, IOException 
    {
		return getDocumentFromStream(new FileInputStream(file));
	}

	/**
	 * Transform the String type XML to Document type
	 * 
	 * @param xml XML specified in String type
	 * @return transformed XML to Document type
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document getDocumentFromString(String xml) 
		throws ParserConfigurationException, SAXException, IOException {
		
		ByteArrayInputStream bais = new ByteArrayInputStream( xml.getBytes("UTF-8") );
		return getDocumentFromStream(bais);
	}

	/**
	 * Read the String type XML contents from InputStream in String type, and transform it to the Document type
	 * 
	 * @param is XML specified in String type
	 * @return transformed XML to Documents type
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document getDocumentFromStream(InputStream is) 
		throws ParserConfigurationException, SAXException, IOException {
		
		try {
			Document retVal = null;
			Document doc = null;
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			DocumentBuilder builder = builderFactory.newDocumentBuilder();

			builderFactory.newDocumentBuilder();
			retVal = builder.parse( is );
			
			return retVal;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

}
