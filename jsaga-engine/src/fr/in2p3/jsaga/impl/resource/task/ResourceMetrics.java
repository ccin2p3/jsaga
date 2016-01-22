package fr.in2p3.jsaga.impl.resource.task;

import fr.in2p3.jsaga.impl.monitoring.MetricImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.resource.task.ResourceTask;
import org.ogf.saga.task.State;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ResourceMetrics implements Cloneable {
    MetricImpl<State> m_State;
    MetricImpl<String> m_StateDetail;

    /** constructor */
    ResourceMetrics(AbstractResourceTaskImpl resource) {
        m_State = new ResourceStateMetricFactoryImpl<State>(resource).createAndRegister(
                ResourceTask.RESOURCE_STATE,
                "fires on state changes of the resource, and has the literal value of the resource state enum.",
                MetricMode.ReadOnly,
                "1",
                MetricType.Enum,
                State.NEW);
        m_StateDetail = new ResourceStateMetricFactoryImpl<String>(resource).createAndRegister(
                ResourceTask.RESOURCE_STATEDETAIL,
                "fires as a resource changes its state detail",
                MetricMode.ReadOnly,
                "1",
                MetricType.String,
                "Unknown:Unknown");
    }

    /** clone */
    public ResourceMetrics clone() throws CloneNotSupportedException {
        ResourceMetrics clone = (ResourceMetrics) super.clone();
        clone.m_State = m_State.clone();
        clone.m_StateDetail = m_StateDetail.clone();
        return clone;
    }
}
