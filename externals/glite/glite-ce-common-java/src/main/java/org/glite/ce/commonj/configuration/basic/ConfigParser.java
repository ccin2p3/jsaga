/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 * 
 * Authors: Paolo Andreetto, <paolo.andreetto@pd.infn.it>
 *
 * Version info: $Id: ConfigParser.java,v 1.1 2007/11/15 14:02:00 squizzat Exp $
 *
 */

package org.glite.ce.commonj.configuration.basic;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

public interface ConfigParser{

    public String getObjectName(String uri,
                                String name,
                                String qName, 
                                Attributes attributes)
        throws SAXParseException;

    public void startElement(String uri, 
                             String name, 
                             String qName, 
                             Attributes attributes,
                             ConfigItem prevContext) 
        throws SAXParseException;

    public void endElement(String uri, 
                           String name, 
                           String qName,
                           ConfigItem currentContext) 
        throws SAXParseException;

    public void characters(char[] chars,
                           int start,
                           int length,
                           ConfigItem currentContext) 
        throws SAXParseException;

}
