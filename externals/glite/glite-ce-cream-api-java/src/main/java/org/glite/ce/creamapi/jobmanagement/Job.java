/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 * 
 * Authors: Luigi Zangrando (zangrando@pd.infn.it)
 */

package org.glite.ce.creamapi.jobmanagement;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;


public class Job implements Serializable, Externalizable {
    private static final long serialVersionUID = 1L;

    private static final String JOBTYPE_INTERACTIVE = "interactive";
    private static final String JOBTYPE_MPICH = "mpich";
    private static final String JOBTYPE_NORMAL = "normal";
    public static final String version = "1.1";
    public static final String NOT_AVAILABLE_VALUE = "N/A";
    public static final String MAX_OUTPUT_SANDBOX_SIZE = "MAX_OUTPUT_SANDBOX_SIZE";
    public static final String DELEGATION_PROXY_CERT_SANDBOX_PATH = "DELEGATION_PROXY_CERT_SANDBOX_PATH";
    private static final Random jobIdGenerator = new Random();
    	
    private String creamURL = null;
    private String id = null;
    private String cerequirements = null;
    private String virtualOrganization = null;
    private String userId = null;
    private String batchSystem = null;
    private String queue = null;
    private String standardInput, standardOutput, standardError = null;
    private String executable = null;
    private String delegationProxyCertPath, authNProxyCertPath = null;
    private String hlrLocation = null;
    private String loggerDestURI = null;
    private String tokenURL = null;
    private String perusalFilesDestURI, perusalListFileURI = null;
    private String prologue, prologueArguments = null;
    private String epilogue, epilogueArguments = null;
    private String sequenceCode = null;
    private String lrmsJobId, lrmsAbsLayerJobId, gridJobId, iceId, fatherJobId, ceId = null;
    private String type, creamInputSandboxURI, creamOutputSandboxURI, sandboxBasePath, inputSandboxBaseURI, outputSandboxBaseDestURI = null;
    private String workerNode = null;
    private String jdl = null;
    private String myProxyServer = null;
    private String localUser, delegationProxyId, delegationProxyInfo, workingDirectory = null;
    private String[] childJobId = null;
    private String[] arguments = null;
    private String[] outputSandboxDestURI, inputFiles, outputFiles = null;
    private int perusalTimeInterval, nodes = 0;
    private Hashtable<String, String> extraAttribute = null;
    private Hashtable<String, String> environment = null;
    private Hashtable<String, Object> volatileProperty = null;
    private List<JobStatus> statusArray = null;
    private List<JobCommand> commandArray = null;
    private Lease lease = null;

    public Job() {
        commandArray = new ArrayList<JobCommand>();
        statusArray = new ArrayList<JobStatus>();
        environment = new Hashtable<String, String>(0);
        extraAttribute = new Hashtable<String, String>(0);
        volatileProperty = new Hashtable<String, Object>(0);

        setId(generateJobId());
    }

    public Job(String id) {
        commandArray = new ArrayList<JobCommand>();
        statusArray = new ArrayList<JobStatus>();
        environment = new Hashtable<String, String>(0);
        extraAttribute = new Hashtable<String, String>(0);
        volatileProperty = new Hashtable<String, Object>(0);

        if (id == null) {
            setId(generateJobId());
        } else {
            setId(id);
        }
    }

    public void addCommandHistory(JobCommand cmd) {
        if (cmd != null) {
            cmd.setJobId(id);
            commandArray.add(cmd);
        }
    }

