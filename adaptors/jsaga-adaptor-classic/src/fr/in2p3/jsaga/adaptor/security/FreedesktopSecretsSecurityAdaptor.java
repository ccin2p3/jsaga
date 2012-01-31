package fr.in2p3.jsaga.adaptor.security;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.freedesktop.DBus;
import org.freedesktop.DBus.Introspectable;
import org.freedesktop.DBus.Properties;
import org.freedesktop.Secret.Collection;
import org.freedesktop.Secret.Item;
import org.freedesktop.Secret.Pair;
import org.freedesktop.Secret.Service;
import org.freedesktop.Secret.Secret;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.ogf.saga.context.Context;
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
   					 new U(COLLECTION),
   					 new UOptional(ID),
   					 new UOptional(LABEL),
   					 new U(Context.USERID),
   			 }
   			 );
	}

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
       		 new Default(Context.USERID, System.getProperty("user.name")),
        };
	}


	public SecurityCredential createSecurityCredential(int usage,
			Map attributes, String contextId) throws IncorrectStateException, TimeoutException, NoSuccessException {
		DBusConnection conn;
		try {
		    //set sys_paths to null: java.library.path will be re-read by JVM before classloader run
		    final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
		    sysPathsField.setAccessible(true);
		    sysPathsField.set(null, null);
		    
		    try {
		    	conn = DBusConnection.getConnection(DBusConnection.SESSION);
		    } catch (UnsatisfiedLinkError ule) {
		    	throw new NoSuccessException(ule.getMessage() + "; check that java.library.path points to the location /path/to/libmatthew/lib/jni");
		    }

			String objectPath;
			Introspectable in;
			Properties prop;
			DBusInterface dbusSession=null;
			String id;
			String label;
			Secret secret;
			
			objectPath = ROOT_OBJECT_PATH;
			
			in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
			try {
				Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug(in.Introspect());
			} catch (DBus.Error.ServiceUnknown su) {
				throw new NoSuccessException("Your Gnome keyring or KDE KWallet should be installed and running");
			}
			
			Service serv = (Service) conn.getRemoteObject(BUS_NAME, objectPath, Service.class);
			// TODO: encrypted ?
			Pair<org.freedesktop.dbus.Variant,DBusInterface> osr = serv.OpenSession("plain", new org.freedesktop.dbus.Variant(""));
			dbusSession = osr.b;

			// First test if the collection exists
			objectPath = COLLECTION_OBJECT_PATH + "/" + (String) attributes.get(COLLECTION);
			Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("ObjectPath="+objectPath);
			in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
			try {
				Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug(in.Introspect());
			} catch (DBusExecutionException dbee) {
				if (dbee.getType().equals(org.freedesktop.Secret.Error.NoSuchObject.class.getCanonicalName())) {
					throw new NoSuccessException("The collection '" + (String) attributes.get(COLLECTION) + "' does not exist");
				}				
			}

			// Search item in collection
			if (attributes.containsKey(ID)) {
				id = (String) attributes.get(ID);
				objectPath = objectPath + "/" + id;
				Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("ObjectPath="+objectPath);
				in = (Introspectable) conn.getRemoteObject(BUS_NAME, objectPath, Introspectable.class);
				Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug(in.Introspect());
	
				prop = (Properties) conn.getRemoteObject(BUS_NAME, objectPath, Properties.class);
				Item inter = (Item) conn.getRemoteObject(BUS_NAME, objectPath, Item.class);
				try {
					secret = inter.GetSecret(dbusSession);
				} catch (DBusExecutionException dbee) {
					if (dbee.getType().equals(org.freedesktop.Secret.Error.IsLocked.class.getCanonicalName())) {
						throw new NoSuccessException("The item is locked. Please unlock before using it.");
					} else {
						throw new NoSuccessException(dbee);
					}
				}
			} else { // Search by Name: get all secrets from collection
	
				Collection collection = (Collection) conn.getRemoteObject(BUS_NAME, objectPath, Collection.class);
				
				HashMap searchprop = new HashMap();
				// Get all Attribute whose name starts with "Attribute-" and use for searching
				Iterator it = attributes.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry attr = (Entry) it.next();
					if(attr.getKey().toString().startsWith("Attribute-")) {
						String key = attr.getKey().toString().substring("Attribute-".length());
						searchprop.put(key, (String)attr.getValue());
					}
				}
				
				// Get matching items
				Pair<List<DBusInterface>,List<DBusInterface>> itemList = collection.SearchItems(searchprop);
				List<DBusInterface> unlockedItems = itemList.a;
				List<DBusInterface> lockedItems = itemList.b;
				if (lockedItems.size()+unlockedItems.size() == 0) {
					throw new NoSuccessException("The collection is empty");
				}
				secret = null;

				// iterate on unlocked items
				for (int i=0; i<unlockedItems.size(); i++) {
					prop = (Properties)unlockedItems.get(i);
					// if JSAGA attribute Label exists, check that it matches
					if (attributes.containsKey(LABEL)) {
						label = (String) attributes.get(LABEL);
						String foundLabel = (String) prop.Get(ITEM_INTERFACE_NAME, LABEL);
						Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("found unlocked: " + foundLabel);
						if (foundLabel.equals(label)) {
							// objects sent by SearchItems do not implement Item, but Properties and Introspectable only
							// so we cannot use directly the GetSecret method
							// instead we get the objectPath as String and get the remote object that implements Item
							secret = getItem(conn,prop).GetSecret(dbusSession);
							break;
						}
					} else {
						secret = getItem(conn,prop).GetSecret(dbusSession);
						break;
					}
				}
				if (secret==null) {
					// check if password is locked
					for (int i=0; i<lockedItems.size(); i++) {
						if (attributes.containsKey(LABEL)) {
							label = (String) attributes.get(LABEL);
							prop = (Properties)lockedItems.get(i);
							String foundLabel = (String) prop.Get(ITEM_INTERFACE_NAME, LABEL);
							Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug("found locked: " + foundLabel);
							if (foundLabel.equals(label)) {
								throw new NoSuccessException("The item is locked. Please unlock before using it.");
							}
						}
					}
					throw new NoSuccessException("No matching passwords");
				}
			}
			byte[] pw = new byte[secret.value.size()];
			for (int i=0; i<pw.length; i++) {
				pw[i] = secret.value.get(i);
			}
			String password = new String(pw);
			return new UserPassSecurityCredential((String) attributes.get(Context.USERID),password);
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

	// get the Object path from toString() !!! because there is not getObjectPath() method...
	// toString = ":busadress+":"+objectpath+":"+iface"
	private Item getItem(DBusConnection c , Properties p) throws DBusException {
		return (Item) c.getRemoteObject(BUS_NAME, p.toString().split(":")[2], Item.class);
	}
	
	
	// not working
	// FIXME: unlock item: error NotConnected
//	objectPath = prop.toString().split(":")[2];
//	List<DBusInterface> itemsToUnlock = new ArrayList<DBusInterface>(1);
//	itemsToUnlock.add(getItem(conn,prop));
//	Pair<List<DBusInterface>,DBusInterface> usr = serv.Unlock(itemsToUnlock);
	// FIXME: unlock all items: error NotConnected
//	Pair<List<DBusInterface>,DBusInterface> usr = serv.Unlock(lockedItems);
	// FIXME: unlock collection: error NotConnected
//	List<DBusInterface> collToUnlock = new ArrayList<DBusInterface>(1);
//	collToUnlock.add(collection);
	// FIXME: Wrong return type: failed to create proxy object for / exported by 1.5
//	Pair<List<DBusInterface>,DBusInterface> usr = serv.Unlock(new ArrayList<DBusInterface>());

//	unlockedItems = usr.a;
//	Logger.getLogger(FreedesktopSecretsSecurityAdaptor.class).debug(unlockedItems.size());

}
