package org.glite.ce.creamapi.jobmanagement.jdl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.glite.ce.creamapi.jobmanagement.Job;
import org.glite.jdl.Jdl;
import org.glite.jdl.JobAd;

import condor.classad.Constant;
import condor.classad.Expr;
import condor.classad.ListExpr;

public final class NormalJob {
    private static final long serialVersionUID = 1L;

    private static String getAttributeValue(JobAd jab, String attribute) {
        String value = null;
        Expr result = jab.lookup(attribute);

        if (result != null) {
            value = result.toString();
            if (value.length() > 0) {
                if (value.charAt(0) == '"') {
                    value = value.substring(1, value.length()-1);
                }
            }
        }
        return value;
    }

    private static String getAttributeValue(JobAd jab, String attribute, String fault) throws IllegalArgumentException, Exception {
        if(jab == null) {
            throw new IllegalArgumentException("JobAd not defined!");
        }
        
        if(attribute == null) {
            throw new IllegalArgumentException("attribute not defined!");
        }
        
        String result = getAttributeValue(jab, attribute);

        if (result == null) {
            throw new Exception(fault);
        }

        return result;
    }

    private static String getBaseURL(JobAd jab, String attribute) throws IllegalArgumentException, Exception {
        if (attribute == null) {
            return null;
        }
        
        if(jab == null) {
            throw new IllegalArgumentException("JobAd not defined!");
        }
        
        String uriValue = getAttributeValue(jab, attribute);
        if (uriValue != null) {
            URI uri = new URI(uriValue);
                
            String scheme = uri.getScheme();

            if (scheme == null || (!scheme.startsWith("gsiftp") && !scheme.startsWith("file") && !scheme.startsWith("http") && !scheme.startsWith("https"))) {
                throw new IllegalArgumentException("invalid argument: the URI \"" + uriValue + "\" doesn't present an allowed scheme (gsiftp, file, http, https)");
            }

            return uriValue;
        }

        return null;
    }

    private static String[] getBaseURLs(JobAd jab, String sb) throws IllegalArgumentException, Exception {
        if (sb == null) {
            return null;
        }

        if(jab == null) {
            throw new IllegalArgumentException("JobAd not defined!");
        }
        
        ArrayList uriArray = new ArrayList(0);

        Expr expression = jab.lookup(sb);
        if (expression == null) {
            return null;
        }

        if (expression instanceof ListExpr) {
            Iterator iter2 = ((ListExpr) expression).iterator();

            while (iter2.hasNext()) {
                String uriValue = ((Constant) iter2.next()).stringValue();
                if (uriValue != null) {
                    URI uri = new URI(uriValue);
                    String scheme = uri.getScheme();

                    if (scheme == null || (!scheme.startsWith("gsiftp") && !scheme.startsWith("file") && !scheme.startsWith("http") && !scheme.startsWith("https"))) {
                        throw (new IllegalArgumentException("invalid argument: the URI \"" + uriValue + "\" doesn't present an allowed scheme (gsiftp, file, http, https)"));
                    }

                    uriArray.add(uriValue);
                }
            }
        } else {
            String uriValue = ((Constant) expression).stringValue();

            if (uriValue != null) {
                URI uri = new URI(uriValue);
                String scheme = uri.getScheme();

                if (scheme == null || (!scheme.startsWith("gsiftp") && !scheme.startsWith("file") && !scheme.startsWith("http") && !scheme.startsWith("https"))) {
                    throw (new IllegalArgumentException("invalid argument: the URI \"" + uriValue + "\" doesn't present an allowed scheme (gsiftp, file, http, https)"));
                }

                uriArray.add(uriValue);
            }
        }

        String[] files = new String[uriArray.size()];
        files = (String[]) uriArray.toArray(files);

        return files;
    }

    private static String[] getFiles(JobAd jab, String sb) throws IllegalArgumentException {
        if (sb == null) {
            return null;
        }

        if(jab == null) {
            throw new IllegalArgumentException("JobAd not defined!");
        }
        
        ArrayList<String> fileArray = new ArrayList<String>(0);

        Expr expression = jab.lookup(sb);
        if (expression == null) {
            return null;
        }

        if (expression instanceof ListExpr) {
            Iterator iter2 = ((ListExpr) expression).iterator();

            while (iter2.hasNext()) {
                String uri = ((Constant) iter2.next()).stringValue();

                fileArray.add(uri);
            }
        } else {
            String uri = ((Constant) expression).stringValue();

            fileArray.add(uri);
        }

        String[] files = new String[fileArray.size()];
        files = (String[]) fileArray.toArray(files);

        return files;
    }

    public static Job makeJob(String jdl) throws Exception {
        if(jdl == null) {
            throw new IllegalArgumentException("JDL not defined!");
        }
        
        return makeJob(new JobAd(jdl));
    }

