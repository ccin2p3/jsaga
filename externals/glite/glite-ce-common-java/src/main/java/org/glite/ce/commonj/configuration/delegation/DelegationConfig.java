package org.glite.ce.commonj.configuration.delegation;

import org.glite.ce.commonj.configuration.CEConfigResource;

public class DelegationConfig implements CEConfigResource {
    private static final long serialVersionUID = 1L;

    String delegationStorage = null;
    String delegationFactory = null;
    String delegationDatabase = null;
    int delegationKeySize = 1024;
    
    public DelegationConfig() {
        this(null, null, null, 1024);
    }
    
    public DelegationConfig(String delegationStorage, String delegationFactory, String delegationDatabase, int delegationKeySize) {
        this.delegationStorage = delegationStorage;
        this.delegationFactory = delegationFactory;
        this.delegationDatabase = delegationDatabase;
        this.delegationKeySize = delegationKeySize;
    }
    
    
    public Object clone() {
        return new DelegationConfig(delegationStorage, delegationFactory, delegationDatabase, delegationKeySize);
    }

    public String getDelegationStorage() {
        return delegationStorage;
    }

    public void setDelegationStorage(String delegationStorage) {
        this.delegationStorage = delegationStorage;
    }

    public String getDelegationFactory() {
        return delegationFactory;
    }

    public void setDelegationFactory(String delegationFactory) {
        this.delegationFactory = delegationFactory;
    }

    public String getDelegationDatabase() {
        return delegationDatabase;
    }

    public void setDelegationDatabase(String delegationDatabase) {
        this.delegationDatabase = delegationDatabase;
    }

    public int getDelegationKeySize() {
        return delegationKeySize;
    }

    public void setDelegationKeySize(int delegationKeySize) {
        this.delegationKeySize = delegationKeySize;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}
