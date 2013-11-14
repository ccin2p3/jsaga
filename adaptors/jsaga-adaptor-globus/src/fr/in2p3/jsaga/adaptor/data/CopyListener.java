package fr.in2p3.jsaga.adaptor.data;

import org.globus.ftp.Marker;
import org.globus.ftp.MarkerListener;
import org.globus.ftp.PerfMarker;

import fr.in2p3.jsaga.impl.file.copy.AbstractCopyTask;

public class CopyListener implements MarkerListener {

	private AbstractCopyTask m_progressMonitor;
	
	public CopyListener(AbstractCopyTask m) {
		this.m_progressMonitor = m;
	}
	
	public void markerArrived(Marker marker) {
		if (marker instanceof PerfMarker) {
			PerfMarker perfMarker = (PerfMarker)marker;
	    	try {
	    		long bytes = perfMarker.getStripeBytesTransferred();
	    	    this.m_progressMonitor.increment(bytes);
  	        } catch (Exception ex) {
   	        }
   	    }
	}
}
