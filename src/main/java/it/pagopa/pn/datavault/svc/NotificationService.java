package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.exceptions.PnInvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementId;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.NotificationRecipientAddressesDto;
import it.pagopa.pn.datavault.mapper.NotificationEntityNotificationRecipientAddressesDtoMapper;
import it.pagopa.pn.datavault.mapper.NotificationTimelineEntityConfidentialTimelineElementDtoMapper;
import it.pagopa.pn.datavault.middleware.db.NotificationDao;
import it.pagopa.pn.datavault.middleware.db.NotificationTimelineDao;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationEntity;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import it.pagopa.pn.datavault.utils.ValidationUtils;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static it.pagopa.pn.commons.exceptions.PnExceptionsCodes.ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED;
import static it.pagopa.pn.datavault.exceptions.PnDatavaultExceptionCodes.ERROR_CODE_DATAVAULT_MALFORMED_INPUT;
import static it.pagopa.pn.datavault.exceptions.PnDatavaultExceptionCodes.ERROR_MESSAGE_DATAVAULT_MALFORMED_BODY_UPDATEADDRESSES;

@Service
@CustomLog
public class NotificationService {

    private final NotificationDao notificationDao;
    private final NotificationTimelineDao notificationTimelineDao;
    private final NotificationEntityNotificationRecipientAddressesDtoMapper mappingsDao;
    private final NotificationTimelineEntityConfidentialTimelineElementDtoMapper timelineMapper;

    public NotificationService(NotificationDao objDao, NotificationTimelineDao notificationTimelineDao,
                               NotificationEntityNotificationRecipientAddressesDtoMapper mappingsDao,
                               NotificationTimelineEntityConfidentialTimelineElementDtoMapper timelineMapper) {
        this.notificationDao = objDao;
        this.notificationTimelineDao = notificationTimelineDao;
        this.mappingsDao = mappingsDao;
        this.timelineMapper = timelineMapper;
    }

    public Mono<Object> deleteNotificationByIun(String iun) {
        if (!StringUtils.hasText(iun))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "iun");

        return notificationDao.deleteNotificationByIun(iun);
    }

    public Flux<NotificationRecipientAddressesDto> getNotificationAddressesByIun(String iun, Boolean normalized) {
        if (normalized == null) {
            /*
            Se il parametro non viene passato allora il metodo dovrà prima comportarsi come se il parametro fosse presente
            e valorizzato true. Se gli indirizzi normalizzati non sono presenti allora deve comportarsi come se il paramtro fosse presente e valorizzato false
             */
            return notificationDao.listNotificationRecipientAddressesDtoById(iun, Boolean.TRUE)
                    .switchIfEmpty(notificationDao.listNotificationRecipientAddressesDtoById(iun, Boolean.FALSE))
                    .map(mappingsDao::toDto);
        }
        return notificationDao.listNotificationRecipientAddressesDtoById(iun, normalized)
                .map(mappingsDao::toDto);
    }

    public Mono<Object> updateNotificationAddressesByIun(String iun, NotificationRecipientAddressesDto[] dtoArray, Boolean normalized) {

        List<NotificationEntity> nelist = new ArrayList<>();
        boolean shouldAutoIncrement = checkRecIndexfromDtoIfNotPresent(dtoArray);
        AtomicInteger recipientIndex = new AtomicInteger(0);
        Arrays.stream(dtoArray).forEach(dto -> {
            // valida l'oggetto
            validate(dto);

            NotificationEntity ne = mappingsDao.toEntity(dto);
            //il setNormalized deve essere richiamato prima del setInternalId per generare nel modo corretto il prefisso della pk
            ne.setNormalizedAddress(normalized);
            ne.setRecipientIndex(String.format("%03d", shouldAutoIncrement ? recipientIndex.getAndIncrement() : dto.getRecIndex()));
            ne.setInternalId(iun);  // il mapping non può mappare l'internalid, non è presente nel dto
            nelist.add(ne);
        });

        return notificationDao.updateNotifications(nelist).map(r -> "OK");
    }

    /**
     * Verifica dell'attributo recIndex nel NotificationRecipientAddressesDto.
     * Se il recIndex non è presente per ogni indirizzo, il metodo restituisce true.
     * Se il recIndex è presente per ogni indirizzo, il metodo restituisce false.
     * Se il recIndex è presente solo per alcuni indirizzi, il metodo restituisce un'eccezione.
     *
     * @param dtoArray
     * @return true se il NotificationRecipientAddressesDto non restituisce i recIndex
     */
    private boolean checkRecIndexfromDtoIfNotPresent(NotificationRecipientAddressesDto[] dtoArray) {

        boolean recIndexNotPresent = false;
        boolean recIndexPresent = false;
        for (NotificationRecipientAddressesDto dto : dtoArray) {
            if (dto.getRecIndex() != null) {
                recIndexPresent = true;
            } else {
                recIndexNotPresent = true;
            }
        }
        if (recIndexNotPresent && recIndexPresent) {
            throw new PnInvalidInputException(ERROR_CODE_DATAVAULT_MALFORMED_INPUT, "body", ERROR_MESSAGE_DATAVAULT_MALFORMED_BODY_UPDATEADDRESSES);
        }
        return recIndexNotPresent;
    }

    public Flux<ConfidentialTimelineElementDto> getNotificationTimelineByIun(String iun) {
        return notificationTimelineDao.getNotificationTimelineByIun(iun)
                .map(timelineMapper::toDto);
    }

    public Mono<ConfidentialTimelineElementDto> getNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId) {
        return notificationTimelineDao.getNotificationTimelineByIunAndTimelineElementId(iun, timelineElementId)
                .map(timelineMapper::toDto);
    }

    public Mono<NotificationTimelineEntity> updateNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId, ConfidentialTimelineElementDto dto) {
        NotificationTimelineEntity nte = timelineMapper.toEntity(dto);
        nte.setInternalId(iun); // il mapper non lo può conoscere, non è presente nel dto
        nte.setTimelineElementId(timelineElementId);    // è presente pure nel dto, ma per sicurezza usiamo quello passato nel metodo
        return notificationTimelineDao.updateNotification(nte);
    }

    private void validate(NotificationRecipientAddressesDto dto) {
        if (!ValidationUtils.checkDenomination(dto.getDenomination()))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "denomination");

        if (!ValidationUtils.checkDigitalAddress(dto.getDigitalAddress()))
            throw new PnInvalidInputException(ERROR_CODE_PN_GENERIC_INVALIDPARAMETER_REQUIRED, "digitalAddress.value");
    }

    public Flux<ConfidentialTimelineElementDto> getNotificationTimelines(Flux<ConfidentialTimelineElementId> confidentialTimelineElementId) {
        return notificationTimelineDao.getNotificationTimelines(confidentialTimelineElementId)
                .map(timelineMapper::toDto);
    }
}
