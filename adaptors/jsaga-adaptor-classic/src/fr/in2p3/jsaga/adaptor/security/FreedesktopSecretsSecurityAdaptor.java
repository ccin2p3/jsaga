package fr.in2p3.jsaga.adaptor.security;

import java.util.Map;
import org.freedesktop.DBus.Introspectable;
import org.freedesktop.DBus.Properties;
import org.freedesktop.Secret.Item;
import org.freedesktop.Secret.Pair;
import org.freedesktop.Secret.Service;
import org.freedesktop.Secret.Session;
import org.freedesktop.Secret.Secret;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Tuple;
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
			String[] names = conn.getNames();
			for (int j=0; j<names.length; j++) {
				System.out.println("names:" + j + "=>"+names[j]);
			}
			String objectPath;
			Introspectable in;
			Properties prop;
			DBusInterface dbusSession=null;
			
			objectPath = OBJECT_PATH;
			in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
			System.out.println(in.Introspect());
			prop = (Properties) conn.getRemoteObject(BUS_NAME, objectPath, Properties.class);
//			Map<String, org.freedesktop.dbus.Variant> props = prop.GetAll("org.freedesktop.Secret" + ".Session");
//			for (Iterator<String> i = props.keySet().iterator() ; i.hasNext() ; ){
//				String key = i.next();
//				org.freedesktop.dbus.Variant value = props.get(key);
//			    System.out.println("***" + key + "=" + value.toString());
//			}
			
//			BusAddress address;
//			try {
//				Transport tconn = new Transport(conn.getAddress());
//				Message m = new MethodCall("org.freedesktop.secrets", 	//BUS_NAME
//						"/org/freedesktop/secrets", 					// PATH
//						"org.freedesktop.Secret.Service", 				// IFACE
//						"OpenSession", (byte) 0, null,					// METHOD
//						"plain",										// ARGUMENTS
//						null);
//				tconn.mout.writeMessage(m);
//				m = tconn.min.readMessage();
//				System.out.println("Response to Hello is: "+m);
//			} catch (ParseException e) {
//				throw new NoSuccessException(e);
//			} catch (IOException e) {
//				throw new NoSuccessException(e);
//			}

			
			Service serv = (Service) conn.getRemoteObject(BUS_NAME, objectPath, Service.class);
			Pair<org.freedesktop.dbus.Variant,DBusInterface> osr = serv.OpenSession("plain", new org.freedesktop.dbus.Variant(""));
			dbusSession = osr.b;
			
//			DBusAsyncReply<OpenSessionReturn> sessionreply = conn.callMethodAsync(prop, "OpenSession", "plain");
//			if (sessionreply.hasReply()) {  
//				System.out.println("reply=" + sessionreply.getReply().toString());
//				dbusSession = (org.freedesktop.Secret.Session) sessionreply.getReply().getParameters()[1];
//			} else {
//				throw new NoSuccessException("no reply");
//			}
			
			
			objectPath = OBJECT_PATH + "/collection" +  "/login/21";
			in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
			String data = in.Introspect();
//			System.out.println(data);
			prop = (Properties) conn.getRemoteObject(BUS_NAME, objectPath, Properties.class);
			System.out.println ("prop.Get -> Label=" + prop.Get(INTERFACE_NAME, "Label"));
			System.out.println ("prop.Get -> Locked=" + prop.Get(INTERFACE_NAME, "Locked"));
			Item inter = (Item) conn.getRemoteObject(BUS_NAME, objectPath, Item.class);
			Secret s = inter.GetSecret(dbusSession);
			System.out.println("secret=" + s);
//			DBusAsyncReply<Secret> stuffreply = conn.callMethodAsync(prop, "GetSecret", new Path(OBJECT_PATH + "/session"));
//			if (stuffreply.hasReply()) {  
//				System.out.println("pw=" + stuffreply.getReply());
//				
//			} else {
//				System.out.println("no reply");
//			}
		} catch (DBusException e) {
			e.printStackTrace();
			throw new NoSuccessException(e);
		}
		return upsc;
	}
	
	public final class OpenSessionParams<String, Variant> extends Tuple {
		public final String algorithm;
		public final Variant input;
		public OpenSessionParams(String a, Variant i) {
			this.algorithm = a;
			this.input = i;
		}
	}
	public final class OpenSessionReturn<Variant, Session> extends Tuple {
		public final Variant output;
		public final Session result;
		public OpenSessionReturn(Variant o, Session r) {
			this.output = o;
			this.result = r;
		}
	}
//	public final class OpenSessionReturn<O, R> extends Tuple {
//		public final O output;
//		public final R result;
//		public OpenSessionReturn(O o, R r) {
//			this.output = o;
//			this.result = r;
//		}
//	}
	
}
