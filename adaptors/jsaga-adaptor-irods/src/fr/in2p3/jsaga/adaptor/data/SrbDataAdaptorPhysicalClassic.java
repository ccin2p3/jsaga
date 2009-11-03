package fr.in2p3.jsaga.adaptor.data;

public class SrbDataAdaptorPhysicalClassic extends IrodsDataAdaptorPhysical {
    public String getType() {
        return "srb-classic";
    }

    protected boolean isClassic() {
        return true;
    }
}
