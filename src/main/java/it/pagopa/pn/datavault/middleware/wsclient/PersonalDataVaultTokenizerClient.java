package it.pagopa.pn.datavault.middleware.wsclient;


import io.netty.handler.timeout.TimeoutException;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.ApiClient;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.api.TokenApi;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.dto.PiiResourceDto;
import it.pagopa.pn.datavault.middleware.wsclient.common.BaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;

/**
 * Classe wrapper di personal-data-vault TOKENIZER, con gestione del backoff
 */
@Component
public class PersonalDataVaultTokenizerClient extends BaseClient {

    private final TokenApi tokenApiPF;
    private final TokenApi tokenApiPG;

    public PersonalDataVaultTokenizerClient(PnDatavaultConfig pnDatavaultConfig){
        // creo 2 istanze diverse, una per PF e l'altra per PG, perchè la differenza
        // sta in un header che viene spedito. Tale header, che è una costante
        // vien definita in fase di creazione, e quindi evito ogni volta di scriverlo.
        this.tokenApiPF = new TokenApi(initApiClient(true, pnDatavaultConfig.getClientTokenizerBasepath()));
        this.tokenApiPG = new TokenApi(initApiClient(false, pnDatavaultConfig.getClientTokenizerBasepath()));
    }

    /**
     * Produce un id OPACO a partire da taxid (CF/PIVA)
     * @param recipientType tipo di utente
     * @param taxId id dell'utente in chiaro
     * @return id opaco
     */
    public Mono<String> ensureRecipientByExternalId(RecipientType recipientType, String taxId)
    {
        PiiResourceDto pii = new PiiResourceDto();
        pii.setPii(taxId);
        return this.getTokeApiForRecipientType(recipientType)
                    .saveUsingPUT(pii)
                    .retryWhen(
                            Retry.backoff(2, Duration.ofMillis(25))
                                    .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                    )
                    .map(r -> encapsulateRecipientType(recipientType, r.getToken().toString()));
    }

    /**
     * Ritorna un internal id "modificato" che contiene anche l'informazione di PF/PG
     * In questo modo, il token che gira all'interno di PN è parlante (perchè devo sapere per quale namespace vado a risolvere
     * il token in userregistry)
     *
     * @param recipientType il tipo di utente
     * @param internalId internal id
     * @return internal id modificato
     */
    private String encapsulateRecipientType(RecipientType recipientType, String internalId)
    {
        return recipientType.getValue() + "-" + internalId;
    }

    /**
     * Ritorna il client corretto in base al tipo di utente
     * @param recipientType tipo di utente
     * @return il client associato
     */
    private TokenApi getTokeApiForRecipientType(RecipientType recipientType)
    {
        if (recipientType == RecipientType.PF)
            return this.tokenApiPF;
        else
            return this.tokenApiPG;
    }

    private ApiClient initApiClient(boolean isPF, String basepath)
    {
         ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), isPF));
         apiClient.setBasePath(basepath);
         return  apiClient;
    }

}
