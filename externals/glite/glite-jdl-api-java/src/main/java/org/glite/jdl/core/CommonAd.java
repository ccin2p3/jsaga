/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 *
 * Authors: Paolo Andreetto, <paolo.andreetto@pd.infn.it>
 *
 * Version info: $Id: CommonAd.java,v 1.13 2006/10/26 14:52:49 pandreet Exp $
 */

package org.glite.jdl.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Collection;
import java.util.ArrayList;
import java.net.URI;
import java.net.MalformedURLException;

import condor.classad.ClassAdParser;
import condor.classad.Expr;
import condor.classad.ListExpr;
import condor.classad.RecordExpr;
import condor.classad.Constant;
import condor.classad.AttrRef;
import condor.classad.CondExpr;
import condor.classad.FuncCall;
import condor.classad.Op;
import condor.classad.SelectExpr;
import condor.classad.SubscriptExpr;
import condor.classad.AttrName;

import org.glite.jdl.Jdl;
import org.glite.jdl.JobAdException;
import org.glite.jdl.ExtractFiles;

public abstract class CommonAd implements Cloneable {

    protected boolean modified;
    protected String lastMessage;

    private String executable = null;
    private String arguments = null;
    private HashMap env = null;
    private ArrayList inputSB = null;
    private ArrayList outputSB = null;
    private String prologue = null;
    private String prologueArgs = null;
    private String epilogue = null;
    private String epilogueArgs = null;
    private String vo = null;
    private Expr requirements = null;
    private Expr rank = null;
    private ArrayList dataReq = null;

    public CommonAd(){
        modified = true;
        lastMessage = null;

        env = new HashMap();
        inputSB = new ArrayList();
        outputSB = new ArrayList();
        dataReq = new ArrayList();
    }

    CommonAd(RecordExpr expr) throws JobAdException {
        build(expr);
        validate();
    }








    public String getExecutable() throws JobAdException {
        validate();
        return executable;
    }

    public void setExecutable(String exec) {
        executable = exec;
        modified = true;
    }

    public String getArguments() throws JobAdException {
        validate();
        return arguments;
    }

    public void setArguments(String args) {
        arguments = args;
        modified = true;
    }

    public Iterator getEnvKeys() throws JobAdException {
        validate();
        return env.keySet().iterator();
    }

    public String getEnvValue(String key) throws JobAdException {
        validate();
        return (String)env.get(key);
    }

    public void setEnvValue(String key, String value) {
        if( value!=null )
            env.put(key, value);
        else
            env.remove(key);
        modified = true;
    }

    public void addInputSandboxURI(String uri) {
        inputSB.add(uri);
        modified = true;
    }

    public Iterator getInputSandboxIterator() throws JobAdException {
        validate();
        return new IteratorWrapper(inputSB.iterator());
    }

    public int getInputSandboxSize() throws JobAdException {
        validate();
        return inputSB.size();
    }

    public void clearInputSandboxList() {
        inputSB.clear();
        modified = true;
    }

    public void addOutputSandboxPair(OutputSandboxPair pair) {
        outputSB.add(pair);
        modified = true;
    }

    public Iterator getOutputSandboxIterator() throws JobAdException {
        validate();
        return new IteratorWrapper(outputSB.iterator());
    }

    public int getOutputSandboxSize() throws JobAdException {
        validate();
        return outputSB.size();
    }

    public void clearOutputSandboxList() {
        outputSB.clear();
        modified = true;
    }

    public String getPrologue() throws JobAdException {
        validate();
        return prologue;
    }

    public void setPrologue(String prologue){
        this.prologue = prologue;
        modified = true;
    }

    public String getPrologueArguments() throws JobAdException {
        validate();
        return prologueArgs;
    }

    public void setPrologueArguments(String args){
        prologueArgs = args;
        modified = true;
    }

    public String getEpilogue() throws JobAdException {
        validate();
        return epilogue;
    }

    public void setEpilogue(String epilogue){
        this.epilogue = epilogue;
        modified = true;
    }

    public String getEpilogueArguments() throws JobAdException {
        validate();
        return epilogueArgs;
    }