    public void addEnvironmentAttribute(String key, String value) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key not specified!");
        }

        if (value == null) {
            throw new IllegalArgumentException("value not specified!");
        }

        environment.put(key, value);
    }

    public void addExtraAttribute(String key, String value) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key not specified!");
        }

        if (value == null) {
            throw new IllegalArgumentException("value not specified!");
        }

        extraAttribute.put(key, value);
    }

    public void addStatus(JobStatus status) {
        if (status == null) {
            return;
        }

        status.setJobId(id);
        statusArray.add(status);
    }

    public boolean containsVolatilePropertyKeys(String key) {
        if (key != null) {
            return volatileProperty.containsKey(key);
        }

        return false;
    }

    public synchronized String generateJobId() {
    	String suffix = "000000000" + jobIdGenerator.nextInt(1000000000);
    	suffix = suffix.substring(suffix.length()-9);
        return "CREAM" + suffix;
    }

    public String[] getArguments() {
        return arguments;
    }

    public String getAuthNProxyCertPath() {
        return authNProxyCertPath;
    }

    public String getBatchSystem() {
        return batchSystem;
    }

    public String getCeId() {
        return ceId;
    }

    public String getCeRequirements() {
        return cerequirements;
    }

    public String[] getChildJobId() {
        return childJobId;
    }

    public List<JobCommand> getCommandHistory() {
        return commandArray;
    }

    public JobCommand getCommandHistoryAt(int index) throws IndexOutOfBoundsException {
        return commandArray.get(index);
    }

    public int getCommandHistoryCount() {
        return commandArray.size();
    }

    public String getCREAMInputSandboxURI() {
        return creamInputSandboxURI;
    }

    public String getCREAMOutputSandboxURI() {
        return creamOutputSandboxURI;
    }

    public String getCREAMSandboxBasePath() {
        return sandboxBasePath;
    }

    public String getCreamURL() {
        return creamURL;
    }

    public String getDelegationProxyCertPath() {
        return delegationProxyCertPath;
    }

    public String getDelegationProxyId() {
        return delegationProxyId;
    }

    public String getDelegationProxyInfo() {
        return delegationProxyInfo;
    }

    public Hashtable<String, String> getEnvironment() {
        return environment;
    }

    public String getEnvironmentAttribute(String key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key not specified!");
        }

        return environment.get(key);
    }

    public String getEpilogue() {
        return epilogue;
    }

    public String getEpilogueArguments() {
        return epilogueArguments;
    }

    public String getExecutable() {
        return executable;
    }

    public Hashtable<String, String> getExtraAttribute() {
        return extraAttribute;
    }

    public String getExtraAttribute(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key not specified!");
        }

        return extraAttribute.get(key);
    }

    public String getFatherJobId() {
        return fatherJobId;
    }

    public String getGridJobId() {
        return gridJobId;
    }

    public String getHlrLocation() {
        return hlrLocation;
    }

    public String getICEId() {
        return iceId;
    }

    public String getId() {
        return id;
    }

    public String[] getInputFiles() {
        return inputFiles;
    }

    public String getInputSandboxBaseURI() {
        return inputSandboxBaseURI;
    }

    public String getJDL() {
        return jdl;
    }

    public JobCommand getLastCommand() {
        if (commandArray.size() > 0) {
            return commandArray.get(commandArray.size() - 1);
        }

        return null;
    }

    public JobStatus getLastStatus() {
        if(statusArray.size() > 0) {
            return statusArray.get(statusArray.size() - 1);
        }
        
        return null;
    }

    public Lease getLease() {
        return lease;
    }

    public String getLocalUser() {
        return localUser;
    }

    public String getLoggerDestURI() {
        return loggerDestURI;
    }

    public String getLRMSAbsLayerJobId() {
        return lrmsAbsLayerJobId;
    }

    public String getLRMSJobId() {
        return lrmsJobId;
    }

    public String getMyProxyServer() {
        return myProxyServer;
    }

    public int getNodeNumber() {
        return nodes;
    }

    public String[] getOutputFiles() {
        return outputFiles;
    }

    public String getOutputSandboxBaseDestURI() {
        return outputSandboxBaseDestURI;
    }

    public String[] getOutputSandboxDestURI() {
        return outputSandboxDestURI;
    }

    public String getPerusalFilesDestURI() {
        return perusalFilesDestURI;
    }

    public String getPerusalListFileURI() {
        return perusalListFileURI;
    }

    public int getPerusalTimeInterval() {
        return perusalTimeInterval;
    }

    public String getPrologue() {
        return prologue;
    }

    public String getPrologueArguments() {
        return prologueArguments;
    }

    public String getQueue() {
        return queue;
    }

    public String getSandboxBasePath() {
        return sandboxBasePath;
    }

    public String getSequenceCode() {
        return sequenceCode;
    }

    public String getStandardError() {
        return standardError;
    }

    public String getStandardInput() {
        return standardInput;
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    public JobStatus getStatusAt(int index) throws IndexOutOfBoundsException {
        return statusArray.get(index);
    }

    public int getStatusCount() {
        return statusArray.size();
    }

    public List<JobStatus> getStatusHistory() {
        return statusArray;
    }

    public String getTokenURL() {
        return tokenURL;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public String getVirtualOrganization() {
        return virtualOrganization;
    }

    public Enumeration<String> getVolatilePropertyKeys() {
        return volatileProperty.keys();
    }

    public Object getVolatilePropertyValue(String key) {
        if (key != null) {
            return volatileProperty.get(key);
        }

        return null;
    }

    public String getWorkerNode() {
        return workerNode;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public boolean isInteractive() {
        return JOBTYPE_INTERACTIVE.equalsIgnoreCase(getType());
    }

    public boolean isMpich() {
        return JOBTYPE_MPICH.equalsIgnoreCase(getType());
    }

    public boolean isNormal() {
        return JOBTYPE_NORMAL.equalsIgnoreCase(getType());
    }

    public void putVolatileProperty(String key, Object value) throws IllegalArgumentException {
        if (key != null && value != null) {
            volatileProperty.put(key, value);
        } else {
            throw new IllegalArgumentException("Neither the key nor the value can be null. (key=" + key + " value=" + value + ")");
        }
    }

    private Calendar readCalendar(ObjectInput in) throws IOException {
        long ts = in.readLong();
        
        if (ts == 0) {
            return null;
        }
        
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(ts);
        return result;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        String version = readString(in);
        if (!version.equalsIgnoreCase(version)) {
            throw new IOException("job serialization version mismatch: found \"" + version + "\" required \"" + this.version + "\"");
        }

        creamURL = readString(in);
        id = readString(in);
        lrmsJobId = readString(in);
        lrmsAbsLayerJobId = readString(in);
        gridJobId = readString(in);
        iceId = readString(in);
        fatherJobId = readString(in);
        ceId = readString(in);
        userId = readString(in);
        jdl = readString(in);
        myProxyServer = readString(in);
        workerNode = readString(in);
        cerequirements = readString(in);
        virtualOrganization = readString(in);
        batchSystem = readString(in);
        queue = readString(in);
        standardInput = readString(in);
        standardOutput = readString(in);
        standardError = readString(in);
        executable = readString(in);
        delegationProxyCertPath = readString(in);
        authNProxyCertPath = readString(in);
        hlrLocation = readString(in);
        loggerDestURI = readString(in);
        tokenURL = readString(in);
        perusalFilesDestURI = readString(in);
        perusalListFileURI = readString(in);
        prologue = readString(in);
        prologueArguments = readString(in);
        epilogue = readString(in);
        epilogueArguments = readString(in);
        sequenceCode = readString(in);
        type = readString(in);
        creamInputSandboxURI = readString(in);
        creamOutputSandboxURI = readString(in);
        sandboxBasePath = readString(in);
        inputSandboxBaseURI = readString(in);
        outputSandboxBaseDestURI = readString(in);
        localUser = readString(in);
        delegationProxyId = readString(in);
        delegationProxyInfo = readString(in);
        workingDirectory = readString(in);
        arguments = readStringArray(in);
        childJobId = readStringArray(in);
        outputSandboxDestURI = readStringArray(in);
        inputFiles = readStringArray(in);
        outputFiles = readStringArray(in);
        nodes = in.readInt();
        perusalTimeInterval = in.readInt();

        int htSize = in.readInt();

        if (htSize == 1) {
            lease = new Lease();
            lease.setLeaseId(readString(in));
            lease.setUserId(readString(in));
            lease.setLeaseTime(readCalendar(in));
        }

        htSize = in.readInt();
        if (htSize >= 0) {
            environment.clear();

            for (int k = 0; k < htSize; k++) {
                environment.put(in.readUTF(), in.readUTF());
            }
        }

        htSize = in.readInt();
        if (htSize >= 0) {
            extraAttribute.clear();

            for (int k = 0; k < htSize; k++) {
                extraAttribute.put(in.readUTF(), in.readUTF());
            }
        }

        htSize = in.readInt();
        if (htSize >= 0) {
            statusArray.clear();
            
            for (int i = 0; i < htSize; i++) {
                statusArray.add(readJobStatus(in));
            }
        }

        htSize = in.readInt();
        if (htSize >= 0) {
            commandArray.clear();
            
            for (int i = 0; i < htSize; i++) {
                commandArray.add(readJobCommand(in));
            }
        }
    }

    private JobCommand readJobCommand(ObjectInput in) throws IOException {
        if (in == null) {
            throw new IOException("readJobCommand error: ObjectInput is null");
        }

        JobCommand cmd = new JobCommand();
        cmd.setCommandExecutorName(readString(in));
        cmd.setDescription(readString(in));
        cmd.setFailureReason(readString(in));
        cmd.setJobId(readString(in));
        cmd.setUserId(readString(in));
        cmd.setCreationTime(readCalendar(in));
        cmd.setExecutionCompletedTime(readCalendar(in));
        cmd.setStartProcessingTime(readCalendar(in));
        cmd.setStartSchedulingTime(readCalendar(in));
        cmd.setStatus(in.readInt());
        cmd.setType(in.readInt());

        return cmd;
    }

    private JobStatus readJobStatus(ObjectInput in) throws IOException {
        if (in == null) {
            throw new IOException("readJobStatus error: ObjectInput is null");
        }

        JobStatus status = new JobStatus(in.readInt());
        status.setDescription(readString(in));
        status.setExitCode(readString(in));
        status.setFailureReason(readString(in));
        status.setJobId(readString(in));
        status.setTimestamp(readCalendar(in));

        return status;
    }

    private String readString(ObjectInput in) throws IOException {
        if (in == null) {
            throw new IOException("ObjectIntput is null");
        }

        String s = in.readUTF();
        if (s == null || s.equals("")) {
            return null;
        }
        return s;
    }

    private String[] readStringArray(ObjectInput in) throws IOException {
        int size = in.readInt();
        if (size >= 0) {
            String[] result = new String[size];
            for (int k = 0; k < size; k++) {
                result[k] = in.readUTF();
            }
            return result;
        }

        return null;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public void setAuthNProxyCertPath(String authNProxyCertPath) {
        this.authNProxyCertPath = authNProxyCertPath;
    }

    public void setBatchSystem(String batchSystem) {
        this.batchSystem = batchSystem;
    }

    public void setCeId(String ceId) {
        this.ceId = ceId;
    }

    public void setCeRequirements(String cerequirements) {
        this.cerequirements = cerequirements;
    }

    public void setChildJobId(String[] childJobId) {
        this.childJobId = childJobId;
    }

    public void setCommandHistory(List<JobCommand> cmdList) {
        if (cmdList != null) {
            commandArray.clear();
            commandArray.addAll(cmdList);
        }
    }

    public void setCREAMInputSandboxURI(String inputSandboxURI) {
        creamInputSandboxURI = inputSandboxURI;
    }

    public void setCREAMOutputSandboxURI(String outputSandboxURI) {
        creamOutputSandboxURI = outputSandboxURI;
    }

    public void setCREAMSandboxBasePath(String sandboxBasePath) {
        this.sandboxBasePath = sandboxBasePath;
    }

    public void setCreamURL(String creamURL) {
        this.creamURL = creamURL;
    }

    public void setDelegationProxyCertPath(String dlgProxyCertPath) {
        this.delegationProxyCertPath = dlgProxyCertPath;
    }

    public void setDelegationProxyId(String proxyDelegationId) {
        this.delegationProxyId = proxyDelegationId;
    }

    public void setDelegationProxyInfo(String proxyInfo) {
        this.delegationProxyInfo = proxyInfo;
    }

    public void setEnvironment(Hashtable<String, String> env) {
        if(env != null) {
            environment.clear();
            environment.putAll(env);
        }
    }

    public void setEpilogue(String epilogue) {
        this.epilogue = epilogue;
    }

    public void setEpilogueArguments(String args) {
        epilogueArguments = args;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void setExtraAttribute(Hashtable<String, String> extraAttr) {
        if(extraAttr != null) {
            extraAttribute.clear();
            extraAttribute.putAll(extraAttr);
        }
    }

    public void setFatherJobId(String fatherJobId) {
        this.fatherJobId = fatherJobId;
    }

    public void setGridJobId(String gridJobId) {
        this.gridJobId = gridJobId;
    }

    public void setHlrLocation(String hl) {
        hlrLocation = hl;
    }

    public void setICEId(String iceId) {
        this.iceId = iceId;
    }

    public void setId(String id) {
        this.id = id;

        for (JobStatus status : statusArray) {
            status.setJobId(id);
        }

        for (JobCommand cmd : commandArray) {
            cmd.setJobId(id);
        }
    }

    public void setInputFiles(String[] inputFiles) {
        this.inputFiles = inputFiles;
    }

    public void setInputSandboxBaseURI(String uri) {
        this.inputSandboxBaseURI = uri;
    }

    public void setJDL(String jdl) {
        this.jdl = jdl;
    }

    public void setLastCommandHistory(JobCommand cmd) {
        if (cmd != null) {
            commandArray.set(commandArray.size() - 1, cmd);
        }
    }

    public void setLastStatus(JobStatus status) {
        if (status == null) {
            return;
        }

        status.setJobId(getId());
        statusArray.set(statusArray.size() - 1, status);
    }

    public void setLease(Lease lease) {
        if (lease != null) {
            lease.setUserId(userId);

            if (lease.getLeaseId() == null) {
                lease.setLeaseId(id);
            }
        }
        this.lease = lease;
    }

    public void setLocalUser(String localUser) {
        this.localUser = localUser;
    }

    public void setLoggerDestURI(String s) {
        loggerDestURI = s;
    }

    public void setLRMSAbsLayerJobId(String lrmsAbsLayerJobId) {
        this.lrmsAbsLayerJobId = lrmsAbsLayerJobId;
    }

    public void setLRMSJobId(String lrmsJobId) {
        this.lrmsJobId = lrmsJobId;
    }

    public void setMyProxyServer(String myProxyServer) {
        this.myProxyServer = myProxyServer;
    }

    public void setNodeNumber(int n) {
        nodes = n;
    }

    public void setOutputFiles(String[] outputFiles) {
        this.outputFiles = outputFiles;
    }

    public void setOutputSandboxBaseDestURI(String uri) {
        outputSandboxBaseDestURI = uri;
    }

    public void setOutputSandboxDestURI(String[] uri) {
        outputSandboxDestURI = uri;
    }

    public void setPerusalFilesDestURI(String perusalFilesDestURI) {
        this.perusalFilesDestURI = perusalFilesDestURI;
    }

    public void setPerusalListFileURI(String perusalListFileURI) {
        this.perusalListFileURI = perusalListFileURI;
    }

    public void setPerusalTimeInterval(int perusalTimeInterval) {
        this.perusalTimeInterval = perusalTimeInterval;
    }

    public void setPrologue(String prologue) {
        this.prologue = prologue;
    }

    public void setPrologueArguments(String args) {
        prologueArguments = args;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public void setSandboxBasePath(String sandboxBasePath) {
        this.sandboxBasePath = sandboxBasePath;
    }

    public void setSequenceCode(String sequenceCode) {
        this.sequenceCode = sequenceCode;
    }

    public void setStandardError(String standardError) {
        this.standardError = standardError;
    }

    public void setStandardInput(String standardInput) {
        this.standardInput = standardInput;
    }

    public void setStandardOutput(String standardOutput) {
        this.standardOutput = standardOutput;
    }

    public void setStatusHistory(List<JobStatus> status) {
        if (status != null) {
            statusArray.clear();
            statusArray.addAll(status);
        }
    }

    public void setTokenURL(String s) {
        tokenURL = s;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setVirtualOrganization(String vo) {
        virtualOrganization = vo;
    }

    public void setWorkerNode(String wn) {
        workerNode = wn;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    private void writeCalendar(ObjectOutput out, Calendar cal) throws IOException {
        out.writeLong(cal != null? cal.getTimeInMillis() : 0);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        if (out == null) {
            throw new IOException("ObjectOutput is null");
        }
        writeString(out, version);
        writeString(out, creamURL);
        writeString(out, id);
        writeString(out, lrmsJobId);
        writeString(out, lrmsAbsLayerJobId);
        writeString(out, gridJobId);
        writeString(out, iceId);
        writeString(out, fatherJobId);
        writeString(out, ceId);
        writeString(out, userId);
        writeString(out, jdl);
        writeString(out, myProxyServer);
        writeString(out, workerNode);
        writeString(out, cerequirements);
        writeString(out, virtualOrganization);
        writeString(out, batchSystem);
        writeString(out, queue);
        writeString(out, standardInput);
        writeString(out, standardOutput);
        writeString(out, standardError);
        writeString(out, executable);
        writeString(out, delegationProxyCertPath);
        writeString(out, authNProxyCertPath);
        writeString(out, hlrLocation);
        writeString(out, loggerDestURI);
        writeString(out, tokenURL);
        writeString(out, perusalFilesDestURI);
        writeString(out, perusalListFileURI);
        writeString(out, prologue);
        writeString(out, prologueArguments);
        writeString(out, epilogue);
        writeString(out, epilogueArguments);
        writeString(out, sequenceCode);
        writeString(out, type);
        writeString(out, creamInputSandboxURI);
        writeString(out, creamOutputSandboxURI);
        writeString(out, sandboxBasePath);
        writeString(out, inputSandboxBaseURI);
        writeString(out, outputSandboxBaseDestURI);
        writeString(out, localUser);
        writeString(out, delegationProxyId);
        writeString(out, delegationProxyInfo);
        writeString(out, workingDirectory);
        writeStringArray(out, arguments);
        writeStringArray(out, childJobId);
        writeStringArray(out, outputSandboxDestURI);
        writeStringArray(out, inputFiles);
        writeStringArray(out, outputFiles);
        out.writeInt(nodes);
        out.writeInt(perusalTimeInterval);

        if (lease != null) {
            out.writeInt(1);
            writeString(out, lease.getLeaseId());
            writeString(out, lease.getUserId());
            writeCalendar(out, lease.getLeaseTime());
        } else {
            out.writeInt(-1);
        }

        if (environment.size() > 0) {
            out.writeInt(environment.size());

            Enumeration<String> allKeys = environment.keys();
            while (allKeys.hasMoreElements()) {
                String key = allKeys.nextElement();
                writeString(out, key);
                writeString(out, environment.get(key));
            }
        } else {
            out.writeInt(-1);
        }

        if (extraAttribute.size() > 0) {
            out.writeInt(extraAttribute.size());

            Enumeration<String> allKeys = extraAttribute.keys();
            while (allKeys.hasMoreElements()) {
                String key = allKeys.nextElement();
                writeString(out, key);
                writeString(out, extraAttribute.get(key));
            }
        } else {
            out.writeInt(-1);
        }

        if (statusArray != null && statusArray.size() > 0) {
            out.writeInt(statusArray.size());
            for (JobStatus status : statusArray) {
                writeJobStatus(out, status);
            }
        } else {
            out.writeInt(-1);
        }

        if (commandArray != null && commandArray.size() > 0) {
            out.writeInt(commandArray.size());
            for (JobCommand cmd : commandArray) {
                writeJobCommand(out, cmd);
            }
        } else {
            out.writeInt(-1);
        }
    }

    private void writeJobCommand(ObjectOutput out, JobCommand cmd) throws IllegalArgumentException, IOException {
        if (cmd == null) {
            throw new IllegalArgumentException("writeJobCommand error: jobCommand not specified!");
        }

        if (out == null) {
            throw new IOException("writeJobCommand error: ObjectInput is null");
        }

        writeString(out, cmd.getCommandExecutorName());
        writeString(out, cmd.getDescription());
        writeString(out, cmd.getFailureReason());
        writeString(out, cmd.getJobId());
        writeString(out, cmd.getUserId());
        writeCalendar(out, cmd.getCreationTime());
        writeCalendar(out, cmd.getExecutionCompletedTime());
        writeCalendar(out, cmd.getStartProcessingTime());
        writeCalendar(out, cmd.getStartSchedulingTime());
        out.writeInt(cmd.getStatus());
        out.writeInt(cmd.getType());
    }

    private void writeJobStatus(ObjectOutput out, JobStatus jobStatus) throws IllegalArgumentException, IOException {
        if (jobStatus == null) {
            throw new IllegalArgumentException("writeJobStatus error: jobStatus not specified!");
        }

        if (out == null) {
            throw new IOException("writeJobStatus error: ObjectInput is null");
        }

        out.writeInt(jobStatus.getType());
        writeString(out, jobStatus.getDescription());
        writeString(out, jobStatus.getExitCode());
        writeString(out, jobStatus.getFailureReason());
        writeString(out, jobStatus.getJobId());
        writeCalendar(out, jobStatus.getTimestamp());
    }

    private void writeString(ObjectOutput out, String s) throws IOException {
        out.writeUTF(s != null? s : "");
    }

    private void writeStringArray(ObjectOutput out, String[] array) throws IOException {
        if (array != null) {
            out.writeInt(array.length);
            
            for (int k = 0; k < array.length; k++) {
                out.writeUTF(array[k] != null? array[k] : "");
            }
        } else {
            out.writeInt(-1);
        }
    }
}
