package org.glite.ce.creamapi.cmdmanagement;

import java.util.List;

import org.glite.ce.creamapi.cmdmanagement.queue.CommandQueueInterface;


public interface CommandExecutor {
    public void addParameter(Parameter parameter);
    public void addParameter(String key, Object value);    
    public void addPolicy(Policy policy);    
    public boolean checkCommandSupport(String name);
    public boolean containsParameterKey(String key);
    public void destroy();
    public void execute(Command cmd) throws CommandExecutorException, CommandException, IllegalArgumentException;
    public void execute(Command[] cmd) throws CommandExecutorException, CommandException, IllegalArgumentException;
    public String getCategory();
    public CommandManagerInterface getCommandManager();  
    public CommandQueueInterface getCommandQueue();
    public String[] getCommands();       
    public String getName();
    public int getParallelismDegree();
    public List<Parameter> getParameter();
    public Object getParameterValue(String key);  
    public String getParameterValueAsString(String key);  
    public List<Policy> getPolicy();      
    public PolicyManager getPolicyManager();  
    public void initCommandWorkerPool();
    public void initExecutor() throws CommandException;
    public void setCategory(String name);
    public void setCommandManager(CommandManagerInterface cmdManager);
    public void setCommandQueue(CommandQueueInterface queue);
    public void setCommands(String[] cmd);
    public void setName(String name);  
    public void setParallelismDegree(int parallelismDegree);
    public void setParameter(List<Parameter> parameters);
    public void setPolicy(List<Policy> policy);
}
