package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.engine.config.attributes.FilePropertiesAttributesParser;
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
public class UserPassSecurityAdaptorBuilderExtended extends UserPassSecurityAdaptorBuilder implements InitializableSecurityAdaptorBuilder {
    private static final Usage CRYPTED = new UAnd(new Usage[]{new U("UserName"), new U("UserPassCrypted")});

    /** override super.getUsage() to add attribute UserPassCrypted */
    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new U("UserName"),
                new UOr(new Usage[]{
                        new UHidden("UserPass"),
                        new U("UserPassCrypted")
                })
        });
    }

    /** override super.getDefaults() to add attribute LifeTime */
    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return new Default[]{
                new Default("UserName", "anonymous"),
                new Default("UserPass", "anon"),
                new Default("LifeTime", "PT12H")
        };
    }

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception {
        if (UNCRYPTED.getMissingValues(attributes) == null) {
            return super.createSecurityAdaptor(attributes);
        } else if (CRYPTED.getMissingValues(attributes) == null) {
            String name = (String) attributes.get("UserName");
            String cryptedPassword = (String) attributes.get("UserPassCrypted");
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
                new U("UserName"),
                new UHidden("UserPass"),
                new UDuration("LifeTime") {
                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                        return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                    }
                }
        });
    }

    public void initBuilder(Map attributes, String contextId) throws Exception {
        // get attributes
        String name = (String) attributes.get("UserName");
        String password = (String) attributes.get("UserPass");
        int lifetime = (attributes.containsKey("LifeTime")
                ? UDuration.toInt(attributes.get("LifeTime"))
                : 12*3600);

        // encrypt password
        String cryptedPassword = PasswordEncrypterSingleton.getInstance(lifetime).encrypt(password);

        // write to user properties file
        Properties prop = new Properties();
        prop.load(new FileInputStream(FilePropertiesAttributesParser.FILE));
        prop.setProperty(contextId+".UserName", name);
        prop.setProperty(contextId+".UserPassCrypted", cryptedPassword);
        prop.store(new FileOutputStream(FilePropertiesAttributesParser.FILE), "JSAGA user attributes");
    }
}
