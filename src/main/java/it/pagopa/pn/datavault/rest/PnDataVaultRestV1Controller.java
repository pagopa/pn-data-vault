package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.api.ByExternalIdApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.api.ByInternalIdApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.datavault.svc.PnDataVaultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
public class PnDataVaultRestV1Controller implements ByExternalIdApi, ByInternalIdApi {

    private final PnDataVaultService svc;

    public PnDataVaultRestV1Controller(PnDataVaultService svc) {
        this.svc = svc;
    }

    @Override
    public Mono<ResponseEntity<String>> ensureRecipientByExternalId(RecipientType recipientType, String taxId, ServerWebExchange exchange) {
        return Mono.fromFuture( svc.ensureRecipientByExternalId( recipientType, taxId )
                .thenApply( internalId -> ResponseEntity.ok( internalId )) );
    }

    @Override
    public Mono<ResponseEntity<RecipientMandatesDto>> getRecipientMandatesByInternalId(String internalId, ServerWebExchange exchange) {
        return Mono.fromFuture( svc.getMandatesByInternalId( internalId )
            .thenApply( optionalDto ->
                        optionalDto.map( dto -> ResponseEntity.ok( dto ))
                            .orElse( ResponseEntity.notFound().build() )
                    ));
    }

    @Override
    public Mono<ResponseEntity<String>> updateRecipientMandateByInternalId(String internalId, String mandateId, Mono<AddressAndDenominationDto> addressDtoMono, ServerWebExchange exchange) {
        return addressDtoMono.flatMap( addressDto -> Mono.fromFuture(
                svc.updateMandate( internalId, mandateId, addressDto )
                    .thenApply( returnedId -> ResponseEntity.ok( returnedId ))
            ));
    }

    @Override
    public Mono<ResponseEntity<RecipientAddressesDto>> getRecipientAddressesByInternalId(String internalId, ServerWebExchange exchange) {
        return Mono.fromFuture( svc.getAddressesByInternalId( internalId )
                .thenApply( optionalDto ->
                        optionalDto.map( dto -> ResponseEntity.ok( dto ))
                                .orElse( ResponseEntity.notFound().build() )
                ));
    }

    @Override
    public Mono<ResponseEntity<String>> updateRecipientAddressByInternalId(String internalId, String addressId, Mono<AddressDto> addressDtoMono, ServerWebExchange exchange) {
        return addressDtoMono.flatMap( addressDto -> Mono.fromFuture(
                svc.updateAddress( internalId, addressId, addressDto )
                        .thenApply( returnedId -> ResponseEntity.ok( returnedId ))
        ));
    }

    @Override
    public Mono<ResponseEntity<Flux<NotificationRecipientAddressesDto>>> getNotificationAddressesByIun(String iun, ServerWebExchange exchange) {
        return Mono.fromFuture(
                svc.getNotificationByIun( iun )
                        .thenApply( optionalDto ->
                            optionalDto.map( dto -> ResponseEntity.ok( Flux.fromArray( dto ) ))
                                    .orElseGet( () -> ResponseEntity.notFound().build() )
                        )
            );
    }

    @Override
    public Mono<ResponseEntity<String>> updateNotificationAddressesByIun(String iun, Flux<NotificationRecipientAddressesDto> notificationRecipientAddressesDtoFlux, ServerWebExchange exchange) {
        return notificationRecipientAddressesDtoFlux
                    .collectList()
                    .flatMap( notificationAddressesDto -> {
                        NotificationRecipientAddressesDto[] toArray = notificationAddressesDto.toArray(new NotificationRecipientAddressesDto[0]);
                        return Mono.fromFuture(
                                svc.updateNotification( iun, toArray )
                                    .thenApply( returnedId -> ResponseEntity.ok( returnedId ) )
                            );
                    });
    }
}
