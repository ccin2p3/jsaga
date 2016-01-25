package org.ogf.saga.resource;

import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractJobTest
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr
* Date:   25 JAN 2016
* ***************************************************
* Description:                                      */

public abstract class ResourceBaseTest extends JSAGABaseTest {
	
    // configuration
    protected URL m_resourcemanager;
    protected Session m_session;

    protected ResourceBaseTest(String jobprotocol) throws Exception {
        super();

        // configure
        m_resourcemanager = URLFactory.createURL(getRequiredProperty(jobprotocol, CONFIG_RM_URL));
        m_session = SessionFactory.createSession(true);
        
    }
    
    /**
     * Creates a new job
     * @param desc The job description
     * @return The new job
     * @throws Exception
     */
//    protected Job createJob(JobDescription desc) throws Exception  {
//        ResourceManager service = ResourceFactory.createResourceManager(m_session, m_resourcemanager);
//        return job;
//    }
    
    /**
     * Creates a new job description
     * @param executable A string with the executable path
     * @param attributes A string array with the job attributes
     * @return The job description
     * @throws Exception
     */
//    protected JobDescription createJob(String executable, Attribute[] attributes, AttributeVector[] attributesVector) throws Exception {
//    	// prepare
//        JobDescription desc = JobFactory.createJobDescription();
//        desc.setAttribute(JobDescription.EXECUTABLE, executable);
//        desc.setAttribute(JobDescription.OUTPUT, "stdout.txt");
//        desc.setAttribute(JobDescription.ERROR, "stderr.txt");
//        if (m_candidateHost != null) {
//            desc.setVectorAttribute(JobDescription.CANDIDATEHOSTS, new String[]{m_candidateHost});
//        }
//        if(attributes != null) {
//        	for (int i = 0; i < attributes.length; i++) {
//        		desc.setAttribute(attributes[i].getKey(), attributes[i].getValue());
//			}
//        }
//        if(attributesVector != null) {
//        	for (int i = 0; i < attributesVector.length; i++) {
//        		desc.setVectorAttribute(attributesVector[i].getKey(), attributesVector[i].getValue());
//			}
//        }
//        return desc;
//    }

//    /**
//     *  Very simple job which prints the execution date
//     * @return The job description
//     * @throws Exception
//     */
//    protected JobDescription createSimpleJob() throws Exception {
//    	return createJob(SIMPLE_JOB_BINARY, null, null);
//    }
//    
//    
//    /**
//     * Job which write 'Test' on stdout
//     * @param textToPrint The string to print in stdout
//     * @return The job description
//     * @throws Exception
//     */
//    protected JobDescription createWriteJob(String textToPrint) throws Exception {
//    	AttributeVector[] attributesV = new AttributeVector[1];
//    	attributesV[0] = new AttributeVector(JobDescription.ARGUMENTS,new String[]{textToPrint});    	
//    	return createJob("/bin/echo", null, attributesV);
//    }
//    
//    /**
//     * Job which generate error like 'Command not found' on stderr
//     * @return The job description
//     * @throws Exception
//     */
//    protected JobDescription createErrorJob() throws Exception {
//    	return createJob("/bin/command-error", null, null);
//    }
//    
//    /**
//     * Long job which sleeps 30 seconds
//     * @return The job description
//     * @throws Exception
//     */
//    protected JobDescription createLongJob() throws Exception {
//    	AttributeVector[] attributesV = new AttributeVector[1];
//    	attributesV[0] = new AttributeVector(JobDescription.ARGUMENTS, new String[]{LONG_JOB_DURATION});
//    	return createJob(LONG_JOB_BINARY, null, attributesV);
//    }


}
