package org.glite.security.authz;


/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://public.eu-egee.org/partners/ for details on the copyright
 * holders.For license conditions see the license file or http://www.eu-egee.org/license.html
 *
 */
/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 * Modified and redistributed under the terms of the Apache Public
 * License, found at http://www.apache.org/licenses/LICENSE-2.0
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;


public class VomsPDPPolicy implements Serializable {
    private ArrayList attrs = null;

    public VomsPDPPolicy(String[] _attrs) {
        this.attrs = new ArrayList();

        if (_attrs != null) {
            for (int i = 0; i < _attrs.length; i++) {
                this.attrs.add(_attrs[i]);
            }
        }
    }

    public Iterator getAttrs() {
        return this.attrs.iterator();
    }

    /**
     * @param attr The attribute to add to policy.
     * @return true if policy was altered.
     */
    public boolean addAttr(String attr) {
        if (!this.attrs.contains(attr)) {
            this.attrs.add(attr);

            return true;
        }

        return false;
    }

    /**
     * @param attr The attribute to remove from policy.
     * @return true if policy was altered.
     */
    public boolean removeAttr(String attr) {
        return this.attrs.remove(attr);
    }

    /**
     * Delete all entries.
     */
    public void clearPolicy() {
        this.attrs.clear();
    }

    public void setAttrs(ArrayList attrs) {
        this.attrs = attrs;
    }

    protected void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
        this.attrs = (ArrayList) ois.readObject();
    }

    protected void writeObject(ObjectOutputStream oos)
        throws IOException {
        oos.writeObject(this.attrs);
    }
}
