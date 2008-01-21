package fr.in2p3.jsaga.adaptor.language;

import fr.in2p3.jsaga.adaptor.language.abstracts.AbstractLanguageAdaptorProperties;
import org.ogf.saga.job.JobDescription;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SAGALanguageAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SAGALanguageAdaptor extends AbstractLanguageAdaptorProperties implements LanguageAdaptor {
    private static final String[] REQUIRED_PROPERTY_NAMES = new String[] {
            JobDescription.EXECUTABLE};
    private static final String[] OPTIONAL_PROPERTY_NAMES = new String[] {
            JobDescription.WORKINGDIRECTORY,
            JobDescription.INTERACTIVE,
            JobDescription.INPUT,
            JobDescription.OUTPUT,
            JobDescription.ERROR,
            "JobName",
            JobDescription.CLEANUP,
            JobDescription.JOBSTARTTIME,
            "Deadline",
            JobDescription.QUEUE,
            JobDescription.NUMBEROFPROCESSES,
            JobDescription.PROCESSESPERHOST,
            JobDescription.THREADSPERPROCESS,
            JobDescription.SPMDVARIATION};
    private static final String[] REQUIRED_VECTOR_PROPERTY_NAMES = new String[] {};
    private static final String[] OPTIONAL_VECTOR_PROPERTY_NAMES = new String[] {
            JobDescription.ARGUMENTS,
            JobDescription.ENVIRONMENT,
            JobDescription.JOBCONTACT,
            JobDescription.FILETRANSFER,
            JobDescription.CPUARCHITECTURE,
            JobDescription.OPERATINGSYSTEMTYPE,
            JobDescription.CANDIDATEHOSTS};

    public String getName() {
        return "SAGA";
    }

    public void initParser() throws Exception {
        super._initParser(REQUIRED_PROPERTY_NAMES, OPTIONAL_PROPERTY_NAMES,
                REQUIRED_VECTOR_PROPERTY_NAMES, OPTIONAL_VECTOR_PROPERTY_NAMES, ",");
    }

    public String getTranslator() {
        return "xsl/language/saga.xsl";
    }
}
