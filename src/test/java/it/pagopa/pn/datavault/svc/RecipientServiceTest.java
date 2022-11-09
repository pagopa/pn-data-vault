package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultTokenizerClient;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultUserRegistryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipientServiceTest {

    Duration d = Duration.ofMillis(3000);

    private RecipientService privateService;

    @Mock
    PersonalDataVaultTokenizerClient client;

    @Mock
    PersonalDataVaultUserRegistryClient userClient;

    @Mock
    PnDatavaultConfig pnDatavaultConfig;


    @Test
    void ensureRecipientByExternalIdPF() {
        //Given
        String uid = "425e4567-e89b-12d3-a456-426655449631";
        String expecteduid = RecipientType.PF.getValue()+"-" + uid;

        when(pnDatavaultConfig.getCacheExpireAfterMinutes()).thenReturn(0);
        when(client.ensureRecipientByExternalId(Mockito.any(), Mockito.any())).thenReturn(Mono.just(expecteduid));
        privateService = new RecipientService(client, userClient, pnDatavaultConfig);

        //When
        String result = privateService.ensureRecipientByExternalId(RecipientType.PF, "RSSMRA85T10A562S").block(d);

        //Then
        assertNotNull(result);
        assertEquals(expecteduid, result);
    }

    @Test
    void ensureRecipientByExternalIdPFWithCache() {
        //Given
        String uid = "425e4567-e89b-12d3-a456-426655449631";
        String expecteduid = RecipientType.PF.getValue()+"-" + uid;

        when(pnDatavaultConfig.getCacheExpireAfterMinutes()).thenReturn(5);
        when(client.ensureRecipientByExternalId(Mockito.any(), Mockito.any())).thenReturn(Mono.just(expecteduid));
        privateService = new RecipientService(client, userClient, pnDatavaultConfig);

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

        when(pnDatavaultConfig.getCacheExpireAfterMinutes()).thenReturn(0);
        when(userClient.getRecipientDenominationByInternalId(Mockito.any())).thenReturn(Flux.fromIterable(res));
        privateService = new RecipientService(client, userClient, pnDatavaultConfig);

        //When
        List<BaseRecipientDto> result = privateService.getRecipientDenominationByInternalId(ids).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getRecipientDenominationByInternalIdWithCache() {
        //Given
        String uid = "425e4567-e89b-12d3-a456-426655449631";
        String expecteduid = RecipientType.PF.getValue()+"-" + uid;
        List<String> ids = List.of(expecteduid);
        BaseRecipientDto brd = new BaseRecipientDto();
        brd.setDenomination("mario rossi");
        brd.setInternalId(expecteduid);
        List<BaseRecipientDto> res = Arrays.asList(brd);

        when(pnDatavaultConfig.getCacheExpireAfterMinutes()).thenReturn(5);
        when(userClient.getRecipientDenominationByInternalId(Mockito.any())).thenReturn(Flux.fromIterable(res));
        privateService = new RecipientService(client, userClient, pnDatavaultConfig);

        //When
        List<BaseRecipientDto> result = privateService.getRecipientDenominationByInternalId(ids).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}