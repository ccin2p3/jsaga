package org.glite.ce.creamapi.cmdmanagement.queue;

import org.glite.ce.creamapi.cmdmanagement.Command;

public interface CommandQueueInterface {
       
       public void enqueueCommand(Command cmd) throws CommandQueueException, IllegalArgumentException;
       public void dequeueCommand(String cmdId) throws CommandQueueException, IllegalArgumentException;
       public Command fetchCommand() throws CommandQueueException, IllegalArgumentException;
       public void setFetchCommandTimeout(long mtTimeout);

       public void recoverQueue() throws CommandQueueException, IllegalArgumentException;

       public String getName();
       public boolean isOpen();
       public void open();
       public void close();
       public void destroy();
       
}
