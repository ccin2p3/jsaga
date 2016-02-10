package fr.in2p3.jsaga.impl.resource.description;

import java.util.Date;

import fr.in2p3.jsaga.impl.attributes.ScalarAttributeImpl;
import fr.in2p3.jsaga.impl.attributes.VectorAttributeImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.instance.Resource;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ResourceDescriptionAttributes implements Cloneable {
    ScalarAttributeImpl<Type> m_Type;
    VectorAttributeImpl<String> m_template;
    ScalarAttributeImpl<Boolean> m_dynamic;
    ScalarAttributeImpl<String> m_placement;
    ScalarAttributeImpl<Date> m_start;
    ScalarAttributeImpl<Date> m_end;
    ScalarAttributeImpl<Date> m_duration;

    /** constructor */
    ResourceDescriptionAttributes(final AbstractResourceDescriptionImpl resourceDescription) {
        m_Type = resourceDescription._addAttribute(new ScalarAttributeImpl<Type>(
                Resource.RESOURCE_TYPE,
                "the description type",
                MetricMode.ReadWrite,
                MetricType.Enum,
                null));
        m_template = resourceDescription._addVectorAttribute(new VectorAttributeImpl<String>(
                ResourceDescription.TEMPLATE,
                "An array of SAGA representation of the template identifier",
                MetricMode.ReadWrite,
                MetricType.String,
                null));
        m_dynamic = resourceDescription._addAttribute(new ScalarAttributeImpl<Boolean>(
                ResourceDescription.DYNAMIC,
                "???",
                MetricMode.ReadWrite,
                MetricType.Bool,
                null));
        m_placement = resourceDescription._addAttribute(new ScalarAttributeImpl<String>(
                ResourceDescription.PLACEMENT,
                "???",
                MetricMode.ReadWrite,
                MetricType.String,
                null));
        m_start = resourceDescription._addAttribute(new ScalarAttributeImpl<Date>(
                ResourceDescription.START,
                "Date/time to start the resource",
                MetricMode.ReadWrite,
                MetricType.Time,
                null));
        m_end = resourceDescription._addAttribute(new ScalarAttributeImpl<Date>(
                ResourceDescription.END,
                "Date/time to stop the resource",
                MetricMode.ReadWrite,
                MetricType.Time,
                null));
        m_duration = resourceDescription._addAttribute(new ScalarAttributeImpl<Date>(
                ResourceDescription.DURATION,
                "Duration of the resource",
                MetricMode.ReadWrite,
                MetricType.Time,
                null));
    }

    /** clone */
    public ResourceDescriptionAttributes clone() throws CloneNotSupportedException {
        ResourceDescriptionAttributes clone = (ResourceDescriptionAttributes) super.clone();
        clone.m_Type = m_Type.clone();
        clone.m_template = this.m_template;
        clone.m_dynamic = this.m_dynamic;
        clone.m_placement = this.m_placement;
        clone.m_start = this.m_start;
        clone.m_end = this.m_end;
        clone.m_duration = this.m_duration;
        return clone;
    }
}
