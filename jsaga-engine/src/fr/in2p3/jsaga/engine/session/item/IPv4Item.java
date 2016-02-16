package fr.in2p3.jsaga.engine.session.item;

import fr.in2p3.jsaga.engine.session.BaseUrlItem;

public class IPv4Item extends BaseUrlItem {

    public IPv4Item(String ip) {
        super(ip, null, null);
    }

    
    @Override
    protected boolean isRequired(boolean hasValue) {
        return true;
    }

    @Override
    protected String getSimpleSeparator() {
        return "://";
    }

    @Override
    protected String getRegExpSeparator() {
        return "://(\\p{Digit}{1,3}\\.\\p{Digit}{1,3}\\.\\p{Digit}{1,3}\\.\\p{Digit}{1,3})?";
    }

    @Override
    protected String getRegExpSeparatorNext() {
        return "[:/]";
    }

    @Override
    protected String getAllowedChars() {
        return "\\p{Digit}\\.";
    }

}
