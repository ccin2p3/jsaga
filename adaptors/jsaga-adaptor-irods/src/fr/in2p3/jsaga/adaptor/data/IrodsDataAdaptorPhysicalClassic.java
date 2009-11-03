package fr.in2p3.jsaga.adaptor.data;

public class IrodsDataAdaptorPhysicalClassic extends IrodsDataAdaptorPhysical {
    public String getType() {
        return "irods-classic";
    }

    protected boolean isClassic() {
        return true;
    }
}
