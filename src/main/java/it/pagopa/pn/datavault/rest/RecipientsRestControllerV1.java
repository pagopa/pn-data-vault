package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.generated.openapi.server.v1.api.AddressBookApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.api.MandatesApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.api.NotificationsApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.api.RecipientsApi;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientAddressesDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.svc.PnDataVaultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
public class RecipientsRestControllerV1 implements RecipientsApi {

    private final PnDataVaultService svc;

    public RecipientsRestControllerV1(PnDataVaultService svc) {
        this.svc = svc;
    }

    @Override
    public Mono<ResponseEntity<String>> ensureRecipientByExternalId(RecipientType recipientType, String taxId, ServerWebExchange exchange) {
        return svc.ensureRecipientByExternalId( recipientType, taxId )
                .map( internalId -> ResponseEntity.ok( internalId ));
    }

    @Override
    public Mono<ResponseEntity<Flux<BaseRecipientDto>>> getRecipientDenominationByInternalId(List<String> internalId, ServerWebExchange exchange) {
        return Mono.fromSupplier( () ->
           ResponseEntity.ok( svc.getRecipientsDenominationsByInternalIds( internalId ) )
        );
    }

}
