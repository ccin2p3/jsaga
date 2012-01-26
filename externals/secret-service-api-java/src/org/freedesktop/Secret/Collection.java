package org.freedesktop.Secret;
import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
public interface Collection extends DBusInterface
{
   public static class ItemCreated extends DBusSignal
   {
      public final DBusInterface item;
      public ItemCreated(String path, DBusInterface item) throws DBusException
      {
         super(path, item);
         this.item = item;
      }
   }
   public static class ItemDeleted extends DBusSignal
   {
      public final DBusInterface item;
      public ItemDeleted(String path, DBusInterface item) throws DBusException
      {
         super(path, item);
         this.item = item;
      }
   }
   public static class ItemChanged extends DBusSignal
   {
      public final DBusInterface item;
      public ItemChanged(String path, DBusInterface item) throws DBusException
      {
         super(path, item);
         this.item = item;
      }
   }

  public DBusInterface Delete();
  public Pair<List<DBusInterface>, List<DBusInterface>> SearchItems(Map<String,String> attributes);
  public Pair<DBusInterface, DBusInterface> CreateItem(Map<String,Variant> props, Secret secret, boolean replace);

}
