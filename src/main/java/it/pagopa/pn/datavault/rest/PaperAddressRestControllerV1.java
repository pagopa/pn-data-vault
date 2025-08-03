package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.api.PaperAddressesApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddress;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddresses;
import it.pagopa.pn.datavault.svc.PaperAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class PaperAddressRestControllerV1 implements PaperAddressesApi {

    private static final String EXIT_LOG = "[exit]";

    private final PaperAddressService paperAddressService;

    public PaperAddressRestControllerV1(PaperAddressService paperAddressService) {
        this.paperAddressService = paperAddressService;
    }

    @Override
    public Mono<ResponseEntity<PaperAddresses>> getPaperAddressesByPaperRequestId(String paperRequestId,
                                                                                  ServerWebExchange exchange) {
        log.info("[enter] paperRequestId:{}", paperRequestId);

        return paperAddressService.getPaperAddressesByPaperRequestId(paperRequestId)
                .map(body -> {
                    log.debug(EXIT_LOG);
                    return ResponseEntity.ok(body);
                });
    }

    @Override
    public Mono<ResponseEntity<PaperAddress>> getPaperAddressByIds(String paperRequestId,
                                                                   String paperAddressId,
                                                                   ServerWebExchange exchange) {
        log.info("[enter] paperRequestId:{} paperAddressId:{}", paperRequestId, paperAddressId);

        return paperAddressService.getPaperAddressByIds(paperRequestId, paperAddressId)
                .map(body -> {
                    log.debug(EXIT_LOG);
                    return ResponseEntity.ok(body);
                })
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    log.debug("Paper address not found for paperRequestId: {} and paperAddressId: {}",
                            paperRequestId, paperAddressId);
                    return ResponseEntity.badRequest().build();
                }));
    }

    @Override
    public Mono<ResponseEntity<Void>> updatePaperAddress(String paperRequestId,
                                                         String paperAddressId,
                                                         Mono<PaperAddress> paperAddress,
                                                         ServerWebExchange exchange) {
        log.info("[enter] paperRequestId:{} paperAddressId:{}", paperRequestId, paperAddressId);

        return paperAddress
                .flatMap(addressValue ->
                        paperAddressService.updatePaperAddress(paperRequestId, paperAddressId, addressValue)
                )
                .then(Mono.fromSupplier(() -> {
                    log.debug(EXIT_LOG);
                    return ResponseEntity.noContent().build();
                }));
    }

    @Override
    public Mono<ResponseEntity<Void>> deletePaperAddress(String paperRequestId, String paperAddressId,
                                                         ServerWebExchange exchange) {
        log.info("[enter] paperRequestId:{} paperAddressId:{}", paperRequestId, paperAddressId);

        return paperAddressService.deletePaperAddress(paperRequestId, paperAddressId)
                .hasElement()
                .map(exists -> {
                    if (exists) {
                        log.debug(EXIT_LOG);
                        return ResponseEntity.noContent().build();
                    } else {
                        log.debug("Paper address not found for deletion - paperRequestId: {} and paperAddressId: {}",
                                paperRequestId, paperAddressId);
                        return ResponseEntity.badRequest().build();
                    }
                });
    }
}