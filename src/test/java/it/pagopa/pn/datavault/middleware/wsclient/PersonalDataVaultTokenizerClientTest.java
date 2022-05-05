package it.pagopa.pn.datavault.middleware.wsclient;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.tokenizer.v1.dto.TokenResourceDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.data-vault.client_tokenizer_basepath=http://localhost:9999",
        "pn.data-vault.tokenizer_api_key_pf=pf",
        "pn.data-vault.tokenizer_api_key_pg=pg"
})
class PersonalDataVaultTokenizerClientTest {


    @Autowired
    private PersonalDataVaultTokenizerClient client;

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
    void ensureRecipientByExternalIdPF() {
        //Given
        String cf = "RSSMRA85T10A562S";
        String iuid = "425e4567-e89b-12d3-a456-426655449631";
        String expectediuid = "PF-"+iuid;


        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("PUT")
                        .withHeader("x-api-key", "pf")
                        .withPath("/tokens"))
                .respond(response()
                        .withBody("{" +
                                "\"" + TokenResourceDto.JSON_PROPERTY_ROOT_TOKEN + "\": " + "\"" + iuid + "\"," +
                                "\"" + TokenResourceDto.JSON_PROPERTY_TOKEN + "\": " + "\"" + iuid + "\"" +
                                "}")
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        String result = client.ensureRecipientByExternalId(RecipientType.PF, cf).block(Duration.ofMillis(3000));

        //Then
        assertNotNull(result);
        assertEquals(expectediuid, result);
    }

    @Test
    void ensureRecipientByExternalIdPG() {
        //Given
        String cf = "123456789";
        String iuid = "425e4567-e89b-12d3-a456-426655449631";
        String expectediuid = "PG-"+iuid;


        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("PUT")
                        .withHeader("x-api-key", "pg")
                        .withPath("/tokens"))
                .respond(response()
                        .withBody("{" +
                                "\"" + TokenResourceDto.JSON_PROPERTY_ROOT_TOKEN + "\": " + "\"" + iuid + "\"," +
                                "\"" + TokenResourceDto.JSON_PROPERTY_TOKEN + "\": " + "\"" + iuid + "\"" +
                                "}")
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        String result = client.ensureRecipientByExternalId(RecipientType.PG, cf).block(Duration.ofMillis(3000));

        //Then
        assertNotNull(result);
        assertEquals(expectediuid, result);
    }
}