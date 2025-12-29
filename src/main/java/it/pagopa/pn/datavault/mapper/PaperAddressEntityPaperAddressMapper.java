package it.pagopa.pn.datavault.mapper;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddress;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddressRequest;
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

    public PaperAddressEntity toEntity(String iun, String addressId, PaperAddressRequest dto) {
        final PaperAddressEntity target = new PaperAddressEntity(iun, addressId);
        assert dto.getPaperAddress() != null;
        target.setAddress(dto.getPaperAddress().getAddress());
        target.setName(dto.getPaperAddress().getName());
        target.setNameRow2(dto.getPaperAddress().getNameRow2());
        target.setNameRow2(dto.getPaperAddress().getAddressRow2());
        target.setCap(dto.getPaperAddress().getCap());
        target.setCity(dto.getPaperAddress().getCity());
        target.setCity2(dto.getPaperAddress().getCity2());
        target.setPr(dto.getPaperAddress().getPr());
        target.setCountry(dto.getPaperAddress().getCountry());

        target.setAttempt(dto.getAttempt());
        target.setRecIndex(dto.getRecIndex());
        target.setPcRetry(dto.getPcRetry());
        target.setNormalized(dto.getNormalized());
        target.setAddressType(dto.getAddressType());
        return target;
    }

}
