package fr.in2p3.jsaga.adaptor.data;

public class IrodsDataAdaptorLogicalClassic extends IrodsDataAdaptorLogical {
    public String getType() {
        return "irods-classic";
    }

    protected boolean isClassic() {
        return true;
    }
}
