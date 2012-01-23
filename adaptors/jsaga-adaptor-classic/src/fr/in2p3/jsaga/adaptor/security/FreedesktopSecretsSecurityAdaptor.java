package fr.in2p3.jsaga.adaptor.security;

import java.util.Map;

import org.apache.log4j.Logger;
import org.freedesktop.DBus.Introspectable;
import org.freedesktop.DBus.Properties;
import org.freedesktop.Secret.Item;
import org.freedesktop.Secret.Pair;
import org.freedesktop.Secret.Service;
import org.freedesktop.Secret.Secret;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserPassStoreSecurityAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   2 mai 2011
* ***************************************************/

public class FreedesktopSecretsSecurityAdaptor implements SecurityAdaptor {

	protected static final String BUS_NAME = "org.freedesktop.secrets";
	protected static final String ITEM_INTERFACE_NAME = "org.freedesktop.Secret.Item";
	protected static final String ROOT_OBJECT_PATH = "/org/freedesktop/secrets";
	protected static final String COLLECTION_OBJECT_PATH = ROOT_OBJECT_PATH + "/collection";
	protected static final String COLLECTION = "Collection";
	protected static final String ID = "Id";

	public Class getSecurityCredentialClass() {
        return UserPassSecurityCredential.class;
	}
	
	public String getType() {
		return "Freedesktop";
	}

	public Usage getUsage() {
		// TODO: Id obligatoire
    	return new UAnd(
   			 new Usage[]{
   					 new UOptional(COLLECTION),
   			 }
   			 );
	}

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
       		 new Default(COLLECTION, "login"),
        };
	}


	public SecurityCredential createSecurityCredential(int usage,
			Map attributes, String contextId) throws IncorrectStateException,
			TimeoutException, NoSuccessException {
		// TODO export this
		System.setProperty("java.library.path", "/home/schwarz/usr/local/lib/libmatthew/lib/jni/");
		DBusConnection conn;
		try {
			conn = DBusConnection.getConnection(DBusConnection.SESSION);

			String objectPath;
			Introspectable in;
			Properties prop;
			DBusInterface dbusSession=null;
			
			objectPath = ROOT_OBJECT_PATH;
			in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
			Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug(in.Introspect());

			
			prop = (Properties) conn.getRemoteObject(BUS_NAME, objectPath, Properties.class);
			
			Service serv = (Service) conn.getRemoteObject(BUS_NAME, objectPath, Service.class);
			// TODO: plain ?
			Pair<org.freedesktop.dbus.Variant,DBusInterface> osr = serv.OpenSession("plain", new org.freedesktop.dbus.Variant(""));
			dbusSession = osr.b;
			
			String id = (String) attributes.get(ID);
			objectPath = COLLECTION_OBJECT_PATH + "/" + (String) attributes.get(COLLECTION) + "/" + id;
			Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("ObjectPath="+objectPath);
			in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
			Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug(in.Introspect());

			prop = (Properties) conn.getRemoteObject(BUS_NAME, objectPath, Properties.class);
			Item inter = (Item) conn.getRemoteObject(BUS_NAME, objectPath, Item.class);
			Secret s = inter.GetSecret(dbusSession);
			byte[] pw = new byte[s.value.size()];
			for (int i=0; i<pw.length; i++) {
				pw[i] = s.value.get(i);
			}
			String password = new String(pw);
			String label = (String) prop.Get(ITEM_INTERFACE_NAME, "Label");
			return new UserPassSecurityCredential(id,password);
		} catch (DBusException e) {
			e.printStackTrace();
			throw new NoSuccessException(e);
		}
	}

}
