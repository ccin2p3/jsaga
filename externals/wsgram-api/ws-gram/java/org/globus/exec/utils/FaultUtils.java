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
package org.globus.exec.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Calendar;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.Text;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.utils.JavaUtils;

import org.oasis.wsrf.faults.BaseFaultType;
import org.oasis.wsrf.faults.BaseFaultTypeDescription;
import org.oasis.wsrf.faults.BaseFaultTypeErrorCode;

import org.globus.util.I18n;

import org.globus.exec.generated.CredentialSerializationFaultType;
import org.globus.exec.generated.ExecutionFailedFaultType;
import org.globus.exec.generated.FaultType;
import org.globus.exec.generated.FilePermissionsFaultType;
import org.globus.exec.generated.InsufficientCredentialsFaultType;
import org.globus.exec.generated.InternalFaultType;
import org.globus.exec.generated.InvalidCredentialsFaultType;
import org.globus.exec.generated.InvalidPathFaultType;
import org.globus.exec.generated.ScriptCommandEnumeration;
import org.globus.exec.generated.ServiceLevelAgreementFaultType;
import org.globus.exec.generated.StagingFaultType;
import org.globus.exec.generated.StagingTypeEnumeration;
import org.globus.exec.generated.StateEnumeration;
import org.globus.exec.generated.UnsupportedFeatureFaultType;
import org.globus.exec.utils.Resources;
import org.globus.gram.GramException;
import org.globus.util.I18n;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.container.ServiceHost;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.utils.AddressingUtils;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.utils.FaultHelper;

import org.w3c.dom.Element;


/**
 * Utility class containing static methods for constructing GRAM faults.
 */
public class FaultUtils
{
    private static Log logger = LogFactory.getLog(FaultUtils.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());


    public static
    ServiceLevelAgreementFaultType createServiceLevelAgreementFault(
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey,
            Exception                               cause) {

        String description = i18n.getMessage(
                "ServiceLevelAgreementFault00",
                new String[] { attribute });

        ServiceLevelAgreementFaultType f
            = (ServiceLevelAgreementFaultType) makeFault(
                ServiceLevelAgreementFaultType.class, resourceKey,
                description, cause,
                jobState, command, gt2ErrorCode);

        f.setAttribute(attribute);

        return f;
    }

    public static
    ServiceLevelAgreementFaultType createServiceLevelAgreementFault(
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey) {
        return createServiceLevelAgreementFault(
                attribute, jobState, command, gt2ErrorCode, resourceKey, null);
    }

    public static InvalidPathFaultType createInvalidPathFault(
            String                                  path,
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey,
            Exception                               cause) {

        String description = null;
        if (path != null) {
            description = i18n.getMessage(
                    "InvalidPathFault00",
                    new String [] { attribute, path });
        } else {
            description = i18n.getMessage(
                    "InvalidPathFault01",
                    new String [] { attribute });
        }

        InvalidPathFaultType f = (InvalidPathFaultType) makeFault(
            InvalidPathFaultType.class, resourceKey, description, cause,
            jobState, command, gt2ErrorCode);

        f.setPath(path);
        f.setAttribute(attribute);

        return f;
    }

    public static InvalidPathFaultType createInvalidPathFault(
            String                                  path,
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey) {
        return createInvalidPathFault(
            path, attribute, jobState, command, gt2ErrorCode, resourceKey, null);
    }

    public static FilePermissionsFaultType createFilePermissionsFault(
            String                                  path,
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey,
            Exception                               cause) {

        String description = null;
        if (path != null) {
            description = i18n.getMessage(
                    "FilePermissionsFault00",
                    new String [] { attribute, path });
        } else {
            description = i18n.getMessage(
                    "FilePermissionsFault01",
                    new String [] { attribute });
        }

        FilePermissionsFaultType f = (FilePermissionsFaultType) makeFault(
            FilePermissionsFaultType.class, resourceKey, description, cause,
            jobState, command, gt2ErrorCode);

        f.setPath(path);
        f.setAttribute(attribute);

        return f;
    }

    public static FilePermissionsFaultType createFilePermissionsFault(
            String                                  path,
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey) {
        return createFilePermissionsFault(
            path, attribute, jobState, command, gt2ErrorCode, resourceKey, null);
    }

