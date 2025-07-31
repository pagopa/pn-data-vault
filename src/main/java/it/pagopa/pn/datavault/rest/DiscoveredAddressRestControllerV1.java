package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.datavault.generated.openapi.server.v1.api.DiscoveredAddressesApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.api.MandatesApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DenominationDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DiscoveredAddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MandateDto;
import it.pagopa.pn.datavault.svc.DiscoveredAddressService;
import it.pagopa.pn.datavault.svc.MandateService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@Slf4j
public class DiscoveredAddressRestControllerV1 implements DiscoveredAddressesApi {

    private static final String EXIT_LOG = "[exit]";

    private final DiscoveredAddressService svc;

    public DiscoveredAddressRestControllerV1(DiscoveredAddressService svc) {
        this.svc = svc;
    }

    @Override
    public Mono<ResponseEntity<DiscoveredAddressDto>> getDiscoveredAddressById(String discoveredAddressId,
                                                                                 final ServerWebExchange exchange) {
        log.info("[enter] discoveredAddressesId:{}", discoveredAddressId);
        return svc.getDiscoveredAddressById( discoveredAddressId )
                .map(body -> {
                    log.debug(EXIT_LOG);
                    return ResponseEntity.ok(body);
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> updateDiscoveredAddressById(String discoveredAddressId,
                                                           Mono<DiscoveredAddressDto> discoveredAddressDto,
                                                           final ServerWebExchange exchange) {
        log.info("[enter] discoveredAddressId:{}", discoveredAddressId);
        return discoveredAddressDto
                .flatMap(dtoValue ->
                        svc.updateDiscoveredAddressById( discoveredAddressId, dtoValue))
                .map(body -> {
                    log.debug(EXIT_LOG);
                    return ResponseEntity.noContent().build();
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteDiscoveredAddressById(String discoveredAddressId,  final ServerWebExchange exchange) {
        log.info("[enter] discoveredAddressesId:{}", discoveredAddressId);
        return svc.deleteDiscoveredAddressById(discoveredAddressId)
                .map( dto -> {
                    log.debug(EXIT_LOG);
                    return ResponseEntity.noContent().build();
                });
    }


}
