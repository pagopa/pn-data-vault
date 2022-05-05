package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.api.AddressBookApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientAddressesDto;
import it.pagopa.pn.datavault.svc.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@RestController
@Slf4j
public class AddressBookRestControllerV1 implements AddressBookApi {

    private final AddressService svc;

    public AddressBookRestControllerV1(AddressService svc) {
        this.svc = svc;
    }

    @Override
    public Mono<ResponseEntity<Void>> updateRecipientAddressByInternalId(String internalId, String addressId, Mono<AddressDto> addressDto, ServerWebExchange exchange) {
        log.info("[enter] internalid:{} addressid:{}", internalId, addressId);
        return addressDto
                    .flatMap( addressDtoValue ->
                            svc.updateAddressByInternalId( internalId, addressId, addressDtoValue)
                    )
                    .map( updateResult -> {
                        log.trace("[exit]");
                        return ResponseEntity.noContent().build();
                    } );
    }

    @Override
    public Mono<ResponseEntity<RecipientAddressesDto>> getRecipientAddressesByInternalId(String internalId, ServerWebExchange exchange) {
        log.info("[enter] internalid:{}", internalId);
        return svc.getAddressByInternalId( internalId )
                .map(body -> {
                    log.trace("[exit]");
                    return ResponseEntity.ok(body);
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteRecipientAddressByInternalId(String internalId, String addressId, ServerWebExchange exchange) {
        log.info("[enter] internalid:{} addressid:{}", internalId, addressId);
        return svc.deleteAddressByInternalId( internalId, addressId )
                .map( dto -> {
                    log.trace("[exit]");
                    return ResponseEntity.noContent().build();
                });
    }


}