    public static UnsupportedFeatureFaultType createUnsupportedFeatureFault(
            String                                  feature,
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey,
            Exception                               cause) {

        String description = i18n.getMessage(
                "UnsupportedFeatureFault00",
                new String [] { attribute, feature });

        UnsupportedFeatureFaultType f = (UnsupportedFeatureFaultType) makeFault(
            UnsupportedFeatureFaultType.class, resourceKey, description, cause,
            jobState, command, gt2ErrorCode);

        f.setFeature(feature);
        f.setAttribute(attribute);

        return f;
    }

    public static UnsupportedFeatureFaultType createUnsupportedFeatureFault(
            String                                  feature,
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey) {
        return createUnsupportedFeatureFault(
            feature, attribute, jobState, command, gt2ErrorCode, resourceKey,
            null);
    }

    public static CredentialSerializationFaultType
            createCredentialSerializationFault(
                StateEnumeration                    jobState,
                String                              command,
                int                                 gt2ErrorCode,
                ResourceKey                         resourceKey,
                Exception                           cause) {

        String description = i18n.getMessage("CredentialSerializationFault00");

        CredentialSerializationFaultType f
            = (CredentialSerializationFaultType) makeFault(
                CredentialSerializationFaultType.class, resourceKey,
                description, cause,
                jobState, command, gt2ErrorCode);

        return f;
    }

    public static CredentialSerializationFaultType
            createCredentialSerializationFault(
                StateEnumeration                    jobState,
                String                              command,
                int                                 gt2ErrorCode,
                ResourceKey                         resourceKey) {
        return createCredentialSerializationFault(
            jobState, command, gt2ErrorCode, resourceKey, null);
    }

    public static InsufficientCredentialsFaultType
            createInsufficientCredentialsFault(
                StateEnumeration                    jobState,
                String                              command,
                int                                 gt2ErrorCode,
                ResourceKey                         resourceKey,
                Exception                           cause) {

        String description = i18n.getMessage("InsufficientCredentialsFault00");

        InsufficientCredentialsFaultType f
            = (InsufficientCredentialsFaultType) makeFault(
                InsufficientCredentialsFaultType.class, resourceKey,
                description, cause,
                jobState, command, gt2ErrorCode);

        return f;
    }

    public static InsufficientCredentialsFaultType
            createInsufficientCredentialsFault(
                StateEnumeration                        jobState,
                String                                  command,
                int                                     gt2ErrorCode,
                ResourceKey                             resourceKey) {
        return createInsufficientCredentialsFault(
            jobState, command, gt2ErrorCode, resourceKey, null);
    }

    public static InvalidCredentialsFaultType createInvalidCredentialsFault(
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey,
            Exception                               cause) {

        String description = i18n.getMessage("InvalidCredentialsFault00");

        InvalidCredentialsFaultType f = (InvalidCredentialsFaultType) makeFault(
            InvalidCredentialsFaultType.class, resourceKey, description, cause,
            jobState, command, gt2ErrorCode);

        return f;
    }

    public static InvalidCredentialsFaultType createInvalidCredentialsFault(
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey) {
        return createInvalidCredentialsFault(
            jobState, command, gt2ErrorCode, resourceKey, null);
    }

    public static InternalFaultType createInternalFault(
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey,
            Exception                               cause) {

        String description = i18n.getMessage(
                "InternalFault00",
                new String [] { command.toString() });

        InternalFaultType f = (InternalFaultType) makeFault(
            InternalFaultType.class, resourceKey, description, cause,
            jobState, command, gt2ErrorCode);

        return f;
    }

    public static InternalFaultType createInternalFault(
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey) {
        return createInternalFault(
            jobState, command, gt2ErrorCode, resourceKey, null);
    }

    public static StagingFaultType createStagingFault(
            String                                  source,
            String                                  destination,
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            ResourceKey                             resourceKey,
            Exception                               cause) {

        String description = null;
        if (source != null && destination != null) {
            description = i18n.getMessage(
                    "StagingFault01",
                    new String [] { attribute, source, destination });
        } else if (attribute == null) {
            description = i18n.getMessage("StagingFault03");
        } else if (attribute.equals("fileCleanUp")) {
            description = i18n.getMessage(
                    "StagingFault02",
                    new String [] { attribute });
        } else {
            description = i18n.getMessage(
                    "StagingFault02",
                    new String [] { attribute });

        }

        StagingFaultType f = (StagingFaultType) makeFault(
            StagingFaultType.class, resourceKey, description, cause,
            jobState, command, 0);

        f.setSource(source);
        f.setDestination(destination);
        f.setAttribute(attribute);

        return f;
    }

