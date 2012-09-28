package org.glite.ce.creamapi.cmdmanagement;

import java.util.Hashtable;
import java.util.Set;

public class CommandResult {
//    private String cmdId, failureReason, description;
//
//    private boolean successfull;

    private Hashtable parameter = null;

//    public CommandResult() {
//        this(null);
//    }

    public CommandResult() {
        parameter = new Hashtable(0);
    }

//    public CommandResult(String cmdId, String failureReason) {
//        this.cmdId = cmdId;
//        this.failureReason = failureReason;
//        this.successfull = false;
//        parameter = new Hashtable(0);
//    }

//    public String getCommandId() {
//        return cmdId;
//    }
//
//    public void setCommandId(String jobId) {
//        this.cmdId = cmdId;
//    }
//
//    public String getFailureReason() {
//        return failureReason;
//    }
//
//    public void setFailureReason(String failureReason) {
//        this.failureReason = failureReason;
//        successfull = false;
//    }
//
//    public boolean isSuccessfull() {
//        return successfull;
//    }
//
//    public void setSuccessfull(boolean successfull) {
//        this.successfull = successfull;
//    }

    public void addParameter(String key, Object value) {
        if (key != null && value != null) {
            parameter.put(key, value);
        }
    }

    public Object getParameter(String key) {
        if (key == null) {
            return null;
        }
        return parameter.get(key);
    }

    public String getParameterAsString(String key) {
        if (key == null) {
            return null;
        }
        return (String) parameter.get(key);
    }

    public Set getParameterKeySet() {
        return parameter.keySet();
    }

//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }

}
