package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.api.MandatesApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DenominationDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MandateDto;
import it.pagopa.pn.datavault.svc.MandateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@Slf4j
public class MandatesRestControllerV1 implements MandatesApi {

    private final MandateService svc;

    public MandatesRestControllerV1(MandateService svc) {
        this.svc = svc;
    }

    @Override
    public Mono<ResponseEntity<Void>> updateMandateById(String mandateId, Mono<DenominationDto> addressAndDenominationDto, ServerWebExchange exchange) {
        log.info("[enter] mandateid:{}", mandateId);
        return addressAndDenominationDto
                .flatMap( dtoValue -> svc.updateMandateByInternalId( mandateId, dtoValue))
                .map( updateResult -> {
                    log.trace("[exit]");
                    return ResponseEntity.noContent().build();
                });
    }

    @Override
    public Mono<ResponseEntity<Flux<MandateDto>>> getMandatesByIds(List<String> mandateId, ServerWebExchange exchange) {
        log.info("[enter] mandateid:{}", mandateId);
        return Mono.fromSupplier( () -> {
                    log.trace("[exit]");
                    return ResponseEntity.ok(svc.getMandatesByInternalIds(mandateId));
                }
        );
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteMandateById(String mandateId, ServerWebExchange exchange) {
        log.info("[enter] mandateid:{}", mandateId);
        return svc.deleteMandateByInternalId( mandateId )
                .map( result -> {
                    log.trace("[exit]");
                    return ResponseEntity.noContent().build();
                });
    }

}
