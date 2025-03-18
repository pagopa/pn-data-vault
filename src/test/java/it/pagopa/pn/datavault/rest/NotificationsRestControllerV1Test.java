package it.pagopa.pn.datavault.rest;

import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementId;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MandateDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.NotificationRecipientAddressesDto;
import it.pagopa.pn.datavault.mapper.NotificationEntityNotificationRecipientAddressesDtoMapper;
import it.pagopa.pn.datavault.mapper.NotificationTimelineEntityConfidentialTimelineElementDtoMapper;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
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
    void getNotificationAddressesByIunWithNormalizedTrue() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        NotificationRecipientAddressesDto dto = mapper.toDto(TestUtils.newNotification(true));
        List<NotificationRecipientAddressesDto> list = new ArrayList<>();
        list.add(dto);

        //When
        Mockito.when( privateService.getNotificationAddressesByIun( "MXLQ-XMWD-YMLH-202206-K-1", true))
                .thenReturn(Flux.fromIterable(list));

        //Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("normalized", true)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(NotificationRecipientAddressesDto.class);

    }

    @Test
    void getNotificationAddressesByIunWithNormalizedFalse() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        NotificationRecipientAddressesDto dto = mapper.toDto(TestUtils.newNotification(false));
        List<NotificationRecipientAddressesDto> list = new ArrayList<>();
        list.add(dto);

        //When
        Mockito.when( privateService.getNotificationAddressesByIun( "MXLQ-XMWD-YMLH-202206-K-1", false))
                .thenReturn(Flux.fromIterable(list));

        //Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("normalized", false)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(NotificationRecipientAddressesDto.class);

    }

    @Test
    void getNotificationAddressesByIunWithNormalizedFalseAndEmptyBodyList() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        //When
        Mockito.when( privateService.getNotificationAddressesByIun( "MXLQ-XMWD-YMLH-202206-K-1", false))
                .thenReturn(Flux.fromIterable(new ArrayList<>()));

        //Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("normalized", false)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(NotificationRecipientAddressesDto.class);

    }

    @Test
    void getNotificationAddressesByIunWithNormalizedTrueAndEmptyBodyList() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        //When
        Mockito.when( privateService.getNotificationAddressesByIun( "MXLQ-XMWD-YMLH-202206-K-1", true))
                .thenReturn(Flux.fromIterable(new ArrayList<>()));

        //Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("normalized", true)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(NotificationRecipientAddressesDto.class);

    }

    @Test
    void getNotificationAddressesByIunWithNotNormalizedParameterAndEmptyBodyList() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        //When
        Mockito.when( privateService.getNotificationAddressesByIun( "MXLQ-XMWD-YMLH-202206-K-1", null))
                .thenReturn(Flux.fromIterable(new ArrayList<>()));

        //Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(NotificationRecipientAddressesDto.class);

    }

    @Test
    void getNotificationAddressesByIunWithNotNormalizedParameter() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        NotificationRecipientAddressesDto dto = mapper.toDto(TestUtils.newNotification(false));
        List<NotificationRecipientAddressesDto> list = new ArrayList<>();
        list.add(dto);

        //When
        Mockito.when( privateService.getNotificationAddressesByIun( "MXLQ-XMWD-YMLH-202206-K-1", null))
                .thenReturn(Flux.fromIterable(list));

        //Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk().expectBodyList(NotificationRecipientAddressesDto.class);

    }

    @Test
    void updateNotificationAddressesByIunWithNormalizedTrue() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        //When
        Mockito.when( privateService.updateNotificationAddressesByIun( Mockito.eq("MXLQ-XMWD-YMLH-202206-K-1"), Mockito.any(), Mockito.eq(true) ))
                .thenReturn(Mono.just("OK"));

        //Then
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("normalized", true)
                        .build())
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void updateNotificationAddressesByIunWithNormalizedFalse() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        //When
        Mockito.when( privateService.updateNotificationAddressesByIun( Mockito.eq("MXLQ-XMWD-YMLH-202206-K-1"), Mockito.any(), Mockito.eq(false) ))
                .thenReturn(Mono.just("OK"));

        //Then
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("normalized", false)
                        .build())
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void updateNotificationAddressesByIunWithNotNormalizedParameter() {
        //Given
        String url = "/datavault-private/v1/notifications/{iun}/addresses"
                .replace("{iun}", "MXLQ-XMWD-YMLH-202206-K-1");

        //When
        Mockito.when( privateService.updateNotificationAddressesByIun( Mockito.eq("MXLQ-XMWD-YMLH-202206-K-1"), Mockito.any(), Mockito.nullable(Boolean.class) ))
                .thenReturn(Mono.just("OK"));

        //Then
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .build())
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

        NotificationTimelineEntity notificationTimelineEntity = new NotificationTimelineEntity();

        //When
        Mockito.when( privateService.updateNotificationTimelineByIunAndTimelineElementId( Mockito.anyString(), Mockito.anyString(), Mockito.any() ))
                .thenReturn(Mono.just(notificationTimelineEntity));

        //Then
        webTestClient.put()
                .uri(url)
                .bodyValue(dto)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getNotificationTimelines(){
        //Given
        String url = "/datavault-private/v1/timelines";
        ConfidentialTimelineElementId confidentialTimelineElementId = TestUtils.newConfidentialTimelineElementId();
        List<ConfidentialTimelineElementId> list = new ArrayList<>();
        list.add(confidentialTimelineElementId);
        ConfidentialTimelineElementDto dto = mappertimeline.toDto(TestUtils.newNotificationTimeline());

        //When
        Mockito.when(privateService.getNotificationTimelines(Mockito.any())).thenReturn(Flux.just(dto));

        //Then
        webTestClient.post()
                .uri(url)
                .bodyValue(list)
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(ConfidentialTimelineElementDto.class);
    }
}