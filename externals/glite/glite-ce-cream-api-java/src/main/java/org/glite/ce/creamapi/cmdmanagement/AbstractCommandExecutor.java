package org.glite.ce.creamapi.cmdmanagement;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.glite.ce.creamapi.cmdmanagement.queue.CommandQueueInterface;

public abstract class AbstractCommandExecutor implements CommandExecutor {
    private static final Logger logger = Logger.getLogger(AbstractCommandExecutor.class.getName());

    private int parallelismDegree;
    private CommandQueueInterface commandQueue;
    private List<CommandWorker> threadPool = null;
    private String[] commands;
    private String category;
    private String name;
    private Hashtable<String, Object> parameter;
    private CommandManagerInterface commandManager;
    
    protected ThreadGroup poolGroup = new ThreadGroup("Manager Worker Threads");

    protected AbstractCommandExecutor(String name, String category) {
        this(name, category, null);
    }

    protected AbstractCommandExecutor(String name, String category, String[] commands) {
        this.name = name;
        this.category = category;
        this.commands = commands;
        parameter = new Hashtable<String, Object>(0);
    }

    public void addParameter(Parameter parameter) {
        if (parameter != null) {
            addParameter(parameter.getName(), parameter.getValue());
        }
    }

    public void addParameter(String key, Object value) {
        if (key != null && value != null) {
            parameter.put(key, value);
        }
    }

    public void addPolicy(Policy policy) {
        try {
            getPolicyManager().addPolicy(policy);
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }
    }

    public boolean checkCommandSupport(String name) {
        if (name == null) {
            return false;
        }

        if (commands != null) {
            for (int i = 0; i < commands.length; i++) {
                if (commands[i].equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsParameterKey(String key) {
        if (key == null) {
            return false;
        }
        return parameter.containsKey(key);
    }

    public void destroy() {
        logger.info("destroy invoked!");

        getPolicyManager().destroy();

        if (commandQueue != null) {
            commandQueue.close();
            
            do {
                for (int i = 0; i < threadPool.size(); i++) {
                    if (!threadPool.get(i).isProcessing() && !threadPool.get(i).isInterrupted()) {
                        threadPool.get(i).destroy();
                        threadPool.remove(i);
                    }
                }

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                }
            } while (threadPool.size() > 0);

            commandQueue.destroy();
        }

        commandQueue = null;
        commandManager = null;
        logger.info("destroyed!");
    }

    public abstract void execute(Command cmd) throws CommandExecutorException, CommandException, IllegalArgumentException;

    public abstract void execute(Command[] cmd) throws CommandExecutorException, CommandException, IllegalArgumentException;

    public String getCategory() {
        return category;
    }

    public CommandManagerInterface getCommandManager() {
        return commandManager;
    }

    public CommandQueueInterface getCommandQueue() {
        return commandQueue;
    }

    public String[] getCommands() {
        return commands;
    }

    public String getName() {
        return name;
    }

    public int getParallelismDegree() {
        return parallelismDegree;
    }

    public List<Parameter> getParameter() {
        List<Parameter> list = new ArrayList<Parameter>(parameter.size());

        for (String key : parameter.keySet()) {
            list.add(new Parameter(key, parameter.get(key)));
        }

        return list;
    }

    public Set<String> getParameterKeySet() {
        return parameter.keySet();
    }

    public Object getParameterValue(String key) {
        if (key != null) {
            return parameter.get(key);
        }

        return null;
    }

    public String getParameterValueAsString(String key) {
        if (key != null) {
            return (String) parameter.get(key);
        }

        return null;
    }

    public List<Policy> getPolicy() {
        return getPolicyManager().getPolicyList();
    }

    public List<Policy> getPolicyByType(String type) {
        return getPolicyManager().getPolicyByType(type);
    }

    public PolicyManager getPolicyManager() {
        return PolicyManager.getInstance();
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCommandManager(CommandManagerInterface cmdManager) {
        this.commandManager = cmdManager;
    }

    public void setCommandQueue(CommandQueueInterface queue) {
        commandQueue = queue;
        // setCommandWorkerPoolSize(3);
    }

    public void setCommands(String[] cmd) {
        commands = cmd;
    }

    public void initCommandWorkerPool() {
        // Initialize the thread pool
        threadPool = new ArrayList<CommandWorker>(parallelismDegree);
        CommandWorker cw = null;
        for (int i = 0; i < parallelismDegree; i++) {
            cw = new CommandWorker(this, i);
            cw.start();

            threadPool.add(cw);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParallelismDegree(int parallelismDegree) {
        this.parallelismDegree = parallelismDegree;
    }

    public void setParameter(List<Parameter> parameters) {
        if (parameter == null) {
            return;
        }
        parameter.clear();

        for (Parameter parameter : parameters) {
            addParameter(parameter);
        }
    }

    public void setPolicy(List<Policy> policyList) {
        try {
            getPolicyManager().setPolicyList(policyList);
        } catch (Throwable e) {
            logger.error(e.getMessage());
        }
    }
}
