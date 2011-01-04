package fr.in2p3.jsaga.adaptor.lfc;

/**
 * @author Jerome Revillard
 */
public class NSGroup {
	private final int gid; // gid
	private final String name;

	protected NSGroup(int gid, String name) {
		this.gid = gid;
		this.name = name;
	}

	/**
	 * @param name
	 *            The VO group (i.e: hec/pdi)
	 */
	public NSGroup(String name) {
		this(-1, name);
	}

	public int gid() {
		return gid;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NSGroup)) {
			return false;
		}
		NSGroup other = (NSGroup) obj;
		if ((this.gid != other.gid)) {
			return false;
		}
		if ((this.name != other.name)) {
			return false;
		}
		return true;
	}
}
