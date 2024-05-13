package it.pagopa.pn.datavault.svc;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultTokenizerClient;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultUserRegistryClient;
import it.pagopa.pn.datavault.middleware.wsclient.SelfcarePGClient;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import it.pagopa.pn.datavault.utils.RecipientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RecipientService {


    private static final String LOG_EXIT = "[exit]";
    private static final String LOG_ENTER = "[enter]";
    private final PersonalDataVaultTokenizerClient client;
    private final PersonalDataVaultUserRegistryClient userClient;
    private final SelfcarePGClient selfcareTokenizerClient;
    private final PnDatavaultConfig pnDatavaultConfig;
    private final AsyncCache<String, String> cacheExtToIntIds;
    private final AsyncCache<String, BaseRecipientDto> cacheIntToExtIds;

    private final boolean cacheEnabled;

    public RecipientService(PersonalDataVaultTokenizerClient client, PersonalDataVaultUserRegistryClient userClient, SelfcarePGClient selfcareTokenizerClient, PnDatavaultConfig pnDatavaultConfig) {
        this.client = client;
        this.userClient = userClient;
        this.selfcareTokenizerClient = selfcareTokenizerClient;
        this.pnDatavaultConfig = pnDatavaultConfig;
        this.cacheExtToIntIds = Caffeine.newBuilder()
                .expireAfterAccess(pnDatavaultConfig.getCacheExpireAfterMinutes(), TimeUnit.MINUTES)
                .maximumSize(pnDatavaultConfig.getCacheMaxSize())
                .buildAsync();
        this.cacheIntToExtIds = Caffeine.newBuilder()
                .expireAfterAccess(pnDatavaultConfig.getCacheExpireAfterMinutes(), TimeUnit.MINUTES)
                .maximumSize(pnDatavaultConfig.getCacheMaxSize())
                .buildAsync();
        this.cacheEnabled = pnDatavaultConfig.getCacheExpireAfterMinutes() > 0;
    }

    public Mono<String> ensureRecipientByExternalId(RecipientType recipientType, String taxId) {
        log.debug(LOG_ENTER + " ensureRecipientByExternalId recipientType={} taxid={}", recipientType.getValue(), LogUtils.maskTaxId(taxId));
        final String extId = encapsulateTaxId(recipientType, taxId);
        if (cacheEnabled)
            return Mono.fromFuture(this.cacheExtToIntIds.get(extId,
                (s, executor) -> this.ensureRecipientByExternalIdPForPG(recipientType, taxId).toFuture()))
                .doOnNext(r -> log.debug(LOG_EXIT + " ensureRecipientByExternalId cache internalId={}",r));
        else
            return this.ensureRecipientByExternalIdPForPG(recipientType, taxId);
    }


    public Flux<BaseRecipientDto> getRecipientDenominationByInternalId(List<String> internalId) {
        log.debug(LOG_ENTER + " getRecipientDenominationByInternalId internalId={}", internalId);
        if (internalId.size() == 1)
        {
            if (cacheEnabled)
            {
                log.debug("request is 1 element only, using cache");
                // caso molto comune, provo a risolverlo nella cache
                // se non lo trovo, lo richiedo
                final CompletableFuture<BaseRecipientDto> baseRecipientDtoFuture = this.cacheIntToExtIds.getIfPresent(internalId.get(0));

                if (baseRecipientDtoFuture != null) {
                    return Mono.fromFuture(baseRecipientDtoFuture)
                            .doOnNext(baseRecipientDto -> log.debug(LOG_EXIT + " getRecipientDenominationByInternalId read from cache taxId={} denomination={}", LogUtils.maskTaxId(baseRecipientDto.getTaxId()), LogUtils.maskGeneric(baseRecipientDto.getDenomination())))
                            .flux();
                }else
                    return this.getRecipientDenominationByInternalIdPForPG(internalId).take(1).next()
                    .map(baseRecipientDto -> {

                        if (baseRecipientDto.getDenomination() != null)
                            this.cacheIntToExtIds.put(internalId.get(0), CompletableFuture.supplyAsync(() -> baseRecipientDto));
                        else
                            log.debug("getRecipientDenominationByInternalId skipping cache because denomination is null taxId={} denomination={}", LogUtils.maskTaxId(baseRecipientDto.getTaxId()), LogUtils.maskGeneric(baseRecipientDto.getDenomination()));

                        log.debug(LOG_EXIT + " getRecipientDenominationByInternalId cache miss, retrieved taxId={} denomination={}", LogUtils.maskTaxId(baseRecipientDto.getTaxId()), LogUtils.maskGeneric(baseRecipientDto.getDenomination()));
                        return baseRecipientDto;
                    }).flux();
            }
            else
            {
                return  this.getRecipientDenominationByInternalIdPForPG( internalId );
            }
        }
        else
        {
            log.debug("request is more than 1 element, not using cache");
            return  this.getRecipientDenominationByInternalIdPForPG( internalId );
        }
    }

    private Mono<String> ensureRecipientByExternalIdPForPG(RecipientType recipientType, String taxId){
        if (pnDatavaultConfig.isDevelopment())
        {
            log.warn("DEVELOPMENT IS ACTIVE, MOCKING REQUEST!!!!");
            return Mono.just(RecipientUtils.encapsulateRecipientType(recipientType, RecipientUtils.reverseString(taxId)));
        }

        if (recipientType == RecipientType.PF) {
            return client.ensureRecipientByExternalId(taxId)
                    .doOnNext(r -> log.debug(LOG_EXIT + " ensureRecipientByExternalId_PF internalId={}",r));
        } else {
            return selfcareTokenizerClient.addInstitutionUsingPOST(taxId)
                    .doOnNext(r -> log.debug(LOG_EXIT + " ensureRecipientByExternalId_PG internalId={}",r));
        }
    }


    private Flux<BaseRecipientDto> getRecipientDenominationByInternalIdPForPG(List<String> internalIds){
        // la risoluzione degli internalIds, va fatta con più attenzione, perchè potrei ricevere liste "miste", ovvero alcuni internalID di PF e altri di PG
        // che devo ovviamente risolvere su client diversi

        if (pnDatavaultConfig.isDevelopment())
        {
            log.warn("DEVELOPMENT IS ACTIVE, MOCKING REQUEST!!!!");
            return RecipientUtils.getRecipientDenominationByInternalIdMock(internalIds);
        }

        List<InternalId> allInternalIds = RecipientUtils.mapToInternalId(internalIds);
        List<InternalId> pfInternalIds = allInternalIds.stream().filter(x -> x.recipientType()==RecipientType.PF).toList();
        List<InternalId> pgInternalIds = allInternalIds.stream().filter(x -> x.recipientType()==RecipientType.PG).toList();


        // il concat di 2 flux non funziona "facilmente" se uno dei 2 è vuoto. Dato che cmq è un caso limite
        // si mettono le due ricerca separate e poi quella combinata
        if (!pfInternalIds.isEmpty() && pgInternalIds.isEmpty())
            return userClient.getRecipientDenominationByInternalId(pfInternalIds)
                    .doOnNext(r -> log.debug(LOG_EXIT + " getRecipientDenominationByInternalIdPForPG_PF internalId={}",r));
        else if (pfInternalIds.isEmpty() && !pgInternalIds.isEmpty())
            return selfcareTokenizerClient.retrieveInstitutionByIdUsingGET(pgInternalIds)
                    .doOnNext(r -> log.debug(LOG_EXIT + " getRecipientDenominationByInternalIdPForPG_PF internalId={}",r));
        else
            return userClient.getRecipientDenominationByInternalId(pfInternalIds)
                .concatWith(selfcareTokenizerClient.retrieveInstitutionByIdUsingGET(pgInternalIds))
                .doOnNext(r -> log.debug(LOG_EXIT + " getRecipientDenominationByInternalIdPForPG_PF_PG internalId={}",r));
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
