package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.exceptions.PnInvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.datavault.mapper.NotificationEntityNotificationRecipientAddressesDtoMapper;
import it.pagopa.pn.datavault.mapper.NotificationTimelineEntityConfidentialTimelineElementDtoMapper;
import it.pagopa.pn.datavault.middleware.db.NotificationDao;
import it.pagopa.pn.datavault.middleware.db.NotificationTimelineDao;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationEntity;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import it.pagopa.pn.datavault.middleware.db.entities.PhysicalAddress;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
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

    @Spy
    NotificationEntityNotificationRecipientAddressesDtoMapper mapper;

    @Mock
    NotificationTimelineEntityConfidentialTimelineElementDtoMapper mappertimeline;

    @Test
    void deleteNotificationByIun() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();

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
        assertThrows(PnInvalidInputException.class, () -> privateService.deleteNotificationByIun(null));

        //Then
        // nothing
    }

    @Test
    void getNotificationAddressesByIunWithNormalizedFalse() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();
        List<NotificationEntity> list = new ArrayList<>();
        list.add(notificationEntity);

        when(objDao.listNotificationRecipientAddressesDtoById(notificationEntity.getInternalId(), false)).thenReturn(Flux.fromIterable(list));

        //When
        List<NotificationRecipientAddressesDto> result = privateService.getNotificationAddressesByIun(notificationEntity.getInternalId(), false).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getNotificationAddressesByIunWithNormalizedTrue() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification(true);
        List<NotificationEntity> list = new ArrayList<>();
        list.add(notificationEntity);

        when(objDao.listNotificationRecipientAddressesDtoById(notificationEntity.getInternalId(), true)).thenReturn(Flux.fromIterable(list));

        //When
        List<NotificationRecipientAddressesDto> result = privateService.getNotificationAddressesByIun(notificationEntity.getInternalId(), true).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getNotificationAddressesByIunWithNormalizedNullAndNormalizedListNotEmpty() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification(true);
        notificationEntity.getPhysicalAddress().setAddress("NORMALIZED");
        List<NotificationEntity> list = new ArrayList<>();
        list.add(notificationEntity);
        NotificationEntity notificationEntityNotNormalized = new NotificationEntity(list.get(0).getInternalId(), "001", false);
        notificationEntityNotNormalized.setPhysicalAddress(notificationEntity.getPhysicalAddress());
        PhysicalAddress physicalAddressNotNormalized = new PhysicalAddress();
        physicalAddressNotNormalized.setAddress("NOT NORMALIZED");

        when(objDao.listNotificationRecipientAddressesDtoById(notificationEntity.getInternalId(), true)).thenReturn(Flux.fromIterable(list));
        when(objDao.listNotificationRecipientAddressesDtoById(notificationEntity.getInternalId(), false)).thenReturn(Flux.fromIterable(List.of(notificationEntityNotNormalized)));

        //When
        List<NotificationRecipientAddressesDto> result = privateService.getNotificationAddressesByIun(notificationEntity.getInternalId(), null).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("NORMALIZED", result.get(0).getPhysicalAddress().getAddress());
    }

    @Test
    void getNotificationAddressesByIunWithNormalizedNullAndNormalizedListEmpty() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification(false);
        notificationEntity.getPhysicalAddress().setAddress("NOT NORMALIZED");
        List<NotificationEntity> list = new ArrayList<>();
        list.add(notificationEntity);


        when(objDao.listNotificationRecipientAddressesDtoById(notificationEntity.getInternalId(), true)).thenReturn(Flux.empty());
        when(objDao.listNotificationRecipientAddressesDtoById(notificationEntity.getInternalId(), false)).thenReturn(Flux.fromIterable(list));

        //When
        List<NotificationRecipientAddressesDto> result = privateService.getNotificationAddressesByIun(notificationEntity.getInternalId(), null).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("NOT NORMALIZED", result.get(0).getPhysicalAddress().getAddress());
    }

    @Test
    void updateNotificationAddressesByIun() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto = new NotificationRecipientAddressesDto();
        dto.setDenomination("sig rossi");
        dto.setDigitalAddress(new AddressDto());
        dto.getDigitalAddress().setValue("rossi@test.it");
        dto.setPhysicalAddress(new AnalogDomicile());
        dto.setRecIndex(0);
        listdto.add(dto);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));

        //When
        assertDoesNotThrow(() -> {
            privateService.updateNotificationAddressesByIun(notificationEntity.getInternalId(), listdto.toArray(new NotificationRecipientAddressesDto[0]), false).block(d);
        });

        //Then
        // nothing
    }

    @Test
    void updateMultiSenderNotificationAddressesByIun() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto1 = new NotificationRecipientAddressesDto();
        NotificationRecipientAddressesDto dto2 = new NotificationRecipientAddressesDto();

        dto1.setDenomination("sig rossi");
        dto1.setDigitalAddress(new AddressDto());
        dto1.getDigitalAddress().setValue("rossi@test.it");
        dto1.setPhysicalAddress(new AnalogDomicile());
        dto1.setRecIndex(0);

        dto2.setDenomination("sig rossi2");
        dto2.setDigitalAddress(new AddressDto());
        dto2.getDigitalAddress().setValue("rossi2@test.it");
        dto2.setPhysicalAddress(new AnalogDomicile());
        dto2.setRecIndex(1);

        listdto.add(dto1);
        listdto.add(dto2);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));

        //When
        assertDoesNotThrow(() -> {
            privateService.updateNotificationAddressesByIun(notificationEntity.getInternalId(), listdto.toArray(new NotificationRecipientAddressesDto[0]), false).block(d);
        });

        //Then
        // nothing
    }


    @Test
    void updateMultiSenderNotificationAddressesByIun_TestWithRecIndexJustForOneSender() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto1 = new NotificationRecipientAddressesDto();
        NotificationRecipientAddressesDto dto2 = new NotificationRecipientAddressesDto();

        dto1.setDenomination("sig rossi");
        dto1.setDigitalAddress(new AddressDto());
        dto1.getDigitalAddress().setValue("rossi@test.it");
        dto1.setPhysicalAddress(new AnalogDomicile());
        dto1.setRecIndex(0);

        dto2.setDenomination("sig rossi2");
        dto2.setDigitalAddress(new AddressDto());
        dto2.getDigitalAddress().setValue("rossi2@test.it");
        dto2.setPhysicalAddress(new AnalogDomicile());

        listdto.add(dto1);
        listdto.add(dto2);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));

        //When
        assertThrows(PnInvalidInputException.class, () -> {
            privateService.updateNotificationAddressesByIun(notificationEntity.getInternalId(), listdto.toArray(new NotificationRecipientAddressesDto[0]), false).block(d);
        });

        //Then
        // nothing
    }

    @Test
    void updateMultiSenderNotificationAddressesByIun_NoRecIndex() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto1 = new NotificationRecipientAddressesDto();
        NotificationRecipientAddressesDto dto2 = new NotificationRecipientAddressesDto();

        dto1.setDenomination("sig rossi");
        dto1.setDigitalAddress(new AddressDto());
        dto1.getDigitalAddress().setValue("rossi@test.it");
        dto1.setPhysicalAddress(new AnalogDomicile());

        dto2.setDenomination("sig rossi2");
        dto2.setDigitalAddress(new AddressDto());
        dto2.getDigitalAddress().setValue("rossi2@test.it");
        dto2.setPhysicalAddress(new AnalogDomicile());

        listdto.add(dto1);
        listdto.add(dto2);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));

        //When
        assertDoesNotThrow(() -> {
            privateService.updateNotificationAddressesByIun(notificationEntity.getInternalId(), listdto.toArray(new NotificationRecipientAddressesDto[0]), false).block(d);
        });

        //Then
        // nothing
    }


    @Test
    void updateNotificationAddressesByIunValidNoDigital() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto = new NotificationRecipientAddressesDto();
        dto.setDenomination("sig rossi");
        dto.setDigitalAddress(null);
        dto.setPhysicalAddress(new AnalogDomicile());
        dto.setRecIndex(0);
        listdto.add(dto);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));

        //When
        String iun = notificationEntity.getInternalId();
        NotificationRecipientAddressesDto[] dtos = listdto.toArray(new NotificationRecipientAddressesDto[0]);
        assertDoesNotThrow(() -> privateService.updateNotificationAddressesByIun(iun, dtos, false));

        //Then
        // nothing
    }

    @Test
    void updateNotificationAddressesByIunValidNoPhysical() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto = new NotificationRecipientAddressesDto();
        dto.setDenomination("sig rossi");
        dto.setDigitalAddress(new AddressDto());
        dto.getDigitalAddress().setValue("rossi@test.it");
        dto.setPhysicalAddress(null);
        dto.setRecIndex(0);
        listdto.add(dto);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));

        //When
        String iun = notificationEntity.getInternalId();
        NotificationRecipientAddressesDto[] dtos = listdto.toArray(new NotificationRecipientAddressesDto[0]);
        assertDoesNotThrow(() -> privateService.updateNotificationAddressesByIun(iun, dtos, false));

        //Then
        // nothing
    }

    @Test
    void updateNotificationAddressesByIunValidNoRecIndex() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto = new NotificationRecipientAddressesDto();
        dto.setDenomination("sig rossi");
        dto.setDigitalAddress(new AddressDto());
        dto.getDigitalAddress().setValue("rossi@test.it");
        dto.setPhysicalAddress(new AnalogDomicile());

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));

        //When
        String iun = notificationEntity.getInternalId();
        NotificationRecipientAddressesDto[] dtos = listdto.toArray(new NotificationRecipientAddressesDto[0]);
        assertDoesNotThrow(() -> privateService.updateNotificationAddressesByIun(iun, dtos, false));

        //Then
        // nothing
    }


    @Test
    void updateNotificationAddressesByIunInvalid1() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto = new NotificationRecipientAddressesDto();
        //dto.setDenomination("sig rossi");
        dto.setDigitalAddress(new AddressDto());
        dto.setPhysicalAddress(new AnalogDomicile());
        dto.setRecIndex(0);
        listdto.add(dto);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));

        //When
        String iun = notificationEntity.getInternalId();
        NotificationRecipientAddressesDto[] dtos = listdto.toArray(new NotificationRecipientAddressesDto[0]);
        assertThrows(PnInvalidInputException.class, () -> privateService.updateNotificationAddressesByIun(iun, dtos, false));

        //Then
        // nothing
    }



    @Test
    void updateNotificationAddressesByIunInvalid2() {
        //Given
        NotificationEntity notificationEntity = TestUtils.newNotification();
        List<NotificationRecipientAddressesDto> listdto = new ArrayList<>();
        NotificationRecipientAddressesDto dto = new NotificationRecipientAddressesDto();
        dto.setDenomination("sig rossi");
        dto.setDigitalAddress(new AddressDto());
        dto.setPhysicalAddress(new AnalogDomicile());
        dto.setRecIndex(0);
        listdto.add(dto);

        when(objDao.updateNotifications(Mockito.any())).thenReturn(Mono.just("OK"));

        //When
        String iun = notificationEntity.getInternalId();
        NotificationRecipientAddressesDto[] dtos = listdto.toArray(new NotificationRecipientAddressesDto[0]);
        assertThrows(PnInvalidInputException.class, () -> privateService.updateNotificationAddressesByIun(iun, dtos, false));

        //Then
        // nothing
    }


    @Test
    void getNotificationTimelineByIun() {
        //Given
        NotificationTimelineEntity notificationEntity = TestUtils.newNotificationTimeline();
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
        NotificationTimelineEntity notificationEntity = TestUtils.newNotificationTimeline();

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
        NotificationTimelineEntity notificationEntity = TestUtils.newNotificationTimeline();
        ConfidentialTimelineElementDto dto = new ConfidentialTimelineElementDto();
        dto.setTimelineElementId("elementid");
        dto.setDigitalAddress(new AddressDto());
        dto.setPhysicalAddress(new AnalogDomicile());

        NotificationTimelineEntity notificationTimelineEntity = new NotificationTimelineEntity();

        when(objtimelineDao.updateNotification(Mockito.any())).thenReturn(Mono.just(notificationTimelineEntity));
        when(mappertimeline.toEntity(Mockito.any())).thenReturn(notificationEntity);

        //When
        assertDoesNotThrow(() -> {
            privateService.updateNotificationTimelineByIunAndTimelineElementId(notificationEntity.getInternalId(), notificationEntity.getTimelineElementId(), dto).block(d);
        });

        //Then
        // nothing
    }

    @Test
    void getNotificationTimelines() {
        //Given
        NotificationTimelineEntity notificationTimelineEntity = TestUtils.newNotificationTimeline();
        NotificationTimelineEntity notificationTimelineEntity1 = TestUtils.newNotificationTimeline();
        List<NotificationTimelineEntity> notificationTimelineEntities = new ArrayList<>();
        notificationTimelineEntities.add(notificationTimelineEntity);
        notificationTimelineEntities.add(notificationTimelineEntity1);
        ConfidentialTimelineElementId confidentialTimelineElementId = new ConfidentialTimelineElementId();
        ConfidentialTimelineElementId confidentialTimelineElementId1 = new ConfidentialTimelineElementId();
        List<ConfidentialTimelineElementId> confidentialTimelineElementIds = new ArrayList<>();
        confidentialTimelineElementIds.add(confidentialTimelineElementId);
        confidentialTimelineElementIds.add(confidentialTimelineElementId1);

        when(objtimelineDao.getNotificationTimelines(Mockito.any())).thenReturn(Flux.fromIterable(notificationTimelineEntities));
        when(mappertimeline.toDto(Mockito.any())).thenReturn(new ConfidentialTimelineElementDto());

        //When
        List<ConfidentialTimelineElementDto> result = privateService.getNotificationTimelines(Flux.fromIterable(confidentialTimelineElementIds)).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}