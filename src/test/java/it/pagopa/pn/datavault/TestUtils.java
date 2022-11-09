package it.pagopa.pn.datavault;

import it.pagopa.pn.datavault.middleware.db.entities.*;

public abstract class TestUtils {

    private TestUtils() {}

    public static AddressEntity newAddress() {
        AddressEntity ae = new AddressEntity("425e4567-e89b-12d3-a456-426655449631", "DD_c_f205_1");
        ae.setValue("test@test.it");
        return  ae;
    }

    public static MandateEntity newMandate(boolean pf) {
        MandateEntity me = new MandateEntity("425e4567-e89b-12d3-a456-426655449631");
        if (pf)
        {
            me.setName("mario");
            me.setSurname("rossi");
        }
        else
            me.setBusinessName("ragione sociale");

        return me;
    }

    public static NotificationEntity newNotification(){
        NotificationEntity ne = new NotificationEntity("425e4567-e89b-12d3-a456-426655449631", "000");
        ne.setDigitalAddress("mario.rossi@test.it");
        PhysicalAddress pa = new PhysicalAddress();
        pa.setAddress("via casa sua");
        pa.setAt("via");
        pa.setAddressDetails("interno 2");
        pa.setMunicipality("Venezia");
        pa.setCap("30000");
        pa.setProvince("VE");
        pa.setState("Italia");
        ne.setPhysicalAddress(pa);
        return ne;
    }

    public static NotificationTimelineEntity newNotificationTimeline(){
        NotificationTimelineEntity ne = new NotificationTimelineEntity("425e4567-e89b-12d3-a456-426655449631", "mario rossi");
        ne.setDigitalAddress("mario.rossi@test.it");
        PhysicalAddress pa = new PhysicalAddress();
        pa.setAddress("via casa sua");
        pa.setAt("via");
        pa.setAddressDetails("interno 2");
        pa.setMunicipality("Venezia");
        pa.setMunicipalityDetails("zattere");
        pa.setCap("30000");
        pa.setProvince("VE");
        pa.setState("Italia");
        ne.setPhysicalAddress(pa);
        return ne;
    }
}
