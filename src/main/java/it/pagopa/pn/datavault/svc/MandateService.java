package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.exceptions.InvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DenominationDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MandateDto;
import it.pagopa.pn.datavault.mapper.MandateEntityMandateDtoMapper;
import it.pagopa.pn.datavault.middleware.db.MandateDao;
import it.pagopa.pn.datavault.middleware.db.entities.MandateEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class MandateService {

    private final MandateDao objDao;
    private final MandateEntityMandateDtoMapper mappingsDao;

    public MandateService(MandateDao objDao, MandateEntityMandateDtoMapper mappingsDao) {
        this.objDao = objDao;
        this.mappingsDao = mappingsDao;
    }

    public Flux<MandateDto> getMandatesByInternalIds( List<String> mandateIds ) {
        return objDao.listMandatesByIds(mandateIds)
                .map(mappingsDao::toDto);
    }


    public Mono<String> updateMandateByInternalId(String mandateId, DenominationDto addressDto) {
        if (!StringUtils.hasText(mandateId))
            throw new InvalidInputException("mandateId is required");
        if (addressDto == null
                || (!StringUtils.hasText(addressDto.getDestName())
                    && !StringUtils.hasText(addressDto.getDestSurname())
                    && !StringUtils.hasText(addressDto.getDestBusinessName()))
                || (!StringUtils.hasText(addressDto.getDestName())
                    && StringUtils.hasText(addressDto.getDestName()))
                || (StringUtils.hasText(addressDto.getDestName())
                    && !StringUtils.hasText(addressDto.getDestName()))
                )
            throw new InvalidInputException("destName is required");


        MandateEntity me = new MandateEntity(mandateId);
        me.setName(addressDto.getDestName());
        me.setSurname(addressDto.getDestSurname());
        me.setBusinessName(addressDto.getDestBusinessName());

        return objDao.updateMandate(me).map(r -> "OK");
    }

    public Mono<String> deleteMandateByInternalId(String mandateId ) {
        if (!StringUtils.hasText(mandateId))
            throw new InvalidInputException("mandateId is required");

        return objDao.deleteMandateId(mandateId).map(r -> "OK");
    }
}
