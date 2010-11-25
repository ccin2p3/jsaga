package fr.in2p3.jsaga.impl.context;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.impl.job.service.JobServiceImpl;

import java.util.Set;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobServiceReset
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   22 janv. 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class JobServiceReset implements Runnable {
    private Set<JobServiceImpl> m_jobServices;
    private SecurityCredential m_credential;

    public JobServiceReset(Set<JobServiceImpl> registry, SecurityCredential credential) {
        m_jobServices = registry;
        m_credential = credential;
    }

    public void run() {
        for (JobServiceImpl jobService : m_jobServices) {
            jobService.resetAdaptors(m_credential);
        }
    }
}
