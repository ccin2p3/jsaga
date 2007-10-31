package org.ogf.saga.rpc;

import org.ogf.saga.buffer.Buffer;

/**
 * Extends the {@link Buffer} interface with methods to set/get the modus
 * of RPC parameters.
 */
public interface Parameter extends Buffer {

    /**
     * Sets the io mode.
     * @param mode the value for io mode.
     */
    public void setIOMode(IOMode mode);

    /**
     * Retrieves the current value for io mode.
     * @return the value of io mode.
     */
    public IOMode getIOMode();
}