    public void setEpilogueArguments(String args){
        epilogueArgs = args;
        modified = true;
    }

    public String getVirtualOrganization() throws JobAdException {
        validate();
        return vo;
    }


    public void setVirtualOrganization(String vo){
        this.vo = vo;
        modified = true;
    }

    public Expr getRequirements() throws JobAdException {
        validate();
        return cloneExpr(requirements);
    }

    public void setRequirements(Expr reqs){
        requirements = fillInRequirements(cloneExpr(reqs));
        modified =  true;
    }

    public Expr getRank() throws JobAdException {
        validate();
        return cloneExpr(rank);
    }

    public void setRank(Expr rank){
        this.rank = fillInRequirements(cloneExpr(rank));
        modified =  true;
    }

    public void addDataRequirement(DataRequirement req) {
        dataReq.add(req);
        modified = true;
    }

    public Iterator getDataRequirementIterator() throws JobAdException {
        validate();
        return new IteratorWrapper(dataReq.iterator());
    }

    public int getDataRequirementSize() throws JobAdException {
        validate();
        return dataReq.size();
    }

    public void clearDataRequirementList() {
        dataReq.clear();
        modified = true;
    }





    public abstract Object clone();

    protected void fillJobAd(CommonAd jAd){
        jAd.executable = executable;
        jAd.arguments = arguments;
        jAd.env = (HashMap)env.clone();
        jAd.inputSB = (ArrayList)inputSB.clone();
        jAd.outputSB = (ArrayList)outputSB.clone();
        jAd.prologue = prologue;
        jAd.prologueArgs = prologueArgs;
        jAd.epilogue = epilogue;
        jAd.epilogueArgs = epilogueArgs;
        jAd.vo = vo;
        jAd.requirements = cloneExpr(requirements);
        jAd.rank = cloneExpr(rank);
        jAd.dataReq = (ArrayList)dataReq.clone();
    }












    public String toString(){
        StringBuffer buff = new StringBuffer("[\n");
        buff.append(fillString());
        buff.append("]\n");
        return buff.toString();
    }

    protected String fillString(){
        StringBuffer buff = new StringBuffer();

        buff.append(Jdl.EXECUTABLE).append("=\"").append(stringNormalize(executable)).append("\";\n");

        if( arguments!=null && arguments.length()>0 )
            buff.append(Jdl.ARGUMENTS).append("=\"").append(stringNormalize(arguments)).append("\";\n");

        if( env.size()>0 ){
            buff.append(Jdl.ENVIRONMENT).append("={\n");
            Iterator keys = env.keySet().iterator();
            for(int k=0; keys.hasNext(); k++ ){
                String key = (String)keys.next();
                if( k>0 )
                    buff.append(",\n");
                buff.append("    \"").append(key).append("=").append((String)env.get(key)).append("\"");
            }
            buff.append("\n};\n");
        }

        if( inputSB.size()>0 ){
            buff.append(Jdl.INPUTSB).append("={\n    \"").append(inputSB.get(0).toString());
            for(int k=1; k<inputSB.size(); k++)
                buff.append("\",\n    \"").append(inputSB.get(k).toString());
            buff.append("\"\n};\n");
        }

        if( outputSB.size()>0 ){
            StringBuffer osbBuffer = (new StringBuffer(Jdl.OUTPUTSB)).append("={\n");
            StringBuffer osbDestBuffer = (new StringBuffer(Jdl.OSBURI)).append("={\n");

            OutputSandboxPair pair = (OutputSandboxPair)outputSB.get(0);
            osbBuffer.append("    \"").append(pair.getRelativePath()).append("\"");
            osbDestBuffer.append("    \"").append(pair.getDestinationURI()).append("\"");

            for(int k=1; k<outputSB.size(); k++){
                pair = (OutputSandboxPair)outputSB.get(k);
                osbBuffer.append(",\n    \"").append(pair.getRelativePath()).append("\"");
                osbDestBuffer.append(",\n    \"").append(pair.getDestinationURI()).append("\"");
            }

            buff.append(osbBuffer).append("\n};\n");
            buff.append(osbDestBuffer).append("\n};\n");
        }

        if( prologue!=null )
            buff.append(Jdl.PROLOGUE).append("=\"").append(stringNormalize(prologue)).append("\";\n");
        if( prologueArgs!=null )
            buff.append(Jdl.PROLOGUE_ARGUMENTS).append("=\"").append(stringNormalize(prologueArgs)).append("\";\n");
        if( epilogue!=null )
            buff.append(Jdl.EPILOGUE).append("=\"").append(stringNormalize(epilogue)).append("\";\n");
        if( epilogueArgs!=null )
            buff.append(Jdl.EPILOGUE_ARGUMENTS).append("=\"").append(stringNormalize(epilogueArgs)).append("\";\n");

        if( vo!=null )
            buff.append(Jdl.VIRTUAL_ORGANISATION).append("=\"").append(vo).append("\";\n");

        if( requirements!=null )
            buff.append(Jdl.REQUIREMENTS).append("=").append(requirements.toString()).append(";\n");

        if( rank!=null )
            buff.append(Jdl.RANK).append("=").append(rank.toString()).append(";\n");

        if( dataReq.size()>0 ){
            buff.append(Jdl.DATA_REQUIREMENTS).append("={\n").append(dataReq.get(0).toString());
            for(int k=1; k<dataReq.size(); k++)
                buff.append(",\n").append(dataReq.get(k).toString());
            buff.append("\n};\n");
        }

        return buff.toString();
    }














