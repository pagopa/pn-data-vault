package it.pagopa.pn.datavault.middleware.wsclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.mandate.microservice.msclient.generated.selfcarepg.v1.dto.InstitutionResponseDto;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "pn.data-vault.client_selfcarepg_basepath=http://localhost:9999",
         "pn.data-vault.selfcarepg_api_key_pg=pg"
})
class SelfcarePGClientTest {

    Duration d = Duration.ofMillis(3000);

    @Autowired
    private SelfcarePGClient client;

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
    void addInstitutionUsingPOST() {
        //Given
        String cf = "123456789";
        String iuid = "425e4567-e89b-12d3-a456-426655449631";
        String expectediuid = "PG-"+iuid;

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("POST")
                        .withHeader("Ocp-Apim-Subscription-Key", "pg")
                        .withPath("/pn-pg/institutions/add"))
                .respond(response()
                        .withBody("425e4567-e89b-12d3-a456-426655449631")
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        String result = client.addInstitutionUsingPOST(cf).block(d);

        //Then
        assertNotNull(result);
        assertEquals(expectediuid, result);
    }

    @Test
    void retrieveInstitutionByIdUsingGET() throws JsonProcessingException {
        //Given
        String name = "";
        String surname = "mario rossi srl";
        String fc = "12345678909";
        String iuid = "a8bdb303-18c0-43dd-b832-ef9f451bfe22";
        String expectediuid = "PG-"+iuid;
        List<String> ids = Arrays.asList(expectediuid);
        List<InternalId> iids = RecipientUtils.mapToInternalId(ids);
        InstitutionResponseDto response = new InstitutionResponseDto();
        response.setDescription(surname);
        response.setExternalId(fc);
        ObjectMapper mapper = new ObjectMapper();
        String respjson = mapper.writeValueAsString(response);

        new MockServerClient("localhost", 9999)
                .when(request()
                        .withMethod("GET")
                        .withHeader("Ocp-Apim-Subscription-Key", "pg")
                        .withPath("/institutions/" + iuid))
                .respond(response()
                        .withBody(respjson)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withStatusCode(200));

        //When
        List<BaseRecipientDto> result = client.retrieveInstitutionByIdUsingGET(iids).collectList().block(Duration.ofMillis(3000));

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(surname, result.get(0).getDenomination());
        assertEquals(fc, result.get(0).getTaxId());
        assertEquals(expectediuid, result.get(0).getInternalId());
    }


}