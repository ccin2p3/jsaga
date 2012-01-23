package org.freedesktop.Secret;
import java.util.List;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;
public final class Secret extends Struct
{
   @Position(0)
   public final DBusInterface session;
   @Position(1)
   public final List<Byte> parameters;
   @Position(2)
   public final List<Byte> value;
  public Secret(DBusInterface a, List<Byte> b, List<Byte> c)
  {
   this.session = a;
   this.parameters = b;
   this.value = c;
  }
}
