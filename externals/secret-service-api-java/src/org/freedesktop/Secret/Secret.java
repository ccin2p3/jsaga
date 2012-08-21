package org.freedesktop.Secret;
import java.util.List;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.Struct;
public final class Secret extends Struct
{
   @Position(0)
   public final DBusInterface session; // The session that was used to encode the secret.
   @Position(1)
   public final List<Byte> parameters; // Algorithm dependent parameters for secret value encoding.
   @Position(2)
   public final List<Byte> value; // Possibly encoded secret value
   @Position(3)
   public final String content_type;
  public Secret(DBusInterface a, List<Byte> b, List<Byte> c, String d) // The content type of the secret. For example: 'text/plain; charset=utf8'
  {
   this.session = a;
   this.parameters = b;
   this.value = c;
   this.content_type = d;
  }
}
