package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.api.NotificationsApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.NotificationRecipientAddressesDto;
import it.pagopa.pn.datavault.svc.PnDataVaultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
public class NotificationsRestControllerV1 implements NotificationsApi {

    private final PnDataVaultService svc;

    public NotificationsRestControllerV1(PnDataVaultService svc) {
        this.svc = svc;
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteNotificationAddressesByIun(String iun, ServerWebExchange exchange) {
        return this.svc.deleteNotificationAddressesByIun( iun )
                .map( result -> ResponseEntity.ok(null));
    }

    @Override
    public Mono<ResponseEntity<Flux<NotificationRecipientAddressesDto>>> getNotificationAddressesByIun(String iun, ServerWebExchange exchange) {
        return this.svc.getNotificationAddressesByIun( iun )
                .map( optionalResult ->
                   optionalResult
                           .map( result -> ResponseEntity.ok( result ))
                           .orElse( ResponseEntity.<Flux<NotificationRecipientAddressesDto>>notFound().build() )
                );
    }

    @Override
    public Mono<ResponseEntity<Void>> updateNotificationAddressesByIun(String iun, Flux<NotificationRecipientAddressesDto> notificationRecipientAddressesDto, ServerWebExchange exchange) {
        return notificationRecipientAddressesDto
                    .collectList()
                    .flatMap( dtoList -> {
                        NotificationRecipientAddressesDto[] dtoArray;
                        dtoArray = dtoList.toArray( new NotificationRecipientAddressesDto[0] );
                        return svc.updateNotificationAddressesByIun( iun, dtoArray );
                    })
                    .map( updateResult -> ResponseEntity.ok(null));
    }
}