    protected void build(RecordExpr record) throws JobAdException {

        modified = true;
        lastMessage = null;
        env = new HashMap();
        inputSB = new ArrayList();
        outputSB = new ArrayList();
        dataReq = new ArrayList();

        try{
            Expr expr = record.lookup(Jdl.EXECUTABLE);
            if( expr!=null )
                executable = ((Constant)expr).stringValue();

            expr = record.lookup(Jdl.ARGUMENTS);
            if( expr!=null )
                arguments = ((Constant)expr).stringValue();

            expr = record.lookup(Jdl.ENVIRONMENT);
            if( expr!=null ){
                Iterator allVar = ((ListExpr)expr).iterator();
                while( allVar.hasNext() ){
                    String line = ((Constant)allVar.next()).stringValue();
                    StringTokenizer strok = new StringTokenizer(line,"=");
                    if( strok.countTokens()!=2 )
                        throw new Exception("Bad format definition for environment: " + line);
                    env.put(strok.nextToken().trim(), strok.nextToken().trim());
                }
            }

            expr = record.lookup(Jdl.INPUTSB);
            if( expr!=null ){

                Iterator items = ((ListExpr)expr).iterator();

                String isbBaseURI = null;
                expr = record.lookup(Jdl.ISBBASEURI);
                if( expr!=null ){
                    isbBaseURI = ((Constant)expr).stringValue();
                    if( !isbBaseURI.startsWith("gsiftp") && !isbBaseURI.startsWith("https")
                        && !isbBaseURI.startsWith("file"))
                        throw new MalformedURLException("Scheme unsupported in ISB base URI: " + isbBaseURI);
                    if( !isbBaseURI.endsWith("/") )
                        isbBaseURI = isbBaseURI + "/";
                }else{
                    isbBaseURI = "file://" + System.getProperty("user.home") + "/";
                }

                while( items.hasNext() ){
                    String tmps = ((Constant)items.next()).stringValue();
                    if( tmps.startsWith("gsiftp") || tmps.startsWith("https") || tmps.startsWith("file")){
                        inputSB.add(tmps);
                    }else{
                        inputSB.add(isbBaseURI + tmps);
                    }
                }
            }

            ListExpr osbExpr = (ListExpr)record.lookup(Jdl.OUTPUTSB);
            ListExpr osbURIExpr = (ListExpr)record.lookup(Jdl.OSBURI);
            Constant osbBaseURIExpr = (Constant)record.lookup(Jdl.OSBBASEURI);

            if( osbExpr==null ){

            }else if( osbBaseURIExpr!=null && osbURIExpr!=null ){

                throw new IllegalArgumentException("Cannot specified both " + Jdl.OSBURI + " and " + Jdl.OSBBASEURI);

            }else if( osbURIExpr!=null ){

                if( osbExpr.size()!=osbURIExpr.size() )
                    throw new IllegalArgumentException(Jdl.OUTPUTSB + " and " + Jdl.OSBURI + " must have the same size");

                for(int k=0; k<osbExpr.size(); k++){
                    String path = ((Constant)osbExpr.sub(k)).stringValue();
                    String dest = ((Constant)osbURIExpr.sub(k)).stringValue();
                    outputSB.add(new OutputSandboxPair(path, dest));
                }

            }else if( osbBaseURIExpr!=null ){

                String prefix = osbBaseURIExpr.stringValue();
                if( !prefix.endsWith("/") )
                    prefix = prefix + "/";

                for(int k=0; k<osbExpr.size(); k++){
                    String path = ((Constant)osbExpr.sub(k)).stringValue();
                    String dest = prefix + path;
                    outputSB.add(new OutputSandboxPair(path, dest));
                }

            }else{
                throw new IllegalArgumentException(Jdl.OUTPUTSB + " or " + Jdl.OSBURI + " must be defined");
            }

            expr = record.lookup(Jdl.PROLOGUE);
            if( expr!=null )
                prologue = ((Constant)expr).stringValue();
                
            expr = record.lookup(Jdl.PROLOGUE_ARGUMENTS);
            if( expr!=null )
                prologueArgs = ((Constant)expr).stringValue();

            expr = record.lookup(Jdl.EPILOGUE);
            if( expr!=null )
                epilogue = ((Constant)expr).stringValue();
                
            expr = record.lookup(Jdl.EPILOGUE_ARGUMENTS);
            if( expr!=null )
                epilogueArgs = ((Constant)expr).stringValue();

            expr = record.lookup(Jdl.VIRTUAL_ORGANISATION);
            if( expr!=null )
                vo = ((Constant)expr).stringValue();

            expr = record.lookup(Jdl.REQUIREMENTS);
            if( expr!=null )
                requirements = fillInRequirements(expr);

            expr = record.lookup(Jdl.RANK);
            if( expr!=null )
                rank = expr;

        }catch(Exception ex){
            ex.printStackTrace();
            raiseException(ex.getMessage());
        }
    }













