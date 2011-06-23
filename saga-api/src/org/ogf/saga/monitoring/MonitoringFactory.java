package org.ogf.saga.monitoring;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

/**
 * Factory for objects in the monitoring package.
 */
public abstract class MonitoringFactory {
        
    private static MonitoringFactory getFactory(String sagaFactoryName)
    	    throws NoSuccessException, NotImplementedException {
	return ImplementationBootstrapLoader.getMonitoringFactory(sagaFactoryName);
    }

    /**
     * Constructs a <code>Metric</code> object with the specified parameters.
     * This method is to be provided by the factory.
     * 
     * @param name
     *            name of the metric.
     * @param desc
     *            description of the metric.
     * @param mode
     *            mode of the metric.
     * @param unit
     *            unit of the metric value.
     * @param type
     *            type of the metric.
     * @param value
     *            value of the metric.
     * @return the metric.
     */
    protected abstract Metric doCreateMetric(String name, String desc,
            String mode, String unit, String type, String value)
            throws NotImplementedException, BadParameterException,
            TimeoutException, NoSuccessException;

    /**
     * Constructs a <code>Metric</code> object with the specified parameters.
     * 
     * @param name
     *      name of the metric.
     * @param desc
     *      description of the metric.
     * @param mode
     *      mode of the metric.
     * @param unit
     *      unit of the metric value.
     * @param type
     *      type of the metric.
     * @param value
     *      value of the metric.
     * @return
     *      the metric.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown on incorrectly formatted 'value' parameter, invalid 'mode'
     *      or 'type' parameter, and empty required parameter (all but 'unit'). 
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public synchronized static Metric createMetric(String name, String desc,
            String mode, String unit, String type, String value)
            throws NotImplementedException, BadParameterException,
            TimeoutException, NoSuccessException {
        return createMetric(null, name, desc, mode, unit, type, value);
    }
    
    /**
     * Constructs a <code>Metric</code> object with the specified parameters.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param name
     *      name of the metric.
     * @param desc
     *      description of the metric.
     * @param mode
     *      mode of the metric.
     * @param unit
     *      unit of the metric value.
     * @param type
     *      type of the metric.
     * @param value
     *      value of the metric.
     * @return
     *      the metric.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception BadParameterException
     *      is thrown on incorrectly formatted 'value' parameter, invalid 'mode'
     *      or 'type' parameter, and empty required parameter (all but 'unit'). 
     * @exception NoSuccessException
     *      is thrown when the operation was not successfully performed,
     *      and none of the other exceptions apply.
     */
    public synchronized static Metric createMetric(String sagaFactoryClassname, String name, String desc,
            String mode, String unit, String type, String value)
            throws NotImplementedException, BadParameterException,
            TimeoutException, NoSuccessException {
        return getFactory(sagaFactoryClassname).doCreateMetric(name, desc, mode, unit, type, value);
    }
}
