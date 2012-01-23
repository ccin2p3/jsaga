package org.freedesktop.Secret;
import org.freedesktop.dbus.DBusInterface;
public interface Item extends DBusInterface
{

  public DBusInterface Delete();
  public Secret GetSecret(DBusInterface session);
  public void SetSecret(Struct2 secret);

}
