package fr.in2p3.jsaga.adaptor.job.local;

import java.io.InputStream;
import java.io.OutputStream;

public class LocalJobProcess extends Process {

	@Override
	public OutputStream getOutputStream() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public InputStream getErrorStream() {
		return null;
	}

	@Override
	public int waitFor() throws InterruptedException {
		return 0;
	}

	@Override
	public int exitValue() {
		return 2;
	}

	@Override
	public void destroy() {
	}

}
