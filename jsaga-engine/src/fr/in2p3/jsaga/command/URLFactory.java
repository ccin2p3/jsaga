package fr.in2p3.jsaga.command;

import fr.in2p3.jsaga.JSagaURL;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   URLFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   10 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class URLFactory {
    public static URL create(String url) throws NotImplemented, BadParameter, NoSuccess {
        if (url.startsWith("file://")) {
            return new URL(JSagaURL.encodePath(url));
        } else if (url.startsWith("srb://") || url.startsWith("irods://")) {
            return new URL(JSagaURL.encodeUrl(url));
        } else {
            // default choice
            return new URL(JSagaURL.encodeUrl(url));
        }
    }
}
