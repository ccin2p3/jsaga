package org.ogf.saga.job;

import org.ogf.saga.job.abstracts.AbstractJobTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobDescriptionTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   1 fev. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobDescriptionTest extends AbstractJobTest {
    private JobService m_jobTranslator;
    private JobDescription m_jobDescription;

    public JobDescriptionTest(String jobprotocol) throws Exception {
        super(jobprotocol);
        m_jobTranslator = JobFactory.createJobService(m_session, m_jobservice);
    }

    ////////////////////////////////////// test supported attributes //////////////////////////////////////

    public void test_executable() throws Exception {
        this.change(JobDescription.EXECUTABLE, "/bin/executable2");
    }
    public void test_arguments() throws Exception {
        this.change(JobDescription.ARGUMENTS, new String[]{"arg", "arg2"});
    }
    public void test_spmdVariation() throws Exception {
        this.change(JobDescription.SPMDVARIATION, "MPI");
    }
    public void test_totalCPUCount() throws Exception {
        this.change(JobDescription.TOTALCPUCOUNT, "2");
    }
    public void test_numberOfProcesses() throws Exception {
        this.change(JobDescription.NUMBEROFPROCESSES, "4");
    }
    public void test_processesPerHost() throws Exception {
        this.change(JobDescription.PROCESSESPERHOST, "2");
    }
    public void test_threadsPerProcess() throws Exception {
        this.change(JobDescription.THREADSPERPROCESS, "2");
    }
    public void test_environment() throws Exception {
        this.change(JobDescription.ENVIRONMENT, new String[]{"var=value", "var2=value2"});
    }
    public void test_workingDirectory() throws Exception {
        this.change(JobDescription.WORKINGDIRECTORY, "/tmp/workdir2");
    }
    public void test_input() throws Exception {
        this.change(JobDescription.INPUT, "stdin2.txt");
    }
    public void test_output() throws Exception {
        this.change(JobDescription.OUTPUT, "stdout2.txt");
    }
    public void test_error() throws Exception {
        this.change(JobDescription.ERROR, "stderr2.txt");
    }
    public void test_fileTransfer() throws Exception {
        this.change(JobDescription.FILETRANSFER, new String[]{"myfile>myf", "file2<f2"});
    }
    public void test_cleanup() throws Exception {
    	this.change(JobDescription.CLEANUP, JobDescription.TRUE);
    }
    public void test_totalCPUTime() throws Exception {
        this.change(JobDescription.TOTALCPUTIME, "120");
    }
    public void test_totalPhysicalMemory() throws Exception {
        this.change(JobDescription.TOTALPHYSICALMEMORY, "2048");
    }
    public void test_cpuArchitecture() throws Exception {
        this.change(JobDescription.CPUARCHITECTURE, new String[]{"x86", "x86_64"});
    }
    public void test_operatingSystemType() throws Exception {
        this.change(JobDescription.OPERATINGSYSTEMTYPE, new String[]{"LINUX", "WIN98"});
    }
    public void test_candidateHosts() throws Exception {
        this.change(JobDescription.CANDIDATEHOSTS, new String[]{"myhost", "host2"});
    }
    public void test_queue() throws Exception {
        this.change(JobDescription.QUEUE, "queue2");
    }

    ////////////////////////////////////// common methods //////////////////////////////////////

    protected void setUp() throws Exception {
        super.setUp();
        m_jobDescription = JobFactory.createJobDescription();
        // attributes to be supported by the adaptor
        m_jobDescription.setAttribute(JobDescription.EXECUTABLE, "/bin/executable1");
        m_jobDescription.setVectorAttribute(JobDescription.ARGUMENTS, new String[]{"arg", "arg1"});
        m_jobDescription.setAttribute(JobDescription.SPMDVARIATION, "None");
        m_jobDescription.setAttribute(JobDescription.TOTALCPUCOUNT, "1");
        m_jobDescription.setAttribute(JobDescription.NUMBEROFPROCESSES, "2");
        m_jobDescription.setAttribute(JobDescription.PROCESSESPERHOST, "1");
        m_jobDescription.setAttribute(JobDescription.THREADSPERPROCESS, "1");
        m_jobDescription.setVectorAttribute(JobDescription.ENVIRONMENT, new String[]{"var=value", "var1=value1"});
        m_jobDescription.setAttribute(JobDescription.WORKINGDIRECTORY, "/tmp/workdir1");
        m_jobDescription.setAttribute(JobDescription.INTERACTIVE, JobDescription.FALSE); // not yet supported by JSAGA
        m_jobDescription.setAttribute(JobDescription.INPUT, "stdin1.txt");
        m_jobDescription.setAttribute(JobDescription.OUTPUT, "stdout1.txt");
        m_jobDescription.setAttribute(JobDescription.ERROR, "stderr1.txt");
        m_jobDescription.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{"myfile>myf", "file1<f1"});
        m_jobDescription.setAttribute(JobDescription.CLEANUP, JobDescription.FALSE);
        // JobDescription.JOBSTARTTIME is not supported by JSAGA
        m_jobDescription.setAttribute(JobDescription.TOTALCPUTIME, "60");
        m_jobDescription.setAttribute(JobDescription.TOTALPHYSICALMEMORY, "1024");
        m_jobDescription.setVectorAttribute(JobDescription.CPUARCHITECTURE, new String[]{"x86", "x86_32"});
        m_jobDescription.setVectorAttribute(JobDescription.OPERATINGSYSTEMTYPE, new String[]{"LINUX", "WIN95"});
        m_jobDescription.setVectorAttribute(JobDescription.CANDIDATEHOSTS, new String[]{"myhost", "host1"});
        m_jobDescription.setAttribute(JobDescription.QUEUE, "queue1");
        // JobDescription.JOBCONTACT is not supported by JSAGA
    }

    protected void tearDown() throws Exception {
        m_jobDescription = null;
        super.tearDown();
    }

    private void change(String attributeName, String attributeValue) throws Exception {
        String unexpected = m_jobTranslator.createJob(m_jobDescription).getAttribute("NativeJobDescription");
        m_jobDescription.setAttribute(attributeName, attributeValue);
        String translated = m_jobTranslator.createJob(m_jobDescription).getAttribute("NativeJobDescription");
        assertFalse(unexpected.equals(translated));
    }

    private void change(String attributeName, String[] attributeValues) throws Exception {
        String unexpected = m_jobTranslator.createJob(m_jobDescription).getAttribute("NativeJobDescription");
        m_jobDescription.setVectorAttribute(attributeName, attributeValues);
        String translated = m_jobTranslator.createJob(m_jobDescription).getAttribute("NativeJobDescription");
        assertFalse(unexpected.equals(translated));
    }
}
