/**
 * SubClusterType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.globus.mds.glue;

public class SubClusterType  extends org.globus.mds.glue.SubClusterOrHostType  implements java.io.Serializable {
    private org.globus.mds.glue.HostType[] host;

    public SubClusterType() {
    }

    public SubClusterType(
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
           org.globus.mds.glue.HostType[] host) {
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
        this.host = host;
    }


    /**
     * Gets the host value for this SubClusterType.
     * 
     * @return host
     */
    public org.globus.mds.glue.HostType[] getHost() {
        return host;
    }


    /**
     * Sets the host value for this SubClusterType.
     * 
     * @param host
     */
    public void setHost(org.globus.mds.glue.HostType[] host) {
        this.host = host;
    }

    public org.globus.mds.glue.HostType getHost(int i) {
        return this.host[i];
    }

    public void setHost(int i, org.globus.mds.glue.HostType _value) {
        this.host[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SubClusterType)) return false;
        SubClusterType other = (SubClusterType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.host==null && other.getHost()==null) || 
             (this.host!=null &&
              java.util.Arrays.equals(this.host, other.getHost())));
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
        if (getHost() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getHost());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getHost(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SubClusterType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "SubClusterType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("host");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "Host"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "HostType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
