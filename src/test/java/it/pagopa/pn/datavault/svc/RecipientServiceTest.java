package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultTokenizerClient;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultUserRegistryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class RecipientServiceTest {

    Duration d = Duration.ofMillis(3000);

    @InjectMocks
    private RecipientService privateService;

    @Mock
    PersonalDataVaultTokenizerClient client;

    @Mock
    PersonalDataVaultUserRegistryClient userClient;

    @Mock
    PnDatavaultConfig pnDatavaultConfig;

    @BeforeEach
    private void init(){
        when(pnDatavaultConfig.getCacheExpireAfterMinutes()).thenReturn(5);
    }

    @Test
    void ensureRecipientByExternalIdPF() {
        //Given
        String uid = "425e4567-e89b-12d3-a456-426655449631";
        String expecteduid = RecipientType.PF.getValue()+"-" + uid;
        when(client.ensureRecipientByExternalId(Mockito.any(), Mockito.any())).thenReturn(Mono.just(expecteduid));

        //When
        String result = privateService.ensureRecipientByExternalId(RecipientType.PF, "RSSMRA85T10A562S").block(d);

        //Then
        assertNotNull(result);
        assertEquals(expecteduid, result);
    }

    @Test
    void getRecipientDenominationByInternalId() {
        //Given
        String uid = "425e4567-e89b-12d3-a456-426655449631";
        String expecteduid = RecipientType.PF.getValue()+"-" + uid;
        List<String> ids = List.of(expecteduid);
        BaseRecipientDto brd = new BaseRecipientDto();
        brd.setDenomination("mario rossi");
        brd.setInternalId(expecteduid);
        List<BaseRecipientDto> res = Arrays.asList(brd);
        when(userClient.getRecipientDenominationByInternalId(Mockito.any())).thenReturn(Flux.fromIterable(res));

        //When
        List<BaseRecipientDto> result = privateService.getRecipientDenominationByInternalId(ids).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}