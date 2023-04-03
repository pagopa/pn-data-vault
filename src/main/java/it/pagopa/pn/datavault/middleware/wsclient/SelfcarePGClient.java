package it.pagopa.pn.datavault.middleware.wsclient;


import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.exceptions.PnDatavaultRecipientNotFoundException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.selfcarepg.v1.ApiClient;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.selfcarepg.v1.api.InstitutionsApi;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.selfcarepg.v1.api.InstitutionsPnpgApi;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.selfcarepg.v1.dto.CreatePnPgInstitutionDtoDto;
import it.pagopa.pn.datavault.middleware.wsclient.common.OcpBaseClient;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import it.pagopa.pn.datavault.utils.RecipientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Classe wrapper di personal-data-vault TOKENIZER, con gestione del backoff
 */
@Component
@Slf4j
public class SelfcarePGClient extends OcpBaseClient {

    private final InstitutionsPnpgApi institutionsPnpgApi;

    private final InstitutionsApi institutionsApi;
    private final PnDatavaultConfig pnDatavaultConfig;
    private final RateLimiter rateLimiter;

    public SelfcarePGClient(PnDatavaultConfig pnDatavaultConfig){
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), pnDatavaultConfig.getSelfcarepgApiKeyPg()).build());
        apiClient.setBasePath(pnDatavaultConfig.getClientSelfcarepgBasepath());
        this.institutionsPnpgApi = new InstitutionsPnpgApi( apiClient );
        this.institutionsApi = new InstitutionsApi(apiClient);
        this.pnDatavaultConfig = pnDatavaultConfig;
        this.rateLimiter = buildRateLimiter(pnDatavaultConfig);
    }

    private RateLimiter buildRateLimiter(PnDatavaultConfig pnDatavaultConfig) {
        return RateLimiter.of("selfcarepg-rate-limit", RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(pnDatavaultConfig.getSelfcarepgRateLimiterMillis()))
                .limitForPeriod(pnDatavaultConfig.getSelfcarepgRateLimiterNrequests())
                .timeoutDuration(Duration.ofMillis(pnDatavaultConfig.getSelfcarepgRateLimiterTimeoutMillis()))
                .build());
    }

    /**
     * Produce un id OPACO a partire da taxid PG (CF/PIVA)
     *
     * @param taxId id dell'utente in chiaro
     * @return id opaco
     */
    public Mono<String> addInstitutionUsingPOST(String taxId)
    {
        log.info("[enter] addInstitutionUsingPOST taxid={}", LogUtils.maskTaxId(taxId));
        if (pnDatavaultConfig.isDevelopment())
        {
            log.warn("DEVELOPMENT IS ACTIVE, MOCKING REQUEST!!!!");
            return Mono.just(RecipientUtils.encapsulateRecipientType(RecipientType.PG, RecipientUtils.reverseString(taxId)));
        }

        CreatePnPgInstitutionDtoDto pii = new CreatePnPgInstitutionDtoDto();
        pii.setExternalId(taxId);
        return this.institutionsPnpgApi.addInstitutionUsingPOST(pii)
                    .map(r -> {
                        if (r == null)
                        {
                            log.error("Invalid empty response from addInstitutionUsingPOST");
                            throw new PnDatavaultRecipientNotFoundException();
                        }

                        String res = RecipientUtils.encapsulateRecipientType(RecipientType.PG, r);
                        log.debug("[exit] addInstitutionUsingPOST token={}", res);
                        return  res;
                    });
    }


    /**
     * Recupera TaxId e denominazione a partire da InternalId
     * @param internalIds id degli utenti
     * @return taxid e denominazione
     */
    public Flux<BaseRecipientDto> retrieveInstitutionByIdUsingGET(List<InternalId> internalIds)
    {
        if (pnDatavaultConfig.isDevelopment())
        {
            log.warn("DEVELOPMENT IS ACTIVE, MOCKING REQUEST!!!!");
            return Flux.fromIterable(internalIds).map(r -> {
                BaseRecipientDto brd = new BaseRecipientDto();
                brd.setDenomination("ragionesociale"+r.internalIdWithRecipientType());
                brd.setTaxId(RecipientUtils.reverseString(r.internalIdWithRecipientType().replace("PF-","").replace("PG-","")));
                brd.setInternalId(r.internalIdWithRecipientType());
                return brd;
            });
        }

        log.debug("[enter] retrieveInstitutionByIdUsingGET internalids:{}", internalIds);
        return Flux.fromIterable(internalIds)
                .flatMap(internalId -> this.institutionsApi.getInstitution(internalId.internalId())
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .map(r ->  { BaseRecipientDto brd = new BaseRecipientDto();
                            brd.setInternalId(internalId.internalIdWithRecipientType());
                            brd.setDenomination(r.getDescription());
                            brd.setTaxId(r.getExternalId());
                            return brd;
                }));
    }

}
