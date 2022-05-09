package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.api.NotificationsApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.NotificationRecipientAddressesDto;
import it.pagopa.pn.datavault.svc.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@Slf4j
public class NotificationsRestControllerV1 implements NotificationsApi {

    private final NotificationService svc;

    public NotificationsRestControllerV1(NotificationService svc) {
        this.svc = svc;
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteNotificationByIun(String iun, ServerWebExchange exchange) {
        log.info("[enter] internalid:{}", iun);
        return this.svc.deleteNotificationByIun( iun )
                .map( result -> {
                    log.trace("[exit]");
                    return ResponseEntity.noContent().build();
                });
    }

    @Override
    public Mono<ResponseEntity<Flux<NotificationRecipientAddressesDto>>> getNotificationAddressesByIun(String iun, ServerWebExchange exchange) {
        log.info("[enter] internalid:{}", iun);
        return this.svc.getNotificationAddressesByIun( iun )
                .collectList()
                .map( result -> {
                    log.trace("[exit]");
                    return result.isEmpty() ? ResponseEntity.notFound().build() :
                            ResponseEntity.ok(Flux.fromIterable(result));
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> updateNotificationAddressesByIun(String iun, Flux<NotificationRecipientAddressesDto> notificationRecipientAddressesDto, ServerWebExchange exchange) {
        log.info("[enter] internalid:{}", iun);
        return notificationRecipientAddressesDto
                    .collectList()
                    .flatMap( dtoList -> {
                        NotificationRecipientAddressesDto[] dtoArray;
                        dtoArray = dtoList.toArray( new NotificationRecipientAddressesDto[0] );
                        return svc.updateNotificationAddressesByIun( iun, dtoArray );
                    })
                    .map( updateResult -> {
                        log.trace("[exit]");
                        return ResponseEntity.noContent().build();
                    });
    }

    @Override
    public Mono<ResponseEntity<Flux<ConfidentialTimelineElementDto>>> getNotificationTimelineByIun(String iun, ServerWebExchange exchange) {
        log.info("[enter] internalid:{}", iun);
        return this.svc.getNotificationTimelineByIun( iun )
                .collectList()
                .map( result -> {
                    log.trace("[exit]");
                    return result.isEmpty() ? ResponseEntity.notFound().build() :
                            ResponseEntity.ok(Flux.fromIterable(result));
                });
    }

    @Override
    public Mono<ResponseEntity<ConfidentialTimelineElementDto>> getNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId, ServerWebExchange exchange) {
        log.info("[enter] internalid:{} timeelementid:{}", iun, timelineElementId);
        return this.svc.getNotificationTimelineByIunAndTimelineElementId( iun, timelineElementId )
                .map(body -> {
                    log.trace("[exit]");
                    return ResponseEntity.ok(body);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.<Flux<NotificationRecipientAddressesDto>>notFound().build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> updateNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId, Mono<ConfidentialTimelineElementDto> confidentialTimelineElementDto, ServerWebExchange exchange) {
        log.info("[enter] internalid:{} timeelementid:{}", iun, timelineElementId);
        return confidentialTimelineElementDto.flatMap( dto -> svc.updateNotificationTimelineByIunAndTimelineElementId( iun, timelineElementId, dto ))
                .map( updateResult -> {
                    log.trace("[exit]");
                    return ResponseEntity.noContent().build();
                });
    }


}
