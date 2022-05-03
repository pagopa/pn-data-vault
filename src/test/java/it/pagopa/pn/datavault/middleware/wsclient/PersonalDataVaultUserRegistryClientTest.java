package it.pagopa.pn.datavault.middleware.wsclient;

import com.google.common.collect.Maps;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.dto.TokenResourceDto;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.userregistry.v1.dto.UserResourceDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.data-vault.client_userregistry_basepath=http://localhost:9999"
})
class PersonalDataVaultUserRegistryClientTest {


    @Autowired
    private PersonalDataVaultUserRegistryClient client;

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startMockServer() {
        mockServer = startClientAndServer(9999);
    }

    @AfterAll
    public static void stopMockServer() {
        mockServer.stop();
    }


    @Test
    void getRecipientDenominationByInternalId() {
        //Given
        String name = "mario";
        String surname = "rossi";
        String iuid = "a8bdb303-18c0-43dd-b832-ef9f451bfe22";
        String expectediuid = "PF-"+iuid;
        List<String> ids = Arrays.asList(expectediuid);


        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("x-api-key", "pf")
                        .withQueryStringParameters(Map.of("fl", Arrays.asList("familyName", "name")))
                        .withPath("/users/" + iuid))
                .respond(response()
                        .withBody("{\n" +
                                "  \"birthDate\": {\n" +
                                "    \"certification\": \"NONE\",\n" +
                                "    \"value\": \"2022-05-03\"\n" +
                                "  },\n" +
                                "  \"email\": {\n" +
                                "    \"certification\": \"NONE\",\n" +
                                "    \"value\": \"string\"\n" +
                                "  },\n" +
                                "  \"familyName\": {\n" +
                                "    \"certification\": \"NONE\",\n" +
                                "    \"value\": \"string\"\n" +
                                "  },\n" +
                                "  \"fiscalCode\": \"string\",\n" +
                                "  \"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                                "  \"name\": {\n" +
                                "    \"certification\": \"NONE\",\n" +
                                "    \"value\": \"string\"\n" +
                                "  },\n" +
                                "  \"workContacts\": {\n" +
                                "    \"additionalProp1\": {\n" +
                                "      \"email\": {\n" +
                                "        \"certification\": \"NONE\",\n" +
                                "        \"value\": \"string\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    \"additionalProp2\": {\n" +
                                "      \"email\": {\n" +
                                "        \"certification\": \"NONE\",\n" +
                                "        \"value\": \"string\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    \"additionalProp3\": {\n" +
                                "      \"email\": {\n" +
                                "        \"certification\": \"NONE\",\n" +
                                "        \"value\": \"string\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  }\n" +
                                "}")
                        /*.withBody("{" +
                                "\"" + UserResourceDto.JSON_PROPERTY_ID + "\": \"" + iuid + "\"," +
                                "\"" + UserResourceDto.JSON_PROPERTY_FAMILY_NAME + "\": {" +
                                    "\"certification\": \"NONE\", " +
                                    "\"value\": \"" + surname + "\"" +
                                 "}," +
                                "\"" + UserResourceDto.JSON_PROPERTY_NAME + "\": {" +
                                    "\"certification\": \"NONE\", " +
                                    "\"value\": \"" + name + "\"" +
                                "}," +
                                "\"workContacts\":{}" +
                                 "}")*/
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        List<BaseRecipientDto> result = client.getRecipientDenominationByInternalId(ids).collectList().block(Duration.ofMillis(3000));

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(name + " " + surname, result.get(0).getDenomination());
        assertEquals(expectediuid, result.get(0).getInternalId());

    }
}