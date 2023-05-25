package it.pagopa.pn.datavault.middleware.wsclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.datavault.generated.openapi.msclient.tokenizer.v1.dto.PiiResourceDto;
import it.pagopa.pn.datavault.generated.openapi.msclient.tokenizer.v1.dto.TokenResourceDto;
import it.pagopa.pn.datavault.generated.openapi.msclient.userregistry.v1.dto.UserResourceDto;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import it.pagopa.pn.datavault.utils.RecipientUtils;
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
import java.util.List;
import java.util.UUID;

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

    Duration d = Duration.ofMillis(3000);

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
    void ensureRecipientByExternalIdPF() throws JsonProcessingException {
        //Given
        String cf = "RSSMRA85T10A562S";
        String iuid = "425e4567-e89b-12d3-a456-426655449631";
        String expectediuid = "PF-"+iuid;
        TokenResourceDto response = new TokenResourceDto();
        response.setToken(UUID.fromString(iuid));
        response.setRootToken(UUID.fromString(iuid));
        ObjectMapper mapper = new ObjectMapper();
        String respjson = mapper.writeValueAsString(response);



        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("PUT")
                        .withHeader("x-api-key", "pf")
                        .withPath("/tokens"))
                .respond(response()
                        .withBody(respjson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        String result = client.ensureRecipientByExternalId(cf).block(d);

        //Then
        assertNotNull(result);
        assertEquals(expectediuid, result);
    }

    @Test
    void findPii() throws JsonProcessingException {

        String internalId_noPF = "425e4567-e89b-12d3-a456-426655449631";
        String internalId = "PF-" + internalId_noPF;
        InternalId internalId1 = RecipientUtils.mapToInternalId(List.of(internalId)).get(0);
        String pii = "PII";


        PiiResourceDto response = new PiiResourceDto();

        response.setPii(pii);

        ObjectMapper mapper = new ObjectMapper();
        String respjson = mapper.writeValueAsString(response);

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("x-api-key", "pf")
                        .withPath("/tokens/{token}/pii".replace("{token}", internalId_noPF)))
                .respond(response()
                        .withBody(respjson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        UserResourceDto result = client.findPii(internalId1).block(d);
        assertNotNull(result);
        assertEquals(result.getFiscalCode(), pii);

    }
}