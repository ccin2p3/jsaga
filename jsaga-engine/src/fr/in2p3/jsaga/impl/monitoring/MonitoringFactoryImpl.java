package fr.in2p3.jsaga.impl.monitoring;

import org.ogf.saga.error.*;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.monitoring.MonitoringFactory;

import java.lang.Exception;

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
    protected Metric doCreateMetric(String name, String desc, String mode, String unit, String type, String value) throws NotImplemented, BadParameter, Timeout, NoSuccess {
        Metric metric = new MetricImpl(name, desc, Mode.valueOf(mode), unit, type);
        if (value != null) {
            try {
                metric.setAttribute("Value", value);
            } catch (Exception e) {
                throw new NoSuccess(e);
            }
        }
        return metric;
    }
}
