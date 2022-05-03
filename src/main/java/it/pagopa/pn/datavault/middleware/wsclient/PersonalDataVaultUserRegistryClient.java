package it.pagopa.pn.datavault.middleware.wsclient;


import io.netty.handler.timeout.TimeoutException;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.ApiClient;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.api.UserApi;
import it.pagopa.pn.datavault.middleware.wsclient.common.BaseClient;
import org.springframework.stereotype.Component;
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
public class PersonalDataVaultUserRegistryClient extends BaseClient {

    public static final String FILTER_FAMILY_NAME = "familyName";
    public static final String FILTER_NAME = "name";
    private final UserApi userClientPF;
    private final UserApi userClientPG;

    public PersonalDataVaultUserRegistryClient(PnDatavaultConfig pnDatavaultConfig){
        this.userClientPF = new UserApi(initApiClient(true, pnDatavaultConfig.getClientUserregistryBasepath()));
        this.userClientPG = new UserApi(initApiClient(false, pnDatavaultConfig.getClientUserregistryBasepath()));
    }


    public Flux<BaseRecipientDto> getRecipientDenominationByInternalId(List<String> internalIds)
    {
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
                           brd.setDenomination(r.getName() + " " + r.getFamilyName());
                           return brd;
                       }));
    }


    private UUID getUUIDFromInternalId(String internalId)
    {
        internalId = internalId.substring(3, internalId.length());
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

    private ApiClient initApiClient(boolean isPF, String basepath)
    {
        ApiClient apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), isPF));
        apiClient.setBasePath(basepath);
        return  apiClient;
    }
}
