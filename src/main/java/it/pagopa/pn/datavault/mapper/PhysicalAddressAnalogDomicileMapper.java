package it.pagopa.pn.datavault.mapper;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AnalogDomicile;
import it.pagopa.pn.datavault.middleware.db.entities.PhysicalAddress;

public abstract class PhysicalAddressAnalogDomicileMapper {

    protected PhysicalAddress toPhysicalAddress(AnalogDomicile analogDomicile)
    {
        PhysicalAddress physicalAddress = null;
        if( analogDomicile != null ) {
            physicalAddress = new PhysicalAddress();
            physicalAddress.setAddress(analogDomicile.getAddress());
            physicalAddress.setAddressDetails(analogDomicile.getAddressDetails());
            physicalAddress.setAt(analogDomicile.getAt());
            physicalAddress.setCap(analogDomicile.getCap());
            physicalAddress.setMunicipality(analogDomicile.getMunicipality());
            physicalAddress.setProvince(analogDomicile.getProvince());
            physicalAddress.setState(analogDomicile.getState());
        }
        return physicalAddress;
    }

    protected AnalogDomicile toAnalogDomicile(PhysicalAddress physicalAddress)
    {
        AnalogDomicile analogDomicile = null;
        if( physicalAddress != null ) {
            analogDomicile = new AnalogDomicile();
            analogDomicile.setAddress(physicalAddress.getAddress());
            analogDomicile.setAddressDetails(physicalAddress.getAddressDetails());
            analogDomicile.setAt(physicalAddress.getAt());
            analogDomicile.setCap(physicalAddress.getCap());
            analogDomicile.setMunicipality(physicalAddress.getMunicipality());
            analogDomicile.setProvince(physicalAddress.getProvince());
            analogDomicile.setState(physicalAddress.getState());
        }
        return analogDomicile;
    }
    
}
