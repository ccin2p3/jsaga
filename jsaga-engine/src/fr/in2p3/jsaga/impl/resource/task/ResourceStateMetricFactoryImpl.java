package fr.in2p3.jsaga.impl.resource.task;

import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ResourceStateMetricFactoryImpl<E> {
    private AbstractResourceTaskImpl m_monitorable;

    public ResourceStateMetricFactoryImpl(AbstractResourceTaskImpl monitorable) {
        m_monitorable = monitorable;
    }

    public ResourceStateMetricImpl<E> createAndRegister(String name, String desc, MetricMode mode, String unit, MetricType type, E initialValue) {
        ResourceStateMetricImpl<E> metric = new ResourceStateMetricImpl<E>(m_monitorable, name, desc, mode, unit, type, initialValue);
        m_monitorable._addMetric(name, metric);
        return metric;
    }
}
