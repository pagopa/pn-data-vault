package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.exceptions.InvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.NotificationRecipientAddressesDto;
import it.pagopa.pn.datavault.mapper.NotificationEntityNotificationRecipientAddressesDtoMapper;
import it.pagopa.pn.datavault.mapper.NotificationTimelineEntityConfidentialTimelineElementDtoMapper;
import it.pagopa.pn.datavault.middleware.db.NotificationDao;
import it.pagopa.pn.datavault.middleware.db.NotificationTimelineDao;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationEntity;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
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
            throw new InvalidInputException("iun is required");

        return notificationDao.deleteNotificationByIun(iun);
    }

    public Flux<NotificationRecipientAddressesDto> getNotificationAddressesByIun(String iun) {
        return notificationDao.listNotificationRecipientAddressesDtoById(iun)
                .map(mappingsDao::toDto);
    }

    public Mono<Object> updateNotificationAddressesByIun(String iun, NotificationRecipientAddressesDto[] dtoArray) {

        List<NotificationEntity> nelist = new ArrayList<>();

        AtomicInteger recipientIndex = new AtomicInteger( 0 );
        Arrays.stream(dtoArray).forEach(dto -> {
            // valida l'oggetto
            validate(dto);

            NotificationEntity ne = mappingsDao.toEntity(dto);
            ne.setRecipientIndex( String.format("%03d", recipientIndex.getAndIncrement() ) );
            ne.setInternalId(iun);  // il mapping non può mappare l'internalid, non è presente nel dto
            nelist.add(ne);
        });

        return notificationDao.updateNotifications(nelist).map(r -> "OK");
    }

    public Flux<ConfidentialTimelineElementDto> getNotificationTimelineByIun(String iun) {
        return notificationTimelineDao.getNotificationTimelineByIun(iun)
                .map(timelineMapper::toDto);
    }

    public Mono<ConfidentialTimelineElementDto> getNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId) {
        return notificationTimelineDao.getNotificationTimelineByIunAndTimelineElementId(iun, timelineElementId)
                .map(timelineMapper::toDto);
    }

    public Mono<Object> updateNotificationTimelineByIunAndTimelineElementId(String iun, String timelineElementId, ConfidentialTimelineElementDto dto) {
        NotificationTimelineEntity nte = timelineMapper.toEntity(dto);
        nte.setInternalId(iun); // il mapper non lo può conoscere, non è presente nel dto
        nte.setTimelineElementId(timelineElementId);    // è presente pure nel dto, ma per sicurezza usiamo quello passato nel metodo
        return notificationTimelineDao.updateNotification(nte);
    }

    private void validate(NotificationRecipientAddressesDto dto) {
        if (!StringUtils.hasText(dto.getDenomination()))
            throw new InvalidInputException("denomination is required");
        if (!StringUtils.hasText(dto.getDigitalAddress().getValue()))
            throw new InvalidInputException("digitalAddress.value is required");
    }
}
