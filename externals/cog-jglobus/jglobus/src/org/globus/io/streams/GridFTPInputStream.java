/*
 * Copyright 1999-2006 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.globus.io.streams;

import java.io.IOException;

import org.globus.ftp.GridFTPClient;
import org.globus.ftp.Session;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.FeatureList;
import org.globus.ftp.exception.FTPException;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.HostAuthorization;

import org.ietf.jgss.GSSCredential;

public class GridFTPInputStream extends FTPInputStream {


    public GridFTPInputStream(GSSCredential cred,
			      String host, 
			      int port, 
			      String file) 
	throws IOException, FTPException {
	this(cred, HostAuthorization.getInstance(),
	     host, port, 
           file, true, Session.TYPE_IMAGE, true, true);
    }

    public GridFTPInputStream(GSSCredential cred,
			      Authorization auth,
			      String host, 
			      int port, 
			      String file,
			      boolean reqDCAU) 
	throws IOException, FTPException {
	this(cred, auth, host, port, 
           file, true, Session.TYPE_IMAGE, reqDCAU, true);
    }

    public GridFTPInputStream(GSSCredential cred,
                            Authorization auth,
                            String host,
                            int port,
                            String file,
                            boolean reqDCAU,
                              boolean useGFD47)
      throws IOException, FTPException {
      this(cred, auth, host, port,
           file, true, Session.TYPE_IMAGE, reqDCAU, useGFD47);
    }

    public GridFTPInputStream(GSSCredential cred,
			      Authorization auth,
			      String host, 
			      int port, 
			      String file,
			      boolean passive,
			      int type,
                            boolean reqDCAU,
                              boolean useGFD47)
	throws IOException, FTPException {
	GridFTPClient gridFtp = new GridFTPClient(host, port);
	gridFtp.setAuthorization(auth);
	gridFtp.authenticate(cred);
	
        this.useGFD47 =
            (useGFD47 && gridFtp.isFeatureSupported(FeatureList.GETPUT));

	if (gridFtp.isFeatureSupported("DCAU")) {
	    if (!reqDCAU) {
		gridFtp.setDataChannelAuthentication(DataChannelAuthentication.NONE);
	    }
	} else {
	    gridFtp.setLocalNoDataChannelAuthentication();
	}

	ftp = gridFtp;
	
	get(passive, type, file);
    }
}
