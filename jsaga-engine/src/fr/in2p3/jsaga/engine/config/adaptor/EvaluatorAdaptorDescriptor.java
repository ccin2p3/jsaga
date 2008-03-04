package fr.in2p3.jsaga.engine.config.adaptor;

import fr.in2p3.jsaga.adaptor.evaluator.BasicEvaluator;
import fr.in2p3.jsaga.engine.config.ConfigurationException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EvaluatorAdaptorDescriptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   3 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EvaluatorAdaptorDescriptor {
    private Class m_class;

    public EvaluatorAdaptorDescriptor(Class[] adaptorClasses) throws ConfigurationException {
        switch(adaptorClasses.length) {
            case 0:
                m_class = BasicEvaluator.class;
                break;
            case 1:
                m_class = adaptorClasses[0];
                break;
            default:
                throw new ConfigurationException("Ambiguity: several evaluator adaptors have been found");
        }
    }

    public Class getClazz() {
        return m_class;
    }
}
