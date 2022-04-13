package it.pagopa.pn.datavault.svc;

public enum Namespaces {
    MANDATES("mandates"),
    ADDRESSES("addresses"),
    NOTIFICATIONS( "notifications"),
    NOTIFICATIONS_TIMELINES( "notifications_timelines"),
    RECIPIENTS("recipients");

    private final String strValue;

    private Namespaces(String strValue) {
        this.strValue = strValue;
    }

    public String getStrValue() {
        return strValue;
    }
}
