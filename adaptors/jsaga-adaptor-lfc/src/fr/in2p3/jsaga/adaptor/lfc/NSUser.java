package fr.in2p3.jsaga.adaptor.lfc;

/**
 * @author Jerome Revillard
 */
public class NSUser {
	private final int uid; // uid
	private final String name;

	protected NSUser(int uid, String name) {
		this.uid = uid;
		this.name = name;
	}

	/**
	 * @param name
	 *            The certificate header of the user (i.e:
	 *            /C=ES/O=Maat_GKnowledge/CN=Jerome Revillard)
	 */
	public NSUser(String name) {
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
		if (!(obj instanceof NSUser)) {
			return false;
		}
		NSUser other = (NSUser) obj;
		if ((this.uid != other.uid)) {
			return false;
		}
		if ((this.name != other.name)) {
			return false;
		}
		return true;
	}
}
