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

    private static MonitoringFactory factory;

    private synchronized static void initializeFactory()
            throws NotImplementedException, NoSuccessException {
        if (factory == null) {
            factory = ImplementationBootstrapLoader.createMonitoringFactory();
        }
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
    public synchronized static Metric createMetric(String name, String desc,
            String mode, String unit, String type, String value)
            throws NotImplementedException, BadParameterException,
            TimeoutException, NoSuccessException {
        initializeFactory();
        return factory.doCreateMetric(name, desc, mode, unit, type, value);
    }
}
