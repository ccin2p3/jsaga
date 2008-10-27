package fr.in2p3.jsaga.impl.task;

import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TaskStateMetricFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TaskStateMetricFactoryImpl<E> {
    private AbstractTaskImpl m_monitorable;

    public TaskStateMetricFactoryImpl(AbstractTaskImpl monitorable) {
        m_monitorable = monitorable;
    }

    public TaskStateMetricImpl<E> createAndRegister(String name, String desc, MetricMode mode, String unit, MetricType type, E initialValue) {
        TaskStateMetricImpl<E> metric = new TaskStateMetricImpl<E>(m_monitorable, name, desc, mode, unit, type, initialValue);
        m_monitorable._addMetric(name, metric);
        return metric;
    }
}
