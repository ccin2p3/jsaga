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

import java.util.ListResourceBundle;

public class Resources extends ListResourceBundle {
    public Object[][] getContents() {
        return contents;
    }

    //secondary faults
    public static final String SECONDARY_FAULT
        = "SecondaryFault";

    //delegated credential errors
    public static final String DELEGATED_PROXY_SUBJECT_ERROR
        = "ProxySubjectError";
    public static final String CREDENTIAL_ACQUISITION_ERROR
        = "CredentialAcquisitionError";
    public static final String MISSING_STAGING_CREDENTIAL
        = "MissingStagingCredential";

    //thread wait interrruption errors
    public static final String INTERRUPTED_CRED_WAIT
        = "InterruptCredentialsAcquisition";
    public static final String INTERRUPTED_RESOURCE_REMOVE_WAIT
        = "InterruptResourceRemove";
    public static final String INTERRUPTED_JOB_SCRIPT_WAIT
        = "InterruptJobScript";
    public static final String INTERRUPTED_TRANSFER_LISTENER_WAIT
        = "InterruptTransferListenerWait";

    //topic persistence errors
    public static final String TOPIC_SERIALIZ_ERROR = "TopicSerializationError";
    public static final String TOPIC_REGISTRATION_ERROR
        = "TopicRegistrationError";

    //Globus Sh Tools errors
    public static final String GLOBUS_SH_TOOLS_NOT_FOUND
        = "GlobusShToolsNotFoundError";
    public static final String GLOBUS_SH_PROPERTY_NOT_FOUND
        = "GlobusShPropertyNotFoundError";

    //JNDI configuration error
    public static final String JNDI_CONFIG_ERROR = "JndiConfigError";
    public static final String JNDI_MJFS_CONFIG_ERROR = "JndiMJFSConfigError";

    //resource destruction errors
    public static final String RFT_RESOURCE_DESTROY_ERROR = "RFTDestroyError";
    public static final String REMOTE_JOB_DESTROY_ERROR
        = "RemoteJobDestructionError";
    public static final String REMOTE_RESOURCE_DESTROY_ERROR
        = "RemoteResourceDestructionError";

    public static final String STATE_NOTIFICATION_PROCESSING_ERROR
        = "StateNotifProcessError";

    //data conversion errors
    public static final String PROCESS_COUNT_TO_CAPACITY_CONVERSION_ERROR
        = "ProcessCountToCapacityError";
    public static final String SUBJOB_COUNT_TO_CAPACITY_CONVERSION_ERROR
        = "SubjobCountToCapacityError";
    public static final String ANY_TO_ELEMENT_CONVERSION_ERROR
        = "AnyToElementError";
    public static final String JOB_DESC_TO_XML_CONVERSION_ERROR
        = "JobDescToXMLError";
    public static final String GLOBUS_CREDENTIAL_TO_GSS_CONVERSION_ERROR
        = "GlobusCredentialToGSSError";

    //object instanciation errors
    public static final String PERSISTENCE_HELPER_CREATION_ERROR
        = "PersistHelperCreationError";
    public static final String FAULT_CREATION_ERROR = "FaultCreationError";

    public static final String JOB_ID_NOT_FOUND = "JobIdNotFound";

    //job id (un)registration errors
    public static final String JOB_ID_UNREGISTRATION_ERROR
        = "JobIdUnregistrationError";
    public static final String JOB_ID_REGISTRATION_ERROR
        = "JobIdRegistrationError";

    //resource find() errors
    public static final String NOTIFICATION_TARGET_RESOURCE_NOT_FOUND
        = "NotificationTargetResourceNotFound";
    public static final String RESOURCE_FIND_ERROR = "ResourceFindError";

    public static final String SCRIPT_STDERR_READ_ERROR
        = "ScriptStderrReadError";

    public static final String JOB_STDERR_EXISTS = "StderrExistsForJob";

    public static final String RP_SET_ERROR = "CouldNotSetRP";
    public static final String RP_NOT_FOUND = "RPnotFound";

