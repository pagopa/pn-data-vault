package it.pagopa.pn.datavault.middleware.wsclient;


import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.datavault.exceptions.PnDatavaultRecipientNotFoundException;
import it.pagopa.pn.datavault.generated.openapi.msclient.selfcarepg.v1.api.InstitutionsApi;
import it.pagopa.pn.datavault.generated.openapi.msclient.selfcarepg.v1.api.InstitutionsPnpgApi;
import it.pagopa.pn.datavault.generated.openapi.msclient.selfcarepg.v1.dto.CreatePnPgInstitutionDtoDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import it.pagopa.pn.datavault.utils.RecipientUtils;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static it.pagopa.pn.datavault.springbootcfg.SpringAnalyzerActivation.SELC_RATE_LIMITER;
import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.*;

/**
 * Classe wrapper di personal-data-vault TOKENIZER, con gestione del backoff
 */
@Component
@CustomLog
public class SelfcarePGClient {

    private final InstitutionsPnpgApi institutionsPnpgApi;

    private final InstitutionsApi institutionsApi;
    private final RateLimiter rateLimiter;

    public SelfcarePGClient(RateLimiterRegistry rateLimiterRegistry, InstitutionsPnpgApi institutionsPnpgApi, InstitutionsApi institutionsApi) {
        this.institutionsPnpgApi = institutionsPnpgApi;
        this.institutionsApi = institutionsApi;
        this.rateLimiter = rateLimiterRegistry.rateLimiter(SELC_RATE_LIMITER);
    }


    /**
     * Produce un id OPACO a partire da taxid PG (CF/PIVA)
     *
     * @param taxId id dell'utente in chiaro
     * @return id opaco
     */
    public Mono<String> addInstitutionUsingPOST(String taxId) {
        log.logInvokingExternalDownstreamService(SELFCARE_PG, "addInstitutionUsingPOST");
        log.info("[enter] addInstitutionUsingPOST taxid={}", LogUtils.maskTaxId(taxId));

        CreatePnPgInstitutionDtoDto pii = new CreatePnPgInstitutionDtoDto();
        pii.setExternalId(taxId);
        return this.institutionsPnpgApi.addInstitutionUsingPOST(pii)
                .doOnError(e -> log.logInvokationResultDownstreamFailed(SELFCARE_PG, CommonBaseClient.elabExceptionMessage(e)))
                .map(r -> {
                    if (r == null) {
                        log.error("Invalid empty response from addInstitutionUsingPOST");
                        throw new PnDatavaultRecipientNotFoundException();
                    }

                    String res = RecipientUtils.encapsulateRecipientType(RecipientType.PG, r.getId().toString());
                    log.debug("[exit] addInstitutionUsingPOST token={}", res);
                    return res;
                });
    }


    /**
     * Recupera TaxId e denominazione a partire da InternalId
     *
     * @param internalIds id degli utenti
     * @return taxid e denominazione
     */
    public Flux<BaseRecipientDto> retrieveInstitutionByIdUsingGET(List<InternalId> internalIds) {

        log.logInvokingExternalDownstreamService(SELFCARE_PG, "retrieveInstitutionByIdUsingGET");
        log.debug("[enter] retrieveInstitutionByIdUsingGET internalids:{}", internalIds);
        return Flux.fromIterable(internalIds)
                .flatMap(internalId -> this.institutionsApi.getInstitution(internalId.internalId())
                        .doOnError(e -> log.logInvokationResultDownstreamFailed(SELFCARE_PG, CommonBaseClient.elabExceptionMessage(e)))

                        .transformDeferred(RateLimiterOperator.of(rateLimiter))
                        .map(r -> {
                            BaseRecipientDto brd = new BaseRecipientDto();
                            brd.setInternalId(internalId.internalIdWithRecipientType());
                            brd.setDenomination(r.getDescription());
                            brd.setTaxId(r.getExternalId());
                            return brd;
                        }));
    }

}
