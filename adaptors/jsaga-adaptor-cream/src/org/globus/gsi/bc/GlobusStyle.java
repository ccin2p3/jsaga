package org.globus.gsi.bc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.X500NameStyle;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

/*
 * TODO: remove this class when upgrading JGlobus
 */
public class GlobusStyle extends BCStyle {

    public static final X500NameStyle INSTANCE = new GlobusStyle();
    /**
     * see {@link BCStyle} DefaultSymbols variable
     */
    protected static final Hashtable<ASN1ObjectIdentifier, String> DefaultSymbols = new Hashtable<ASN1ObjectIdentifier, String>();
    /**
     * see {@link BCStyle} DefaultLookUp variable
     */
    protected static final Hashtable<String, ASN1ObjectIdentifier> DefaultLookUp = new Hashtable<String, ASN1ObjectIdentifier>();

    /**
     * IP - StringType(SIZE(2))
     */
    public static final ASN1ObjectIdentifier IP = new ASN1ObjectIdentifier("1.3.6.1.4.1.42.2.11.2.1");

    static {
        DefaultSymbols.put(C, "C");
        DefaultSymbols.put(O, "O");
        DefaultSymbols.put(T, "T");
        DefaultSymbols.put(OU, "OU");
        DefaultSymbols.put(CN, "CN");
        DefaultSymbols.put(L, "L");
        DefaultSymbols.put(ST, "ST");
        DefaultSymbols.put(SN, "SERIALNUMBER");
        DefaultSymbols.put(EmailAddress, "E");
        // The following line is commented this line to fix the EMAIL issue in DN
        //DefaultSymbols.put(EmailAddress, "EMAIL");
        DefaultSymbols.put(DC, "DC");
        DefaultSymbols.put(UID, "UID");
        DefaultSymbols.put(STREET, "STREET");
        DefaultSymbols.put(SURNAME, "SURNAME");
        DefaultSymbols.put(GIVENNAME, "GIVENNAME");
        DefaultSymbols.put(INITIALS, "INITIALS");
        DefaultSymbols.put(GENERATION, "GENERATION");
        DefaultSymbols.put(UnstructuredAddress, "unstructuredAddress");
        DefaultSymbols.put(UnstructuredName, "unstructuredName");
        DefaultSymbols.put(UNIQUE_IDENTIFIER, "UniqueIdentifier");
        DefaultSymbols.put(DN_QUALIFIER, "DN");
        DefaultSymbols.put(PSEUDONYM, "Pseudonym");
        DefaultSymbols.put(POSTAL_ADDRESS, "PostalAddress");
        DefaultSymbols.put(NAME_AT_BIRTH, "NameAtBirth");
        DefaultSymbols.put(COUNTRY_OF_CITIZENSHIP, "CountryOfCitizenship");
        DefaultSymbols.put(COUNTRY_OF_RESIDENCE, "CountryOfResidence");
        DefaultSymbols.put(GENDER, "Gender");
        DefaultSymbols.put(PLACE_OF_BIRTH, "PlaceOfBirth");
        DefaultSymbols.put(DATE_OF_BIRTH, "DateOfBirth");
        DefaultSymbols.put(POSTAL_CODE, "PostalCode");
        DefaultSymbols.put(BUSINESS_CATEGORY, "BusinessCategory");
        DefaultSymbols.put(TELEPHONE_NUMBER, "TelephoneNumber");
        DefaultSymbols.put(NAME, "Name");
        DefaultSymbols.put(IP, "IP");

        DefaultLookUp.put("c", C);
        DefaultLookUp.put("o", O);
        DefaultLookUp.put("t", T);
        DefaultLookUp.put("ou", OU);
        DefaultLookUp.put("cn", CN);
        DefaultLookUp.put("l", L);
        DefaultLookUp.put("st", ST);
        DefaultLookUp.put("sn", SN);
        DefaultLookUp.put("serialnumber", SN);
        DefaultLookUp.put("street", STREET);
        DefaultLookUp.put("emailaddress", E);
        DefaultLookUp.put("dc", DC);
        DefaultLookUp.put("e", E);
        DefaultLookUp.put("uid", UID);
        DefaultLookUp.put("surname", SURNAME);
        DefaultLookUp.put("givenname", GIVENNAME);
        DefaultLookUp.put("initials", INITIALS);
        DefaultLookUp.put("generation", GENERATION);
        DefaultLookUp.put("unstructuredaddress", UnstructuredAddress);
        DefaultLookUp.put("unstructuredname", UnstructuredName);
        DefaultLookUp.put("uniqueidentifier", UNIQUE_IDENTIFIER);
        DefaultLookUp.put("dn", DN_QUALIFIER);
        DefaultLookUp.put("pseudonym", PSEUDONYM);
        DefaultLookUp.put("postaladdress", POSTAL_ADDRESS);
        DefaultLookUp.put("nameofbirth", NAME_AT_BIRTH);
        DefaultLookUp.put("countryofcitizenship", COUNTRY_OF_CITIZENSHIP);
        DefaultLookUp.put("countryofresidence", COUNTRY_OF_RESIDENCE);
        DefaultLookUp.put("gender", GENDER);
        DefaultLookUp.put("placeofbirth", PLACE_OF_BIRTH);
        DefaultLookUp.put("dateofbirth", DATE_OF_BIRTH);
        DefaultLookUp.put("postalcode", POSTAL_CODE);
        DefaultLookUp.put("businesscategory", BUSINESS_CATEGORY);
        DefaultLookUp.put("telephonenumber", TELEPHONE_NUMBER);
        DefaultLookUp.put("name", NAME);
        DefaultLookUp.put("ip", IP);
    }

