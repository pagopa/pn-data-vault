package it.pagopa.pn.datavault.middleware.wsclient;


import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Classe wrapper di pn-data-vault, con gestione del backoff
 */
@Component
public class PnDataVaultClient {

    // TODO

    public PnDataVaultClient(PnDatavaultConfig pnMandateConfig){
    }

    public Flux<BaseRecipientDto> getRecipientDenominationByInternalId(List<String> internalIds)
    {
       return null;
    }

    public Mono<String> ensureRecipientByExternalId(RecipientType recipientType, String taxId)
    {
        return null;
    }


}
