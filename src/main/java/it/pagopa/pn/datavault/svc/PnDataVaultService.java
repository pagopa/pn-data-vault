package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.dao.ConfidentialObjectDao;
import it.pagopa.pn.datavault.dao.ExternalToInternalIdMappingDao;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
public class PnDataVaultService {

    public static final String NOTIFICATION_ADDRESSES_SORT_KEY = "N/A";
    private final ConfidentialObjectDao objDao;
    private final ExternalToInternalIdMappingDao mappingsDao;

    public PnDataVaultService(ConfidentialObjectDao objDao, ExternalToInternalIdMappingDao mappingsDao) {
        this.objDao = objDao;
        this.mappingsDao = mappingsDao;
    }

    public CompletableFuture<String> ensureRecipientByExternalId(RecipientType recipientType, String taxId) {
        String externalId = recipientType.getValue() + "::" + taxId;

        return this.mappingsDao.getObjectMapping( externalId )
                .thenCompose( alreadyPresent -> alreadyPresent
                        .map( internalId -> CompletableFuture.completedFuture( internalId ))
                        .orElseGet( () -> mappingsDao.createObjectMapping( externalId ) )
                );
    }

    public CompletableFuture<Optional<RecipientMandatesDto>> getMandatesByInternalId(String internalId) {
        return objDao.getByInternalId(
                Namespaces.MANDATES.getStrValue(),
                internalId,
                AddressDto.class
            )
            .thenApply( addresses -> {
                RecipientMandatesDto dto = new RecipientMandatesDto();
                dto.setMandateAddresses( addresses );
                return Optional.of( dto );
            });
    }

    public CompletableFuture<String> updateMandate(String internalId, String mandateId, AddressDto addressDto) {
        return objDao.updateFieldByInternalId(
                Namespaces.MANDATES.getStrValue(),
                internalId,
                mandateId,
                addressDto
            );
    }


    public CompletableFuture<Optional<RecipientAddressesDto>> getAddressesByInternalId(String internalId) {
        return objDao.getByInternalId(
                Namespaces.ADDRESSES.getStrValue(),
                internalId,
                AddressDto.class
            )
            .thenApply( addresses -> {
                RecipientAddressesDto dto = new RecipientAddressesDto();
                dto.setPlatformAddresses( addresses );
                return Optional.of( dto );
            });
    }

    public CompletableFuture<String> updateAddress(String internalId, String mandateId, AddressDto addressDto) {
        return objDao.updateFieldByInternalId(
                Namespaces.ADDRESSES.getStrValue(),
                internalId,
                mandateId,
                addressDto
            );
    }

    public CompletableFuture<Optional<NotificationRecipientAddressesDto[]>> getNotificationByIun(String iun) {
        return objDao.getByInternalId(
                    Namespaces.NOTIFICATIONS.getStrValue(),
                    iun,
                    NotificationRecipientAddressesDto[].class
                )
                .thenApply( notificationRawData -> {
                    NotificationRecipientAddressesDto[] dto;
                    if( notificationRawData != null ) {
                        dto = notificationRawData.get( NOTIFICATION_ADDRESSES_SORT_KEY );
                    }
                    else {
                        dto = null;
                    }
                    return Optional.ofNullable( dto );
                });
    }

    public CompletableFuture<String> updateNotification(String iun, NotificationRecipientAddressesDto notificationAddresses[]) {
        return objDao.updateFieldByInternalId(
                Namespaces.NOTIFICATIONS.getStrValue(),
                iun,
                NOTIFICATION_ADDRESSES_SORT_KEY,
                notificationAddresses
        );
    }
}