    protected boolean validate() throws JobAdException {
        if( !modified ){
            if( lastMessage!=null ){
                throw new JobAdException(lastMessage);
            }else{
                return false;
            }
        }

        modified = false;
        lastMessage = null;

        if( executable==null || executable.length()==0 )
            raiseException("Missing executable attribute");

        if( arguments!=null && arguments.indexOf('`')>=0)
            raiseException("Character ` not allowed in arguments");

        if( prologueArgs!=null && prologueArgs.indexOf('`')>=0)
            raiseException("Character ` not allowed in prologue arguments");

        if( epilogueArgs!=null && epilogueArgs.indexOf('`')>=0)
            raiseException("Character ` not allowed in epilogue arguments");

        boolean missing = !ExtractFiles.isAbsolute(executable);
        for(int k=0; k<inputSB.size(); k++){
            String name = (String)inputSB.get(k); 
            if( name.endsWith(executable) )
                missing = false;
            if( name.startsWith("lfn:") || name.startsWith("guid:")
                || name.startsWith("si-lfn:") || name.startsWith("si-guid:"))
                raiseException("Cannot specify a logical file name in ISB");
        }
        if( missing )
            raiseException("Executable missing in ISB");

        for(int k=0; k<inputSB.size()-1; k++){

            String name1 = (String)inputSB.get(k);
            name1 = name1.substring(name1.lastIndexOf("/")+1);
            if( name1.length()==0 )
                raiseException("Malformed URI: " + name1);

            for(int j=k+1; j<inputSB.size(); j++){
                String name2 = (String)inputSB.get(j);
                if( name2.endsWith(name1) )
                    raiseException("Duplicate name in ISB: " + name1);
            }
        }

        if( requirements==null )
            raiseException(Jdl.REQUIREMENTS + " attribute is missing");

        if( rank==null )
            raiseException(Jdl.RANK  + " attribute is missing");
        /* ********************************************************************************************

           Check rank against datarequirements (if other.DataAccessCost is specified)

           ******************************************************************************************** */

        return true;
    }