    public static StagingFaultType createStagingFault(
            String                                  source,
            String                                  destination,
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            ResourceKey                             resourceKey) {
        return createStagingFault(
            source, destination, attribute, jobState,
            command, resourceKey, null);
    }

    public static ExecutionFailedFaultType createExecutionFailedFault(
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey,
            Exception                               cause) {

        String description = i18n.getMessage("ExecutionFailedFault00");

        ExecutionFailedFaultType f = (ExecutionFailedFaultType) makeFault(
            ExecutionFailedFaultType.class, resourceKey, description, cause,
            jobState, command, gt2ErrorCode);

        f.setAttribute(attribute);

        return f;
    }

    public static ExecutionFailedFaultType createExecutionFailedFault(
            String                                  attribute,
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey) {
        return createExecutionFailedFault(
            attribute, jobState, command, gt2ErrorCode, resourceKey, null);
    }

    public static FaultType createFault(
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey,
            Exception                               cause) {

        logger.debug("Script Command: " + command);

        String description = null;
        if (gt2ErrorCode > 0) {
            description = GramException.getMessage(gt2ErrorCode);
        } else {
            description = i18n.getMessage("Fault00");
        }

        FaultType f = makeFault(
            FaultType.class, resourceKey, description, cause,
            jobState, command, gt2ErrorCode);

        return f;
    }

    public static FaultType createFault(
            StateEnumeration                        jobState,
            String                                  command,
            int                                     gt2ErrorCode,
            ResourceKey                             resourceKey) {
        logger.debug("Script Command: " + command);
        return createFault(jobState, command, gt2ErrorCode, resourceKey, null);
    }

    public static FaultType makeFault(
            Class                                   faultClass,
            ResourceKey                             resourceKey,
            String                                  description,
            Exception                               cause,
            StateEnumeration                        stateWhenFailureOccurred,
            String                                  command,
            int                                     gt2ErrorCode)
    {
        Calendar timestamp = Calendar.getInstance();

        if (logger.isDebugEnabled())
        {
            logger.debug("Fault Class: " + faultClass);
            logger.debug("Resource Key: " + resourceKey);
            logger.debug("Description: " + description);
            logger.debug("Cause: " + cause);
            logger.debug("State when failure occurred "
                        + stateWhenFailureOccurred);
            logger.debug("Script Command: " + command);
            logger.debug("GT2 Error Code: " + gt2ErrorCode);
        }

        //originator
        EndpointReferenceType originator = getEndpointReference(resourceKey);

        //errorCode
        /*
        BaseFaultTypeErrorCode errorCode = new BaseFaultTypeErrorCode();
        //errorCode.setDialect(FaultHelper.STACK_TRACE);
        errorCode.setDialect(FaultHelper.EXCEPTION);
        String stackTrace = JavaUtils.stackToString(
            new Exception().fillInStackTrace());
        errorCode.set_any(AnyHelper.toText(stackTrace));
        */

        FaultType fault = null;
        try {
            fault = (FaultType) faultClass.newInstance();
            FaultHelper faultHelper = new FaultHelper(fault);

            fault.setOriginator(originator);

            /*
            if (errorCode != null)
            {
                logger.debug("setting fault error code");
                fault.setErrorCode(errorCode);
            }
            */

            faultHelper.addDescription(description);

            if (cause != null)
            {
                BaseFaultType faultCause = null;
                if (!(cause instanceof BaseFaultType))
                {
                    faultCause = FaultHelper.toBaseFault(cause);
                }
                else
                {
                    faultCause = (BaseFaultType) cause;
                }

                //addFaultData(faultCause);

                logger.debug("setting fault cause");
                faultHelper.addFaultCause(faultCause);
            }

            fault.setStateWhenFailureOccurred(stateWhenFailureOccurred);
            logger.debug("Script Command: " + command);
            fault.setCommand(command);
            fault.setGt2ErrorCode(0);
        } catch (Exception e) {
            String errorMessage =
                i18n.getMessage(Resources.FAULT_CREATION_ERROR,
                                faultClass.getName());
            logger.error(errorMessage, e);
        }

        return fault;
    }

