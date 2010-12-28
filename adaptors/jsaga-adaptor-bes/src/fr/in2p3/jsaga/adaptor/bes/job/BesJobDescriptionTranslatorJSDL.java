package fr.in2p3.jsaga.adaptor.bes.job;

import org.ogf.saga.error.NoSuccessException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorJSDL;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   BesJobDescriptionTranslatorJSDL
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   28 déc 2010
 * ***************************************************
 * Description:                                      */
/**
 * Use this translator if language supported by targeted scheduler is JSDL
 */
public class BesJobDescriptionTranslatorJSDL extends JobDescriptionTranslatorJSDL {
    /**
     * Serialize JSDL document to string
     */
    public String translate(Document jsdl, String uniqId) throws NoSuccessException {
    	Node stagingDir = jsdl.createElement("StagingDirectory");
    	//stagingDir.setPrefix("jsaga");
    	// TODO: add protocol
    	stagingDir.setTextContent("file:/tmp/" + uniqId);
    	
    	jsdl.getElementsByTagName("jsdl:JobDescription").item(0).appendChild(stagingDir);

    	String translated = super.translate(jsdl, uniqId);
    	//System.out.println(translated);
        return translated;
    }
}
