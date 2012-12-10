package org.glite.ce.creamapi.cmdmanagement;

public abstract class PolicyTask {
    private CommandExecutor executor;
    private String name;

    public PolicyTask(String name) {
        this(name, null);
    }

    public PolicyTask(String name, CommandExecutor executor) {
        this.name = name;
        this.executor = executor;
    }

    public abstract void execute(Policy policy) throws PolicyException;

    public CommandExecutor getCommandExecutor() {
        return executor;
    }

    public String getName() {
        return name;
    }

    public void setCommandExecutor(CommandExecutor executor) {
        this.executor = executor;
    }
}
