package fr.in2p3.jsaga.adaptor.lfc;

/**
 * Representation of a {@link NSConnection#MSG_STATUSES} response
 * 
 * @author Jerome Revillard
 * 
 */
 public class NSStatus extends NSObject{
	private int status;

	public NSStatus(int status){
		super(null);
		this.status = status;
	}
	
	/* (non-Javadoc)
	 * @see org.pandora.gateway.dpm.datamanagementdaemon.ns.NSConnection.NSObject#fillObject()
	 */
	@Override
	protected void fillObject() {}


	public int getStatus() {
		return status;
	}

	/**
	 * @return The string representation of the {@link NSStatus} object
	 */
	@Override
	public String toString() {
		return ""+status;
	}
 }
