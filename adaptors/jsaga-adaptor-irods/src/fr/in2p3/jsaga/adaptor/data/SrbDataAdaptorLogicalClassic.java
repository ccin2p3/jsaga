package fr.in2p3.jsaga.adaptor.data;

public class SrbDataAdaptorLogicalClassic extends SrbDataAdaptorLogical {
    public String getType() {
        return "srb-classic";
    }

    protected boolean isClassic() {
        return true;
    }
}
