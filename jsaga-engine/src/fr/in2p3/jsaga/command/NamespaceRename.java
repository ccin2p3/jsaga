package fr.in2p3.jsaga.command;

import org.apache.commons.cli.CommandLine;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.session.Session;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NamespaceRename
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NamespaceRename extends NamespaceMove {
    public static void main(String[] args) throws Exception {
        NamespaceRename command = new NamespaceRename();
        CommandLine line = command.parse(args);
        command.execute(line);
    }

    protected void changeBehavior(Session session, URL target) throws Exception {
        try {
            NSFactory.createNSEntry(session, target, Flags.NONE.getValue());
            throw new AlreadyExistsException("Target entry already exists: "+target);
        } catch(DoesNotExistException e) {
            // do nothing
        }
    }
}
