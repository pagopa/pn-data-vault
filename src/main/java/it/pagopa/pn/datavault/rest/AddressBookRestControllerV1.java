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
import java.math.BigDecimal;


@RestController
@Slf4j
public class AddressBookRestControllerV1 implements AddressBookApi {

    private static final String EXIT_LOG = "[exit]";

    private final AddressService svc;

    public AddressBookRestControllerV1(AddressService svc) {
        this.svc = svc;
    }

    @Override
    public Mono<ResponseEntity<Void>> updateRecipientAddressByInternalId(String internalId, String addressId, BigDecimal ttl, Mono<AddressDto> addressDto, ServerWebExchange exchange) {
        log.info("[enter] internalid:{} addressid:{} ttl:{}", internalId, addressId, ttl);
        return addressDto
                    .flatMap( addressDtoValue ->
                            svc.updateAddressByInternalId( internalId, addressId, addressDtoValue, null)
                    )
                    .map( updateResult -> {
                        log.debug(EXIT_LOG);
                        return ResponseEntity.noContent().build();
                    } );
    }

    @Override
    public Mono<ResponseEntity<RecipientAddressesDto>> getRecipientAddressesByInternalId(String internalId, ServerWebExchange exchange) {
        log.info("[enter] internalid:{}", internalId);
        return svc.getAddressByInternalId( internalId )
                .map(body -> {
                    log.debug(EXIT_LOG);
                    return ResponseEntity.ok(body);
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteRecipientAddressByInternalId(String internalId, String addressId, ServerWebExchange exchange) {
        log.info("[enter] internalid:{} addressid:{}", internalId, addressId);
        return svc.deleteAddressByInternalId( internalId, addressId )
                .map( dto -> {
                    log.debug(EXIT_LOG);
                    return ResponseEntity.noContent().build();
                });
    }


}