    public static final String NO_FILE_MAPPING_FOUND = "NoFileMappingFound";

    public static final String FILE_MAPPING_CONFIG_EMPTY
        = "FileMappingConfigEmpty";

    public static final String STAGE_IN_DEST_NOT_A_URL = "StageInDestIsNoURL";

    public static final String UNKNOWN_GRAM_FAULT_CODE = "UnknownGRAMFaultCode";

    public static final String FETCH_JOB_DESCRIPTION_ERROR
        = "FetchJobDescriptionError";

    //proxy file I/O errors
    public static final String PROXY_FILE_CREATION_ERROR
        = "ProxyFileCreationError";
    public static final String PROXY_FILE_WRITING_ERROR
        = "ProxyFileWritingError";
    public static final String PROXY_FILE_REMOVE_ERROR = "ProxyFileRemoveError";

    //resource data errors
    public static final String RESOURCE_DATA_INSTROSPECTION_ERROR
        = "ResourceDataInstrospectError";
    public static final String RESOURCE_DATUM_NOT_FOUND
        = "ResourceDatumNotFound";
    public static final String RESOURCE_DATUM_SET_ERROR
        = "ResourceDatumSetError";
    public static final String RESOURCE_DATUM_GET_ERROR
        = "ResourceDatumGetError";

    //fault write errors
    public static final String FAULT_WRITE_METHOD_NOT_FOUND
        = "FaultWriteMethodNotFound";
    public static final String FAULT_WRITE_METHOD_ERROR
        = "FaultWriteMethodError";

    public static final String FAULT_PROPERTY_READ_METHOD_ERROR
        = "FaultPropertyReadMethodError";

    public static final String STATE_TRANSITION_METHOD_NOT_FOUND
        = "StateTransitionMethodNotFound";
    public static final String STATE_TRANSITION_METHOD_ERROR
        = "StateTransitionMethodError";

    //EPR creation errors
    public static final String DELEGATION_EPR_CREATION_ERROR
        = "DelegationEPRCreationError";
    public static final String STAGING_EPR_CREATION_ERROR
        = "StagingEPRCreationError";

    public static final String INTERNAL_ERROR = "InternalError";

    //errors while storing persistently
    public static final String POST_UPDATE_PERSISTENCE_ERROR
        = "PostUpdateSaveError";
    public static final String POST_SUBJOB_CREATION_PERSISTENCE_ERROR
        = "PostUpdateSaveError";

    public static final String RESOURCE_SET_STATE_NOTIFY_ERROR
        = "ResourceSetStateAndNotifyError";
    public static final String MULTI_RESOURCE_SET_STATE_FAILED_NOTIFY_ERROR
        = "MultiResourceSetStateToFailedAndNotifyError";

    public static final String SCRIPT_EXECUTION_ERROR = "ScriptExecutionError";

    public static final String NOTIFICATION_CONSUMER_CREATION_ERROR
        = "NotificationConsumerCreationError";
    public static final String NOTIFICATION_CONSUMER_REMOVAL_ERROR
        = "NotificationConsumerRemovalError";

    public static final String CREDENTIAL_CONFIGURATION_ERROR
        = "CredentialConfigurationError";

    public static final String CONTAINER_CONFIGURATION_NOT_FOUND
        = "ContainerConfigurationNotFound";

    public static final String UNKNOWN_AUTHORIZATION = "UnknownAuthorization";

    public static final String PRECONDITION_VIOLATION = "PreconditionViolation";

    //stdout/err RP error
    public static final String INVALID_OUTPUT_URL = "BadOutputtUrl";

    //sudo error
    public static final String MISCONFIGURED_SUDO = "MisconfiguredSudo";

    //variable resolution error
    public static final String VARIABLE_RESOLUTION_ERROR
        = "VariableResolutionError";
    public static final String NULL_VARIABLE_VALUE
        = "NullVariableValue";

