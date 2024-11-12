package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.exceptions.PnDatavaultExceptionCodes;
import it.pagopa.pn.datavault.exceptions.PnInvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientAddressesDto;
import it.pagopa.pn.datavault.mapper.AddressEntityAddressDtoMapper;
import it.pagopa.pn.datavault.middleware.db.AddressDao;
import it.pagopa.pn.datavault.middleware.db.entities.AddressEntity;
import it.pagopa.pn.datavault.utils.ValidationUtils;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

import static it.pagopa.pn.commons.exceptions.PnExceptionsCodes.ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED;

@Service
@CustomLog
public class AddressService {

    private final AddressDao objDao;
    private final AddressEntityAddressDtoMapper addressesMapper;

    public AddressService(AddressDao objDao, AddressEntityAddressDtoMapper addressesMapper) {
        this.objDao = objDao;
        this.addressesMapper = addressesMapper;
    }

    public Mono<RecipientAddressesDto> getAddressByInternalId(String id) {
        return objDao.listAddressesById(id)
                .collectMap(AddressEntity::getAddressId, addressesMapper::toDto)
                .map(m -> {
                    RecipientAddressesDto r = new RecipientAddressesDto();
                    r.addresses(m);
                    log.trace("[exit]");
                    return r;
                } );
    }


    public Mono<String> updateAddressByInternalId(String internalId, String addressId, AddressDto addressDto, BigDecimal ttl) {
        if (! ValidationUtils.checkInternalId(internalId))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "internalId");

        if (! ValidationUtils.checkAddressId(addressId))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "addressId");

        if (! ValidationUtils.checkAddressDto(addressDto))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "addressDto.value");

        AddressEntity me = new AddressEntity(internalId, addressId);
        me.setValue(addressDto.getValue());

        return objDao.updateAddress(me).map(r -> "OK");
    }

    public Mono<String> deleteAddressByInternalId(String internalId, String addressId ) {
        if (! ValidationUtils.checkInternalId(internalId))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "internalId");

        if (! ValidationUtils.checkAddressId(addressId))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "addressDto.value");

        return objDao.deleteAddressId(internalId, addressId).map(r -> "OK");
    }
}
