package it.pagopa.pn.datavault.middleware.wsclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.datavault.generated.openapi.msclient.userregistry.v1.dto.CertifiableFieldResourceOfstringDto;
import it.pagopa.pn.datavault.generated.openapi.msclient.userregistry.v1.dto.UserResourceDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import it.pagopa.pn.datavault.utils.RecipientUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.data-vault.client_userregistry_basepath=http://localhost:9999",
        "pn.data-vault.userregistry_api_key_pf=pf",
        "pn.data-vault.userregistry_api_key_pg=pg"
})
class PersonalDataVaultUserRegistryClientTest {


    @Autowired
    private PersonalDataVaultUserRegistryClient client;

    private static ClientAndServer mockServer;

    @MockitoBean
    private PersonalDataVaultTokenizerClient personalDataVaultTokenizerClient;

    @BeforeAll
    static void startMockServer() {
        mockServer = startClientAndServer(9999);
    }

    @AfterAll
    static void stopMockServer() {
        mockServer.stop();
    }


    @Test
    void getRecipientDenominationByInternalIdPF() throws JsonProcessingException {
        //Given
        String name = "mario";
        String surname = "rossi";
        String fc = "RSSMRA85T10A562S";
        String iuid = "a8bdb303-18c0-43dd-b832-ef9f451bfe22";
        String expectediuid = "PF-"+iuid;
        List<String> ids = List.of(expectediuid);
        List<InternalId> iids = RecipientUtils.mapToInternalId(ids);
        UserResourceDto response = new UserResourceDto();
        CertifiableFieldResourceOfstringDto certifiableFieldResourceOfstringDto = new CertifiableFieldResourceOfstringDto();
        certifiableFieldResourceOfstringDto.setCertification(CertifiableFieldResourceOfstringDto.CertificationEnum.NONE);
        certifiableFieldResourceOfstringDto.setValue(name);
        response.setName(certifiableFieldResourceOfstringDto);
        certifiableFieldResourceOfstringDto = new CertifiableFieldResourceOfstringDto();
        certifiableFieldResourceOfstringDto.setCertification(CertifiableFieldResourceOfstringDto.CertificationEnum.NONE);
        certifiableFieldResourceOfstringDto.setValue(surname);
        response.setFamilyName(certifiableFieldResourceOfstringDto);
        response.setFiscalCode(fc);
        response.setId(UUID.fromString(iuid));
        ObjectMapper mapper = new ObjectMapper();
        String respjson = mapper.writeValueAsString(response);




        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("x-api-key", "pf")
                        .withQueryStringParameters(Map.of("fl", Arrays.asList("familyName", "name", "fiscalCode")))
                        .withPath("/users/" + iuid))
                .respond(response()
                        .withBody(respjson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        List<BaseRecipientDto> result = client.getRecipientDenominationByInternalId(iids).collectList().block(Duration.ofMillis(3000));

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(name + " " + surname, result.get(0).getDenomination());
        assertEquals(fc, result.get(0).getTaxId());
        assertEquals(expectediuid, result.get(0).getInternalId());

    }

    @Test
    void getRecipientDenominationByInternalIdPFOnlySurname() throws JsonProcessingException {
        //Given
        String surname = "rossi";
        String fc = "RSSMRA85T10A562S";
        String iuid = "b8bdb303-18c0-43dd-b832-ef9f451bfe22";
        String expectediuid = "PF-"+iuid;
        List<String> ids = List.of(expectediuid);
        List<InternalId> iids = RecipientUtils.mapToInternalId(ids);
        UserResourceDto response = new UserResourceDto();
        CertifiableFieldResourceOfstringDto certifiableFieldResourceOfstringDto = new CertifiableFieldResourceOfstringDto();
        certifiableFieldResourceOfstringDto.setCertification(CertifiableFieldResourceOfstringDto.CertificationEnum.NONE);
        response.setName(certifiableFieldResourceOfstringDto);
        certifiableFieldResourceOfstringDto = new CertifiableFieldResourceOfstringDto();
        certifiableFieldResourceOfstringDto.setCertification(CertifiableFieldResourceOfstringDto.CertificationEnum.NONE);
        certifiableFieldResourceOfstringDto.setValue(surname);
        response.setFamilyName(certifiableFieldResourceOfstringDto);
        response.setFiscalCode(fc);
        response.setId(UUID.fromString(iuid));
        ObjectMapper mapper = new ObjectMapper();
        String respjson = mapper.writeValueAsString(response);




        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("x-api-key", "pf")
                        .withQueryStringParameters(Map.of("fl", Arrays.asList("familyName", "name", "fiscalCode")))
                        .withPath("/users/" + iuid))
                .respond(response()
                        .withBody(respjson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        List<BaseRecipientDto> result = client.getRecipientDenominationByInternalId(iids).collectList().block(Duration.ofMillis(3000));

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(surname, result.get(0).getDenomination());
        assertEquals(fc, result.get(0).getTaxId());
        assertEquals(expectediuid, result.get(0).getInternalId());

    }

    @Test
    void getRecipientDenominationByInternalIdPFOnlyName() throws JsonProcessingException {
        //Given
        String name = "mario";
        String fc = "RSSMRA85T10A562S";
        String iuid = "c8bdb303-18c0-43dd-b832-ef9f451bfe22";
        String expectediuid = "PF-"+iuid;
        List<String> ids = List.of(expectediuid);
        List<InternalId> iids = RecipientUtils.mapToInternalId(ids);
        UserResourceDto response = new UserResourceDto();
        CertifiableFieldResourceOfstringDto certifiableFieldResourceOfstringDto = new CertifiableFieldResourceOfstringDto();
        certifiableFieldResourceOfstringDto.setCertification(CertifiableFieldResourceOfstringDto.CertificationEnum.NONE);
        certifiableFieldResourceOfstringDto.setValue(name);
        response.setName(certifiableFieldResourceOfstringDto);
        response.setFiscalCode(fc);
        response.setId(UUID.fromString(iuid));
        ObjectMapper mapper = new ObjectMapper();
        String respjson = mapper.writeValueAsString(response);




        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("x-api-key", "pf")
                        .withQueryStringParameters(Map.of("fl", Arrays.asList("familyName", "name", "fiscalCode")))
                        .withPath("/users/" + iuid))
                .respond(response()
                        .withBody(respjson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        List<BaseRecipientDto> result = client.getRecipientDenominationByInternalId(iids).collectList().block(Duration.ofMillis(3000));

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(name, result.get(0).getDenomination());
        assertEquals(fc, result.get(0).getTaxId());
        assertEquals(expectediuid, result.get(0).getInternalId());

    }

    @Test
    void getRecipientDenominationByInternalIdPFMissingUser() {
        //Given
        String fc = "RSSMRA85T10A562S";
        String iuid = "a8bdb303-18c0-43dd-b832-ef9f451bfe22";
        String expectediuid = "PF-"+iuid;
        List<String> ids = List.of(expectediuid);
        List<InternalId> iids = RecipientUtils.mapToInternalId(ids);
        UserResourceDto response = new UserResourceDto();
        response.setFiscalCode(fc);
        response.setId(UUID.fromString(iuid));

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("x-api-key", "pf")
                        .withQueryStringParameters(Map.of("fl", Arrays.asList("familyName", "name", "fiscalCode")))
                        .withPath("/users/" + iuid))
                .respond(response()
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(404));

        when(personalDataVaultTokenizerClient.findPii(Mockito.any())).thenReturn(Mono.just(response));

        //When
        List<BaseRecipientDto> result = client.getRecipientDenominationByInternalId(iids).collectList().block(Duration.ofMillis(3000));

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(fc, result.get(0).getTaxId());
        assertEquals(expectediuid, result.get(0).getInternalId());

    }


    @Test
    void getRecipientDenominationByInternalIdPFAfterRetryableException() throws JsonProcessingException {
        //Given
        String name = "mario";
        String surname = "rossi";
        String fc = "RSSMRA85T10A562S";
        String iuid = "a8bdb303-18c0-43dd-b832-ef9f451bfe24";
        String expectediuid = "PF-"+iuid;
        List<String> ids = List.of(expectediuid);
        List<InternalId> iids = RecipientUtils.mapToInternalId(ids);
        UserResourceDto response = new UserResourceDto();
        CertifiableFieldResourceOfstringDto certifiableFieldResourceOfstringDto = new CertifiableFieldResourceOfstringDto();
        certifiableFieldResourceOfstringDto.setCertification(CertifiableFieldResourceOfstringDto.CertificationEnum.NONE);
        certifiableFieldResourceOfstringDto.setValue(name);
        response.setName(certifiableFieldResourceOfstringDto);
        certifiableFieldResourceOfstringDto = new CertifiableFieldResourceOfstringDto();
        certifiableFieldResourceOfstringDto.setCertification(CertifiableFieldResourceOfstringDto.CertificationEnum.NONE);
        certifiableFieldResourceOfstringDto.setValue(surname);
        response.setFamilyName(certifiableFieldResourceOfstringDto);
        response.setFiscalCode(fc);
        response.setId(UUID.fromString(iuid));
        ObjectMapper mapper = new ObjectMapper();
        String respjson = mapper.writeValueAsString(response);



        MockServerClient mockServerClient = new MockServerClient("localhost", 9999);
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withHeader("x-api-key", "pf")
                        .withQueryStringParameters(Map.of("fl", Arrays.asList("familyName", "name", "fiscalCode")))
                        .withPath("/users/" + iuid), Times.exactly(1))
                .respond(response()
//                        .withBody(respjson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(429)
                );

        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withHeader("x-api-key", "pf")
                        .withQueryStringParameters(Map.of("fl", Arrays.asList("familyName", "name", "fiscalCode")))
                        .withPath("/users/" + iuid))
                .respond(response()
                        .withBody(respjson)
                                .withBody(respjson)
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withStatusCode(200)
                );

        //When
        List<BaseRecipientDto> result = client.getRecipientDenominationByInternalId(iids).collectList().block();

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(name + " " + surname, result.get(0).getDenomination());
        assertEquals(fc, result.get(0).getTaxId());
        assertEquals(expectediuid, result.get(0).getInternalId());

    }

    @Test
    void getRecipientDenominationByInternalIdPFAfterNotRetryableException() {
        //Given
        String iuid = "a8bdb303-18c0-43dd-b832-ef9f451bfe23";
        String expectediuid = "PF-"+iuid;
        List<String> ids = List.of(expectediuid);
        List<InternalId> iids = RecipientUtils.mapToInternalId(ids);

        MockServerClient mockServerClient = new MockServerClient("localhost", 9999);
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withHeader("x-api-key", "pf")
                        .withQueryStringParameters(Map.of("fl", Arrays.asList("familyName", "name", "fiscalCode")))
                        .withPath("/users/" + iuid), Times.exactly(1))
                .respond(response()
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withStatusCode(500)
                );

       assertThrows(
               WebClientResponseException.InternalServerError.class,
               () -> client.getRecipientDenominationByInternalId(iids).collectList().block()
       );

    }
}