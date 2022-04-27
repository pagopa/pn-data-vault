package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.middleware.db.ConfidentialObjectDao;
import it.pagopa.pn.datavault.middleware.db.ExternalToInternalIdMappingDao;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static it.pagopa.pn.datavault.svc.Namespaces.*;

@Service
public class PnDataVaultService {

    private static final String NOT_APPLICABLE_SORT_KEY = "N/A";


    private final ConfidentialObjectDao objDao;
    private final ExternalToInternalIdMappingDao mappingsDao;

    public PnDataVaultService(ConfidentialObjectDao objDao, ExternalToInternalIdMappingDao mappingsDao) {
        this.objDao = objDao;
        this.mappingsDao = mappingsDao;
    }

    public Mono<String> ensureRecipientByExternalId(RecipientType recipientType, String taxId) {
        String externalId = recipientType.getValue() + "::" + taxId;

        CompletableFuture<String> future = this.mappingsDao.getObjectMapping( externalId )
                .thenCompose( alreadyPresent -> alreadyPresent
                        .map( internalId -> CompletableFuture.completedFuture( internalId ))
                        .orElseGet( () -> mappingsDao.createObjectMapping( externalId ) )
                );
        return Mono.fromFuture( future );
    }


    public Mono<String> setDenominationByInternalId( String internalId, BaseRecipientDto denomination) {

        return this.objDao.updateFieldByInternalId(
                RECIPIENTS.getStrValue(),
                internalId,
                NOT_APPLICABLE_SORT_KEY,
                denomination
            );
    }

    public Mono<Optional<BaseRecipientDto>> getRecipientDenominationByInternalId(String internalId ) {
         return this.objDao.getByInternalId( RECIPIENTS.getStrValue(), internalId, BaseRecipientDto.class )
                 .map( all -> Optional.ofNullable( all.get( NOT_APPLICABLE_SORT_KEY )));
    }

    public Flux<BaseRecipientDto> getRecipientsDenominationsByInternalIds( List<String> internalIds ) {
        return Flux.fromStream( internalIds.stream() )
                .flatMap( internalId ->
                        this.getRecipientDenominationByInternalId( internalId )
                                .map( optionalResult -> optionalResult.orElse(null ) )
                )
                .filter( el -> el != null);
    }



    public Mono<RecipientAddressesDto> getAddressesByInternalId(String internalId) {
        return objDao.getByInternalId(
                Namespaces.ADDRESSES.getStrValue(),
                internalId,
                AddressDto.class
            )
            .map( addresses -> {
                RecipientAddressesDto dto = new RecipientAddressesDto();
                dto.setAddresses( addresses );
                return dto;
            });
    }

    public Mono<String> updateAddressByInternalId(String internalId, String addressId, AddressDto addressDto) {
        return objDao.updateFieldByInternalId(
                Namespaces.ADDRESSES.getStrValue(),
                internalId,
                addressId,
                addressDto
            );
    }

    public Mono<String> deleteAddressByInternalId(String internalId, String addressId ) {
        return objDao.deleteFieldByInternalId(
                Namespaces.ADDRESSES.getStrValue(),
                internalId,
                addressId
            );
    }






    public Mono<Optional<MandateDto>> getMandateByInternalId( String mandateId ) {
        return objDao.getByInternalId(
                Namespaces.MANDATES.getStrValue(),
                mandateId,
                DenominationDto.class
            )
            .map( all ->
                    Optional.ofNullable( all.get( NOT_APPLICABLE_SORT_KEY ) )
                        .map( info -> {
                            MandateDto dto = new MandateDto();
                            dto.setMandateId( mandateId );
                            dto.setInfo( info );
                            return dto;
                        })
            );
    }

    public Flux<MandateDto> getMandatesByInternalIds( List<String> mandateIds ) {
        return Flux.fromStream( mandateIds.stream() )
                .flatMap( mandateId ->
                        this.getMandateByInternalId( mandateId )
                                .map( optionalDto -> optionalDto.orElse(null))
                )
                .filter( el -> el != null);
    }


    public Mono<String> updateMandateByInternalId(String mandateId, DenominationDto addressDto) {
        return objDao.updateFieldByInternalId(
                Namespaces.MANDATES.getStrValue(),
                mandateId,
                NOT_APPLICABLE_SORT_KEY,
                addressDto
        );
    }

    public Mono<String> deleteMandateByInternalId(String mandateId ) {
        return objDao.deleteFieldByInternalId(
                Namespaces.MANDATES.getStrValue(),
                mandateId,
                NOT_APPLICABLE_SORT_KEY
        );
    }






    public Mono<Optional<Flux<NotificationRecipientAddressesDto>>> getNotificationAddressesByIun( String iun ) {
        return objDao.getByInternalId(
                Namespaces.NOTIFICATIONS.getStrValue(),
                iun,
                NotificationRecipientAddressesDto[].class
            )
                .map( all ->
                        Optional.ofNullable( all.get( NOT_APPLICABLE_SORT_KEY ) )
                            .map( addressArray -> Flux.fromArray( addressArray ))
                );
    }


    public Mono<String> updateNotificationAddressesByIun(String iun, NotificationRecipientAddressesDto[] info) {
        return objDao.updateFieldByInternalId(
                Namespaces.NOTIFICATIONS.getStrValue(),
                iun,
                NOT_APPLICABLE_SORT_KEY,
                info
        );
    }

    public Mono<String> deleteNotificationByIun(String iun ) {
        return objDao.deleteFieldByInternalId(
                Namespaces.NOTIFICATIONS.getStrValue(),
                iun,
                NOT_APPLICABLE_SORT_KEY
            )
            .flatMap( result ->
                objDao.deleteByInternalId(
                    NOTIFICATIONS_TIMELINES.getStrValue(),
                    iun
                )
            );
    }

    public Mono<Optional<ConfidentialTimelineElementDto>> getNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId) {
        return objDao.getByInternalId(
                NOTIFICATIONS_TIMELINES.getStrValue(),
                iun,
                ConfidentialTimelineElementDto.class
            )
            .map( allFields ->  Optional.ofNullable( allFields.get( timelineElementId )));
    }

    public Mono<String> updateNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId, ConfidentialTimelineElementDto dto) {
        dto.setTimelineElementId( timelineElementId );
        return objDao.updateFieldByInternalId(
                Namespaces.NOTIFICATIONS_TIMELINES.getStrValue(),
                iun,
                timelineElementId,
                dto
            );
    }

    public Mono<Optional<Flux<ConfidentialTimelineElementDto>>> getNotificationTimelineByIun(String iun) {
        return objDao.getByInternalId(
                Namespaces.NOTIFICATIONS_TIMELINES.getStrValue(),
                iun,
                ConfidentialTimelineElementDto.class
            )
            .map( allFields -> {
                Optional<Flux<ConfidentialTimelineElementDto>> result;
                if(allFields.isEmpty()) {
                    result = Optional.empty();
                }
                else {
                    Flux<ConfidentialTimelineElementDto> timeline;
                    timeline = Flux.fromIterable( allFields.values() );
                    result = Optional.of( timeline );
                }
                return result;
            });
    }
}
