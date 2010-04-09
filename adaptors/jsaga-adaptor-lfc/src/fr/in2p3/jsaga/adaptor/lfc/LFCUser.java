package fr.in2p3.jsaga.adaptor.lfc;

/**
 * @author Jerome Revillard
 */
public class LFCUser {
	private final int uid; // uid
	private final String name;

	protected LFCUser(int uid, String name) {
		this.uid = uid;
		this.name = name;
	}

	/**
	 * @param name
	 *            The certificate header of the user (i.e:
	 *            /C=ES/O=Maat_GKnowledge/CN=Jerome Revillard)
	 */
	public LFCUser(String name) {
		this(-1, name);
	}

	public int uid() {
		return uid;
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
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LFCUser)) {
			return false;
		}
		LFCUser other = (LFCUser) obj;
		if ((this.uid != other.uid)) {
			return false;
		}
		if ((this.name != other.name)) {
			return false;
		}
		return true;
	}
}
