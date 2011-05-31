package fr.in2p3.jsaga.adaptor.batchssh.job;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.ogf.saga.error.NoSuccessException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BatchSSHJob
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   10 mai 2011
* ***************************************************
* Description:                                      */

public class BatchSSHJob {

	private String m_id;
	private HashMap<String, String> m_attributes;
	
	public static String ATTR_EXIT_STATUS = "EXIT_STATUS";
	public static String ATTR_JOB_STATE = "JOB_STATE";
	public static String ATTR_CREATE_TIME = "CTIME";
	public static String ATTR_START_TIME = "START_TIME";
	public static String ATTR_END_TIME = "MTIME";
	public static String ATTR_EXEC_HOST = "EXEC_HOST";
	public static String ATTR_OUTPUT = "OUTPUT_PATH";
	public static String ATTR_STAGEOUT = "STAGEOUT";
	public static String ATTR_SERVER = "SERVER";
	public static String ATTR_VARS = "VARIABLE_LIST";
	public static String ATTR_VAR_WORKDIR = "PBS_O_WORKDIR";
	public static String ATTR_VAR_JSAGA_STAGEOUT = "PBS_JSAGASTAGEOUT";
	public static String ATTR_VAR_JSAGA_STAGEIN = "PBS_JSAGASTAGEIN";
	public static String ATTR_VAR_JSAGA_EXECUTABLE = "PBS_JSAGAEXECUTABLE";
	
	public BatchSSHJob(String nativeJobId) {
		this.m_id = nativeJobId;
		this.m_attributes = new HashMap<String, String>();
	}
	
	public String getId() {
		return this.m_id;
	}
	
	public void setAttribute(String key, String value) {
		this.m_attributes.put(key, value);
	}
	
	public String getAttribute(String key) throws NoSuccessException {
		if (this.m_attributes.containsKey(key))
			return this.m_attributes.get(key);
		if (key.startsWith("PBS_")) {
			return this.getVariableValue(key);
		}
		throw new NoSuccessException("Could not get " + key + " attribute");
	}
	
	public Date getDateAttribute(String key) throws NoSuccessException {
		DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);
		try {
			return df.parse(this.m_attributes.get(key));
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}		
	}
	
	public int getExitCode() throws NoSuccessException {
		return new Integer(this.m_attributes.get(ATTR_EXIT_STATUS));
	}
	
	public BatchSSHJobStatus getJobStatus() throws NoSuccessException {
		if (this.m_attributes.containsKey(ATTR_EXIT_STATUS)) {
			return new BatchSSHJobStatus(m_id, this.m_attributes.get(ATTR_JOB_STATE), this.getExitCode());
		} else {
			return new BatchSSHJobStatus(m_id, this.m_attributes.get(ATTR_JOB_STATE));
		}
	}
	
	/**
	 * If not done before, parse Variable_List = PBS_O_HOME=/afs/in2p3.fr/home/s/schwarz,PBS_O_LANG=C,PBS_O_LOGNAME=schwarz,...
	 * and adds VAR=VALUE to m_attributes
	 * 
	 * @param varname
	 * @return the value of the variable varname
	 * @throws NoSuccessException if no "Variable_List" was found
	 */
	public String getVariableValue(String varname) throws NoSuccessException {
		String[] assignations = this.getAttribute(ATTR_VARS).split(",");
		for (int i=0; i<assignations.length; i++) {
			String [] arr = assignations[i].split("=",2);
			this.setAttribute(arr[0].trim().toUpperCase(), arr[1].trim());
		}
		return this.m_attributes.get(varname);
	}
	
	/**
	 * Get the list of INPUT or OUTPUT transfers (String[2] in the form [local,remote])
	 * @param input true for INPUT transfers, false for OUTPUT
	 * @return the list of transfers
	 * @throws NoSuccessException
	 */
	public ArrayList<String[]> getStagingTransfers(boolean input) throws NoSuccessException {
    	ArrayList<String[]> vars = new ArrayList<String[]>();
		String crit;
		if (input) {
	    	crit = ATTR_VAR_JSAGA_STAGEIN;
		} else {
	    	crit = ATTR_VAR_JSAGA_STAGEOUT;
		}
		return BatchSSHJob.getFilteredVars(this.getAttribute(ATTR_VARS), crit);
	}
	
	/**
	 * Get the list of variables matching the filter (String[2])
	 * If the variable contains a '@' (transfers), it is cut in 2 parts
	 * @param var_string the variable to parse in the form VAR1=text,VAR2=value,VAR3=local@remote,...
	 * @param filter the string that a variable name should start with
	 * @return the list of matching variables values
	 */
	public static ArrayList<String[]> getFilteredVars(String var_string, String filter) {
    	ArrayList<String[]> vars = new ArrayList<String[]>();
		String[] assignations = var_string.split(",");
		for (int i=0; i<assignations.length; i++) {
			String [] arr = assignations[i].split("=",2);
			if (arr[0].trim().toUpperCase().startsWith(filter)) {
				vars.add(arr[1].trim().split("@",2));
			}
		}
		return vars;
	}
}
