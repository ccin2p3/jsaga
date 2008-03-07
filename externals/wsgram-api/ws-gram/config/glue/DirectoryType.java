/**
 * DirectoryType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.globus.mds.glue;

public class DirectoryType  extends org.globus.mds.glue.FileType  implements java.io.Serializable {
    /* From the UML diagram: Directory contains file */
    private org.globus.mds.glue.FileType[] file;

    /* The FileSystemName attribute should refer to the name
     * 		     of a FileSystem object. From the UML diagram: mount. */
    private org.globus.mds.glue.DirectoryTypeMount[] mount;

    public DirectoryType() {
    }

    public DirectoryType(
           java.lang.String name,
           int size,
           java.util.Calendar creationDate,
           java.util.Calendar lastModified,
           java.util.Calendar lastAccessed,
           org.apache.axis.types.Duration latency,
           java.util.Calendar lifeTime,
           java.lang.String owner,
           org.apache.axis.message.MessageElement [] _any,
           org.globus.mds.glue.FileType[] file,
           org.globus.mds.glue.DirectoryTypeMount[] mount) {
        super(
        	_any,
            name,
            size,
            creationDate,
            lastModified,
            lastAccessed,
            latency,
            lifeTime,
            owner);
        this.file = file;
        this.mount = mount;
    }


    /**
     * Gets the file value for this DirectoryType.
     * 
     * @return file   * From the UML diagram: Directory contains file
     */
    public org.globus.mds.glue.FileType[] getFile() {
        return file;
    }


    /**
     * Sets the file value for this DirectoryType.
     * 
     * @param file   * From the UML diagram: Directory contains file
     */
    public void setFile(org.globus.mds.glue.FileType[] file) {
        this.file = file;
    }

    public org.globus.mds.glue.FileType getFile(int i) {
        return this.file[i];
    }

    public void setFile(int i, org.globus.mds.glue.FileType _value) {
        this.file[i] = _value;
    }


    /**
     * Gets the mount value for this DirectoryType.
     * 
     * @return mount   * The FileSystemName attribute should refer to the name
     * 		     of a FileSystem object. From the UML diagram: mount.
     */
    public org.globus.mds.glue.DirectoryTypeMount[] getMount() {
        return mount;
    }


    /**
     * Sets the mount value for this DirectoryType.
     * 
     * @param mount   * The FileSystemName attribute should refer to the name
     * 		     of a FileSystem object. From the UML diagram: mount.
     */
    public void setMount(org.globus.mds.glue.DirectoryTypeMount[] mount) {
        this.mount = mount;
    }

    public org.globus.mds.glue.DirectoryTypeMount getMount(int i) {
        return this.mount[i];
    }

    public void setMount(int i, org.globus.mds.glue.DirectoryTypeMount _value) {
        this.mount[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DirectoryType)) return false;
        DirectoryType other = (DirectoryType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.file==null && other.getFile()==null) || 
             (this.file!=null &&
              java.util.Arrays.equals(this.file, other.getFile()))) &&
            ((this.mount==null && other.getMount()==null) || 
             (this.mount!=null &&
              java.util.Arrays.equals(this.mount, other.getMount())));
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
        if (getFile() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getFile());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getFile(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getMount() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMount());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMount(), i);
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
        new org.apache.axis.description.TypeDesc(DirectoryType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "DirectoryType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("file");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "File"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "FileType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", "mount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://mds.globus.org/glue/ce/1.1", ">DirectoryType>mount"));
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
