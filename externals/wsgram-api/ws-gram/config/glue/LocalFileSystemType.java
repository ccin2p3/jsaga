/**
 * LocalFileSystemType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.globus.mds.glue;

public class LocalFileSystemType  extends org.globus.mds.glue.FileSystemType  implements java.io.Serializable {
    private org.globus.mds.glue.DirectoryType[] export;

    public LocalFileSystemType() {
    }

    public LocalFileSystemType(
           java.lang.String name,
           java.lang.String root,
           long size,
           long availableSpace,
           boolean readOnly,
           java.lang.String type,
           org.apache.axis.message.MessageElement [] _any,
           org.globus.mds.glue.DirectoryType[] export) {
        super(
        		_any,
            name,
            root,
            size,
            availableSpace,
            readOnly,
            type);
        this.export = export;
    }


    /**
     * Gets the export value for this LocalFileSystemType.
     * 
     * @return export
     */
    public org.globus.mds.glue.DirectoryType[] getExport() {
        return export;
    }


    /**
     * Sets the export value for this LocalFileSystemType.
     * 
     * @param export
     */
    public void setExport(org.globus.mds.glue.DirectoryType[] export) {
        this.export = export;
    }

    public org.globus.mds.glue.DirectoryType getExport(int i) {
        return this.export[i];
    }

    public void setExport(int i, org.globus.mds.glue.DirectoryType _value) {
        this.export[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof LocalFileSystemType)) return false;
        LocalFileSystemType other = (LocalFileSystemType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.export==null && other.getExport()==null) || 
             (this.export!=null &&
              java.util.Arrays.equals(this.export, other.getExport())));
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
        if (getExport() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getExport());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getExport(), i);
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
        new org.apache.axis.description.TypeDesc(LocalFileSystemType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "LocalFileSystemType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("export");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "export"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "DirectoryType"));
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
