package it.pagopa.pn.datavault.mapper;


import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DenominationDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MandateDto;
import it.pagopa.pn.datavault.middleware.db.entities.MandateEntity;
import org.springframework.stereotype.Component;

@Component
public class MandateEntityMandateDtoMapper implements BaseMapperInterface<MandateDto, MandateEntity> {


    private MandateEntityMandateDtoMapper(){
        super();
    }     

    @Override
    public MandateEntity toEntity(MandateDto dto) {
        final MandateEntity target = new MandateEntity(dto.getMandateId());
        DenominationDto info = dto.getInfo();
        target.setName(info.getDestName());
        target.setSurname(info.getDestSurname());
        target.setBusinessName(info.getDestBusinessName());
        return target;
    }

    @Override
    public MandateDto toDto(MandateEntity entity) {
        final MandateDto target = new MandateDto();
        target.setMandateId(entity.getMandateId());
        DenominationDto info = new DenominationDto();
        info.setDestName(entity.getName());
        info.setDestSurname(entity.getSurname());
        info.setDestBusinessName(entity.getBusinessName());
        target.setInfo (info);
        return target;
    }
}
