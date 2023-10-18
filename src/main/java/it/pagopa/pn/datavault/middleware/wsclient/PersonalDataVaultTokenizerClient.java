package it.pagopa.pn.datavault.middleware.wsclient;


import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.datavault.exceptions.PnDatavaultRecipientNotFoundException;
import it.pagopa.pn.datavault.generated.openapi.msclient.tokenizer.v1.api.TokenApi;
import it.pagopa.pn.datavault.generated.openapi.msclient.tokenizer.v1.dto.PiiResourceDto;
import it.pagopa.pn.datavault.generated.openapi.msclient.userregistry.v1.dto.UserResourceDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import it.pagopa.pn.datavault.utils.RecipientUtils;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.*;
import static it.pagopa.pn.datavault.job.CloudWatchMetricJob.PDV_RATE_LIMITER;

/**
 * Classe wrapper di personal-data-vault TOKENIZER, con gestione del backoff
 */
@Component
@CustomLog
public class PersonalDataVaultTokenizerClient {

    private final TokenApi tokenApiPF;
    private final RateLimiter rateLimiter;

    private final String PDV_TOKENIZER = PDV + "_Tokenizer";

    public PersonalDataVaultTokenizerClient(TokenApi tokenApiPF, RateLimiterRegistry rateLimiterRegistry) {
        this.tokenApiPF = tokenApiPF;
        this.rateLimiter = rateLimiterRegistry.rateLimiter(PDV_RATE_LIMITER);
    }

    /**
     * Produce un id OPACO a partire da taxid (CF/PIVA)
     *
     * @param taxId id dell'utente in chiaro
     * @return id opaco
     */
    public Mono<String> ensureRecipientByExternalId(String taxId) {
        log.logInvokingExternalDownstreamService(PDV_TOKENIZER, "ensureRecipientByExternalId");
        log.info("[enter] ensureRecipientByExternalId taxid={}", LogUtils.maskTaxId(taxId));

        PiiResourceDto pii = new PiiResourceDto();
        pii.setPii(taxId);
        return this.tokenApiPF.saveUsingPUT(pii)
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .doOnError(e -> log.logInvokationResultDownstreamFailed(PDV_TOKENIZER, CommonBaseClient.elabExceptionMessage(e)))
                .map(r -> {
                    if (r == null) {
                        log.error("Invalid empty response from tokenizer");
                        throw new PnDatavaultRecipientNotFoundException();
                    }

                    String res = RecipientUtils.encapsulateRecipientType(RecipientType.PF, r.getToken().toString());
                    log.debug("[exit] ensureRecipientByExternalId token={}", res);
                    return res;
                });
    }

    public Mono<UserResourceDto> findPii(InternalId internalId) {
        log.logInvokingExternalDownstreamService(PDV_TOKENIZER, "findPii");
        log.info("[enter] findPii token={}", internalId);
        return this.tokenApiPF.findPiiUsingGET(internalId.internalId())
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .doOnError(e -> log.logInvokationResultDownstreamFailed(PDV_TOKENIZER, CommonBaseClient.elabExceptionMessage(e)))
                .map(r -> {
                    UserResourceDto brd = new UserResourceDto();
                    brd.setId(internalId.internalId());
                    brd.setFiscalCode(r.getPii());
                    log.debug("[exit] findPii token={}", LogUtils.maskTaxId(r.getPii()));
                    return brd;
                });
    }

}
