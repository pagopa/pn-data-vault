package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.api.AddressBookApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientAddressesDto;
import it.pagopa.pn.datavault.svc.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@RestController
public class AddressBookRestControllerV1 implements AddressBookApi {

    private final AddressService svc;

    public AddressBookRestControllerV1(AddressService svc) {
        this.svc = svc;
    }

    @Override
    public Mono<ResponseEntity<Void>> updateRecipientAddressByInternalId(String internalId, String addressId, Mono<AddressDto> addressDto, ServerWebExchange exchange) {
        return addressDto
                    .flatMap( addressDtoValue ->
                            svc.updateAddressByInternalId( internalId, addressId, addressDtoValue)
                    )
                    .map( updateResult -> ResponseEntity.noContent().build() );
    }

    @Override
    public Mono<ResponseEntity<RecipientAddressesDto>> getRecipientAddressesByInternalId(String internalId, ServerWebExchange exchange) {
        return svc.getAddressByInternalId( internalId )
                .map( ResponseEntity::ok );
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteRecipientAddressByInternalId(String internalId, String addressId, ServerWebExchange exchange) {
        return svc.deleteAddressByInternalId( internalId, addressId )
                .map( dto -> ResponseEntity.noContent().build());
    }


}
