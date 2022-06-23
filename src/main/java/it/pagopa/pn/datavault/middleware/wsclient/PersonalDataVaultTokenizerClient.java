package it.pagopa.pn.datavault.middleware.wsclient;


import io.netty.handler.timeout.TimeoutException;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.exceptions.NotFoundException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.ApiClient;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.api.TokenApi;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.dto.PiiResourceDto;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.dto.UserResourceDto;
import it.pagopa.pn.datavault.middleware.wsclient.common.BaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;

/**
 * Classe wrapper di personal-data-vault TOKENIZER, con gestione del backoff
 */
@Component
@Slf4j
public class PersonalDataVaultTokenizerClient extends BaseClient {

    private final TokenApi tokenApiPF;
    private final TokenApi tokenApiPG;
    private final PnDatavaultConfig pnDatavaultConfig;


    public PersonalDataVaultTokenizerClient(PnDatavaultConfig pnDatavaultConfig, PnDatavaultConfig pnDatavaultConfig1){
        // creo 2 istanze diverse, una per PF e l'altra per PG, perchè la differenza
        // sta in un header che viene spedito. Tale header, che è una costante
        // vien definita in fase di creazione, e quindi evito ogni volta di scriverlo.
        this.tokenApiPF = new TokenApi(initApiClient(pnDatavaultConfig.getTokenizerApiKeyPf(), pnDatavaultConfig.getClientTokenizerBasepath()));
        this.tokenApiPG = new TokenApi(initApiClient(pnDatavaultConfig.getTokenizerApiKeyPg(), pnDatavaultConfig.getClientTokenizerBasepath()));
        this.pnDatavaultConfig = pnDatavaultConfig1;
    }

    /**
     * Produce un id OPACO a partire da taxid (CF/PIVA)
     * @param recipientType tipo di utente
     * @param taxId id dell'utente in chiaro
     * @return id opaco
     */
    public Mono<String> ensureRecipientByExternalId(RecipientType recipientType, String taxId)
    {
        log.info("[enter] ensureRecipientByExternalId taxid={}", LogUtils.maskTaxId(taxId));
        if (pnDatavaultConfig.isDevelopment())
        {
            log.warn("DEVELOPMENT IS ACTIVE, MOCKING REQUEST!!!!");
            return Mono.just(encapsulateRecipientType(recipientType, reverseString(taxId)));
        }

        PiiResourceDto pii = new PiiResourceDto();
        pii.setPii(taxId);
        return this.getTokeApiForRecipientType(recipientType)
                    .saveUsingPUT(pii)
                    .retryWhen(
                            Retry.backoff(2, Duration.ofSeconds(1)).jitter(0.75)
                                    .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                    )
                    .map(r -> {
                        if (r == null)
                        {
                            log.error("Invalid empty response from tokenizer");
                            throw new NotFoundException();
                        }

                        String res = encapsulateRecipientType(recipientType, r.getToken().toString());
                        log.debug("[exit] ensureRecipientByExternalId token={}", res);
                        return  res;
                    });
    }

    public Mono<UserResourceDto> findPii(String internalId)
    {
        log.info("[enter] findPii token={}", internalId);
        return this.getTokeApiForRecipientType(getRecipientTypeFromInternalId(internalId))
                .findPiiUsingGET(getUUIDFromInternalId(internalId))
                .retryWhen(
                        Retry.backoff(2, Duration.ofSeconds(1)).jitter(0.75)
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .map(r -> {
                    UserResourceDto brd = new UserResourceDto();
                    brd.setId(getUUIDFromInternalId(internalId));
                    brd.setFiscalCode(r.getPii());
                    log.debug("[exit] findPii token={}", LogUtils.maskTaxId(r.getPii()));
                    return  brd;
                });
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

    private ApiClient initApiClient(String apiKey, String basepath)
    {
         ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), apiKey));
         apiClient.setBasePath(basepath);
         return  apiClient;
    }

    private String reverseString(String inputvalue) {
        byte[] strAsByteArray = inputvalue.getBytes();
        byte[] resultoutput = new byte[strAsByteArray.length];
        for (int i = 0; i < strAsByteArray.length; i++)
            resultoutput[i] = strAsByteArray[strAsByteArray.length - i - 1];

        return new String(resultoutput);
    }
}
