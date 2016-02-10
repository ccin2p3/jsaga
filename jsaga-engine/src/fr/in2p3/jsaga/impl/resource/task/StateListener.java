package fr.in2p3.jsaga.impl.resource.task;

import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public interface StateListener {
    public void startListening(String nativeResourceId, ResourceMonitorCallback callback) throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException;
    public void stopListening(String nativeResourceId) throws NotImplementedException, TimeoutException, NoSuccessException;
}
