package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.evaluator.Evaluator;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.adaptor.EvaluatorAdaptorDescriptor;
import org.ogf.saga.error.NoSuccessException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EvaluatorAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EvaluatorAdaptorFactory {
    private EvaluatorAdaptorDescriptor m_descriptor;

    public EvaluatorAdaptorFactory(Configuration configuration) {
        m_descriptor = configuration.getDescriptors().getEvaluatorDesc();
    }

    /**
     * Create a new instance of evaluator adaptor.
     * @return the evaluator adaptor instance
     */
    public Evaluator getEvaluatorAdaptor() throws NoSuccessException {
        Class clazz = m_descriptor.getClazz();
        try {
            return (Evaluator) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }
}
