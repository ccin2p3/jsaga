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
 * Version info: $Id: CacheLockBin.java,v 1.2 2007/12/19 15:58:02 zangran Exp $
 *
 */

package org.glite.ce.commonj.jndi.provider.fscachedprovider;

import java.util.HashMap;

public class CacheLockBin extends HashMap{

    private int count;
    private int reserved;
    private int lock;
    private int[] opCount;
    private int[] opLock;
    private int[] opReserved;

    public CacheLockBin(){
        super();
        count = 0;
        reserved = 0;
        lock = 0;
        opLock = new int[CacheLock.NUM_OF_OPERATION];
        opCount = new int[CacheLock.NUM_OF_OPERATION];
        opReserved = new int[CacheLock.NUM_OF_OPERATION];

        for(int k=0; k<CacheLock.NUM_OF_OPERATION; k++){
            opLock[k] = 0;
            opCount[k] = 0;
            opReserved[k] = 0;
        }
    }

    public void incrCount(int op){
        opCount[op]++;
        count++;
    }

    public void decrCount(int op){
        opCount[op]--;
        count--;
    }

    public int getCount(int op){
        return opCount[op];
    }

    public int getCount(){
        return count;
    }

    public int getOpLock(int op){
        return opLock[op];
    }

    public int[] getOpLock(){
        return opLock;
    }

    public void addOpLock(int op){
        opLock[op]++;
        lock++;
    }

    public void removeOpLock(int op){
        opLock[op]--;
        lock--;
    }

    public boolean isFree(){
        return lock==0;
    }

    public void incrReserved(int op){
        opReserved[op]++;
        reserved++;
    }

    public void decrReserved(int op){
        opReserved[op]--;
        reserved--;
    }

    public int getReserved(int op){
        return opReserved[op];
    }

    public int getReserved(){
        return reserved;
    }

}
