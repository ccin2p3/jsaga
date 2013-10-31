package fr.in2p3.jsaga.impl.file.copy;

import java.io.IOException;
import java.io.OutputStream;

import org.ogf.saga.file.FileOutputStream;

public class MonitoredOutputStream extends OutputStream {

	private FileOutputStream out;
	private AbstractCopyTask monitor;
	
	public MonitoredOutputStream(FileOutputStream o, AbstractCopyTask a) {
		this.out = o;
		this.monitor = a;
	}
	
	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte b[]) throws IOException {
		this.out.write(b);
		this.monitor.increment(b.length);
	}
	
	@Override
	public void write(byte b[], int off, int len) throws IOException {
		this.out.write(b, off, len);
		this.monitor.increment(len);
	}
}
