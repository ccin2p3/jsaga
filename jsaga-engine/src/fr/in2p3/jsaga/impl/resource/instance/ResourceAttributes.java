package fr.in2p3.jsaga.impl.resource.instance;

import fr.in2p3.jsaga.impl.attributes.ScalarAttributeImpl;
import fr.in2p3.jsaga.impl.attributes.VectorAttributeImpl;
import fr.in2p3.jsaga.impl.monitoring.MetricMode;
import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.instance.Resource;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ResourceAttributes implements Cloneable {
    ScalarAttributeImpl<Type> m_Type;
    public ScalarAttributeImpl<String> m_ResourceID;
    ScalarAttributeImpl<String> m_ManagerID;
    VectorAttributeImpl<String> m_Access;
    ScalarAttributeImpl<String> m_Description;

    /** constructor */
    ResourceAttributes(final AbstractResourceImpl resource) {
        m_Type = resource._addAttribute(new ScalarAttributeImpl<Type>(
                Resource.RESOURCE_TYPE,
                "the resource type",
                MetricMode.ReadOnly,
                MetricType.Enum,
                null));
        m_ResourceID = resource._addAttribute(new ScalarAttributeImpl<String>(
                Resource.RESOURCE_ID,
                "SAGA representation of the resource identifier",
                MetricMode.ReadOnly,
                MetricType.String,
                null));
        m_ManagerID = resource._addAttribute(new ScalarAttributeImpl<String>(
                Resource.MANAGER_ID,
                "URL representation of the resource manager that created the resource",
                MetricMode.ReadOnly,
                MetricType.String,
                null));
        m_Access = resource._addVectorAttribute(new VectorAttributeImpl<String>(
                Resource.ACCESS,
                "list of access URLs",
                MetricMode.ReadOnly,
                MetricType.String,
                null));
        m_Description = resource._addAttribute(new ScalarAttributeImpl<String>(
                Resource.RESOURCE_DESCRIPTION,
                "the resource description",
                MetricMode.ReadOnly,
                MetricType.String,
                null));
    }

    /** clone */
    public ResourceAttributes clone() throws CloneNotSupportedException {
        ResourceAttributes clone = (ResourceAttributes) super.clone();
        clone.m_Type = m_Type.clone();
        clone.m_ResourceID = m_ResourceID.clone();
        clone.m_ManagerID = m_ManagerID.clone();
        clone.m_Access = m_Access.clone();
        clone.m_Description = m_Description.clone();
        return clone;
    }
}
