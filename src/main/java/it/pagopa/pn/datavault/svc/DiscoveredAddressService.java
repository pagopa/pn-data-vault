package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.exceptions.PnInvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DenominationDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DiscoveredAddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MandateDto;
import it.pagopa.pn.datavault.mapper.DiscoveredAddressEntityDiscoveredAddressDtoMapper;
import it.pagopa.pn.datavault.mapper.MandateEntityMandateDtoMapper;
import it.pagopa.pn.datavault.middleware.db.DiscoveredAddressDao;
import it.pagopa.pn.datavault.middleware.db.MandateDao;
import it.pagopa.pn.datavault.middleware.db.entities.DiscoveredAddressEntity;
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
public class DiscoveredAddressService {

    private final DiscoveredAddressDao objDao;
    private final DiscoveredAddressEntityDiscoveredAddressDtoMapper mappingsDao;

    public DiscoveredAddressService(DiscoveredAddressDao objDao, DiscoveredAddressEntityDiscoveredAddressDtoMapper mappingsDao) {
        this.objDao = objDao;
        this.mappingsDao = mappingsDao;
    }

    public Mono<DiscoveredAddressDto> getDiscoveredAddressById(String discoveredAddressId) {
        return objDao.getDiscoveredAddressById(discoveredAddressId).map(mappingsDao::toDto);
    }


    public Mono<String> updateDiscoveredAddressById(String discoveredAddressId, DiscoveredAddressDto addressDto) {
//        if (! ValidationUtils.checkMandateId(mandateId))
//            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "mandateId");
//
//        if (! ValidationUtils.checkDenominationDto(addressDto))
//            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "destName");

        DiscoveredAddressEntity entity = mappingsDao.toEntity(discoveredAddressId, addressDto);

        return objDao.updateMandate(entity).map(r -> "OK");
    }

    public Mono<String> deleteDiscoveredAddressById(String discoveredAddressId) {
//        if (! ValidationUtils.checkMandateId(mandateId))
//            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "mandateId");

        return objDao.deleteMandateId(discoveredAddressId).map(r -> "OK");
    }
}
