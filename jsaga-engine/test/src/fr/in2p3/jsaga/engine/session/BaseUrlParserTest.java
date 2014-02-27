package fr.in2p3.jsaga.engine.session;

import org.junit.Assert;
import org.junit.Test;

import fr.in2p3.jsaga.generated.parser.*;
import junit.framework.TestCase;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   BaseUrlParserTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class BaseUrlParserTest {
    @Test
    public void test_success() throws ParseException {
        final String[] SUCCESS = {
                "gridftp->gsiftp://cc*.*in2p3.fr/*/dteam",
                "gridftp->gsiftp://cc*.*in2p3.fr[:2811]/*/dteam",
                "gridftp->gsiftp://*:1234"
        };
        for (String url : SUCCESS) {
            String result = new BaseUrlPattern(BaseUrlParser.parse(url)).toString();
            Assert.assertEquals(url, result);
        }
    }

    @Test
    public void test_failure() {
        final String[] FAILURE = {
                "gridftp>gsiftp://cc*.*in2p3.fr/*/dteam",
                "gridftp->gsiftp://cc*.*in2p3.fr/*/dt*am",
                "gridftp->gsiftp://:1234"
        };
        for (String url : FAILURE) {
            try {
                BaseUrlParser.parse(url);
                Assert.fail("Expected exception: "+ParseException.class);
            } catch (TokenMgrError e) {
                // success
            } catch (ParseException e) {
                // success
            }
        }
    }
}
