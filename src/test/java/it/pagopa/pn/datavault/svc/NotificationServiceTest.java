package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.exceptions.InvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AnalogDomicile;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.NotificationRecipientAddressesDto;
import it.pagopa.pn.datavault.mapper.NotificationEntityNotificationRecipientAddressesDtoMapper;
import it.pagopa.pn.datavault.mapper.NotificationTimelineEntityConfidentialTimelineElementDtoMapper;
import it.pagopa.pn.datavault.middleware.db.NotificationDao;
import it.pagopa.pn.datavault.middleware.db.NotificationDaoTestIT;
import it.pagopa.pn.datavault.middleware.db.NotificationTimelineDao;
import it.pagopa.pn.datavault.middleware.db.NotificationTimelineDaoTestIT;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationEntity;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest {


    Duration d = Duration.ofMillis(3000);

    @InjectMocks
    private NotificationService privateService;

    @Mock
    NotificationDao objDao;

    @Mock
    NotificationTimelineDao objtimelineDao;

    @Mock
    NotificationEntityNotificationRecipientAddressesDtoMapper mapper;

    @Mock
    NotificationTimelineEntityConfidentialTimelineElementDtoMapper mappertimeline;

    @Test
    void deleteNotificationByIun() {
        //Given
        NotificationEntity notificationEntity = NotificationDaoTestIT.newNotification();

        when(objDao.deleteNotificationByIun(Mockito.any())).thenReturn(Mono.just("OK"));

        //When
        assertDoesNotThrow(() -> {
            privateService.deleteNotificationByIun(notificationEntity.getInternalId()).block(d);
        });

        //Then
        // nothing
    }

    @Test
    void deleteNotificationByIunInvalid1() {
        //Given

        //When
        assertThrows(InvalidInputException.class, () -> privateService.deleteNotificationByIun(null));

        //Then
        // nothing
    }

    @Test
    void getNotificationAddressesByIun() {
        //Given
        NotificationEntity notificationEntity = NotificationDaoTestIT.newNotification();
        List<NotificationEntity> list = new ArrayList<>();
        list.add(notificationEntity);

        when(objDao.listNotificationRecipientAddressesDtoById(Mockito.any())).thenReturn(Flux.fromIterable(list));
        when(mapper.toDto(Mockito.any())).thenReturn(new NotificationRecipientAddressesDto());

        //When
        List<NotificationRecipientAddressesDto> result = privateService.getNotificationAddressesByIun("").collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateNotificationAddressesByIun() {
        //Given
        NotificationEntity notificationEntity = NotificationDaoTestIT.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto = new NotificationRecipientAddressesDto();
        dto.setDenomination("sig rossi");
        dto.setDigitalAddress(new AddressDto());
        dto.getDigitalAddress().setValue("rossi@test.it");
        dto.setPhysicalAddress(new AnalogDomicile());
        listdto.add(dto);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));
        when(mapper.toEntity(Mockito.any())).thenReturn(notificationEntity);

        //When
        assertDoesNotThrow(() -> {
            privateService.updateNotificationAddressesByIun(notificationEntity.getInternalId(), listdto.toArray(new NotificationRecipientAddressesDto[0])).block(d);
        });

        //Then
        // nothing
    }

    @Test
    void updateNotificationAddressesByIunInvalid1() {
        //Given
        NotificationEntity notificationEntity = NotificationDaoTestIT.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto = new NotificationRecipientAddressesDto();
        dto.setDenomination("sig rossi");
        dto.setDigitalAddress(new AddressDto());
        dto.setPhysicalAddress(new AnalogDomicile());
        listdto.add(dto);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));
        when(mapper.toEntity(Mockito.any())).thenReturn(notificationEntity);

        //When
        String iun = notificationEntity.getInternalId();
        NotificationRecipientAddressesDto[] dtos = listdto.toArray(new NotificationRecipientAddressesDto[0]);
        assertThrows(InvalidInputException.class, () -> privateService.updateNotificationAddressesByIun(iun, dtos));

        //Then
        // nothing
    }

    @Test
    void updateNotificationAddressesByIunInvalid2() {
        //Given
        NotificationEntity notificationEntity = NotificationDaoTestIT.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto = new NotificationRecipientAddressesDto();
        dto.setDenomination("sig rossi");
        dto.setDigitalAddress(new AddressDto());
        dto.getDigitalAddress().setValue("rossi@test.it");
        listdto.add(dto);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));
        when(mapper.toEntity(Mockito.any())).thenReturn(notificationEntity);

        //When
        String iun = notificationEntity.getInternalId();
        NotificationRecipientAddressesDto[] dtos = listdto.toArray(new NotificationRecipientAddressesDto[0]);
        assertThrows(InvalidInputException.class, () -> privateService.updateNotificationAddressesByIun(iun, dtos));

        //Then
        // nothing
    }

    @Test
    void updateNotificationAddressesByIunInvalid3() {
        //Given
        NotificationEntity notificationEntity = NotificationDaoTestIT.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto = new NotificationRecipientAddressesDto();
        dto.setDigitalAddress(new AddressDto());
        dto.getDigitalAddress().setValue("rossi@test.it");
        dto.setPhysicalAddress(new AnalogDomicile());
        listdto.add(dto);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));
        when(mapper.toEntity(Mockito.any())).thenReturn(notificationEntity);

        //When
        String iun = notificationEntity.getInternalId();
        NotificationRecipientAddressesDto[] dtos = listdto.toArray(new NotificationRecipientAddressesDto[0]);
        assertThrows(InvalidInputException.class, () -> privateService.updateNotificationAddressesByIun(iun, dtos));

        //Then
        // nothing
    }

    @Test
    void getNotificationTimelineByIun() {
        //Given
        NotificationTimelineEntity notificationEntity = NotificationTimelineDaoTestIT.newNotification();
        List<NotificationTimelineEntity> list = new ArrayList<>();
        list.add(notificationEntity);

        when(objtimelineDao.getNotificationTimelineByIun(Mockito.any())).thenReturn(Flux.fromIterable(list));
        when(mappertimeline.toDto(Mockito.any())).thenReturn(new ConfidentialTimelineElementDto());

        //When
        List<ConfidentialTimelineElementDto> result = privateService.getNotificationTimelineByIun("").collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getNotificationTimelineByIunAndTimelineElementId() {
        //Given
        NotificationTimelineEntity notificationEntity = NotificationTimelineDaoTestIT.newNotification();

        when(objtimelineDao.getNotificationTimelineByIunAndTimelineElementId(Mockito.any(), Mockito.any())).thenReturn(Mono.just(notificationEntity));
        when(mappertimeline.toDto(Mockito.any())).thenReturn(new ConfidentialTimelineElementDto());

        //When
        ConfidentialTimelineElementDto result = privateService.getNotificationTimelineByIunAndTimelineElementId(notificationEntity.getInternalId(), notificationEntity.getTimelineElementId()).block(d);

        //Then
        assertNotNull(result);
    }

    @Test
    void updateNotificationTimelineByIunAndTimelineElementId() {
        //Given
        NotificationTimelineEntity notificationEntity = NotificationTimelineDaoTestIT.newNotification();
        ConfidentialTimelineElementDto dto = new ConfidentialTimelineElementDto();
        dto.setTimelineElementId("elementid");
        dto.setDigitalAddress(new AddressDto());
        dto.setPhysicalAddress(new AnalogDomicile());

        when(objtimelineDao.updateNotification(Mockito.any())).thenReturn(Mono.just("OK"));
        when(mappertimeline.toEntity(Mockito.any())).thenReturn(notificationEntity);

        //When
        assertDoesNotThrow(() -> {
            privateService.updateNotificationTimelineByIunAndTimelineElementId(notificationEntity.getInternalId(), notificationEntity.getTimelineElementId(), dto).block(d);
        });

        //Then
        // nothing
    }
}