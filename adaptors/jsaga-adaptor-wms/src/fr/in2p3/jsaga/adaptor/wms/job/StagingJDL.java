package fr.in2p3.jsaga.adaptor.wms.job;

import condor.classad.*;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;

import java.io.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JDL
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 avr. 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class StagingJDL {
    private static final String LIST_INPUT = "InputSandboxPreStaging";
    private static final String LIST_OUTPUT = "OutputSandboxPostStaging";
    private static final String VALUE_FROM = "From";
    private static final String VALUE_TO = "To";
    private static final String VALUE_APPEND = "Append";
    private static final String VALUE_BASEDIR = "SandboxDirectory";

    protected RecordExpr m_expr;

    public StagingJDL(String jdl) throws NoSuccessException {
        this(new ByteArrayInputStream(jdl.getBytes()));
    }
    public StagingJDL(InputStream jdlStream) throws NoSuccessException {
        ClassAdParser parser = new ClassAdParser(jdlStream, ClassAdParser.TEXT);
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        parser.setErrorStream(new PrintStream(errorStream));

        // parse JDL
        m_expr = (RecordExpr) parser.parse();
        if (errorStream.size() > 0) {
            throw new NoSuccessException("Syntax error: "+ errorStream.toString());
        }
    }

    public String getStagingDirectory() {
        try {
            return getValue(m_expr, VALUE_BASEDIR);
        } catch (DoesNotExistException e) {
            return null;
        }
    }

    public StagingTransfer[] getInputStagingTransfer(String baseUri) {
        try {
            ListExpr input = (ListExpr) findExpr(m_expr, LIST_INPUT);
            StagingTransfer[] transfers = new StagingTransfer[input.size()];
            for (int i=0; i<input.size(); i++) {
                RecordExpr r = (RecordExpr) input.sub(i);
                transfers[i] = new StagingTransfer(
                        getValue(r,VALUE_FROM),
                        baseUri+getValue(r,VALUE_TO),
                        Boolean.parseBoolean(getValue(r,VALUE_APPEND)));
            }
            return transfers;
        } catch (DoesNotExistException e) {
            return new StagingTransfer[]{};
        }
    }

    public StagingTransfer[] getOutputStagingTransfers(String baseUri) {
        try {
            ListExpr output = (ListExpr) findExpr(m_expr, LIST_OUTPUT);
            StagingTransfer[] transfers = new StagingTransfer[output.size()];
            for (int i=0; i<output.size(); i++) {
                RecordExpr r = (RecordExpr) output.sub(i);
                transfers[i] = new StagingTransfer(
                        baseUri+getValue(r,VALUE_FROM),
                        getValue(r,VALUE_TO),
                        Boolean.parseBoolean(getValue(r,VALUE_APPEND)));
            }
            return transfers;
        } catch (DoesNotExistException e) {
            return new StagingTransfer[]{};
        }
    }

    private static Expr findExpr(RecordExpr expr, String key) throws DoesNotExistException {
        Expr child = expr.lookup(key);
        if (child != null) {
            return child;
        } else {
            throw new DoesNotExistException("Attribute not found in JDL: "+key);
        }
    }

    private static String getValue(RecordExpr expr, String key) throws DoesNotExistException {
        String value = findExpr(expr, key).toString();

        // remove quotes
        if (value!=null && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length()-1);
        } else if (value!=null && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length()-1);
        } else {
            return value;
        }
    }
}
