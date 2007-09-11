package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import org.ogf.saga.namespace.NamespaceEntry;

import java.util.HashSet;
import java.util.Set;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataConnection
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DataConnection {
    private DataAdaptor m_adaptor;
    private Set m_entries;

    public DataConnection(DataAdaptor adaptor) {
        m_adaptor = adaptor;
        m_entries = new HashSet();
    }

    public DataAdaptor getDataAdaptor() {
        return m_adaptor;
    }

    public void registerEntry(NamespaceEntry entry) {
        m_entries.add(entry);
    }

    public void unregisterEntry(NamespaceEntry entry) {
        m_entries.remove(entry);
    }

    public boolean hasRegisteredEntries() {
        return ! m_entries.isEmpty();
    }
}