    //rate limiting messages
    public static final String BAD_RUN_QUEUE_THREAD_COUNT
        = "BadRunQueueThreadCount";

    public static final String CONFIGURATION_PARAMETER_CHANGED_FROM_DEFAULT
        = "ConfigurationParameterChangedFromDefault";

    public static final String REPORT_RUN_QUEUE_THREAD_COUNT
        = "ReportRunQueueThreadCount";

    //user cancel messages
    public static final String USER_CANCEL
        = "UserCancel";

    //No fault message
    public static final String NO_FAULT_ON_FAILURE
        = "NoFaultOnFailure";

    //Variable resolution messages
    public static final String VARIABLE_DEPTH_EXCEEDED
        = "VariableDepthExceeded";
    public static final String UNALLOWED_FILE_URL_HOST
        = "UnallowedFileUrlHost";
    public static final String MISSING_URL_HOST
        = "MissingUrlHost";

    //RFT errors
    public static final String MISSING_RFT_STATUS
        = "MissingRftStatus";

    //Process termination
    public static final String ABNORMAL_PROCESS_TERMINATION
        = "ProcessDied";

    //StateMachine failure
    public static final String STATE_TRANSITION_FAILED
        = "StateTransitionFailed";


    static final Object[][] contents = {
        {
            SECONDARY_FAULT,
            "A secondary fault occured while trying to gracefully fail."
        },
        {
            "Fault00",
            "A fault occurred."
        },
        {
            PRECONDITION_VIOLATION,
            "Precondition violation: {0}"
        },
        {
            UNKNOWN_AUTHORIZATION,
            "Unsupported authorization method class {0}"
        },
        {
            FAULT_PROPERTY_READ_METHOD_ERROR,
            "Failed to get fault property {0}"
        },
        {
            CREDENTIAL_CONFIGURATION_ERROR,
            "Invalid credential configuration in security descriptor"
        },
        {
            GLOBUS_CREDENTIAL_TO_GSS_CONVERSION_ERROR,
            "Unable to convert GlobusCredential to GlobusGSSCredentialImpl"
        },
        {
            NOTIFICATION_CONSUMER_REMOVAL_ERROR,
            "Unable to remove notification consumer"
        },
        {
            NOTIFICATION_CONSUMER_CREATION_ERROR,
            "Unable to setup notification consumer"
        },
        {
            SCRIPT_EXECUTION_ERROR,
            "Unable to invoke runScript()."
        },
        {
            MULTI_RESOURCE_SET_STATE_FAILED_NOTIFY_ERROR,
            "Unable to notify about failed sub-job creation."
        },
        {
            RESOURCE_SET_STATE_NOTIFY_ERROR,
            "Unable to set resource state."
        },
        {
            STATE_TRANSITION_FAILED,
            "Unable to process state transition."
        },
        {
            STATE_TRANSITION_METHOD_ERROR,
            "Unable to invoke state transition method {0}"
        },
        {
            STATE_TRANSITION_METHOD_NOT_FOUND,
            "Unable to get state transition method {0}"
        },
        {
            POST_UPDATE_PERSISTENCE_ERROR,
            "Unable to save resource after setting \"{0}\"."
        },
        {
            POST_SUBJOB_CREATION_PERSISTENCE_ERROR,
            "Unable to update resource data after creating sub-jobs."
        },
        {
            DELEGATION_EPR_CREATION_ERROR,
            "Unable to set delegation service endpoint with address {0}"
        },
        {
            STAGING_EPR_CREATION_ERROR,
            "Unable to set staging factory service endpoint with address {0}"
        },
        {
            INTERNAL_ERROR,
            "Internal error."
        },
        {
            FAULT_WRITE_METHOD_NOT_FOUND,
            "Failed to get write method for fault type {0}."
        },
        {
            FAULT_WRITE_METHOD_ERROR,
            "Failed to set fault of type {0}."
        },
        {
            CREDENTIAL_ACQUISITION_ERROR,
            "Couldn't obtain a delegated credential."
        },
        {
            RESOURCE_DATA_INSTROSPECTION_ERROR,
            "Unable to introspect resource data class {0}."
        },
        {
            RESOURCE_DATUM_NOT_FOUND,
            "Failed to find resource datum {0}"
        },
        {
            RESOURCE_DATUM_GET_ERROR,
            "Failed to get resource datum {0}"
        },
        {
            RESOURCE_DATUM_SET_ERROR,
            "Failed to set resource datum {0}"
        },
        {
            PROXY_FILE_CREATION_ERROR,
            "Could not create proxy file"
        },
        {
            PROXY_FILE_REMOVE_ERROR,
            "Unabled to remove user proxy file at {0}"
        },
        {
            PROXY_FILE_WRITING_ERROR,
            "Could not save the proxy for user {0} to the file {1}"
        },
        {
            FETCH_JOB_DESCRIPTION_ERROR,
            "Could not get job description"
        },
        {
            REMOTE_JOB_DESTROY_ERROR,
            "Could not destroy job automatically"
        },
        {
            REMOTE_RESOURCE_DESTROY_ERROR,
            "Unable to destroy resource"
        },
        {
            UNKNOWN_GRAM_FAULT_CODE,
            "Unhandled fault code {0}"
        },
        {
            STAGE_IN_DEST_NOT_A_URL,
            "destination of fileStageIn is not a file URL"
        },
        {
            NO_FILE_MAPPING_FOUND,
            "No mapping for path"
        },
        {
            FILE_MAPPING_CONFIG_EMPTY,
            "File mapping configuration file {0} has no mappings."
        },
        {
            ANY_TO_ELEMENT_CONVERSION_ERROR,
            "AnyHelper.toElement() failed"
        },
        {
            JOB_ID_NOT_FOUND,
            "Unknown jobId: {0}"
        },
        {
            RP_NOT_FOUND,
            "Undefined resource property: {0}"
        },
        {
            RP_SET_ERROR,
            "Unable to set resource property {0}. Intended value is {1}."
        },
        {
            INTERRUPTED_JOB_SCRIPT_WAIT,
            "Interrupted waiting for job manager script with command {0}"
        },
        {
            INTERRUPTED_TRANSFER_LISTENER_WAIT,
            "Interrupted waiting for transfer listener."
        },
        {
            JOB_STDERR_EXISTS,
            "Script stderr:\n{0}"
        },
        {
            SCRIPT_STDERR_READ_ERROR,
            "Unable to read from script stderr"
        },
        {
            PERSISTENCE_HELPER_CREATION_ERROR,
            "Could not instanciate persistence helper"
        },
        {
            FAULT_CREATION_ERROR,
            "problem instantiating fault of type {0}"
        },
        {
            RESOURCE_FIND_ERROR,
            "Resource with ID {0} not found."
        },
        {
            JOB_ID_UNREGISTRATION_ERROR,
            "unable to stop monitoring job for state changes"
        },
        {
            JOB_ID_REGISTRATION_ERROR,
            "unable to monitor job for state changes"
        },
        {
            NOTIFICATION_TARGET_RESOURCE_NOT_FOUND,
            "Unable to deliver state change notification " +
            "-- resource associated with job does not exist"
        },
        {
            PROCESS_COUNT_TO_CAPACITY_CONVERSION_ERROR,
            "Could not convert the process count {0} to a rendezvous " +
            "capacity. Check that the <count> job attribute is > 0"
        },
        {
            SUBJOB_COUNT_TO_CAPACITY_CONVERSION_ERROR,
            "Could not convert the subjob count {0} to a rendezvous " +
            "capacity."
        },
        {
            STATE_NOTIFICATION_PROCESSING_ERROR,
            "Failed to process state notification."
        },
        {
            RFT_RESOURCE_DESTROY_ERROR,
            "Unable to destroy transfer."
        },
        {
            INTERRUPTED_RESOURCE_REMOVE_WAIT,
            "Interrupted waiting to remove resource."
        },
        {
            JOB_DESC_TO_XML_CONVERSION_ERROR,
            "An exception occured when transforming the job " +
                    "description object to an XML string."
        },
        {
            DELEGATED_PROXY_SUBJECT_ERROR,
            "Unable to get delegated proxy's subject name."
        },
        {
            JNDI_CONFIG_ERROR,
            "Problem getting GRAM JNDI configuration resource " +
                "with lookup string: {0}"
        },
        {
            JNDI_MJFS_CONFIG_ERROR,
            "Problem getting Managed Job Factory Service configuration"
        },
        {
            GLOBUS_SH_TOOLS_NOT_FOUND,
            "Unable to load globus-sh-tools-vars.sh as a properties file"
        },
        {
            GLOBUS_SH_PROPERTY_NOT_FOUND,
            "Globus Sh property not found for key {0}"
        },
        {
             TOPIC_SERIALIZ_ERROR,
             "Could not convert the resource topics " +
                "to a set of serializable subscription descriptions."
        },
        {
             TOPIC_REGISTRATION_ERROR,
             "Could not register the deserialized " +
                "notification listeners with the resource topics"
        },
        {
            "InvalidPathFault00",
            "Invalid {0} path \"{1}\"."
        },
        {
            "InvalidPathFault01",
            "Invalid {0} path."
        },
        {
            "StagingFault01",
            "Staging error for RSL element {0}, from {1} to {2}."
        },
        {
            "StagingFault02",
            "Staging error for RSL element {0}."
        },
        {
            "StagingFault03",
            "A staging fault occurred."
        },
        {
            "InternalFault00",
            "Internal fault occurred while running the {0} script."
        },
        {
            "DatabaseAccessFault00",
            "Unable to access the job state database."
        },
        {
            "CredentialFault01",
            "Unable to set service credential."
        },
        {
            "CredentialFault02",
            "Insufficient credentials to stream {0} to {1}."
        },
        {
            INTERRUPTED_CRED_WAIT,
            "Interrupted waiting for credential notification"
        },
        {
            "StreamServiceCreationFault00",
            "Unable to access {0} streaming service."
        },
        {
            "UnsupportedFeatureFault00",
            "The {0} {1} feature is not available on this resource."
        },
        {
            "FilePermissionsFault00",
            "Invalid file permissions on {0} {1}."
        },
        {
            "FilePermissionsFault01",
            "Invalid file permissions on {0}."
        },
        {
            "ServiceMisconfigured00",
            "The ManagedJobService was unable to activate the service for {0}."
        },
        {
            "JobCancelled00",
            "The managed job was cancelled."
        },
        {
            "ExecutionFailedFault00",
            "The executable could not be started."
        },
        {
            "ServiceLevelAgreementFault00",
            "Invalid {0} element."
        },
        {
            "RepeatedlyStartedFault00",
            "The Managed Job has already been started."
        },
        {
            "InvalidCredentialsFault00",
            "Unable to use delegated credentials."
        },
        {
            "InsufficientCredentialsFault00",
            "The delegated credentials are not sufficient to access "
                + "this resource."
        },
        {
            "CredentialSerializationFault00",
            "Unable to serialize a delegated credential."
        },
        {
            "persistedResourcePropertiesDirectoryMissing",
            "Unabled to locate persisted resource properties directory."
        },
        {
            "rslParsingFailed",
            "Unable to parse RSL"
        },
        {
            "resourceSecurityInitializationFailed",
            "Unabled to initialize resource security configuration."
        },
        {
            "startSecurityFailure",
            "Unable to start job because of a security failure."
        },
        {
            "filePermissionsChangeFailed",
            "Unable to set {0} permissions on file {1}."
        },
        {
            "changeOwnerFailed",
            "Unable to change owner of file {1} to {0}."
        },
        {
            "fileMoveFailed",
            "Unable to move file {0} to {1}."
        },
        {
            "cacheCreationFailed",
            "Unable to create job data directories."
        },
        {
            "cacheRemovalFailed",
            "Unable to remove job data directories."
        },
        {
            "multiJobNotSupported",
            "Multi-job RSL not supported for resource manager type \"{0}\"."
        },
        {
            "singleJobNotSupported",
            "Single-job RSL not supported for resource manager type \"{0}\"."
        },
        {
            "jobCreationFailed",
            "Job creation failed."
        },
        {
            "rslMissingEndpoint",
            "Endpoint missing from RSL."
        },
        {
            "rslMissing",
            "Netiher a single-job or multi-job RSL were found in the input "
            + "to the createManagedJob operation."
        },
        {
            "objectCreationFailed",
            "Unable to create {0} object instance."
        },
        {
            "beanMethodInvocationFailed",
            "Unable to invoke method {1} for bean class {0}."
        },
        {
            "unexpectedHoldState",
            "Expected sub-job description holdState value {0}, but found {1}."
        },
        {
            "credentialRetrievalFailed",
            "Unable to retrieve delegated credential from {0}."
        },
        {
            "subJobsCreationFailed",
            "Unable to create sub-jobs."
        },
        {
            "subJobSubscribeFailed",
            "Unable to subscribe to state notifications for "
            + "sub-job with resource ID {0}."
        },
        {
            "subJobUnsubscribeFailed",
            "Unable to unsubscribe to state notifications for "
            + "sub-job with resource ID {0}."
        },
        {
            "subJobDestroyFailed",
            "Unable to destroy sub-job with resource ID {0}."
        },
        {
            "endpointCreationFromHandleFailed",
            "Unable to create endpoint from handle {0}."
        },
        {
            "unauthorizedLocalUserId",
            "Requested local user ID {0} is not authorized."
        },
        {
            "badHoldRequest",
            "State {0} unavailable for hold."
        },
        {
            "improperFileUrlUsage",
            "Use of file:// not permitted in the {1} element of {0}."
        },
        {
            MISSING_STAGING_CREDENTIAL,
            "A stagingCredentialEndpoint element was not specified in the RSL, but is needed for staging."
        },
        {
            INVALID_OUTPUT_URL,
            "Invalid URL constructed from {0} path: {1}"
        },
        {
            MISCONFIGURED_SUDO,
            "Sudo is misconfigured to run the {0} script for user {1}."
        },
        {
            VARIABLE_RESOLUTION_ERROR,
            "An unexpected error happened while trying to resolve RSL substitution variables."
        },
        {
            NULL_VARIABLE_VALUE,
            "No value found for RSL substitution variable {0}."
        },
        {
            BAD_RUN_QUEUE_THREAD_COUNT,
            "An invalid value was set for home configuration parameter runQueueThreadCount."
        },
        {
            REPORT_RUN_QUEUE_THREAD_COUNT,
            "Starting state machine with {0} run queues."
        },
        {
            CONFIGURATION_PARAMETER_CHANGED_FROM_DEFAULT,
            "The configuration parameter \"{0}\" is not supported, but was changed from it's default value."
        },
        {
            USER_CANCEL,
            "The job was canceled by the user."
        },
        {
            NO_FAULT_ON_FAILURE,
            "No fault was set for Failed job {0}."
        },
        {
            VARIABLE_DEPTH_EXCEEDED,
            "Maximum nested RSL variable depth limit exceeded while processing variable \"{0}\" with value \"{1}\"."
        },
        {
            UNALLOWED_FILE_URL_HOST,
            "Host not allowed in file URL \"{0}\", but found \"{1}\"."
        },
        {
            MISSING_URL_HOST,
            "Host expected in URL \"{0}\", but found none"
        },
        {
            MISSING_RFT_STATUS,
            "RFT status notification is missing a status object."
        },
        {
            ABNORMAL_PROCESS_TERMINATION,
            "The job process terminated abnormally."
        }
    };
}
