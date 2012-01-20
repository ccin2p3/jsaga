package fr.in2p3.jsaga.adaptor.security;

import java.util.Map;
import java.util.Vector;

import org.freedesktop.DBus.Introspectable;
import org.freedesktop.DBus.Properties;
import org.freedesktop.dbus.DBusAsyncReply;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassStoreSecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserPassStoreSecurityAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   2 mai 2011
* ***************************************************/

public class FreedesktopSecretsSecurityAdaptor extends UserPassStoreSecurityAdaptor {

	protected static final String BUS_NAME = "org.freedesktop.secrets";
	protected static final String INTERFACE_NAME = "org.freedesktop.Secret.Item";
	protected static final String OBJECT_PATH = "/org/freedesktop/secrets";
	protected static final String KEYRING = "Keyring";
	protected static final String ID = "Id";

	public String getType() {
		return "Freedesktop";
	}

	public Usage getUsage() {
    	return new UAnd(
   			 new Usage[]{
   					 new UOptional(KEYRING),
   			 }
   			 );
	}

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
       		 new Default(KEYRING, "login"),
        };
	}


	public SecurityCredential createSecurityCredential(int usage,
			Map attributes, String contextId) throws IncorrectStateException,
			TimeoutException, NoSuccessException {
		System.setProperty("java.library.path", "/home/schwarz/usr/local/lib/libmatthew/lib/jni/");
		UserPassStoreSecurityCredential upsc = new UserPassStoreSecurityCredential();
		DBusConnection conn;
		try {
			conn = DBusConnection.getConnection(DBusConnection.SESSION);
			
			String objectPath;
			Introspectable in;
			Properties prop;
			
			objectPath = OBJECT_PATH + "/session";
			in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
			System.out.println(in.Introspect());
			prop = (Properties) conn.getRemoteObject(BUS_NAME, objectPath, Properties.class);
			DBusAsyncReply<OpenSessionReturn> sessionreply = conn.callMethodAsync(prop, "OpenSession", "plain", new Variant(""));
			if (sessionreply.hasReply()) {  
				System.out.println("reply=" + sessionreply.getReply().toString());
				
			} else {
				System.out.println("no reply");
			}

			
			
			
			objectPath = OBJECT_PATH + "/collection" +  "/login/21";
			in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
			String data = in.Introspect();
			System.out.println(data);
			prop = (Properties) conn.getRemoteObject(BUS_NAME, objectPath, Properties.class);
			System.out.println ("Label=" + prop.Get(INTERFACE_NAME, "Label"));
//			String[] atts = prop.Get(INTERFACE_NAME, "Attributes");
//			for (int i=0; i<atts.length; i++) {
//				System.out.println(atts[i]);
//			}
//			Item inter = (Item) conn.getRemoteObject(BUS_NAME, objectPath, ItemInterface.class);
//			System.out.println("Label=" + inter.Label);
			DBusAsyncReply<Secret> stuffreply = conn.callMethodAsync(prop, "GetSecret", prop);
			if (stuffreply.hasReply()) {  
				System.out.println("pw=" + stuffreply.getReply());
				
			} else {
				System.out.println("no reply");
			}
		} catch (DBusException e) {
			throw new NoSuccessException(e);
		}
		return upsc;
	}
	private final class OpenSessionReturn<O, R> extends Tuple {
		public final O output;
		public final R result;
		public OpenSessionReturn(O o, R r) {
			this.output = o;
			this.result = r;
		}
//		public String toString() {
//			return "output=" + this.output.toString() + " -- result=" + this.result.toString();
//		}
	}
	
	private final class Secret {
		DBusInterface session;
		Byte[] parameters;
		Byte[] value;
		String content_type;
	}
	
	private interface ItemInterface extends DBusInterface {
		public String getSecret(String session);
	}
	private class Item implements ItemInterface {
		protected String Label;

		public boolean isRemote() {
			// TODO Auto-generated method stub
			return false;
		}

		public String getSecret(String session) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
