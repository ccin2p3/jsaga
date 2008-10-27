package fr.in2p3.jsaga.impl.monitoring;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MetricFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MetricFactoryImpl<E> {
    private AbstractMonitorableImpl m_monitorable;

    public MetricFactoryImpl(AbstractMonitorableImpl monitorable) {
        m_monitorable = monitorable;
    }

    public MetricImpl<E> createAndRegister(String name, String desc, MetricMode mode, String unit, MetricType type, E initialValue) {
        MetricImpl<E> metric = new MetricImpl<E>(m_monitorable, name, desc, mode, unit, type, initialValue);
        m_monitorable._addMetric(name, metric);
        return metric;
    }
}
