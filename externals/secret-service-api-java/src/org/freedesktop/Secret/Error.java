package org.freedesktop.Secret;

import org.freedesktop.dbus.exceptions.DBusExecutionException;

public interface Error {
    /**
     * Thrown if a message if item is locked
     */
    @SuppressWarnings("serial")
    public class IsLocked extends DBusExecutionException
    {
       public IsLocked(String message)
       {
          super(message);
       }
    }

    /**
     * Thrown if a message if the session does not exist
     */
    @SuppressWarnings("serial")
    public class NoSession extends DBusExecutionException
    {
       public NoSession(String message)
       {
          super(message);
       }
    }

    /**
     * Thrown if no such item or collection exists
     */
    @SuppressWarnings("serial")
    public class NoSuchObject extends DBusExecutionException
    {
       public NoSuchObject(String message)
       {
          super(message);
       }
    }

}
