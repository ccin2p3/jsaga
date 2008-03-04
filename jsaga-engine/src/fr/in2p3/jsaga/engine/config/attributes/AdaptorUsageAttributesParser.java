package fr.in2p3.jsaga.engine.config.attributes;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.config.adaptor.AdaptorDescriptors;
import fr.in2p3.jsaga.engine.schema.config.*;
import org.ogf.saga.error.DoesNotExist;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AdaptorUsageAttributesParser
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 * Update attributes with adaptor usages
 */
public class AdaptorUsageAttributesParser implements AttributesParser {
    private EffectiveConfig m_config;
    private AdaptorDescriptors m_descriptors;

    public AdaptorUsageAttributesParser(EffectiveConfig config, AdaptorDescriptors descriptors) {
        m_config = config;
        m_descriptors = descriptors;
    }

    public void updateAttributes() throws ConfigurationException {
        // contexts
        for (int c=0; c<m_config.getContextCount(); c++) {
            Context context = m_config.getContext(c);
            Usage usage = m_descriptors.getSecurityDesc().getUsage(context.getType());
            this.updateAttributes(context, usage);
        }

        // protocols
        for (int p=0; p<m_config.getProtocolCount(); p++) {
            Protocol protocol = m_config.getProtocol(p);
            for (int i=0; i<protocol.getDataServiceCount(); i++) {
                DataService service = protocol.getDataService(i);
                Usage usage = m_descriptors.getDataDesc().getUsage(service.getType());
                this.updateAttributes(service, usage);
            }
        }

        // execution
        for (int e=0; e<m_config.getExecutionCount(); e++) {
            Execution execution = m_config.getExecution(e);
            for (int i=0; i<execution.getJobServiceCount(); i++) {
                JobService service = execution.getJobService(i);
                Usage usage = m_descriptors.getJobDesc().getUsage(service.getType());
                this.updateAttributes(service, usage);
            }
        }        
    }

    private void updateAttributes(ObjectType object, Usage usage) {
        if (usage != null) {
            for (int a=0; a<object.getAttributeCount(); a++) {
                Attribute attribute = object.getAttribute(a);
                if (attribute.getValue() != null) {
                    try {
                        attribute.setValue(usage.correctValue(attribute.getName(), attribute.getValue()));
                    } catch(DoesNotExist e) {/*do nothing*/}
                }
            }
        }
    }
}
