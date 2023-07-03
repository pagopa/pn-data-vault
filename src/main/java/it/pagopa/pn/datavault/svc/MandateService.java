package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.exceptions.PnInvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DenominationDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MandateDto;
import it.pagopa.pn.datavault.mapper.MandateEntityMandateDtoMapper;
import it.pagopa.pn.datavault.middleware.db.MandateDao;
import it.pagopa.pn.datavault.middleware.db.entities.MandateEntity;
import it.pagopa.pn.datavault.utils.ValidationUtils;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static it.pagopa.pn.commons.exceptions.PnExceptionsCodes.ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED;

@Service
@CustomLog
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
        if (! ValidationUtils.checkMandateId(mandateId))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "mandateId");

        if (! ValidationUtils.checkDenominationDto(addressDto))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "destName");

        MandateEntity me = new MandateEntity(mandateId);
        me.setName(addressDto.getDestName());
        me.setSurname(addressDto.getDestSurname());
        me.setBusinessName(addressDto.getDestBusinessName());

        return objDao.updateMandate(me).map(r -> "OK");
    }

    public Mono<String> deleteMandateByInternalId(String mandateId ) {
        if (! ValidationUtils.checkMandateId(mandateId))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "mandateId");

        return objDao.deleteMandateId(mandateId).map(r -> "OK");
    }
}
