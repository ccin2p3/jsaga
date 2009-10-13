package fr.in2p3.jsaga.adaptor.wms.job;

import condor.classad.*;
import org.ogf.saga.error.BadParameterException;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DefaultJDL
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   6 oct. 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class DefaultJDL {
    private RecordExpr m_expr;

    public DefaultJDL(File defaultJdlFile) throws BadParameterException, FileNotFoundException {
        this(new FileInputStream(defaultJdlFile));
    }
    public DefaultJDL(InputStream defaultJdlStream) throws BadParameterException {
        // parse JDL
        ClassAdParser parser = new ClassAdParser(defaultJdlStream, ClassAdParser.TEXT);
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        parser.setErrorStream(new PrintStream(errorStream));
        RecordExpr expr = (RecordExpr) parser.parse();
        if (errorStream.size() > 0) {
            throw new BadParameterException("Syntax error: "+ errorStream.toString());
        }
        m_expr = (RecordExpr) expr.lookup("WmsClient");
    }

    public void fill(Map attributes) {
        for (Iterator it=m_expr.attributes(); it.hasNext(); ) {
            AttrName key = (AttrName) it.next();
            Expr value = m_expr.lookup(key);
            attributes.put(key.rawString(), removeQuotes(value.toString()));
        }
    }

    private static String removeQuotes(String value) {
        if (value!=null && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length()-1);
        } else if (value!=null && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length()-1);
        } else {
            return value;
        }
    }
}
