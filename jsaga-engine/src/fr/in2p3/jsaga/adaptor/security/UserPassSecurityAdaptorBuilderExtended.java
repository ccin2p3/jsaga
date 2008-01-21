package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.engine.config.attributes.FilePropertiesAttributesParser;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserPassSecurityAdaptorBuilderExtended
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UserPassSecurityAdaptorBuilderExtended extends UserPassSecurityAdaptorBuilder implements ExpirableSecurityAdaptorBuilder {
    private static final String USERPASSCRYPTED = "UserPassCrypted";
    private static final Usage CRYPTED = new UAnd(new Usage[]{new U(Context.USERID), new U(USERPASSCRYPTED)});

    /** override super.getUsage() to add attribute UserPassCrypted */
    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new U(Context.USERID),
                new UOr(new Usage[]{
                        new UHidden(Context.USERPASS),
                        new U(USERPASSCRYPTED)
                })
        });
    }

    /** override super.getDefaults() to add attribute LifeTime */
    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return new Default[]{
                new Default(Context.USERID, "anonymous"),
                new Default(Context.USERPASS, "anon"),
                new Default(Context.LIFETIME, "PT12H")
        };
    }

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception {
        if (UNCRYPTED.getMissingValues(attributes) == null) {
            return super.createSecurityAdaptor(attributes);
        } else if (CRYPTED.getMissingValues(attributes) == null) {
            String name = (String) attributes.get(Context.USERID);
            String cryptedPassword = (String) attributes.get(USERPASSCRYPTED);
            PasswordDecrypterSingleton decrypter = PasswordDecrypterSingleton.getInstance();
            int expiryDate = decrypter.getExpiryDate();
            int currentDate = (int) (System.currentTimeMillis()/1000);
            String password;
            if (currentDate > expiryDate) {
                password = null;
            } else {
                password = decrypter.decrypt(cryptedPassword);
            }
            return new UserPassExpirableSecurityAdaptor(name, password, expiryDate);
        } else {
            throw new BadParameter("Missing attribute(s): "+this.getUsage().getMissingValues(attributes));
        }
    }

    public Usage getInitUsage() {
        return new UAnd(new Usage[]{
                new U(Context.USERID),
                new UHidden(Context.USERPASS),
                new UDuration(Context.LIFETIME) {
                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                        return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                    }
                }
        });
    }

    public void initBuilder(Map attributes, String contextId) throws Exception {
        // get attributes
        String name = (String) attributes.get(Context.USERID);
        String password = (String) attributes.get(Context.USERPASS);
        int lifetime = (attributes.containsKey(Context.LIFETIME)
                ? UDuration.toInt(attributes.get(Context.LIFETIME))
                : 12*3600);

        // encrypt password
        String cryptedPassword = PasswordEncrypterSingleton.getInstance(lifetime).encrypt(password);

        // write to user properties file
        Properties prop = new Properties();
        prop.load(new FileInputStream(FilePropertiesAttributesParser.FILE));
        prop.setProperty(contextId+"."+Context.USERID, name);
        prop.setProperty(contextId+"."+USERPASSCRYPTED, cryptedPassword);
        prop.store(new FileOutputStream(FilePropertiesAttributesParser.FILE), "JSAGA user attributes");
    }

    public void destroyBuilder(Map attributes, String contextId) throws Exception {
        Properties prop = new Properties();
        prop.load(new FileInputStream(FilePropertiesAttributesParser.FILE));
        prop.remove(contextId+"."+USERPASSCRYPTED);
        prop.store(new FileOutputStream(FilePropertiesAttributesParser.FILE), "JSAGA user attributes");
    }
}