    public static Job makeJob(JobAd jobAd) throws Exception {
        if(jobAd == null) {
            throw new IllegalArgumentException("JobAd not defined!");
        }
        
        jobAd.setLocalAccess(false);
        jobAd.checkAll(new String[] { Jdl.EXECUTABLE, Jdl.QUEUENAME, Jdl.JOBTYPE, "BatchSystem" });

        Job job = new Job();
        job.setLRMSAbsLayerJobId("N/A");
        job.setLRMSJobId("N/A");
        job.setWorkerNode("N/A");

        // if (creamURL == null) {
        // setCREAMJobId(getId());
        // } else {
        // setCREAMJobId(creamURL + (creamURL.endsWith("/") ? "" : "/")
        // + getId());
        // }

        // setId(getAttributeValue(job, Jdl.JOBID));
        job.setType(getAttributeValue(jobAd, Jdl.JOBTYPE));
        job.setBatchSystem(getAttributeValue(jobAd, "BatchSystem"));
        job.setQueue(getAttributeValue(jobAd, Jdl.QUEUENAME));
        job.setVirtualOrganization(getAttributeValue(jobAd, Jdl.VIRTUAL_ORGANISATION));
        job.setStandardError(getAttributeValue(jobAd, Jdl.STDERROR));
        job.setStandardInput(getAttributeValue(jobAd, Jdl.STDINPUT));
        job.setStandardOutput(getAttributeValue(jobAd, Jdl.STDOUTPUT));
        job.setExecutable(getAttributeValue(jobAd, Jdl.EXECUTABLE));
        job.setCeRequirements(getAttributeValue(jobAd, Jdl.CE_REQUIREMENTS));
        job.setInputSandboxBaseURI(getBaseURL(jobAd, "InputSandboxBaseURI"));
        job.setOutputSandboxBaseDestURI(getBaseURL(jobAd, "OutputSandboxBaseDestURI"));
        job.setOutputSandboxDestURI(getBaseURLs(jobAd, "OutputSandboxDestURI"));
        job.setInputFiles(getFiles(jobAd, Jdl.INPUTSB));
        job.setOutputFiles(getFiles(jobAd, Jdl.OUTPUTSB));
        job.setHlrLocation(getAttributeValue(jobAd, Jdl.HLR_LOCATION));
        job.setMyProxyServer(getAttributeValue(jobAd, Jdl.MYPROXY));
        job.setPrologue(getAttributeValue(jobAd, "Prologue"));
        job.setPrologueArguments(getAttributeValue(jobAd, "PrologueArguments"));
        job.setEpilogue(getAttributeValue(jobAd, "Epilogue"));
        job.setEpilogueArguments(getAttributeValue(jobAd, "EpilogueArguments"));
        job.setSequenceCode(getAttributeValue(jobAd, "LB_sequence_code"));
        job.setTokenURL(getAttributeValue(jobAd, "ReallyRunningToken"));

        String mwVersion = getAttributeValue(jobAd, Jdl.MW_VERSION);
        if (mwVersion != null) {
            job.addExtraAttribute(Jdl.MW_VERSION, mwVersion);
        }
        
        String maxOutputSandboxSize = getAttributeValue(jobAd, Jdl.MAX_OUTPUT_SANDBOX_SIZE);
        if (maxOutputSandboxSize != null) {
          job.putVolatileProperty(Job.MAX_OUTPUT_SANDBOX_SIZE, maxOutputSandboxSize);
        }
        
        if(job.getOutputSandboxBaseDestURI() != null && job.getOutputSandboxBaseDestURI().length() > 0 &&
                job.getOutputSandboxDestURI() != null && job.getOutputSandboxDestURI().length > 0) {
            throw new Exception("the OutputSandboxDestURI and OutputSandboxBaseDestURI attributes cannot be specified toghether in the same JDL");
        }
        
        if(job.getOutputFiles() != null) {
            if(job.getOutputSandboxDestURI() == null && job.getOutputSandboxBaseDestURI() == null) {
                throw new Exception("the OutputSandbox attribute requires the specification in the same JDL of one of the following attributes: OutputSandboxDestURI or OutputSandboxBaseDestURI");
            }
            
            if(job.getOutputSandboxDestURI() != null && job.getOutputSandboxDestURI().length != job.getOutputFiles().length) {
                throw new Exception("the OutputSandbox and OutputSandboxBaseDestURI attributes must have the same cardinality");
            }
        }
        
        String args = getAttributeValue(jobAd, Jdl.ARGUMENTS);
        if (args != null && args.length() > 0) {
            job.setArguments(new String[] { args });
        }

        String edg_jobid = getAttributeValue(jobAd, "edg_jobid");
        if (edg_jobid == null) {
            edg_jobid = "N/A";
        }
        job.setGridJobId(edg_jobid);

        if (job.getType() == null) {
            job.setType("normal");
        }

        Expr expression = jobAd.lookup(Jdl.ENVIRONMENT);

        if (expression != null && expression instanceof ListExpr) {
            Iterator item = ((ListExpr) expression).iterator();

            while (item.hasNext()) {
                String tmps = ((Constant) item.next()).stringValue();
                String[] tokens = tmps.split("=");
                job.addEnvironmentAttribute(tokens[0].trim(), tokens[1].trim());
            }
        }

        expression = jobAd.lookup(Jdl.CPUNUMB);
        if (expression != null && expression instanceof Constant) {
            job.setNodeNumber(expression.intValue());
        } else {
            expression = jobAd.lookup(Jdl.NODENUMB);
            if (expression != null && expression instanceof Constant) {
                job.setNodeNumber(expression.intValue());
            }
        }

        expression = jobAd.lookup("PerusalFileEnable");
        if (expression != null && expression.type == Expr.BOOLEAN && expression.isTrue()) {
            expression = jobAd.lookup("PerusalTimeInterval");
            if (expression != null && expression instanceof Constant) {
                job.setPerusalTimeInterval(expression.intValue());
            } else {
                job.setPerusalTimeInterval(5);
            }
            job.setPerusalFilesDestURI(getAttributeValue(jobAd, "PerusalFilesDestURI"));
            job.setPerusalListFileURI(getAttributeValue(jobAd, "PerusalListFileURI"));
        } else {
            job.setPerusalFilesDestURI(null);
        }

        return job;
    }
}
