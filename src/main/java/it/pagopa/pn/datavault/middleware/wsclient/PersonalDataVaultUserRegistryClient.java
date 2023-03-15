package it.pagopa.pn.datavault.middleware.wsclient;


import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.ApiClient;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.api.UserApi;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.dto.UserResourceDto;
import it.pagopa.pn.datavault.middleware.wsclient.common.BaseClient;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import it.pagopa.pn.datavault.utils.RecipientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Classe wrapper di personal-data-vault, con gestione del backoff
 */
@Component
@Slf4j
public class PersonalDataVaultUserRegistryClient extends BaseClient {

    public static final String FILTER_FAMILY_NAME = "familyName";
    public static final String FILTER_NAME = "name";
    public static final String FILTER_FISCAL_CODE = "fiscalCode";
    private final UserApi userClientPF;
    private final PnDatavaultConfig pnDatavaultConfig;
    private final PersonalDataVaultTokenizerClient personalDataVaultTokenizerClient;
    private final RateLimiter rateLimiter;

    public PersonalDataVaultUserRegistryClient(PnDatavaultConfig pnDatavaultConfig, PersonalDataVaultTokenizerClient personalDataVaultTokenizerClient){
        this.userClientPF = new UserApi(initApiClient(pnDatavaultConfig.getUserregistryApiKeyPf(), pnDatavaultConfig.getClientUserregistryBasepath()));
        this.pnDatavaultConfig = pnDatavaultConfig;
        this.personalDataVaultTokenizerClient = personalDataVaultTokenizerClient;
        this.rateLimiter = buildRateLimiter(pnDatavaultConfig);
    }


    public Flux<BaseRecipientDto> getRecipientDenominationByInternalId(List<InternalId> internalIds)
    {
        if (pnDatavaultConfig.isDevelopment())
        {
            log.warn("DEVELOPMENT IS ACTIVE, MOCKING REQUEST!!!!");
            return Flux.fromIterable(internalIds).map(r -> {
                BaseRecipientDto brd = new BaseRecipientDto();
                brd.setDenomination("Nome cognome"+r.internalIdWithRecipientType());
                brd.setTaxId(RecipientUtils.reverseString(r.internalIdWithRecipientType().replace("PF-","").replace("PG-","")));
                brd.setInternalId(r.internalIdWithRecipientType());
                return brd;
            });
        }

        log.debug("[enter] getRecipientDenominationByInternalId internalids:{}", internalIds);
        return Flux.fromIterable(internalIds)
                .flatMap(uid -> this.userClientPF.findByIdUsingGET(uid.internalId(), Arrays.asList(FILTER_FAMILY_NAME, FILTER_NAME, FILTER_FISCAL_CODE))
                        .transformDeferred(RateLimiterOperator.of(rateLimiter))
                        .onErrorResume(WebClientResponseException.class,
                                ex -> ex.getRawStatusCode() == 404 ? this.personalDataVaultTokenizerClient.findPii(uid): Mono.error(ex))
                       .map(r -> {
                           BaseRecipientDto brd = new BaseRecipientDto();
                           brd.setInternalId(uid.internalIdWithRecipientType());
                           brd.setDenomination(buildDenomination(r));
                           brd.setTaxId(r.getFiscalCode());
                           return brd;
                       }));
    }

    private String buildDenomination(UserResourceDto dto)
    {
        String name = dto.getName()==null?null:dto.getName().getValue();
        String surname = dto.getFamilyName()==null?null:dto.getFamilyName().getValue();

        if (StringUtils.hasText(name) && StringUtils.hasText(surname))
            return name + " " + surname;
        else if (StringUtils.hasText(surname))
            return surname;
        else if (StringUtils.hasText(name))
            return name;
        else
            return "";
    }


    private ApiClient initApiClient(String apiKey, String basepath)
    {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), apiKey));
        apiClient.setBasePath(basepath);
        return  apiClient;
    }


    private RateLimiter buildRateLimiter(PnDatavaultConfig pnDatavaultConfig) {
        return RateLimiter.of("user-registry-rate-limit", RateLimiterConfig.custom()
                        .limitRefreshPeriod(Duration.ofMillis(pnDatavaultConfig.getUserregistryRateLimiterMillis()))
                        .limitForPeriod(pnDatavaultConfig.getUserregistryRateLimiterNrequests())
                        .timeoutDuration(Duration.ofMillis(pnDatavaultConfig.getUserregistryRateLimiterTimeoutMillis()))
                .build());
    }
}
