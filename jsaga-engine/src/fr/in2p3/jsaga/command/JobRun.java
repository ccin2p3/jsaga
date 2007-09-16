package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.adaptor.language.LanguageAdaptor;
import fr.in2p3.jsaga.engine.adaptor.LanguageAdaptorFactory;
import fr.in2p3.jsaga.engine.job.preprocess.JobPreprocessor;
import org.w3c.dom.Document;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobRun
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   4 avr. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobRun {
    public static void main(String[] args) throws Exception {
        // parse command line
        if (args.length!=2) {
            System.err.println("usage: JobRun <job> <resources>");
            System.exit(1);
        }
        File jobDescFile = new File(args[0]);
        if (!jobDescFile.exists()) {
            throw new FileNotFoundException("File not found: "+jobDescFile.getAbsolutePath());
        }
        File resourcesFile = new File(args[1]);
        if (!resourcesFile.exists()) {
            throw new FileNotFoundException("File not found: "+resourcesFile.getAbsolutePath());
        }

        // run job
        LanguageAdaptor parser = LanguageAdaptorFactory.getLanguageAdaptor("jsdl");
        parser.loadLanguageDefinitionResources();
        Document jobDesc = parser.jobDescriptionToDOM(new FileInputStream(jobDescFile));
        JobPreprocessor preprocessor = new JobPreprocessor();
        preprocessor.preprocess(jobDesc);
        preprocessor.dump();
    }
}
