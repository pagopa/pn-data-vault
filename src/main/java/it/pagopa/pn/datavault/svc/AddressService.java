package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.exceptions.InvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientAddressesDto;
import it.pagopa.pn.datavault.mapper.AddressEntityAddressDtoMapper;
import it.pagopa.pn.datavault.middleware.db.AddressDao;
import it.pagopa.pn.datavault.middleware.db.entities.AddressEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Service
@Slf4j
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


    public Mono<String> updateAddressByInternalId(String internalId, String addressId, AddressDto addressDto) {
        if (!StringUtils.hasText(internalId))
            throw new InvalidInputException("internalId is required");
        if (!StringUtils.hasText(addressId))
            throw new InvalidInputException("addressId is required");
        if (addressDto == null || !StringUtils.hasText(addressDto.getValue()))
            throw new InvalidInputException("address.value is required");

        AddressEntity me = new AddressEntity(internalId, addressId);
        me.setValue(addressDto.getValue());

        return objDao.updateAddress(me).map(r -> "OK");
    }

    public Mono<String> deleteAddressByInternalId(String internalId, String addressId ) {
        if (!StringUtils.hasText(internalId))
            throw new InvalidInputException("internalId is required");
        if (!StringUtils.hasText(addressId))
            throw new InvalidInputException("addressId is required");

        return objDao.deleteAddressId(internalId, addressId).map(r -> "OK");
    }
}
