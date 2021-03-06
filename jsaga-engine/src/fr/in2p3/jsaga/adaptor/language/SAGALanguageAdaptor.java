package fr.in2p3.jsaga.adaptor.language;

import fr.in2p3.jsaga.adaptor.language.abstracts.AbstractLanguageAdaptorProperties;
import fr.in2p3.jsaga.impl.attributes.Attribute;
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
    public static final String[] OPTIONAL_PROPERTY_NAMES = new String[] {
            JobDescription.SPMDVARIATION,
            JobDescription.TOTALCPUCOUNT,
            JobDescription.NUMBEROFPROCESSES,
            JobDescription.PROCESSESPERHOST,
            JobDescription.THREADSPERPROCESS,
            JobDescription.WORKINGDIRECTORY,
            JobDescription.INTERACTIVE,
            JobDescription.INPUT,
            JobDescription.OUTPUT,
            JobDescription.ERROR,
            JobDescription.CLEANUP,
            JobDescription.JOBSTARTTIME,
            JobDescription.WALLTIMELIMIT,
            JobDescription.TOTALCPUTIME,
            JobDescription.TOTALPHYSICALMEMORY,
            JobDescription.CPUARCHITECTURE,     // deviation from SAGA specification (for consistency with JSDL)
            JobDescription.OPERATINGSYSTEMTYPE, // deviation from SAGA specification (for consistency with JSDL)
            JobDescription.JOBPROJECT,
            JobDescription.QUEUE};
    private static final String[] REQUIRED_VECTOR_PROPERTY_NAMES = new String[] {};
    public static final String[] OPTIONAL_VECTOR_PROPERTY_NAMES = new String[] {
            JobDescription.ARGUMENTS,
            JobDescription.ENVIRONMENT,
            JobDescription.FILETRANSFER,
            JobDescription.CANDIDATEHOSTS,
            JobDescription.JOBCONTACT};

    public String getName() {
        return "SAGA";
    }

    public void initParser() {
        super._initParser(
                REQUIRED_PROPERTY_NAMES, OPTIONAL_PROPERTY_NAMES,
                REQUIRED_VECTOR_PROPERTY_NAMES, OPTIONAL_VECTOR_PROPERTY_NAMES,
                Attribute.SEPARATOR);
    }

    public String getTranslator() {
        return "xsl/language/saga.xsl";
    }
}
