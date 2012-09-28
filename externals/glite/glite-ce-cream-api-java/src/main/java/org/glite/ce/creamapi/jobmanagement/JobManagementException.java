package org.glite.ce.creamapi.jobmanagement;

public class JobManagementException extends Exception {

    public JobManagementException() {
    }

    public JobManagementException(String message) {
        super(message);
    }

    public JobManagementException(Throwable cause) {
        super(cause);
    }

    public JobManagementException(String message, Throwable cause) {
        super(message, cause);
    }

}
