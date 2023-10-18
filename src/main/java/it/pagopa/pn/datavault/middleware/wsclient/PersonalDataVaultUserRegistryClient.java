package it.pagopa.pn.datavault.middleware.wsclient;


import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.datavault.generated.openapi.msclient.userregistry.v1.api.UserApi;
import it.pagopa.pn.datavault.generated.openapi.msclient.userregistry.v1.dto.UserResourceDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static it.pagopa.pn.datavault.job.CloudWatchMetricJob.PDV_RATE_LIMITER;
import static it.pagopa.pn.commons.log.PnLogger.EXTERNAL_SERVICES.*;

/**
 * Classe wrapper di personal-data-vault, con gestione del backoff
 */
@Component
@CustomLog
public class PersonalDataVaultUserRegistryClient {

    public static final String FILTER_FAMILY_NAME = "familyName";
    public static final String FILTER_NAME = "name";
    public static final String FILTER_FISCAL_CODE = "fiscalCode";
    private final UserApi userClientPF;
    private final PersonalDataVaultTokenizerClient personalDataVaultTokenizerClient;
    private final RateLimiter rateLimiter;

    private static final String PDV_USER_REGISTRY = PDV + "_UserRegistry";

    public PersonalDataVaultUserRegistryClient(UserApi userClientPF, PersonalDataVaultTokenizerClient personalDataVaultTokenizerClient, RateLimiterRegistry rateLimiterRegistry) {
        this.userClientPF = userClientPF;
        this.personalDataVaultTokenizerClient = personalDataVaultTokenizerClient;
        this.rateLimiter = rateLimiterRegistry.rateLimiter(PDV_RATE_LIMITER);
    }


    public Flux<BaseRecipientDto> getRecipientDenominationByInternalId(List<InternalId> internalIds) {
        log.logInvokingExternalDownstreamService(PDV_USER_REGISTRY, "getRecipientDenominationByInternalId");
        log.debug("[enter] getRecipientDenominationByInternalId internalids:{}", internalIds);
        return Flux.fromIterable(internalIds)
                .flatMap(uid -> this.userClientPF.findByIdUsingGET(uid.internalId(), Arrays.asList(FILTER_FAMILY_NAME, FILTER_NAME, FILTER_FISCAL_CODE))
                        .transformDeferred(RateLimiterOperator.of(rateLimiter))
                        .onErrorResume(WebClientResponseException.class, ex -> {
                            log.logInvokationResultDownstreamFailed(PDV_USER_REGISTRY, CommonBaseClient.elabExceptionMessage(ex));
                            return ex.getRawStatusCode() == 404 ? this.personalDataVaultTokenizerClient.findPii(uid) : Mono.error(ex);
                        })
                        .map(r -> {
                            BaseRecipientDto brd = new BaseRecipientDto();
                            brd.setInternalId(uid.internalIdWithRecipientType());
                            brd.setDenomination(buildDenomination(r));
                            brd.setTaxId(r.getFiscalCode());
                            return brd;
                        }));
    }

    private String buildDenomination(UserResourceDto dto) {
        String name = dto.getName() == null ? null : dto.getName().getValue();
        String surname = dto.getFamilyName() == null ? null : dto.getFamilyName().getValue();

        if (StringUtils.hasText(name) && StringUtils.hasText(surname))
            return name + " " + surname;
        else if (StringUtils.hasText(surname))
            return surname;
        else if (StringUtils.hasText(name))
            return name;
        else
            return "";
    }

}
