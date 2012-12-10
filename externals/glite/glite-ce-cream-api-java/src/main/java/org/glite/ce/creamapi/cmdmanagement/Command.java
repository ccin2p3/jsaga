package org.glite.ce.creamapi.cmdmanagement;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class Command {
    public enum ExecutionModeValues { SERIAL(EXECUTION_MODE_SERIAL), PARALLEL(EXECUTION_MODE_PARALLEL);
        private final String stringValue;
        
        ExecutionModeValues(String stringValue) {
            this.stringValue = stringValue;
        }
        
        public String getStringValue(){
            return stringValue;
        }
    }

    private static final long serialVersionUID = 1L;
    
    public static final int LOW_PRIORITY = 0;
    public static final int NORMAL_PRIORITY = 1;
    public static final int MEDIUM_PRIORITY = 2;    
    public static final int HIGH_PRIORITY = 3;

    public static final int CREATED = 0;
    public static final int QUEUED = 1;
    public static final int SCHEDULED = 2;
    public static final int RESCHEDULED = 3;
    public static final int PROCESSING = 4;
    public static final int REMOVED = 5;
    public static final int SUCCESSFULL = 6;
    public static final int ERROR = 7;

    public static final String[] statusName = new String[] { "CREATED", "QUEUED", "SCHEDULED", "RESCHEDULED", "PROCESSING", "REMOVED", "SUCCESSFULL", "ERROR" };
    
    public static final String EXECUTION_MODE_SERIAL   = "S";    
    public static final String EXECUTION_MODE_PARALLEL = "P";

    public static String getStatusName(int type) {
        if (type < 0 || type >= statusName.length) {
            return null;
        }
        return statusName[type];
    }
    
    private String cmdExecutorName, userId;
    private String id = null, name, category, failureReason, description;
    private Calendar creationTime, startSchedulingTime, startProcessingTime, executionCompletedTime;
    private Hashtable<String, Object> parameter = null;
    private boolean lock = false;
    private boolean asynchronous = true;
    private int status;
    private CommandResult result;
    private int priorityLevel = NORMAL_PRIORITY;
    private String commandGroupId = null;

   private ExecutionModeValues executionMode = ExecutionModeValues.SERIAL;

    public Command() {
        this(null, null);
    }

    public Command(String name) {
        this(name, null);
    }

    public Command(String name, String category) {
        super();
        setName(name);
        parameter = new Hashtable<String, Object>(0);
        setStatus(CREATED);
        setCreationTime(Calendar.getInstance());
        this.category = category;
    }

    public String toString() {
        StringBuffer info = new StringBuffer("cmdName=");
        info.append(name);
        
        if(category != null) {
            info.append("; category=").append(category);
        }
        
        if(cmdExecutorName != null) {
            info.append("; cmdExecutorName=").append(cmdExecutorName);
        }
        
        if(userId != null) {
            info.append("; userId=").append(userId);
        }
        
        info.append("; status=").append(statusName[status]); 
        
        if(failureReason != null) {
            info.append("; failureReason=").append(failureReason);             
        }

        if(description != null) {
            info.append("; description=").append(description);             
        }

        info.append(";");
                
        return info.toString();
    }
    
    public void addParameter(String key, List<String> value) {
        if (key != null && value != null) {
            if(parameter.containsKey(key)) {
                parameter.remove(key);
            }
            parameter.put(key.toUpperCase(), value);
        }
    }

    public void addParameter(String key, String value) {
        if (key != null && value != null) {
            if(parameter.containsKey(key)) {
                parameter.remove(key);
            }
            parameter.put(key.toUpperCase(), value);
        }
    }

    public boolean checkResult() {
        return result != null;
    }

    public void clearParameter() {
        parameter.clear();
    }
    
    public boolean containsParameterKey(String key) {
        return parameter.containsKey(key.toUpperCase());
    }

    public void deleteParameter(String key) {
        if (key != null && parameter.containsKey(key.toUpperCase())) {
            parameter.remove(key.toUpperCase());
        }
    }

    public String getCategory() {
        return category;
    }

    public String getCommandExecutorName() {
        return cmdExecutorName;
    }

    public String getCommandGroupId() {
		return commandGroupId;
	}
	
    public Calendar getCreationTime() {
        return creationTime;
    }

    public String getDescription() {
        return description;
    }

    public Calendar getExecutionCompletedTime() {
        return executionCompletedTime;
    }
     
    public ExecutionModeValues getExecutionMode() {
		return executionMode;
	}

    public String getFailureReason() {
        return failureReason;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Object getParameter(String key) {
        if (key == null) {
            return null;
        }
        return parameter.get(key.toUpperCase());
    }

    public String getParameterAsString(String key) {
        if (key == null) {
            return null;
        }
        return (String)parameter.get(key.toUpperCase());
    }

    public Set<String> getParameterKeySet() {
        return parameter.keySet();
    }

    public List<String> getParameterMultivalue(String key) {
        if (key == null) {
            return null;
        }
        
        Object obj = parameter.get(key.toUpperCase());
        if(obj instanceof String) {
            ArrayList<String> list = new ArrayList<String>(0);
            list.add((String)obj);
            return list;
        }
        return (List<String>)obj;
    }
     
    public int getPriorityLevel() {
		return priorityLevel;
	}

    public CommandResult getResult() {
        if (result == null) {
            result = new CommandResult();
        }

        return result;
    }

    public Calendar getStartProcessingTime() {
        return startProcessingTime;
    }

    public Calendar getStartSchedulingTime() {
        return startSchedulingTime;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusName() {
        return statusName[status];
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAsynchronous() {
        return asynchronous;
    }

    public boolean isLocked() {
        return lock;
    }

    public boolean isSuccessfull() {
        return status == SUCCESSFULL;
    }

    private Calendar readCalendar(ObjectInput in) throws IOException {
        long ts = in.readLong();
        if (ts == 0)
            return null;
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(ts);
        return result;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setId(in.readUTF());
        setCategory(in.readUTF());
        cmdExecutorName = in.readUTF();
        userId = in.readUTF();
        setName(in.readUTF());
        setStatus(in.readInt());
        setPriorityLevel(in.readInt());
        String executionModeRead = in.readUTF();
        if ("S".equals(executionModeRead)){
        	this.setExecutionMode(Command.ExecutionModeValues.SERIAL);
        } else if ("P".equals(executionModeRead)){
        	this.setExecutionMode(Command.ExecutionModeValues.PARALLEL);
        } else {
        	throw new IOException("ExecutionMode parameter is not correct: " + executionModeRead);
        }
        setCommandGroupId(in.readUTF());
        setFailureReason(in.readUTF());
        setCreationTime(readCalendar(in));
        setStartSchedulingTime(readCalendar(in));
        setStartProcessingTime(readCalendar(in));
        setExecutionCompletedTime(readCalendar(in));
        lock = in.readBoolean();
        asynchronous = in.readBoolean();

        int htSize = in.readInt();
        parameter.clear();
        if (htSize >= 0) {
            for (int k = 0; k < htSize; k++) {
                parameter.put(in.readUTF(), in.readObject());
            }
        }
    }

    public synchronized void setAsynchronous(boolean b) {
        asynchronous = b;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCommandExecutorName(String name) {
        cmdExecutorName = name;
    }
     
    public void setCommandGroupId(String commandGroupId) {
		this.commandGroupId = commandGroupId;
	}

    public void setCreationTime(Calendar creationTime) {
        this.creationTime = creationTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExecutionCompletedTime(Calendar executionCompletedTime) {
        this.executionCompletedTime = executionCompletedTime;
    }

    public void setExecutionMode(ExecutionModeValues executionMode) {
		this.executionMode = executionMode;
	}
	
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setId(String id) {
        this.id = id;
    }

    public synchronized void setLock(boolean b) {
        lock = b;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setPriorityLevel(int priorityLevel) {
		this.priorityLevel = priorityLevel;
	}

    public void setResult(CommandResult result) {
        this.result = result;
    }

    public void setStartProcessingTime(Calendar startProcessingTime) {
        this.startProcessingTime = startProcessingTime;
    }

    public void setStartSchedulingTime(Calendar startSchedulingTime) {
        this.startSchedulingTime = startSchedulingTime;
    }

    public void setStatus(int status) {
        if (status < 0 || status >= statusName.length) {
            return;
        }
        this.status = status;

        switch (status) {
        case SCHEDULED:
            startSchedulingTime = Calendar.getInstance();
            startProcessingTime = null;
            executionCompletedTime = null;
            break;
        case PROCESSING:
            startProcessingTime = Calendar.getInstance();
            executionCompletedTime = null;
            break;
        case ERROR:
        case REMOVED:
        case SUCCESSFULL:
            executionCompletedTime = Calendar.getInstance();
        }
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private void writeCalendar(ObjectOutput out, Calendar cal) throws IOException {
        out.writeLong(cal != null ? cal.getTimeInMillis() : 0);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        writeString(out, getId());
        writeString(out, getCategory());
        writeString(out, cmdExecutorName);
        writeString(out, userId);
        writeString(out, getName());
        out.writeInt(status);
        out.writeInt(getPriorityLevel());
        writeString(out, getExecutionMode().getStringValue());
        writeString(out, getCommandGroupId());
        writeString(out, getFailureReason());
        writeCalendar(out, getCreationTime());
        writeCalendar(out, getStartSchedulingTime());
        writeCalendar(out, getStartProcessingTime());
        writeCalendar(out, getExecutionCompletedTime());
        out.writeBoolean(lock);
        out.writeBoolean(asynchronous);

        if (parameter != null) {
            out.writeInt(parameter.size());
            for(String key : parameter.keySet()) {
                out.writeUTF(key);
                out.writeObject(parameter.get(key));
            }
        } else {
            out.writeInt(-1);
        }
    }

    private void writeString(ObjectOutput out, String s) throws IOException {
        out.writeUTF(s != null ? s : "");
    }
}
