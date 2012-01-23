package org.freedesktop.Secret;
import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
public interface Service extends DBusInterface
{
   public static class CollectionCreated extends DBusSignal
   {
      public final DBusInterface collection;
      public CollectionCreated(String path, DBusInterface collection) throws DBusException
      {
         super(path, collection);
         this.collection = collection;
      }
   }
   public static class CollectionDeleted extends DBusSignal
   {
      public final DBusInterface collection;
      public CollectionDeleted(String path, DBusInterface collection) throws DBusException
      {
         super(path, collection);
         this.collection = collection;
      }
   }
   public static class CollectionChanged extends DBusSignal
   {
      public final DBusInterface collection;
      public CollectionChanged(String path, DBusInterface collection) throws DBusException
      {
         super(path, collection);
         this.collection = collection;
      }
   }

  public Pair<Variant, DBusInterface> OpenSession(String algorithm, Variant input);
  public Pair<DBusInterface, DBusInterface> CreateCollection(Map<String,Variant> props);
  public Pair<List<DBusInterface>, List<DBusInterface>> SearchItems(Map<String,String> attributes);
  public Pair<List<DBusInterface>, DBusInterface> Unlock(List<DBusInterface> objects);
  public Pair<List<DBusInterface>, DBusInterface> Lock(List<DBusInterface> objects);
  public Map<DBusInterface,Secret> GetSecrets(List<DBusInterface> items, DBusInterface session);
  public DBusInterface ReadAlias(String name);
  public void SetAlias(String name, DBusInterface collection);

}
