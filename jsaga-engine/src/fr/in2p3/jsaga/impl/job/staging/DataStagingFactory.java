package fr.in2p3.jsaga.impl.job.staging;

import org.ogf.saga.error.*;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DataStagingFactory
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   3 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DataStagingFactory {
    private static final Pattern PATTERN = Pattern.compile("([^<>]*) *(>>|>|<<|<) *([^<>]*)");

    public static AbstractDataStaging create(String fileTransfer, URL intermediaryURL) throws NotImplementedException, BadParameterException, NoSuccessException {
        Matcher m = PATTERN.matcher(fileTransfer);
        if (m.matches() && m.groupCount()==3) {
            String local = m.group(1).trim();
            String operator = m.group(2).trim();
            String worker = m.group(3).trim();

            // set localURL
            URL localURL;
            if (isURL(local)) {
                localURL = URLFactory.createURL(local);
            } else if (new File(local).isAbsolute()) {
                localURL = URLFactory.createURL(new File(local).toURI().toString());
            } else {
                localURL = URLFactory.createURL("file://./" + local.replaceAll("\\\\", "/"));
            }

            // create DataStaging
            if (">>".equals(operator) || ">".equals(operator)) {
                boolean append = ">>".equals(operator);
                if (isURL(worker)) {
                    return new InputDataStagingToRemote(localURL, URLFactory.createURL(worker), append);
                } else if (intermediaryURL != null) {
                    return new InputDataStagingToIntermediary(localURL, URLFactory.createURL(worker), append, intermediaryURL);
                } else {
                    return new InputDataStagingToWorker(localURL, worker, append);
                }
            } else if ("<<".equals(operator) || "<".equals(operator)) {
                boolean append = "<<".equals(operator);
                if (isURL(worker)) {
                    return new OutputDataStagingFromRemote(localURL, URLFactory.createURL(worker), append);
                } else if (intermediaryURL != null) {
                    return new OutputDataStagingFromIntermediary(localURL, URLFactory.createURL(worker), append, intermediaryURL);
                } else {
                    return new OutputDataStagingFromWorker(localURL, worker, append);
                }
            } else {
                throw new BadParameterException("[INTERNAL ERROR] Unexpected operator: " + operator);
            }
        } else {
            throw new BadParameterException("Syntax error in attribute " + JobDescription.FILETRANSFER + ": " + fileTransfer);
        }
    }

    private static boolean isURL(String file) {
        final boolean hasProtocolScheme = file.contains(":/");
        final boolean isLinuxAbsolutePath = file.startsWith("/");
        final boolean isWindowsAbsolutePath = file.indexOf(':')<=1;  // -1 or 1 (none or "_:")
        return hasProtocolScheme && ! (isLinuxAbsolutePath || isWindowsAbsolutePath);
    }
}
