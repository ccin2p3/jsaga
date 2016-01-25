package fr.in2p3.jsaga.adaptor.openstack.util;

public final class OpenstackConstants {
	public final static String DIRAC_GET_PARAM_GROUP = "group";
	public final static String DIRAC_GET_PARAM_SETUP = "setup";
	public final static String DIRAC_GET_PARAM_GRANT_TYPE = "grant_type";
	public final static String DIRAC_GET_PARAM_ACCESS_TOKEN = "access_token";
	public final static String DIRAC_GET_RETURN_TOKEN = "token";
	public final static String DIRAC_GET_RETURN_JID = "jid";
	public final static String DIRAC_GET_RETURN_JIDS = "jids";
	public final static String DIRAC_GET_RETURN_STATUS = "status";
	public final static String DIRAC_GET_RETURN_MINOR_STATUS = "minorStatus";
	public final static String DIRAC_GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
	
	public final static String DIRAC_MANIFEST_OUTPUTSANDBOX = "OutputSandbox";
	public final static String DIRAC_MANIFEST_STDOUTPUT = "StdOutput";
	public final static String DIRAC_MANIFEST_STDERROR = "StdError";
	public final static String DIRAC_MANIFEST_JOBNAME = "JobName";
	public final static String DIRAC_MANIFEST_SITE = "Site";

	public final static String DIRAC_PATH_AUTH = "/oauth2";
	public final static String DIRAC_PATH_TOKEN = DIRAC_PATH_AUTH + "/token";
	public final static String DIRAC_PATH_GROUPS = DIRAC_PATH_AUTH + "/groups";
	public final static String DIRAC_PATH_SETUPS = DIRAC_PATH_AUTH + "/setups";

	public final static String DIRAC_PATH_JOBS = "/jobs";
}
