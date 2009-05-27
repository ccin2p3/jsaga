package fr.in2p3.jsaga.impl.job.staging;

import java.io.OutputStream;
import java.io.PrintStream;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   UnixPrintStream
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   26 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class UnixPrintStream extends PrintStream {
    public UnixPrintStream(OutputStream outputStream) {
        super(outputStream);
    }

    public void println() {
        newLine();
    }

    public synchronized void println(boolean flag) {
        print(flag);
        newLine();
    }

    public synchronized void println(char c) {
        print(c);
        newLine();
    }

    public synchronized void println(int i) {
        print(i);
        newLine();
    }

    public synchronized void println(long l) {
        print(l);
        newLine();
    }

    public synchronized void println(float f) {
        print(f);
        newLine();
    }

    public synchronized void println(double d) {
        print(d);
        newLine();
    }

    public synchronized void println(char ac[]) {
        print(ac);
        newLine();
    }

    public synchronized void println(String s) {
        print(s);
        newLine();
    }

    public synchronized void println(Object obj) {
        print(obj);
        newLine();
    }

    private void newLine() {
        super.print('\n');
    }
}
