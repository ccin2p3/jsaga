package fr.in2p3.jsaga.engine.factories;

import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.monitoring.MonitoringFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MonitoringFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MonitoringFactoryImpl extends MonitoringFactory {
    protected Metric doCreateMetric(String name, String desc, String mode, String unit, String type, String value) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        return null;  //todo: Implement method doCreateMetric()
    }
}
