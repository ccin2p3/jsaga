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
 * Version info: $Id: ParameterRange.java,v 1.1 2006/10/05 09:26:28 pandreet Exp $
 */

package org.glite.jdl.core;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ParameterRange implements Parameters {

    private int begin;
    private int end;
    private int step;

    public ParameterRange(int begin, int end, int step){
        if( begin<0 )
            throw new IllegalArgumentException("Range begin cannot be negative");
        this.begin = begin;

        if( end<begin )
            throw new IllegalArgumentException("Range end cannot be lesser than begin");
        this.end = end;

        if( step<1 )
            throw new IllegalArgumentException("Wrong increment");
        this.step = step;
    }

    public Iterator getParameters(){

        return new ParameterRangeIterator();
    }

    public int size(){
        return (end-begin)/step;
    }

    public Object clone(){
        return new ParameterRange(begin, end, step);
    }

    public int getBegin(){
        return begin;
    }

    public int getEnd(){
        return end;
    }

    public int getStep(){
        return step;
    }

    class ParameterRangeIterator implements Iterator {

        private int current;

        ParameterRangeIterator(){
            current = begin;
        }

        public boolean hasNext(){
            return current+step<end;
        }

        public Object next(){
            if( current>=end )
                throw new NoSuchElementException();
            Integer result = new Integer(current);
            current += step;
            return result;
        }

        public void remove(){
            throw new UnsupportedOperationException();
        }
    }

}
