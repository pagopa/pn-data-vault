package it.pagopa.pn.datavault.svc;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultTokenizerClient;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultUserRegistryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RecipientService {


    private final PersonalDataVaultTokenizerClient client;
    private final PersonalDataVaultUserRegistryClient userClient;
    private final AsyncCache<String, String> cacheExtToIntIds;
    private final AsyncCache<String, BaseRecipientDto> cacheIntToExtIds;

    private final boolean cacheEnabled;

    public RecipientService(PersonalDataVaultTokenizerClient client, PersonalDataVaultUserRegistryClient userClient, PnDatavaultConfig pnDatavaultConfig) {
        this.client = client;
        this.userClient = userClient;
        this.cacheExtToIntIds = Caffeine.newBuilder()
                .expireAfterAccess(pnDatavaultConfig.getCacheExpireAfterMinutes(), TimeUnit.MINUTES)
                .buildAsync();
        this.cacheIntToExtIds = Caffeine.newBuilder()
                .expireAfterAccess(pnDatavaultConfig.getCacheExpireAfterMinutes(), TimeUnit.MINUTES)
                .buildAsync();
        this.cacheEnabled = pnDatavaultConfig.getCacheExpireAfterMinutes() > 0;
    }

    public Mono<String> ensureRecipientByExternalId(RecipientType recipientType, String taxId) {
        log.debug("[enter] ensureRecipientByExternalId recipientType={} taxid={}", recipientType.getValue(), LogUtils.maskTaxId(taxId));
        final String extId = encapsulateTaxId(recipientType, taxId);
        if (cacheEnabled)
            return Mono.fromFuture(this.cacheExtToIntIds.get(extId,
                (s, executor) -> client.ensureRecipientByExternalId(recipientType, taxId).toFuture()))
                .map(r -> {
                    log.debug("[exit] ensureRecipientByExternalId internalId={}",r);
                    return r;
                });
        else
            return client.ensureRecipientByExternalId(recipientType, taxId)
                    .map(r -> {
                        log.debug("[exit] ensureRecipientByExternalId internalId={}",r);
                        return r;
                    });
    }


    public Flux<BaseRecipientDto> getRecipientDenominationByInternalId(List<String> internalId) {
        log.debug("[enter] getRecipientDenominationByInternalId internalId={}", internalId);
        if (internalId.size() == 1)
        {
            if (cacheEnabled)
            {
                log.debug("request is 1 element only, using cache");
                // caso molto comune, provo a risolverlo nella cache
                return Mono.fromFuture(this.cacheIntToExtIds.get(internalId.get(0),
                                (s, executor) -> userClient.getRecipientDenominationByInternalId(internalId).take(1).next().toFuture()))
                        .map(baseRecipientDto -> {
                            log.debug("[exit] getRecipientDenominationByInternalId taxId={}", LogUtils.maskTaxId(baseRecipientDto.getTaxId()));
                            return baseRecipientDto;
                        })
                        .flux();
            }
            else
            {
                return  userClient.getRecipientDenominationByInternalId( internalId );
            }
        }
        else
        {
            log.debug("request is more than 1 element, not using cache");
            return  userClient.getRecipientDenominationByInternalId( internalId );
        }
    }


    /**
     * Ritorna un tax id "modificato" che contiene anche l'informazione di PF/PG
     *
     * @param recipientType il tipo di utente
     * @param taxId internal id
     * @return tax id modificato
     */
    private String encapsulateTaxId(RecipientType recipientType, String taxId)
    {
        return recipientType.getValue() + "-" + taxId;
    }
}
