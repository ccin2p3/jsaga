package fr.in2p3.jsaga.adaptor.job.local;

import java.io.*;

public class LocalJobProcess extends Process {
    private String m_message;

    public LocalJobProcess(String message) {
        if (message != null) {
            m_message = message;
        } else {
            m_message = "Unknown cause";
        }
    }

	@Override
	public OutputStream getOutputStream() {
		return new DevNullOutputStream();
	}

	@Override
	public InputStream getInputStream() {
        return new ByteArrayInputStream("".getBytes());
	}

	@Override
	public InputStream getErrorStream() {
		return new ByteArrayInputStream(m_message.getBytes());
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
