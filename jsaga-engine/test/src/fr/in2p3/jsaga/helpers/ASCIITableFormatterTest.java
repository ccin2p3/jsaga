package fr.in2p3.jsaga.helpers;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ASCIITableFormatterTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   3 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ASCIITableFormatterTest extends Assert {
    @Test
    public void test_dump() throws Exception {
        ASCIITableFormatter formatter = new ASCIITableFormatter(new String[] {
                "first_column", "col2", "col3",});
        formatter.append(new String[] {
                "data", "long data content"});
        formatter.append(new String[] {
                "d", "another data", null});
        formatter.append(new String[] {
                "last", null, "finish"});

        ByteArrayOutputStream ref = new ByteArrayOutputStream();
        PrintStream r = new PrintStream(ref);
        r.println("first_column |       col2        |  col3 ");
        r.println("-------------+-------------------+-------");
        r.println("data         | long data content |       ");
        r.println("d            | another data      |       ");
        r.println("last         |                   | finish");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        formatter.dump(out);
        assertEquals(ref.toString(), out.toString());
    }
}
