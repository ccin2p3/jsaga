package fr.in2p3.jsaga.adaptor.job.control.description;

import org.ogf.saga.error.NoSuccessException;
import org.w3c.dom.Document;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobDescriptionTranslator
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   23 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 * Although you have the possibility to implement your own job description translator,
 * we highly recommand to re-use existing translator in order to keep your code easy to maintain.
 * @see JobDescriptionTranslatorJSDL
 * @see JobDescriptionTranslatorXSLT
 */
public interface JobDescriptionTranslator {
    /**
     * Identifier for attribute HostName (host name of the scheduler managing the job).
     * Value of this attribute will be set through method setAttribute().
     */
    public static final String HOSTNAME = "HostName";

    /**
     * Pass an adaptor-specific configuration or job service connection attribute
     * (including attribute 'HostName') to the translator.
     * @param key the name of the attribute
     * @param value the value of the attribute
     */
    public void setAttribute(String key, String value) throws NoSuccessException;

    /**
     * Translate the job description from JSDL to targeted native language
     * @param jsdl a JSDL document
     * @param uniqId an identifier unique to this job (not the job identifier, which is not generated yet)
     * @return a job description understood by the targeted scheduler
     * @throws NoSuccessException if can not translate JSDL to targeted native language
     */
    public String translate(Document jsdl, String uniqId) throws NoSuccessException;
}