    protected GlobusStyle() {
        super();
    }

    public static Map<String, ASN1ObjectIdentifier> getDefaultlookup() {
        return Collections.unmodifiableMap(DefaultLookUp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.bouncycastle.asn1.x500.style.BCStyle#fromString(java.lang.String)
     */
    @Override
    public RDN[] fromString(String dirName) {
        String id = dirName.trim();
        X500NameBuilder builder = new X500NameBuilder(this);
        if (!id.isEmpty()) {
            StringBuilder attrBuilder = new StringBuilder(5);
            StringBuilder valueBuilder = new StringBuilder();

            final int IDLE = 0;
            final int VALUE = 1;
            final int KEY = 3;

            boolean multiValue = false;
            List<ASN1ObjectIdentifier> multiValueOIDs = null;
            List<String> multiValueValues = null;
            
            int state = IDLE;

            char[] asChars = id.toCharArray();

            /*
             * walk in reverse order and split into RDN
             */
            for (int i = asChars.length - 1; i >= 0; i--) {

                char c = asChars[i];
                switch (state) {
                case KEY:
                    if (c == '/') {
                        if(!multiValue){
                            builder.addRDN(attrNameToOID(attrBuilder.reverse().toString()), valueBuilder.reverse().toString());
                            attrBuilder.setLength(0);
                            valueBuilder.setLength(0);
                        }else{
                            multiValueOIDs.add(attrNameToOID(attrBuilder.reverse().toString()));
                            multiValueValues.add(valueBuilder.reverse().toString());
                            attrBuilder.setLength(0);
                            valueBuilder.setLength(0);
                            Collections.reverse(multiValueOIDs);
                            Collections.reverse(multiValueValues);
                            builder.addMultiValuedRDN(multiValueOIDs.toArray(new ASN1ObjectIdentifier[multiValueOIDs.size()]), multiValueValues.toArray(new String[multiValueValues.size()]));
                            multiValueOIDs.clear();
                            multiValueValues.clear();
                            multiValue = false;
                        }
                        state = IDLE;
                    }else if (c == '+') {
                        if(multiValueOIDs == null){
                            multiValueOIDs = new ArrayList<ASN1ObjectIdentifier>(3);
                            multiValueValues = new ArrayList<String>(3);
                        }
                        multiValue = true;
                        multiValueOIDs.add(attrNameToOID(attrBuilder.reverse().toString()));
                        multiValueValues.add(valueBuilder.reverse().toString());
                        attrBuilder.setLength(0);
                        valueBuilder.setLength(0);
                        state = IDLE;
                    }else if(c == ' '){
                        continue;
                    }else{
                        attrBuilder.append(c);
                    }
                    break;
                case VALUE:
                    if (c == '=') {
                        state = KEY;
                    } else {
                        valueBuilder.append(c);
                    }
                    break;
                default:
                    // idle
                    if (c == '/' || c == ' '){
                        continue;
                    } else {
                        valueBuilder.append(c);
                        state = VALUE;
                    }
                }
            }
        }
        return builder.build().getRDNs();
    }
    
    public static RDN[] swap(RDN[] rdns) {
        RDN temp = null;
        for (int start = 0, end = rdns.length - 1; start < end; start++, end--) {
            // swap rdns
            temp = rdns[start];
            if (temp.isMultiValued()) {
                temp = new RDN(invertAttributeTypeAndValueArray(temp.getTypesAndValues()));
            }
            if (rdns[end].isMultiValued()) {
                rdns[end] = new RDN(invertAttributeTypeAndValueArray(rdns[end].getTypesAndValues()));
            }
            rdns[start] = rdns[end];
            rdns[end] = temp;
        }
        if (rdns.length % 2 != 0) {
            if (rdns[((rdns.length + 1) / 2) - 1].isMultiValued()) {
                rdns[((rdns.length + 1)) / 2] = new RDN(
                        invertAttributeTypeAndValueArray(rdns[((rdns.length + 1)) / 2].getTypesAndValues()));
            }
        }
        return rdns;
    }

    private static AttributeTypeAndValue[] invertAttributeTypeAndValueArray(
            AttributeTypeAndValue[] attributeTypeAndValues) {
        AttributeTypeAndValue temp = null;
        for (int start = 0, end = attributeTypeAndValues.length - 1; start < end; start++, end--) {
            // swap
            temp = attributeTypeAndValues[start];
            attributeTypeAndValues[start] = attributeTypeAndValues[end];
            attributeTypeAndValues[end] = temp;
        }
        return attributeTypeAndValues;
    }

    @Override
    public ASN1ObjectIdentifier attrNameToOID(String attrName) {
        return IETFUtils.decodeAttrName(attrName, DefaultLookUp);
    }

    @Override
    public String toString(X500Name name) {
        StringBuffer buf = new StringBuffer();
        RDN[] rdns = name.getRDNs();

        // Check if reverse or not
        boolean revert = toRevert(name);
        if (revert) {
            for (int i = rdns.length - 1; i >= 0; i--) {
                appendRDNInfo(buf, rdns[i], "/");
            }
        } else {
            for (int i = 0; i < rdns.length; i++) {
                appendRDNInfo(buf, rdns[i], "/");
            }
        }

        return buf.toString();
    }
    
    public static boolean toRevert(X500Name name){
        RDN[] rdns = name.getRDNs();
        // Check if reverse or not
        if (rdns.length > 1) {
            RDN rdn1 = rdns[0];
            RDN rdn2 = rdns[rdns.length - 1];
            Set<ASN1ObjectIdentifier> asn1ObjectIdentifiers = DefaultSymbols.keySet();
            for (ASN1ObjectIdentifier asn1ObjectIdentifier : asn1ObjectIdentifiers) {
                if (asn1ObjectIdentifier.equals(AttributeTypeAndValue.getInstance(
                        ((ASN1Set) rdn1.getDERObject()).getObjectAt(0)).getType())) {
                    // Revert
                    return true;
                }
                if (asn1ObjectIdentifier.equals(AttributeTypeAndValue.getInstance(
                        ((ASN1Set) rdn2.getDERObject()).getObjectAt(0)).getType())) {
                    // Do not revert;
                    return false;
                }
            }
        }
        return false;
    }

    protected void appendRDNInfo(StringBuffer buf, RDN rdn, String separator) {
        buf.append(separator);
        if (rdn.isMultiValued()) {
            AttributeTypeAndValue[] atv = rdn.getTypesAndValues();
            boolean firstAtv = true;

            for (int j = 0; j != atv.length; j++) {
                if (firstAtv) {
                    firstAtv = false;
                } else {
                    buf.append('/');
                }

                IETFUtils.appendTypeAndValue(buf, atv[j], DefaultSymbols);
            }
        } else {
            IETFUtils.appendTypeAndValue(buf, rdn.getFirst(), DefaultSymbols);
        }
    }
}
