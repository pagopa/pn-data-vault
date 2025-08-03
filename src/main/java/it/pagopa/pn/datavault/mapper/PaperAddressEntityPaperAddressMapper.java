package it.pagopa.pn.datavault.mapper;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddress;
import it.pagopa.pn.datavault.middleware.db.entities.PaperAddressEntity;
import org.springframework.stereotype.Component;

@Component
public class PaperAddressEntityPaperAddressMapper implements BaseMapperInterface<PaperAddress, PaperAddressEntity> {


    private PaperAddressEntityPaperAddressMapper(){
        super();
    }

    @Override
    public PaperAddressEntity toEntity(PaperAddress dto) {
        return toEntity(null, null, dto);
    }

    @Override
    public PaperAddress toDto(PaperAddressEntity entity) {
        final PaperAddress target = new PaperAddress();
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

    public PaperAddressEntity toEntity(String paperRequestId, String addressId, PaperAddress dto) {
        final PaperAddressEntity target = new PaperAddressEntity(paperRequestId, addressId);
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

}