    Expr fillInRequirements(Expr expr){
        return expr;
    }

    String stringNormalize(String str){
        StringBuffer buff = new StringBuffer();
        for(int k=0; k<str.length(); k++){
            if(str.charAt(k)=='"')
                buff.append("\\\"");
            else if(str.charAt(k)=='\\')
                buff.append("\\\\");
            else
                buff.append(str.charAt(k));
        }
        return buff.toString();
    }

    boolean checkWildChars(String str){
        if( str.charAt(0)=='*' || str.charAt(0)=='?' || str.charAt(0)=='[')
            return true;

        for(int k=1; k<str.length(); k++){
            if( str.charAt(k)=='*' && str.charAt(k-1)!='\\' )
                return true;
            if( str.charAt(k)=='?' && str.charAt(k-1)!='\\' )
                return true;
            if( str.charAt(k)=='[' && str.charAt(k-1)!='\\' )
                return true;
        }

        return false;
    }

    protected void raiseException(String msg) throws JobAdException {
        lastMessage = msg;
        throw new JobAdException(msg);
    }

    Expr cloneExpr(Expr expr){
        if( expr instanceof AttrRef ){

            return new AttrRef(expr.toString());

        }else if( expr instanceof CondExpr ){

            CondExpr cond = (CondExpr)expr;
            return new CondExpr(cloneExpr(cond.ec), cloneExpr(cond.et), cloneExpr(cond.ef));

        }else if( expr instanceof Constant ){

            return expr;

        }else if( expr instanceof FuncCall ){

            FuncCall func = (FuncCall)expr;
            ArrayList args = new ArrayList(func.args.size());
            Iterator argItems = func.args.iterator();
            while( argItems.hasNext() ){
                args.add(cloneExpr((Expr)argItems.next()));
            }
            return FuncCall.getInstance(func.func, args);
            
        }else if( expr instanceof ListExpr ){

            ListExpr source = (ListExpr)expr;
            ListExpr target = new ListExpr();
            for(int k=0; k<source.size(); k++){
                target.add(cloneExpr(source.sub(k)));
            }
            return target;

        }else if( expr instanceof Op ){

            Op op = (Op)expr;
            if( op.arg2!=null )
                return new Op(op.op, cloneExpr(op.arg1), cloneExpr(op.arg2));
            return new Op(op.op, cloneExpr(op.arg1));

        }else if( expr instanceof RecordExpr ){

            RecordExpr source = (RecordExpr)expr;
            RecordExpr target = new RecordExpr();
            Iterator attrs = source.attributes();
            while( attrs.hasNext() ){
                AttrName name = (AttrName)attrs.next();
                target.insertAttribute(name, cloneExpr(source.lookup(name)));
            }
            return target;

        }else if( expr instanceof SelectExpr ){

            SelectExpr sel = (SelectExpr)expr;
            return new SelectExpr(cloneExpr(sel.base), sel.selector);

        }else if( expr instanceof SubscriptExpr ){

            SubscriptExpr subscr = (SubscriptExpr)expr;
            return new SubscriptExpr(cloneExpr(subscr.base), cloneExpr(subscr.selector));

        }

        throw new RuntimeException("Unrecognized expression: " + expr.toString());
    }

    class IteratorWrapper implements Iterator {

        private Iterator iterator;

        public IteratorWrapper(Iterator iter){
            iterator = iter;
        }

        public boolean hasNext(){
            return iterator.hasNext();
        }

        public Object next(){
            return iterator.next();
        }

        public void remove(){
            modified = true;
            iterator.remove();
        }
    }

    public class OutputSandboxPair implements Cloneable {

        private String path;
        private String dest;

        public OutputSandboxPair(String relativePath, String destinationURI){

            path = relativePath;
            dest = destinationURI;
        }

        public String getRelativePath(){
            return path;
        }

        public String getDestinationURI(){
            return dest;
        }

        public Object clone(){
            return new OutputSandboxPair(path, dest);
        }
    }

}
