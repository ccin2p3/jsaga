package fr.in2p3.jsaga.helpers;

import fr.in2p3.jsaga.impl.monitoring.MetricType;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.NotImplemented;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AttributeSerializer
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AttributeSerializer<E> {
    private MetricType m_type;

    public AttributeSerializer(MetricType type) {
        m_type = type;
    }

    public E fromString(String value) throws NotImplemented, DoesNotExist {
        if (value!=null && !value.equals("")) {
            switch(m_type) {
                case String:
                    return (E) value;
                case Int:
                    return (E) Integer.valueOf(value);
                case Enum:
                    throw new NotImplemented("Metrics with type Enum must override method fromString");
                case Float:
                    return (E) Float.valueOf(value);
                case Bool:
                    return (E) Boolean.valueOf(value);
                case Time:
                    try {
                        return (E) new SimpleDateFormat().parse(value);
                    } catch (ParseException e) {
                        throw new RuntimeException();
                    }
                case Trigger:
                    throw new DoesNotExist("Metrics with type Trigger have no value");
                default:
                    throw new NotImplemented("Metrics with unknown type must override method fromString");
            }
        } else {
            return null;
        }
    }

    public String toString(E value) throws DoesNotExist {
        if (value != null) {
            switch(m_type) {
                case String:
                    return (String) value;
                case Int:
                    return value.toString();
                case Enum:
                    return ((Enum) value).name();
                case Float:
                    return value.toString();
                case Bool:
                    return ((Boolean) value).booleanValue() ? "True" : "False";
                case Time:
                    return new SimpleDateFormat().format((Date) value);
                case Trigger:
                    throw new DoesNotExist("Metrics of type Trigger have no value");
                default:
                    return value.toString();
            }
        } else {
            return "";
        }
    }

    public E fromStringArray(String[] values) throws NotImplemented, DoesNotExist {
        if (values!=null && values.length>0) {
            return this.fromString(values[0]);
        } else {
            return null;
        }
    }

    public String[] toStringArray(E value) throws DoesNotExist {
        if (value != null) {
            return new String[]{this.toString(value)};
        } else {
            return new String[0];
        }
    }
}
