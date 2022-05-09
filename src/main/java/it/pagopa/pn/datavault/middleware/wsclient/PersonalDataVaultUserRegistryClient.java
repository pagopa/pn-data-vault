package it.pagopa.pn.datavault.middleware.wsclient;


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
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Classe wrapper di personal-data-vault, con gestione del backoff
 */
@Component
@Slf4j
public class PersonalDataVaultUserRegistryClient extends BaseClient {

    public static final String FILTER_FAMILY_NAME = "familyName";
    public static final String FILTER_NAME = "name";
    private final UserApi userClientPF;
    private final UserApi userClientPG;

    public PersonalDataVaultUserRegistryClient(PnDatavaultConfig pnDatavaultConfig){
        this.userClientPF = new UserApi(initApiClient(pnDatavaultConfig.getUserregistryApiKeyPf(), pnDatavaultConfig.getClientUserregistryBasepath()));
        this.userClientPG = new UserApi(initApiClient(pnDatavaultConfig.getUserregistryApiKeyPg(), pnDatavaultConfig.getClientUserregistryBasepath()));
    }


    public Flux<BaseRecipientDto> getRecipientDenominationByInternalId(List<String> internalIds)
    {
        log.trace("[enter]");
        return Flux.fromIterable(internalIds)
                .flatMap(uid -> this.getUserApiForRecipientType(getRecipientTypeFromInternalId(uid))
                        .findByIdUsingGET(getUUIDFromInternalId(uid), Arrays.asList(FILTER_FAMILY_NAME, FILTER_NAME))
                       .retryWhen(
                               Retry.backoff(2, Duration.ofMillis(25))
                                       .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                       )
                       .map(r -> {
                           BaseRecipientDto brd = new BaseRecipientDto();
                           brd.setInternalId(uid);
                           brd.setDenomination(buildDenomination(r));
                           log.trace("[exit]");
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

    private UUID getUUIDFromInternalId(String internalId)
    {
        internalId = internalId.substring(3);
        return UUID.fromString(internalId);
    }

    private RecipientType getRecipientTypeFromInternalId(String internalId)
    {
        if (internalId.startsWith(RecipientType.PF.getValue()))
            return RecipientType.PF;
        else
            return RecipientType.PG;
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
}
