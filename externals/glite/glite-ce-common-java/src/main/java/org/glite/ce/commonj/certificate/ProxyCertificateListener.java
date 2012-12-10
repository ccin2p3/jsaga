/*
 * Copyright (c) 2004 on behalf of the EU EGEE Project:
 * The European Organization for Nuclear Research (CERN),
 * Istituto Nazionale di Fisica Nucleare (INFN), Italy
 * Datamat Spa, Italy
 * Centre National de la Recherche Scientifique (CNRS), France
 * CS Systeme d'Information (CSSI), France
 * Royal Institute of Technology, Center for Parallel Computers (KTH-PDC), Sweden
 * Universiteit van Amsterdam (UvA), Netherlands
 * University of Helsinki (UH.HIP), Finland
 * University of Bergen (UiB), Norway
 * Council for the Central Laboratory of the Research Councils (CCLRC), United Kingdom
 * 
 * Authors: Luigi Zangrando <zangrando@pd.infn.it>
 *
 * Version info: $Id: ProxyCertificateListener.java,v 1.1 2008/06/06 12:49:26 zangran Exp $
 */

package org.glite.ce.commonj.certificate;

public interface ProxyCertificateListener {
     public void proxyCertificateAdded(ProxyCertificate proxyCert);
     public void proxyCertificateUpdated(ProxyCertificate proxyCert);
     public void proxyCertificateRemoved(ProxyCertificate proxyCert);
}
