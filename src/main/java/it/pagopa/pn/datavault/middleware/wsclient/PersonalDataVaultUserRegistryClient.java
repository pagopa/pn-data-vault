package it.pagopa.pn.datavault.middleware.wsclient;


import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.netty.handler.timeout.TimeoutException;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.ApiClient;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.api.UserApi;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.dto.UserResourceDto;
import it.pagopa.pn.datavault.middleware.wsclient.common.BaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.UnknownHostException;
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

    private static final long RATE_LIMIT_MILLIS = 1000L;
    private static final int RATE_LIMIT_NUMBER = 20;

    private final UserApi userClientPF;
    private final UserApi userClientPG;
    private final PnDatavaultConfig pnDatavaultConfig;
    private final PersonalDataVaultTokenizerClient personalDataVaultTokenizerClient;
    private final RateLimiter rateLimiter;

    public PersonalDataVaultUserRegistryClient(PnDatavaultConfig pnDatavaultConfig, PnDatavaultConfig pnDatavaultConfig1, PersonalDataVaultTokenizerClient personalDataVaultTokenizerClient){
        this.userClientPF = new UserApi(initApiClient(pnDatavaultConfig.getUserregistryApiKeyPf(), pnDatavaultConfig.getClientUserregistryBasepath()));
        this.userClientPG = new UserApi(initApiClient(pnDatavaultConfig.getUserregistryApiKeyPg(), pnDatavaultConfig.getClientUserregistryBasepath()));
        this.pnDatavaultConfig = pnDatavaultConfig1;
        this.personalDataVaultTokenizerClient = personalDataVaultTokenizerClient;
        this.rateLimiter = buildRateLimiter();
    }


    public Flux<BaseRecipientDto> getRecipientDenominationByInternalId(List<String> internalIds)
    {
        if (pnDatavaultConfig.isDevelopment())
        {
            log.warn("DEVELOPMENT IS ACTIVE, MOCKING REQUEST!!!!");
            return Flux.fromIterable(internalIds).map(r -> {
                BaseRecipientDto brd = new BaseRecipientDto();
                brd.setDenomination("Nome cognome"+r);
                brd.setTaxId(reverseString(r.replace("PF-","").replace("PG-","")));
                brd.setInternalId(r);
                return brd;
            });
        }

        log.debug("[enter] getRecipientDenominationByInternalId internalids:{}", internalIds);
        return Flux.fromIterable(internalIds)
                .flatMap(uid -> this.getUserApiForRecipientType(getRecipientTypeFromInternalId(uid))
                       .findByIdUsingGET(getUUIDFromInternalId(uid), Arrays.asList(FILTER_FAMILY_NAME, FILTER_NAME, FILTER_FISCAL_CODE))
                        .retryWhen(
                               Retry.backoff(2, Duration.ofSeconds(1)).jitter(0.75)
                                       .filter(this::retryableException)
                       )
                        .transformDeferred(RateLimiterOperator.of(rateLimiter))
                        .onErrorResume(WebClientResponseException.class,
                                ex -> ex.getRawStatusCode() == 404 ? this.personalDataVaultTokenizerClient.findPii(uid): Mono.error(ex))
                       .map(r -> {
                           BaseRecipientDto brd = new BaseRecipientDto();
                           brd.setInternalId(uid);
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


    /**
     * Ritorna il client corretto in base al tipo di utente
     * @param recipientType tipo di utente
     * @return il client associato
     */
    private UserApi getUserApiForRecipientType(RecipientType recipientType)
    {
        if (recipientType == RecipientType.PF)
            return this.userClientPF;
        else
            return this.userClientPG;
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

    private boolean retryableException(Throwable throwable) {
        return throwable instanceof TimeoutException
                || throwable instanceof ConnectException
                || throwable instanceof UnknownHostException
                || throwable.getCause() instanceof UnknownHostException
                || throwable.getCause() instanceof SSLHandshakeException
                || throwable instanceof WebClientResponseException.TooManyRequests
                ;
    }

    private RateLimiter buildRateLimiter() {
        return RateLimiter.of("user-registry-rate-limit", RateLimiterConfig.custom()
                        .limitRefreshPeriod(Duration.ofMillis(RATE_LIMIT_MILLIS))
                        .limitForPeriod(RATE_LIMIT_NUMBER)
                        .timeoutDuration(Duration.ofMillis(RATE_LIMIT_MILLIS * RATE_LIMIT_NUMBER))
                .build());
    }
}
