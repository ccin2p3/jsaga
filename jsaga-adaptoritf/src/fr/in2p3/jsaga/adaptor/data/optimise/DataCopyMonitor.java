package fr.in2p3.jsaga.adaptor.data.optimise;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataCopyMonitor
* Author: lionel.schwarz@in2p3.fr
* Date:   15 NOV 2013
* ***************************************************
* Description:                                      */

public interface DataCopyMonitor {
	   public void increment(long writtenBytes);
	   public void setTotal(long writtenBytes);
}