    private static void addFaultData(
        BaseFaultType                       fault)
    {
        logger.debug("Entering addStackTraceFault()");

        //pull out faultData element from faultDetails
        QName faultDataName = new QName("faultData");
        Element faultDataElement = fault.lookupFaultDetail(faultDataName);

        if (faultDataElement == null) {
            logger.debug("Unable to find fault detail.");
            return;
        }
        fault.removeFaultDetail(faultDataName);

        //pull out exceptionName element from faultDetails
        Element faultTypeElement = fault.lookupFaultDetail(
            Constants.QNAME_FAULTDETAIL_EXCEPTIONNAME);
        fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_EXCEPTIONNAME);
        String faultTypeName = null;
        try {
            faultTypeName = (String) ObjectDeserializer.toObject(
                faultTypeElement, String.class);
        } catch (Exception e)
        {
            logger.debug("Deserialization of faultData failed", e);
            return;
        }

        //use exceptionName to deserialize faultData
        BaseFaultType faultData = null;
        try {
            faultData = (BaseFaultType) ObjectDeserializer.toObject(
                faultDataElement, Class.forName(faultTypeName));
        } catch (Exception e)
        {
            logger.debug("Deserialization of exceptionName failed", e);
            return;
        }

        //add deserialized faultData as a FaultCause
        logger.debug("Adding fault detail as a FaultCause.");
        FaultHelper faultHelper = new FaultHelper(fault, false);
        faultHelper.addFaultCause(faultData);

        logger.debug("Leaving addStackTraceFault()");
    }


    private static EndpointReferenceType getEndpointReference(
            ResourceKey                             resourceKey) {
        try
        {
            EndpointReferenceType epr = AddressingUtils.createEndpointReference(
                ServiceHost.getBaseURL() + "ManagedJobFactoryService",
                resourceKey);

            return epr;
        }
        catch(Exception e)
        {
            logger.error("Error creating job EPR", e);
        }

        return null;
    }

    /**
     *
     * <b>precondition</b> fault != null
     * @return String
     */
    public static String faultToString(BaseFaultType fault) {
        if (fault == null) {
            String errorMessage = i18n.getMessage(
                Resources.PRECONDITION_VIOLATION, "fault == null");
            throw new RuntimeException(errorMessage);
        }

        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(fault.getClass());
        } catch (Exception e) {
            String errorMessage =
                i18n.getMessage(Resources.RESOURCE_DATA_INSTROSPECTION_ERROR,
                                fault.getClass().getName());
            throw new RuntimeException(errorMessage, e);
        }

        PropertyDescriptor[] propertyDescriptors
            = beanInfo.getPropertyDescriptors();

        StringBuffer faultDescription = new StringBuffer(
            "fault type: " + fault.getClass().getName() + ":\n");
        //StringBuffer cause = new StringBuffer();
        FaultHelper faultHelper = new FaultHelper(fault);
        for (int index=0; index<propertyDescriptors.length; index++)
        {
            PropertyDescriptor propertyDescriptor
                = propertyDescriptors[index];
            String propertyName = propertyDescriptor.getName();

            //skip over some known properties
            if (   propertyName.equals("serializer")
                || propertyName.equals("deserializer")
                || propertyName.equals("errorCode")
                || propertyName.equals("faultCause")
                || propertyName.equals("faultCode")
                || propertyName.equals("faultDetails")
                || propertyName.equals("class"))
            {
                continue;
            }

            //get the read method (continue if property can't be read)
            Method readMethod = propertyDescriptor.getReadMethod();
            if (readMethod == null)
            {
                continue;
            }

            //get the property value
            Object value  = null;
            try {
                value = readMethod.invoke(fault, null);
            } catch (Exception e) {
                String errorMessage = i18n.getMessage(
                    Resources.FAULT_PROPERTY_READ_METHOD_ERROR, propertyName);
                throw new RuntimeException(errorMessage);
            }

            //append appropriate text to the fault description
            if (value != null)
            {
                if (propertyName.equals("description"))
                {
                    faultDescription.append(
                        propertyName + ":\n"
                        + faultHelper.getDescriptionAsString() + "\n");
                }
                else
                if (propertyName.equals("stackTrace"))
                {
                    faultDescription.append(
                        propertyName + ":\n"
                        + faultHelper.getStackTrace() + "\n");
                }
                else
                {
                    //append the property description
                    faultDescription.append(
                        propertyName + ": " + value.toString() + "\n");
                }
            }
            else
            if (logger.isDebugEnabled())
            {
                logger.debug("Fault property " + propertyName + " is null.");
            }
        }

        faultDescription.append(
            "Message:\n"
            + faultHelper.getMessage());

        return faultDescription.toString();
    }
}
