/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.tools.wsdl;

import org.apache.axis.utils.CLOption;
import org.apache.axis.utils.CLOptionDescriptor;
import org.apache.axis.wsdl.toJava.Emitter;
import org.apache.axis.wsdl.gen.Parser;

public class WSDL2Java 
    extends org.apache.axis.message.addressing.tools.wsdl.WSDL2Java {

    /** Field type collision protection disable */
    protected static final int DISABLE_TYPE_COLLISION_OPT = 'l';

    /** Field emitter */
    private Emitter emitter;
    
    protected static final CLOptionDescriptor[] options =
        new CLOptionDescriptor[]{
            new CLOptionDescriptor("noTypeCollisionProtection",
                                   CLOptionDescriptor.ARGUMENT_DISALLOWED,
                                   DISABLE_TYPE_COLLISION_OPT,
                                   "disable type collision protection")
        };

    protected WSDL2Java() {
        addOptions(options);
    }

    protected Parser createParser() {
        this.emitter = (Emitter)super.createParser();
        return this.emitter;
    }

    protected void parseOption(CLOption option) {
        switch (option.getId()) {
        case DISABLE_TYPE_COLLISION_OPT:
            emitter.setTypeCollisionProtection(false);
            break;
        default:
            super.parseOption(option);
        }
    }

    public static void main(String[] args) {
        WSDL2Java gsdl2java = new WSDL2Java();
        gsdl2java.run(args);
    }
    
}
