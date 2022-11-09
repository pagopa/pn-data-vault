package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MandateDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.NotificationRecipientAddressesDto;
import it.pagopa.pn.datavault.mapper.NotificationEntityNotificationRecipientAddressesDtoMapper;
import it.pagopa.pn.datavault.mapper.NotificationTimelineEntityConfidentialTimelineElementDtoMapper;
import it.pagopa.pn.datavault.svc.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@WebFluxTest(controllers = {NotificationsRestControllerV1.class})
@Import({NotificationEntityNotificationRecipientAddressesDtoMapper.class, NotificationTimelineEntityConfidentialTimelineElementDtoMapper.class})
class NotificationsRestControllerV1Test {


    @Autowired
    WebTestClient webTestClient;

    @Autowired
    NotificationEntityNotificationRecipientAddressesDtoMapper mapper;

    @Autowired
    NotificationTimelineEntityConfidentialTimelineElementDtoMapper mappertimeline;

    @MockBean
    private NotificationService privateService;


    @Test
    void deleteNotificationByIun() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        //When
        Mockito.when( privateService.deleteNotificationByIun( Mockito.anyString()))
                .thenReturn(Mono.just("OK"));

        //Then
        webTestClient.delete()
                .uri(url)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getNotificationAddressesByIun() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        NotificationRecipientAddressesDto dto = mapper.toDto(TestUtils.newNotification());
        List<NotificationRecipientAddressesDto> list = new ArrayList<>();
        list.add(dto);

        //When
        Mockito.when( privateService.getNotificationAddressesByIun( Mockito.any()))
                .thenReturn(Flux.fromIterable(list));

        //Then
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(NotificationRecipientAddressesDto.class);

    }

    @Test
    void updateNotificationAddressesByIun() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        //When
        Mockito.when( privateService.updateNotificationAddressesByIun( Mockito.anyString(), Mockito.any() ))
                .thenReturn(Mono.just("OK"));

        //Then
        webTestClient.put()
                .uri(url)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getNotificationTimelineByIun() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/timeline"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        ConfidentialTimelineElementDto dto = mappertimeline.toDto(TestUtils.newNotificationTimeline());
        List<ConfidentialTimelineElementDto> list = new ArrayList<>();
        list.add(dto);

        //When
        Mockito.when( privateService.getNotificationTimelineByIun( Mockito.any()))
                .thenReturn(Flux.fromIterable(list));

        //Then
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(MandateDto.class);
    }

    @Test
    void getNotificationTimelineByIunAndTimelineElementId() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/timeline/{timelineElementId}"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1")
                .replace("{timelineElementId}", "abcd");

        ConfidentialTimelineElementDto dto = mappertimeline.toDto(TestUtils.newNotificationTimeline());


        //When
        Mockito.when( privateService.getNotificationTimelineByIunAndTimelineElementId( Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.just(dto));

        //Then
        webTestClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(MandateDto.class);
    }

    @Test
    void updateNotificationTimelineByIunAndTimelineElementId() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/timeline/{timelineElementId}"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1")
                .replace("{timelineElementId}", "mario rossi");
        ConfidentialTimelineElementDto dto = mappertimeline.toDto(TestUtils.newNotificationTimeline());


        //When
        Mockito.when( privateService.updateNotificationTimelineByIunAndTimelineElementId( Mockito.anyString(), Mockito.anyString(), Mockito.any() ))
                .thenReturn(Mono.just("OK"));

        //Then
        webTestClient.put()
                .uri(url)
                .bodyValue(dto)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }
}