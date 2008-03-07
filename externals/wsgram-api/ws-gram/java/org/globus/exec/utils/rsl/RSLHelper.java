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
package org.globus.exec.utils.rsl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.exec.generated.CreateManagedJobInputType;
import org.globus.exec.generated.MultiJobDescriptionType;
import org.globus.exec.generated.NameValuePairType;
import org.globus.exec.generated.JobDescriptionType;
import org.globus.exec.utils.Resources;
import org.globus.util.I18n;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;
import org.globus.wsrf.utils.XmlUtils;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class RSLHelper {

    /**
     * QName of the element "factoryEndpoint" used as a job attribute in RSL.
     */
    public static final QName FACTORY_ENDPOINT_ATTRIBUTE_QNAME =
        JobDescriptionType.getTypeDesc().getFieldByName("factoryEndpoint").
                                    getXmlName();

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    /**
     * Add environment variable to job description if there is no variable
     * with the same name.
     * @param jobDescription JobDescriptionType
     * @param variableName String
     * @param value String
     */
    public static void addEnvironmentVariable(JobDescriptionType jobDescription,
                                        String variableName,
                                        String value)
    {
        NameValuePairType[] specifiedEnv = jobDescription.getEnvironment();
        int existingLength = 0;
        if (specifiedEnv != null) {
            existingLength = specifiedEnv.length;
        }
        //first check if the env var exists already
        for (int i = 0; i < existingLength; i++) {
            if (specifiedEnv[i].getName().equals(variableName)) {
                logger.debug(
                    "The environment variable already exists - no addition");
                return;
            }
        }
        NameValuePairType[] newEnv =
            new NameValuePairType[existingLength + 1];
        if (specifiedEnv != null) {
            System.arraycopy(specifiedEnv, 0, newEnv, 0, existingLength);
        }

        NameValuePairType eprEnvVar = new NameValuePairType();
        eprEnvVar.setName(variableName);
        eprEnvVar.setValue(value);
        newEnv[existingLength] = eprEnvVar;
        jobDescription.setEnvironment(newEnv);
    }


    /**
     * Makes a simple JobDescriptionType based on a command line. The resulting
     * RSL will have only an "executable" and zero or more "arguments" tags.
     * @param commandLineString the command line to execute when executing
     *          the job. It must at least have one string.
     * @return JobDescriptionType
     */
    public static JobDescriptionType makeSimpleJob(String commandLine) {

        StringTokenizer st = new StringTokenizer(commandLine);

        if (!st.hasMoreTokens()) {
            throw new RuntimeException(
                "Precondition violation: empty command line");
        }

        if (!st.hasMoreTokens()) {
            String errorMessage = i18n.getMessage(
                Resources.PRECONDITION_VIOLATION,
                "!(new StringTokenizer(commandLine)).hasMoreTokens()");
            throw new RuntimeException(errorMessage);
        }

        JobDescriptionType jobDescription = new JobDescriptionType();

        //executable
        jobDescription.setExecutable(st.nextToken());

        //arguments
        List arguments = new ArrayList();
        while (st.hasMoreTokens()) {
            String arg = st.nextToken();
            if (logger.isDebugEnabled()) {
                logger.debug("parsed command-line argument: " + arg);
            }
            arguments.add(arg);
        }
        jobDescription.setArgument((String[]) arguments.toArray(new String[0]));

        return jobDescription;
    }


    public static JobDescriptionType readRSL(
            File                            rslFile)
            throws                          RSLParseException,
                                            FileNotFoundException
    {
        FileInputStream inputStream =
            new FileInputStream(rslFile.getAbsolutePath());
        return readRSL(inputStream);
    }

    public static JobDescriptionType readRSL(
            String                          rslString)
            throws                          RSLParseException
    {
        ByteArrayInputStream inputStream = null;
        try {
            inputStream =
                new ByteArrayInputStream(rslString.getBytes());
        } catch (Exception e) {
            throw new RSLParseException(i18n.getMessage("rslParsingFailed"), e);
        }
        return readRSL(inputStream);
    }

    public static JobDescriptionType readRSL(
            InputStream                     rslInputStream)
            throws                          RSLParseException
    {
        JobDescriptionType jobDescription = null;
        Element rslElement = null;
        try {
            rslElement =
                XmlUtils.newDocument(rslInputStream).getDocumentElement();
            logger.debug("read RSL : \n" +
                         XmlUtils.toString((Element)rslElement));
        } catch (Exception e) {
            throw new RSLParseException(i18n.getMessage("rslParsingFailed"), e);
        }
        try {
            jobDescription =
                (JobDescriptionType)ObjectDeserializer.toObject(
                    rslElement,
                    JobDescriptionType.class);
        } catch (Exception e) {
            try {
                jobDescription =
                    (JobDescriptionType)ObjectDeserializer.toObject(
                        rslElement,
                        MultiJobDescriptionType.class);
            } catch (Exception ee) {
                throw new RSLParseException(
                    i18n.getMessage("rslParsingFailed"), e);
            }
        }
        return jobDescription;
    }

    /**
     * Serializes the job description to XML.
     *
     * @param jobDescription JobDescriptionType The job description to convert
     * @throws Exception
     * @return String An XML string representation of the job description
     */
    public static String convertToString(
            JobDescriptionType              jobDescription)
    {
        Element jobElement = null;
        try {
            jobElement = ObjectSerializer.toElement(
                jobDescription,
                CreateManagedJobInputType.getTypeDesc().
                    getFieldByName("job").getXmlName());
        } catch (Exception e) {
            String errorMessage = i18n.getMessage(
                        Resources.JOB_DESC_TO_XML_CONVERSION_ERROR);
            logger.error("could not serialize job description", e);
        }
        return XmlUtils.toString(jobElement);
    }

    private static Log logger = LogFactory.getLog(RSLHelper.class.getName());
}
