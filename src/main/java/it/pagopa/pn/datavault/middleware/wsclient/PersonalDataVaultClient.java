package it.pagopa.pn.datavault.middleware.wsclient;


import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe wrapper di personal-data-vault, con gestione del backoff
 */
@Component
public class PersonalDataVaultClient {

    // FIXME:  ovviamente fare le dovute implementazioni non fake

    public PersonalDataVaultClient(PnDatavaultConfig pnMandateConfig){
    }

    public Flux<BaseRecipientDto> getRecipientDenominationByInternalId(List<String> internalIds)
    {
       List<BaseRecipientDto> r = new ArrayList<>();
       internalIds.forEach(x -> {
           BaseRecipientDto br = new BaseRecipientDto();
           br.setInternalId(x);
           br.setRecipientType(RecipientType.PF);
           br.setDenomination("nome cognome fake di" + x);
           r.add(br);
       });
        return Flux.fromIterable(r);
    }

    public Mono<String> ensureRecipientByExternalId(RecipientType recipientType, String taxId)
    {
        return Mono.just(reverseString(taxId));
    }

    public static String reverseString(String str){
        StringBuilder sb=new StringBuilder(str);
        sb.reverse();
        return sb.toString();
    }

}
