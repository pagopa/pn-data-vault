package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.api.RecipientsApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.svc.RecipientService;
import it.pagopa.pn.datavault.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@Slf4j
public class RecipientsRestControllerV1 implements RecipientsApi {

    private final RecipientService svc;

    public RecipientsRestControllerV1(RecipientService svc) {
        this.svc = svc;
    }

    @Override
    public Mono<ResponseEntity<String>> ensureRecipientByExternalId(RecipientType recipientType, String taxId, ServerWebExchange exchange) {
        log.info("[enter] ensureRecipientByExternalId recipientType:{} taxId:{}", recipientType, LogUtils.maskTaxId(taxId));
        return svc.ensureRecipientByExternalId( recipientType, taxId )
                .map(body -> {
                    log.debug("[exit] ensureRecipientByExternalId");
                    return ResponseEntity.ok(body);
                });
    }

    @Override
    public Mono<ResponseEntity<Flux<BaseRecipientDto>>> getRecipientDenominationByInternalId(List<String> internalId, ServerWebExchange exchange) {
        log.info("[enter] getRecipientDenominationByInternalId internalIds:{}", internalId);
        return Mono.fromSupplier( () -> {
                    log.debug("[exit] getRecipientDenominationByInternalId");
                    return ResponseEntity.ok(svc.getRecipientDenominationByInternalId(internalId));
                }
        );
    }

}
