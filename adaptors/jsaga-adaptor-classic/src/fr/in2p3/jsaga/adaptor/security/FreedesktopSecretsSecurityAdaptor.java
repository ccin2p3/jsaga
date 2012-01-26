package fr.in2p3.jsaga.adaptor.security;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freedesktop.DBus.Introspectable;
import org.freedesktop.DBus.Properties;
import org.freedesktop.Secret.Collection;
import org.freedesktop.Secret.Item;
import org.freedesktop.Secret.Pair;
import org.freedesktop.Secret.Service;
import org.freedesktop.Secret.Secret;
import org.freedesktop.dbus.DBusAsyncReply;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FreedesktopSecretsSecurityAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   24 jan 2012
* ***************************************************/

public class FreedesktopSecretsSecurityAdaptor implements SecurityAdaptor {

	protected static final String BUS_NAME = "org.freedesktop.secrets";
	protected static final String ITEM_INTERFACE_NAME = "org.freedesktop.Secret.Item";
	protected static final String ROOT_OBJECT_PATH = "/org/freedesktop/secrets";
	protected static final String COLLECTION_OBJECT_PATH = ROOT_OBJECT_PATH + "/collection";
	protected static final String COLLECTION = "Collection";
	protected static final String ID = "Id";
	protected static final String LABEL = "Label";

	public Class getSecurityCredentialClass() {
        return UserPassSecurityCredential.class;
	}
	
	public String getType() {
		return "Freedesktop";
	}

	public Usage getUsage() {
    	return new UAnd(
   			 new Usage[]{
   					 new UOptional(COLLECTION),
   					 new UOptional(ID),
   					 new UOptional(LABEL),
   			 }
   			 );
	}

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
       		 new Default(COLLECTION, "login"),
       		 new Default(LABEL, System.getProperty("user.name")),
        };
	}


	public SecurityCredential createSecurityCredential(int usage,
			Map attributes, String contextId) throws IncorrectStateException,
			TimeoutException, NoSuccessException {
		DBusConnection conn;
		try {
		    //set sys_paths to null: java.library.path will be re-read by JVM before classloader run
		    final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
		    sysPathsField.setAccessible(true);
		    sysPathsField.set(null, null);
		    
			conn = DBusConnection.getConnection(DBusConnection.SESSION);

			String objectPath;
			Introspectable in;
			Properties prop;
			DBusInterface dbusSession=null;
			String id;
			String label;
			Secret secret;
			
			objectPath = ROOT_OBJECT_PATH;
			in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
			Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug(in.Introspect());

			
			prop = (Properties) conn.getRemoteObject(BUS_NAME, objectPath, Properties.class);
			
			Service serv = (Service) conn.getRemoteObject(BUS_NAME, objectPath, Service.class);
			// TODO: encrypted ?
			Pair<org.freedesktop.dbus.Variant,DBusInterface> osr = serv.OpenSession("plain", new org.freedesktop.dbus.Variant(""));
			dbusSession = osr.b;
			
			if (attributes.containsKey(ID)) {
				id = (String) attributes.get(ID);
				objectPath = COLLECTION_OBJECT_PATH + "/" + (String) attributes.get(COLLECTION) + "/" + id;
				Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("ObjectPath="+objectPath);
				in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
				Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug(in.Introspect());
	
				prop = (Properties) conn.getRemoteObject(BUS_NAME, objectPath, Properties.class);
				label = (String) prop.Get(ITEM_INTERFACE_NAME, LABEL);
				Item inter = (Item) conn.getRemoteObject(BUS_NAME, objectPath, Item.class);
				secret = inter.GetSecret(dbusSession);
			} else { // Search by Name: get all secrets from collection
				objectPath = COLLECTION_OBJECT_PATH + "/" + (String) attributes.get(COLLECTION);
				Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("ObjectPath="+objectPath);
				in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
				Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug(in.Introspect());
	
				Collection collection = (Collection) conn.getRemoteObject(BUS_NAME, objectPath, Collection.class);
				
				HashMap searchprop = new HashMap();
				label = (String) attributes.get(LABEL);
				// FIXME: return no credentials
//				searchprop.put(LABEL, label);
				Pair<List<DBusInterface>,List<DBusInterface>> itemList = collection.SearchItems(searchprop);
				List<DBusInterface> unlockedItems = itemList.a;
				List<DBusInterface> lockedItems = itemList.b;
//				Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("nbLocked=" + lockedItems.size());
//				Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("nbUnLocked=" + unlockedItems.size());
				if (lockedItems.size()+unlockedItems.size() == 0) {
					throw new NoSuccessException("No credential matching Label '" + label + "'");
				}
				if (unlockedItems.size() == 0) {
					throw new NoSuccessException("Matching credentials are locked");
				}
				secret = null;
				// objects sent by SearchItems do not implement Item, but Properties and Introspectable only
				// so we cannot use directly the GetSecret method
				// instead we get the objectPath as String and get the remote object that implements Item
				for (int i=0; i<unlockedItems.size(); i++) {
					prop = (Properties)unlockedItems.get(i);
//					Method[] methods = prop.getClass().getDeclaredMethods();
//					for (int j=0; j<methods.length; j++) {
//						Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("method=" + methods[j].getName());
//					}
					String foundLabel = (String) prop.Get(ITEM_INTERFACE_NAME, LABEL);
					Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("found label: " + foundLabel);
					if (foundLabel.equals(label)) {
						// get the Object path from toString() !!! because there is not getObjectPath() method...
						// toString = ":busadress+":"+objectpath+":"+iface"
						objectPath = prop.toString().split(":")[2];
						Item inter = (Item) conn.getRemoteObject(BUS_NAME, objectPath, Item.class);
						secret = inter.GetSecret(dbusSession);
						break;
					}
				}
				if (secret==null) {
					throw new NoSuccessException("No credential matching Label '" + label + "'");
				}
			}
			byte[] pw = new byte[secret.value.size()];
			for (int i=0; i<pw.length; i++) {
				pw[i] = secret.value.get(i);
			}
			String password = new String(pw);
			return new UserPassSecurityCredential(label,password);
		} catch (DBusException e) {
			throw new NoSuccessException(e);
		} catch (SecurityException e) {
			throw new NoSuccessException(e);
		} catch (NoSuchFieldException e) {
			throw new NoSuccessException(e);
		} catch (IllegalArgumentException e) {
			throw new NoSuccessException(e);
		} catch (IllegalAccessException e) {
			throw new NoSuccessException(e);
		}
	}

}
