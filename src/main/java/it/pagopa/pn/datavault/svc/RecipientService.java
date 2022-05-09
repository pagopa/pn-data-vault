package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultTokenizerClient;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultUserRegistryClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class RecipientService {


    private final PersonalDataVaultTokenizerClient client;
    private final PersonalDataVaultUserRegistryClient userClient;

    public RecipientService(PersonalDataVaultTokenizerClient client, PersonalDataVaultUserRegistryClient userClient) {
        this.client = client;
        this.userClient = userClient;
    }

    public Mono<String> ensureRecipientByExternalId(RecipientType recipientType, String taxId) {
        return client.ensureRecipientByExternalId( recipientType, taxId );
    }

    public Flux<BaseRecipientDto> getRecipientDenominationByInternalId(List<String> internalId) {
        return  userClient.getRecipientDenominationByInternalId( internalId );
    }
}
