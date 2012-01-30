package org.freedesktop.Secret;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
public interface Prompt extends DBusInterface
{
   public static class Completed extends DBusSignal
   {
      public final boolean dismissed;
      public final Variant result;
      public Completed(String path, boolean dismissed, Variant result) throws DBusException
      {
         super(path, dismissed, result);
         this.dismissed = dismissed;
         this.result = result;
      }
   }

  public void Prompt(String windowId);
  public void Dismiss();

}
