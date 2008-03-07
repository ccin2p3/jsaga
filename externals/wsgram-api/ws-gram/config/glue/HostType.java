/**
 * HostType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.globus.mds.glue;


/**
 * A host may have any of the properties of a subcluster.
 * 
 *     TODO: really a Host shouldn't be allowed another Host below it?
 * Can I
 *     specify a restriction in here to limit the number of subordinate
 * Hosts
 *     to 0, or some trick like that?
 */
public class HostType  extends org.globus.mds.glue.SubClusterOrHostType  implements java.io.Serializable {
    private org.globus.mds.glue.LoadType processorLoad;

    private org.globus.mds.glue.LoadType SMPLoad;

    public HostType() {
    }

    public HostType(
           java.lang.String name,
           java.lang.String uniqueID,
           org.apache.axis.types.URI informationServiceURL,
           org.globus.mds.glue.BenchmarkType benchmark,
           org.globus.mds.glue.ProcessorType processor,
           org.globus.mds.glue.MainMemoryType mainMemory,
           org.globus.mds.glue.OperatingSystemType operatingSystem,
           org.globus.mds.glue.StorageDeviceType[] storageDevice,
           org.globus.mds.glue.ArchitectureType architecture,
           org.globus.mds.glue.ApplicationSoftwareType applicationSoftware,
           org.globus.mds.glue.FileSystemType[] fileSystem,
           org.globus.mds.glue.NetworkAdapterType[] networkAdapter,
           org.apache.axis.message.MessageElement [] _any,
           org.globus.mds.glue.LoadType processorLoad,
           org.globus.mds.glue.LoadType SMPLoad) {
        super(
            benchmark,
            processor,
            mainMemory,
            operatingSystem,
            storageDevice,
            architecture,
            applicationSoftware,
            fileSystem,
            networkAdapter,
            _any,
            name,
            uniqueID,
            informationServiceURL);
        this.processorLoad = processorLoad;
        this.SMPLoad = SMPLoad;
    }


    /**
     * Gets the processorLoad value for this HostType.
     * 
     * @return processorLoad
     */
    public org.globus.mds.glue.LoadType getProcessorLoad() {
        return processorLoad;
    }


    /**
     * Sets the processorLoad value for this HostType.
     * 
     * @param processorLoad
     */
    public void setProcessorLoad(org.globus.mds.glue.LoadType processorLoad) {
        this.processorLoad = processorLoad;
    }


    /**
     * Gets the SMPLoad value for this HostType.
     * 
     * @return SMPLoad
     */
    public org.globus.mds.glue.LoadType getSMPLoad() {
        return SMPLoad;
    }


    /**
     * Sets the SMPLoad value for this HostType.
     * 
     * @param SMPLoad
     */
    public void setSMPLoad(org.globus.mds.glue.LoadType SMPLoad) {
        this.SMPLoad = SMPLoad;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof HostType)) return false;
        HostType other = (HostType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.processorLoad==null && other.getProcessorLoad()==null) || 
             (this.processorLoad!=null &&
              this.processorLoad.equals(other.getProcessorLoad()))) &&
            ((this.SMPLoad==null && other.getSMPLoad()==null) || 
             (this.SMPLoad!=null &&
              this.SMPLoad.equals(other.getSMPLoad())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getProcessorLoad() != null) {
            _hashCode += getProcessorLoad().hashCode();
        }
        if (getSMPLoad() != null) {
            _hashCode += getSMPLoad().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(HostType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "HostType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("processorLoad");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "ProcessorLoad"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "LoadType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SMPLoad");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "SMPLoad"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "LoadType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
