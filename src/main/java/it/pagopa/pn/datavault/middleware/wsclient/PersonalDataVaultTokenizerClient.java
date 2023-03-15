package it.pagopa.pn.datavault.middleware.wsclient;


import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.exceptions.PnDatavaultRecipientNotFoundException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.ApiClient;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.api.TokenApi;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.dto.PiiResourceDto;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.dto.UserResourceDto;
import it.pagopa.pn.datavault.middleware.wsclient.common.BaseClient;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import it.pagopa.pn.datavault.utils.RecipientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Classe wrapper di personal-data-vault TOKENIZER, con gestione del backoff
 */
@Component
@Slf4j
public class PersonalDataVaultTokenizerClient extends BaseClient {

    private final TokenApi tokenApiPF;
    private final PnDatavaultConfig pnDatavaultConfig;


    public PersonalDataVaultTokenizerClient(PnDatavaultConfig pnDatavaultConfig, PnDatavaultConfig pnDatavaultConfig1){
        this.tokenApiPF = new TokenApi(initApiClient(pnDatavaultConfig.getTokenizerApiKeyPf(), pnDatavaultConfig.getClientTokenizerBasepath()));
        this.pnDatavaultConfig = pnDatavaultConfig1;
    }

    /**
     * Produce un id OPACO a partire da taxid (CF/PIVA)
     *
     * @param taxId id dell'utente in chiaro
     * @return id opaco
     */
    public Mono<String> ensureRecipientByExternalId(String taxId)
    {
        log.info("[enter] ensureRecipientByExternalId taxid={}", LogUtils.maskTaxId(taxId));
        if (pnDatavaultConfig.isDevelopment())
        {
            log.warn("DEVELOPMENT IS ACTIVE, MOCKING REQUEST!!!!");
            return Mono.just(RecipientUtils.encapsulateRecipientType(RecipientType.PF, reverseString(taxId)));
        }

        PiiResourceDto pii = new PiiResourceDto();
        pii.setPii(taxId);
        return this.tokenApiPF.saveUsingPUT(pii)
                    .map(r -> {
                        if (r == null)
                        {
                            log.error("Invalid empty response from tokenizer");
                            throw new PnDatavaultRecipientNotFoundException();
                        }

                        String res = RecipientUtils.encapsulateRecipientType(RecipientType.PF, r.getToken().toString());
                        log.debug("[exit] ensureRecipientByExternalId token={}", res);
                        return  res;
                    });
    }

    public Mono<UserResourceDto> findPii(InternalId internalId)
    {
        log.info("[enter] findPii token={}", internalId);
        return this.tokenApiPF.findPiiUsingGET(internalId.internalId())
                .map(r -> {
                    UserResourceDto brd = new UserResourceDto();
                    brd.setId(internalId.internalId());
                    brd.setFiscalCode(r.getPii());
                    log.debug("[exit] findPii token={}", LogUtils.maskTaxId(r.getPii()));
                    return  brd;
                });
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
