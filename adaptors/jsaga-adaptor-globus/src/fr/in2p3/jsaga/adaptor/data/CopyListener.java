package fr.in2p3.jsaga.adaptor.data;

import org.globus.ftp.Marker;
import org.globus.ftp.MarkerListener;
import org.globus.ftp.PerfMarker;

import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyMonitor;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CopyListener
* Author: lionel.schwarz@in2p3.fr
* Date:   15 NOV 2013
* ***************************************************
* Description:                                      */


public class CopyListener implements MarkerListener {

	private DataCopyMonitor m_progressMonitor;
	
	public CopyListener(DataCopyMonitor m) {
		this.m_progressMonitor = m;
	}
	
	public void markerArrived(Marker marker) {
		if (marker instanceof PerfMarker) {
			PerfMarker perfMarker = (PerfMarker)marker;
	    	try {
	    		long bytes = perfMarker.getStripeBytesTransferred();
	    	    this.m_progressMonitor.setTotal(bytes);
  	        } catch (Exception ex) {
   	        }
   	    }
	}
}
