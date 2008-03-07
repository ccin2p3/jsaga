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
package org.globus.wsrf.utils;

import java.util.ListResourceBundle;

/**
 * English translations of error messages.
 */
public class Resources extends ListResourceBundle
{
    public Object[][] getContents()
    {
        return contents;
    }

    static final Object[][] contents =
        {
            {"configError", "{0} not configured correctly"},
            {"configFileError", "Configuration file {0} not found"},
            {"consumerError", "Failed to contact consumer {0}"},
            {"schemaPathNotFound", "Path to schema was not found"},
            {"noDeserializer", "Could not find deserializer for: {0}"},
            {
                "rpSerializationError",
                "Failed to serialize resource property {0}"
            },
            {
                "rpDocSerializationError",
                "Failed to create resource resource property document"
            },
            {
                "invalidQueryExpressionDialect",
                "Invalid query expression dialect"
            },
            {
                "noQueryString",
                "No query string specified"
            },
            {
                "unsupportedXpathReturn",
                "Unsupported XPath result return type ''{0}''"
            },
            {
                "unsupportedQueryReturnType",
                "Unsupported query return type ''{0}''"
            },
            {
                "queryFailed",
                "Failed to execute query"
            },
            {
                "containerPropertiesNotSpecified",
                "Container properties must be specified"
            },
            {
                "containerClassError",
                "Class {0} must be an instance of SerivceContainer"
            },
            {
                "startServer",
                "Starting SOAP server at: {0} \nWith the following services:\n"
            },
            {
                "startServerError",
                "Failed to start container: {0}"
            },
            {
                "stopServer",
                "Stopped SOAP Axis server at: {0}                         "
            },
            {
                "serviceListError",
                "Failed to obtain a list of services from ''{0}'' service: {1}"
            },
            {
                "nodeliver",
                "Exception occurred during notification delivery, msg={0} "
            },
            {
                "initialContextFactorySet",
                "INITIAL_CONTEXT_FACTORY already set to {0}"
            },
            {"jndiConfigFileOption", "jndiConfigFile"},
            {"nullJNDIConfigInput", "JNDI config input stream is null"},
            {
                "jndiConfigNotFound",
                "Could not find JNDI configuration file"
            },
            {
                "jndiConfigParseError",
                "Failed to read ''{0}'' JNDI configuration file"
            },
            {"expectedType", "Expected object of type ''{0}''"},
            {"invalidType", "Invalid type ''{0}''"},
            {"invalidResourceType", "Resource object ''{0}'' must implement Resource interface"},
            {
                "invalidValueForType",
                "Invalid value ''{0}'' for type ''{1}''"
            },
            {
                "cantConvertType",
                "Unable to convert {0} to {1}"
            },
            {
                "failedToConvert",
                "Data conversion failed: {0}"
            },
            {
                "unsupportedType",
                "Type not supported"
            },
            {"nullArgument", "Argument {0} is null"},
            {"causedBy", " caused by "},
            {"causedBy01", "Caused by: "},
            {"timestamp", "Timestamp: "},
            {"originator", "Originator: "},
            {
                "notificationConsumerHomeLookupFailure",
                "Failed to acquire notification consumer home instance from registry"
            },
            {
                "notificationConsumerNotListening",
                "Notification Consumer must be in listening state when invoking this operation"
            },
            {
                "notificationConsumerArgumentMismatch",
                "There must be an equal number of topic paths and callbacks"
            },
            {
                "notificationConsumerCleanupFailed",
                "Failed to remove notification consumer"
            },
            {
                "topicEngineConfigError",
                "Failed to get engine instance"
            },
            {
                "topicEngineInitError",
                "Failed to initialize topic expression engine"
            },
            {
                "invalidSimpleTopicPath",
                "Topic path either null or not of size 1"
            },
            {
                "addingSubTopicToReference",
                "Can''t add sub topics to a reference topic"
            },
            {
                "contextNotMessageContext",
                "{0} must be a Axis MessageContext"
            },
            {
                "reflectionRPNoAccessorMethod",
                "Accessor method required for ''{0}'' property"
            },
            {
                "reflectionRPNoGetMethod",
                "Object or propertyName not set"
            },
            {
                "modifyReadOnly",
                "Cannot modify read-only property"
            },
            {
                "resourceInitError",
                "Failed to initialize resource"
            },
            {
                "queryEngineInitError",
                "Failed to initialize query expression engine"
            },
            {
                "queryEngineLookupError",
                "Failed to initialize query engine"
            },
            {
                "unsupportedQueryDialect",
                "Query dialect not supported: {0}"
            },
            {
                "rpNotElement",
                "The Schema type for the resource property set has a" +
                " field called ''{0}'' that is not an element. This is " +
                "forbidden by the WS-ResourceProperties specification, " +
                "section 4.2, item 3"
            },
            {
                "resourceClassConfigError",
                "Resource class not configured"
            },
            {
                "resourceKeyConfigError",
                "Resource key name not configured"
            },
            {
                "invalidResourceClass",
                "Resource class must implement {0} interface"
            },
            {
                "backingFileNotFound",
                "File {0} for resource {1} was not found"
            },
            {
                "resourceLoadFailed",
                "Failed to load the resource"
            },
            {
                "resourceStoreFailed",
                "Failed to store the resource"
            },
            {
                "noQuery",
                "Empty query"
            },
            {
                "noRPName",
                "Empty resource property name"
            },
            {
                "noRPNames",
                "Empty resource property names"
            },
            {
                "eprCreationFailed",
                "Failed to create endpoint reference"
            },
            {
                "resourceDisoveryFailed",
                "Failed to acquire resource"
            },
            {
                "emptyTopicExpression",
                "Empty topic expression"
            },
            {
                "emptyConsumerReference",
                "Empty consumer reference"
            },
            {
                "topicExpressionResolutionFailed",
                "Failed to resolve topic expression to a set of concrete topics"
            },
            {
                "subscriptionRemoveFailed",
                "Failed to remove subscription resource"
            },
            {
                "subscriptionCreateFailed",
                "Failed to create subscription resource"
            },
            {
                "subscriptionFindFailed",
                "Failed to acquire subscription resource with key ''{0}''"
            },
            {
                "notifyCallbackError",
                "Failed to invoke notify callback for notification"
            },
            {
                "notificationFailed",
                "Failed to send notification for subscription with key ''{0}''"
            },
            {
                "notificationSerializationError",
                "Failed to serialize notification message"
            },
            {
                "resourceRemoveFailed",
                "Failed to remove resource"
            },
            {
                "rpsNotSupported",
                "Resource does not implement ResourceProperties interface"
            },
            {
                "rltNotSupported",
                "Resource does not implement ResourceLifetime interface"
            },
            {
                "noTerminationTimeRP",
                "No termination time resource property"
            },
            {
                "messageLoggingError",
                "Failed to log SOAPEnvelope"
            },
            {
                "notImplemented",
                "Not implemented"
            },
            {
                "genericSerializationError",
                "Serialziation failed"
            },
            {
                "genericDeserializationError",
                "Deserialziation failed"
            },
            {
                "noDialectSerializer",
                "Couldn''t find serializer factory for ''{0}'' dialect"
            },
            {
                "noDialectDeserializer",
                "Couldn''t find deserializer factory for ''{0}'' dialect"
            },
            {
                "noServiceName",
                "Service name must be specified since no path was found"
            },
            {
                "buildURLError",
                "Could not reconstruct URL"
            },
            {
                "noTypeDesc",
                "The ''{0}'' bean does not have type description information"
            },
            {
                "illegalReferenceType",
                "The reference type ''{0}'' is neither 0 (hard), 1 (soft) or 2 (weak)"
            },
            {
                "noToHeader",
                "The WS-Addressing ''To'' request header is missing"
            },
            {
                "secureNotificationSetupFailed",
                "Failed to set up the notification security descriptor"
            },
            {
                "secureSubscriptionSetupFailed",
                "Failed to set up the subscription security descriptor"
            },
            {
              "noTopicList",
              "Notification producer WS-Resource does not implement the correct java interface"
            },
            {
                "filename00",
                "File name is: {0}"
            },
            {
                "filename01",
                "{0}: requested file name = ''{1}''"
            },
            {
                "params00",
                "Parameters are: {0}"
            },
            {
                "serverFault00",
                "Error processing request"
            },
            {
                "serverFault01",
                "Run out of heap during request processing"
            },
            {
                "serverFault02",
                "Unexpected error during request processing"
            },
            {
                "unexpectedEOS00",
                "Unexpected end of stream"
            },
            {
                "badRequest00",
                "Cannot handle non-GET, non-POST, non-HEAD request"
            },
            {
                "unsupportedHTTPMajor",
                "Unsupported HTTP major version"
            },
            {
                "unsupportedHTTMinor",
                "Unsupported HTTP minor version"
            },
            {
                "malformedHTTPVersion",
                "Malformed HTTP version information"
            },
            {
                "noMsgContext",
                "MessageContext not associated with the thread"
            },
            {
                "noTargetServiceSet",
                "Target service not set"
            },
            {
                "noServiceSet",
                "Service not set"
            },
            {
                "noEngineSet",
                "Engine not set"
            },
            {
                "applicationScopeNeeded",
                "Service ''{0}'' must be deployed with application scope"
            },
            {
                "containerInitError",
                "Container failed to initialize"
            },
            {
                "containerStopError",
                "Container failed to stop"
            },
            {
                "beanInitFailed",
                "Bean initialization failed"
            },
            {
                "beanSecInitFailed",
                "Bean security initialization failed"
            },
            {
                "typeOrClassRequired",
                "type or Java class required"
            },
            {
                "storDirFailed",
                "Failed to create storage directory: ''{0}''"
            },
            {
                "storDirPerm",
                "No permissions to read and/or write to storage directory: ''{0}''"
            },
            {
                "noTransURL",
                "Transport URL property not set"
            },
            {
                "hardShutdownNotSupported",
                "Hard shutdown not supported"
            },
            {
                "shutdownFailure",
                "Shutdown failed"
            },
            {
                "failedInitService",
                "Failed to initialize ''{0}'' service"
            },
            {
                "invalidStorageDir",
                "Invalid storage directory ''{0}''"
            },
            {
              "noValidCreds",
              "Secure container requires valid credentials"
            },
            {
                "requestFailed",
                "Error processing request"
            },
            {
                "invalidWebRoot",
                "Invalid web root path"
            },
            {
                "noHostname",
                "Failed to get host name"
            },
            {
                "general",
                "Mystery error"
            },
            {
                "errorClosingInputStream",
                "Error closing input stream"
            },
            {
                "errorClosingOutputStream",
                "Error closing output stream"
            },
            {
                "errorClosingSocket",
                "Error closing socket"
            },
            {
                "errorWritingResponse",
                "Error writing response"
            },
            {
                "unexpectedError",
                "Unexpected Error"
            }
        };
}
