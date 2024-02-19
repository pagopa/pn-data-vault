package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.datavault.generated.openapi.server.v1.api.NotificationsApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementId;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.NotificationRecipientAddressesDto;
import it.pagopa.pn.datavault.svc.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
        MDC.put(MDCUtils.MDC_PN_IUN_KEY, iun);
        log.info("[enter] deleteNotificationByIun internalid:{}", iun);
        return this.svc.deleteNotificationByIun( iun )
                .map( result -> {
                    log.debug("[exit] deleteNotificationByIun");
                    return ResponseEntity.noContent().build();
                });
    }

    @Override
    public Mono<ResponseEntity<Flux<NotificationRecipientAddressesDto>>> getNotificationAddressesByIun(String iun, Boolean normalized, ServerWebExchange exchange) {
        MDC.put(MDCUtils.MDC_PN_IUN_KEY, iun);
        log.info("[enter] getNotificationAddressesByIun internalid:{}", iun);
        return MDCUtils.addMDCToContextAndExecute(this.svc.getNotificationAddressesByIun( iun, normalized )
                .collectList()
                .map( result -> {
                    log.debug("[exit] getNotificationAddressesByIun");
                    return result.isEmpty() ? ResponseEntity.notFound().build() :
                            ResponseEntity.ok(Flux.fromIterable(result));
                }));
    }

    @Override
    public Mono<ResponseEntity<Void>> updateNotificationAddressesByIun(String iun, Boolean normalized, Flux<NotificationRecipientAddressesDto> notificationRecipientAddressesDto, ServerWebExchange exchange) {
        MDC.put(MDCUtils.MDC_PN_IUN_KEY, iun);
        log.info("[enter] updateNotificationAddressesByIun internalid:{}", iun);
        return MDCUtils.addMDCToContextAndExecute(notificationRecipientAddressesDto
                    .collectList()
                    .flatMap( dtoList -> {
                        NotificationRecipientAddressesDto[] dtoArray;
                        dtoArray = dtoList.toArray( new NotificationRecipientAddressesDto[0] );
                        return svc.updateNotificationAddressesByIun( iun, dtoArray, normalized );
                    })
                    .map( updateResult -> {
                        log.debug("[exit] updateNotificationAddressesByIun");
                        return ResponseEntity.noContent().build();
                    }));
    }

    @Override
    public Mono<ResponseEntity<Flux<ConfidentialTimelineElementDto>>> getNotificationTimelineByIun(String iun, ServerWebExchange exchange) {
        MDC.put(MDCUtils.MDC_PN_IUN_KEY, iun);
        log.info("[enter] getNotificationTimelineByIun internalid:{}", iun);
        return MDCUtils.addMDCToContextAndExecute(this.svc.getNotificationTimelineByIun( iun )
                .collectList()
                .map( result -> {
                    log.debug("[exit] getNotificationTimelineByIun");
                    return ResponseEntity.ok(Flux.fromIterable(result));
                }));
    }

    @Override
    public Mono<ResponseEntity<ConfidentialTimelineElementDto>> getNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId, ServerWebExchange exchange) {
        MDC.put(MDCUtils.MDC_PN_IUN_KEY, iun);
        MDC.put(MDCUtils.MDC_PN_CTX_REQUEST_ID, timelineElementId);
        log.info("[enter] getNotificationTimelineByIunAndTimelineElementId internalid:{} timeelementid:{}", iun, timelineElementId);
        return MDCUtils.addMDCToContextAndExecute(this.svc.getNotificationTimelineByIunAndTimelineElementId( iun, timelineElementId )
                .map(body -> {
                    log.debug("[exit] getNotificationTimelineByIunAndTimelineElementId");
                    return ResponseEntity.ok(body);
                }));
    }

    @Override
    public Mono<ResponseEntity<Void>> updateNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId, Mono<ConfidentialTimelineElementDto> confidentialTimelineElementDto, ServerWebExchange exchange) {
        MDC.put(MDCUtils.MDC_PN_IUN_KEY, iun);
        MDC.put(MDCUtils.MDC_PN_CTX_REQUEST_ID, timelineElementId);
        log.info("[enter] updateNotificationTimelineByIunAndTimelineElementId internalid:{} timeelementid:{}", iun, timelineElementId);
        return MDCUtils.addMDCToContextAndExecute(confidentialTimelineElementDto.flatMap( dto -> svc.updateNotificationTimelineByIunAndTimelineElementId( iun, timelineElementId, dto ))
                .map( updateResult -> {
                    log.debug("[exit] updateNotificationTimelineByIunAndTimelineElementId");
                    return ResponseEntity.noContent().build();
                }));
    }

    @Override
    public Mono<ResponseEntity<Flux<ConfidentialTimelineElementDto>>> getNotificationTimelines(Flux<ConfidentialTimelineElementId> confidentialTimelineElementId, ServerWebExchange exchange) {
        log.info("[enter] getNotificationTimelines");
        return MDCUtils.addMDCToContextAndExecute(this.svc.getNotificationTimelines(confidentialTimelineElementId)
                .collectList()
                .map(confidentialTimelineElementDtos -> {
                    log.debug("[exit] getNotificationTimelines");
                    return ResponseEntity.ok(Flux.fromIterable(confidentialTimelineElementDtos));
                }));
    }
}
