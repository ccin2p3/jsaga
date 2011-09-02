package fr.in2p3.jsaga.adaptor.unicore.job;

import java.util.Map;

import org.ogf.saga.error.IncorrectStateException;

import de.fzj.unicore.uas.client.TSSClient;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.unicore.UnicoreAbstract;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreJobAdaptorAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   01/09/2011
* ***************************************************/

public abstract class UnicoreJobAdaptorAbstract extends UnicoreAbstract {

	protected TSSClient m_client;
	
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default(TARGET, "DEMO-SITE"),
    			new Default(SERVICE_NAME, "TargetSystemFactoryService"), 
    			new Default(RES, "default_target_system_factory"),
    			};
    }


}
