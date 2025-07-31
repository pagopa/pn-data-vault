package it.pagopa.pn.datavault.mapper;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DiscoveredAddressDto;
import it.pagopa.pn.datavault.middleware.db.entities.DiscoveredAddressEntity;
import org.springframework.stereotype.Component;

@Component
public class DiscoveredAddressEntityDiscoveredAddressDtoMapper implements BaseMapperInterface<DiscoveredAddressDto, DiscoveredAddressEntity> {


    private DiscoveredAddressEntityDiscoveredAddressDtoMapper(){
        super();
    }

    @Override
    public DiscoveredAddressDto toDto(DiscoveredAddressEntity entity) {
        final DiscoveredAddressDto target = new DiscoveredAddressDto();
        target.setAddress(entity.getAddress());
        target.setName(entity.getName());
        target.setNameRow2(entity.getNameRow2());
        target.setNameRow2(entity.getAddressRow2());
        target.setCap(entity.getCap());
        target.setCity(entity.getCity());
        target.setCity2(entity.getCity2());
        target.setPr(entity.getPr());
        target.setCountry(entity.getCountry());
        return target;
    }

    public DiscoveredAddressEntity toEntity(String addressId, DiscoveredAddressDto dto) {
        final DiscoveredAddressEntity target = new DiscoveredAddressEntity(addressId);
        target.setAddress(dto.getAddress());
        target.setName(dto.getName());
        target.setNameRow2(dto.getNameRow2());
        target.setNameRow2(dto.getAddressRow2());
        target.setCap(dto.getCap());
        target.setCity(dto.getCity());
        target.setCity2(dto.getCity2());
        target.setPr(dto.getPr());
        target.setCountry(dto.getCountry());
        return target;
    }

    @Override
    public DiscoveredAddressEntity toEntity(DiscoveredAddressDto dto) {
        return this.toEntity(null, dto);
    }

}
