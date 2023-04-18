package it.pagopa.pn.datavault.middleware.wsclient;


import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
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

import static it.pagopa.pn.datavault.job.CloudWatchMetricJob.PDV_RATE_LIMITER;

/**
 * Classe wrapper di personal-data-vault TOKENIZER, con gestione del backoff
 */
@Component
@Slf4j
public class PersonalDataVaultTokenizerClient extends BaseClient {

    private final TokenApi tokenApiPF;
    private final RateLimiter rateLimiter;


    public PersonalDataVaultTokenizerClient(PnDatavaultConfig pnDatavaultConfig, RateLimiterRegistry rateLimiterRegistry){
        this.tokenApiPF = new TokenApi(initApiClient(pnDatavaultConfig.getTokenizerApiKeyPf(), pnDatavaultConfig.getClientTokenizerBasepath()));
        this.rateLimiter = rateLimiterRegistry.rateLimiter(PDV_RATE_LIMITER);
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

        PiiResourceDto pii = new PiiResourceDto();
        pii.setPii(taxId);
        return this.tokenApiPF.saveUsingPUT(pii)
                    .transformDeferred(RateLimiterOperator.of(rateLimiter))
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
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
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

}
