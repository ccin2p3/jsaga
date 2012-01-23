package org.freedesktop.Secret;
import java.util.List;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;
public final class Secret extends Struct
{
   @Position(0)
   public final DBusInterface a;
   @Position(1)
   public final List<Byte> b;
   @Position(2)
   public final List<Byte> c;
  public Secret(DBusInterface a, List<Byte> b, List<Byte> c)
  {
   this.a = a;
   this.b = b;
   this.c = c;
  }
}
