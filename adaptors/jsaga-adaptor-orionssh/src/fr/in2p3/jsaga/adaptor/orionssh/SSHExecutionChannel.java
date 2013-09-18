package fr.in2p3.jsaga.adaptor.orionssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   16 juillet 2013
* ***************************************************/

public class SSHExecutionChannel {

	private Session m_session;

	public SSHExecutionChannel(Connection conn) throws IOException {
		m_session = conn.openSession();
	}
	
	public void execute(String command) throws IOException, InterruptedException {
		this.execute(command, 0);
	}
	
	public void execute(String command, int pause) throws IOException, InterruptedException {
		m_session.execCommand(command);
		if (pause > 0) 
			Thread.sleep(pause);
	}
	
	public Integer getExitStatus() {
		return m_session.getExitStatus();
	}
	
	public String getOutput() throws IOException {
		StringBuilder sb = new StringBuilder();
		InputStream stdout = new StreamGobbler(m_session.getStdout());
		BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
		while (true)
		{
			String line = br.readLine();
			if (line == null)
				break;
			sb.append(line);
		}
		return sb.toString();
	}
	
	public void close() {
		m_session.close();
	}

	public boolean isClosed() {
		try {
			m_session.ping();
		} catch (IOException e) {
			return true;
		}
		return false;
	}
	
	protected void finalize() throws Throwable {
		try {
			close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			super.finalize();
		}
	}
}
