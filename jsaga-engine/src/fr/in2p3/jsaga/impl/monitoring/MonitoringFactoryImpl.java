package fr.in2p3.jsaga.impl.monitoring;

import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.monitoring.MonitoringFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MonitoringFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MonitoringFactoryImpl extends MonitoringFactory {
    protected Metric doCreateMetric(String name, String desc, String mode, String unit, String type, String value) throws NotImplementedException, BadParameterException, TimeoutException, NoSuccessException {
        return new MetricImpl(null, name, desc, MetricMode.valueOf(mode), unit, MetricType.valueOf(type), value);
    }
}
