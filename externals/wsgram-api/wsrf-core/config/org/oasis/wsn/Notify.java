/**
 * Notify.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Jan 25, 2005 (02:00:55 EST) WSDL2Java emitter.
 */

package org.oasis.wsn;

public class Notify  implements java.io.Serializable {
    private org.oasis.wsn.NotificationMessageHolderType[] notificationMessage;

    public Notify() {
    }

    public Notify(
           org.oasis.wsn.NotificationMessageHolderType[] notificationMessage) {
           this.notificationMessage = notificationMessage;
    }


    /**
     * Gets the notificationMessage value for this Notify.
     * 
     * @return notificationMessage
     */
    public org.oasis.wsn.NotificationMessageHolderType[] getNotificationMessage() {
        return notificationMessage;
    }


    /**
     * Sets the notificationMessage value for this Notify.
     * 
     * @param notificationMessage
     */
    public void setNotificationMessage(org.oasis.wsn.NotificationMessageHolderType[] notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public org.oasis.wsn.NotificationMessageHolderType getNotificationMessage(int i) {
        return this.notificationMessage[i];
    }

    public void setNotificationMessage(int i, org.oasis.wsn.NotificationMessageHolderType _value) {
        this.notificationMessage[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Notify)) return false;
        Notify other = (Notify) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.notificationMessage==null && other.getNotificationMessage()==null) || 
             (this.notificationMessage!=null &&
              java.util.Arrays.equals(this.notificationMessage, other.getNotificationMessage())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getNotificationMessage() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getNotificationMessage());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getNotificationMessage(), i);
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
        new org.apache.axis.description.TypeDesc(Notify.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd", ">Notify"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("notificationMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd", "NotificationMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd", "NotificationMessageHolderType"));
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
