package org.glite.ce.creamapi.cmdmanagement;

import java.util.Calendar;
import java.util.List;

public interface CommandManagerInterface {
    public static final String COMMAND_DATABASE_NAME = "creamdb";
    public static final String COMMAND_DATASOURCE_NAME = "java:comp/env/jdbc/creamdb";
    
    public void addCommandExecutor(CommandExecutor cmdExec) throws CommandManagementException, IllegalArgumentException;

    public void addCommandExecutor(List<CommandExecutor> cmdExecList) throws CommandManagementException, IllegalArgumentException;

    public void deleteCommand(String cmdId) throws CommandException, IllegalArgumentException;

    public void destroy();

    public Command getCommand(String cmdId) throws CommandException, IllegalArgumentException;

    public long getCurrentThroughput();
    
    public Calendar getLastThroughputUpdate();

    public long getMaxThroughput();

    public void insertCommand(Command cmd) throws CommandException, IllegalArgumentException;

    public void insertCommands(List<Command> cmdList) throws CommandException, IllegalArgumentException;

    public void removeCommandExecutor(CommandExecutor cmdExec) throws CommandManagementException, IllegalArgumentException;
    
    public void removeCommandExecutor(List<CommandExecutor> cmdExecList) throws CommandManagementException, IllegalArgumentException;

    public void removeCommandExecutor(String name, String category) throws CommandManagementException, IllegalArgumentException;

    public void updateCommand(Command cmd) throws CommandException, IllegalArgumentException;

    public void updateCommand(List<Command> cmdList) throws CommandException, IllegalArgumentException;
}
