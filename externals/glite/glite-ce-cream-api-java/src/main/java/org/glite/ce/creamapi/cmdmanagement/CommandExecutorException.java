package org.glite.ce.creamapi.cmdmanagement;

public class CommandExecutorException extends Exception {

    public CommandExecutorException() {
        super();
    }

    public CommandExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandExecutorException(Throwable cause) {
        super(cause);
    }

    public CommandExecutorException(String message) {
        super(message);
    }

}
