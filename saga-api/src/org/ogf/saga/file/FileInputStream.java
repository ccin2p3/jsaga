package org.ogf.saga.file;

import java.io.InputStream;

import org.ogf.saga.SagaObject;

/**
 * Since Java programmers are used to streams, the Java language bindings of
 * SAGA provide them. In contrast to everything else in the language bindings,
 * this is an abstract class, not an interface, because it is supposed to be
 * a java.io.InputStream (which is a class, not an interface).
 * Implementations should redefine methods of java.io.InputStream.
 * TODO: Should we have tasking versions of these methods???
 */
public abstract class FileInputStream extends InputStream implements SagaObject {

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
