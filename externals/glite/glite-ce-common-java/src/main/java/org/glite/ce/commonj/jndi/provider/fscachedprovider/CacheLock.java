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
 * Version info: $Id: CacheLock.java,v 1.2 2007/12/19 15:58:02 zangran Exp $
 *
 */

package org.glite.ce.commonj.jndi.provider.fscachedprovider;

import java.util.Iterator;
import javax.naming.Name;

import org.apache.log4j.Logger;

public class CacheLock {
	private static Logger logger = Logger.getLogger(CacheLock.class.getName());

	public static final int READ_OPERATION = 0;
	public static final int WRITE_OPERATION = 1;
	public static final int DELETE_OPERATION = 2;
	public static final int LIST_OPERATION = 3;
	public static final int NUM_OF_OPERATION = 4;

	private CacheLockBin rootBin;

	public CacheLock() {
		rootBin = new CacheLockBin();
	}

	
	public synchronized void lock(Name name, int op) {
		logger.debug("Locking " + name.toString() + " code " + op);

		CacheLockBin currentBin = rootBin;
		currentBin.incrReserved(op);

		for (int k = 0; k < name.size(); k++) {
			CacheLockBin tmpBin = (CacheLockBin) currentBin.get(name.get(k));
			if (tmpBin == null) {
				tmpBin = new CacheLockBin();
				currentBin.put(name.get(k), tmpBin);
			}

			tmpBin.incrReserved(op);
			currentBin = tmpBin;
		}

		currentBin = rootBin;
		currentBin.incrCount(op);

		for (int k = 0; k < name.size(); k++) {

			CacheLockBin tmpBin = (CacheLockBin) currentBin.get(name.get(k));

			tmpBin.incrCount(op);

			while (isBlockedIn(op, tmpBin)) {
				logger.debug("Operation " + op + " locked on " + name.get(k));
				try {
					wait();
				} catch (InterruptedException intEx) {
				}
			}

			currentBin = tmpBin;
		}

		while (isBlockedIn(op, currentBin) || isScopeBlocked(op, currentBin)) {
			logger.debug("Operation " + op + " locked on destination");
			try {
				wait();
			} catch (InterruptedException intEx) {
			}
		}

		currentBin.addOpLock(op);
		logger.debug("Locked " + name.toString() + " code " + op);
	}

	public synchronized void unlock(Name name, int op) {
		logger.debug("Unlocking " + name.toString() + " code " + op);

		CacheLockBin currentBin = rootBin;
		currentBin.decrReserved(op);
		currentBin.decrCount(op);

		boolean notRemoved = true;
		for (int k = 0; k < name.size() && notRemoved; k++) {

			CacheLockBin tmpBin = (CacheLockBin) currentBin.get(name.get(k));
			tmpBin.decrReserved(op);
			tmpBin.decrCount(op);

			if (tmpBin.getReserved() == 0) {
				logger.debug("Removing subtree rooted at " + name.get(k));
				currentBin.remove(name.get(k));
				notRemoved = false;
				break; // the gc should have to remove the sub-tree
			}
			currentBin = tmpBin;
		}

		logger.debug("Unlocked " + name.toString() + " code " + op);
		if (notRemoved) {
			currentBin.removeOpLock(op);
		}
		notify();
	}

	
	private boolean isBlockedIn(int op, CacheLockBin clBin) {
		if (clBin.isFree())
			return false;

		int[] opLock = clBin.getOpLock();

		return !((op == READ_OPERATION || op == LIST_OPERATION)
				&& opLock[WRITE_OPERATION] == 0 && opLock[DELETE_OPERATION] == 0);
	}

	
	private boolean isScopeBlocked(int op, CacheLockBin clBin) {
		if (op == DELETE_OPERATION) {

			return clBin.getCount() > 1;
		}

		if (op == LIST_OPERATION) {
			Iterator childList = clBin.values().iterator();
			while (childList.hasNext()) {
				int[] childOpLock = ((CacheLockBin) childList.next())
						.getOpLock();
				if (childOpLock[WRITE_OPERATION] > 0
						|| childOpLock[DELETE_OPERATION] > 0)
					return true;
			}
		}

		return false;
	}
	

	private void printBinStatus(String name, CacheLockBin clBin) {
		StringBuffer buff = new StringBuffer("(" + name + ")  OpLock [");

		int[] opLock = clBin.getOpLock();
		for (int j = 0; j < NUM_OF_OPERATION; j++)
			buff.append(opLock[j] + " ");
		buff.append("]  OpCount [");
		for (int j = 0; j < NUM_OF_OPERATION; j++)
			buff.append(clBin.getCount(j) + " ");
		buff.append("]  OpReserved [");
		for (int j = 0; j < NUM_OF_OPERATION; j++)
			buff.append(clBin.getReserved(j) + " ");
		buff.append("]");

		logger.debug(buff.toString());
	}
}
