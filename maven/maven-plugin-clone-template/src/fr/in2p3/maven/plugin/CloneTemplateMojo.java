package fr.in2p3.maven.plugin;

import org.apache.maven.plugin.*;

import java.io.*;
import java.util.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   CloneTemplateMojo
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */
/**
 * This goal filter resources with multiple values for property.
 * @goal filter
 * @phase process-resources
 * todo: replace phase by "prepare-package" when it will be avaible (version 2.1 and above)
 */
public class CloneTemplateMojo extends AbstractMojo {
    /**
     * The template file to filter
     * @parameter
     * @required
     */
    private File file;

    /**
     * The output directory
     * @parameter
     * @required
     */
    private File outputDirectory;

    /**
     * The extension of output files
     * @parameter
     */
    private String outputExtension;

    /**
     * The name of the property to filter
     * @parameter
     * @required
     */
    private String propertyName;

    /**
     * Properties file containing the names of the destination files,
     * mapped with the values of the property to filter
     * @parameter
     * @required
     */
    private File propertyValues;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // check
        if (!file.exists() || !propertyValues.exists()) {
            return; //ignore
        }
        if (!file.exists()) {
            throw new MojoFailureException("Missing template file: "+file.getAbsolutePath());
        }
        if (!propertyValues.exists()) {
            throw new MojoFailureException("Missing property values file: "+propertyValues.getAbsolutePath());
        }
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        // load template
        byte[] template = new byte[(int)file.length()];
        try {
            InputStream in = new FileInputStream(file);
            in.read(template);
            in.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read file: "+file.getAbsolutePath(), e);
        }

        // load property values
        Properties prop = new Properties();
        try {
            InputStream in = new FileInputStream(propertyValues);
            prop.load(in);
            in.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read file: "+propertyValues.getAbsolutePath(), e);
        }

        // filter template file
        getLog().info("Filtering template file: "+file.getAbsolutePath());
        String pattern = "\\$\\{"+propertyName+"\\}";
        for (Iterator it=prop.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String destFilename = (outputExtension!=null
                    ? entry.getKey()+"."+outputExtension
                    : (String) entry.getKey());
            File destFile = new File(outputDirectory, destFilename);
            String destValue = (String) entry.getValue();
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(template)));
            try {
                PrintStream out = new PrintStream(new FileOutputStream(destFile));
                while ((line=in.readLine()) != null) {
                    out.println(line.replaceAll(pattern, destValue));
                }
                out.close();
                in.close();
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to write file: "+destFile.getAbsolutePath(), e);
            }
        }
    }
}
