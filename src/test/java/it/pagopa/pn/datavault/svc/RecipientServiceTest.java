package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultTokenizerClient;
import it.pagopa.pn.datavault.middleware.wsclient.PersonalDataVaultUserRegistryClient;
import it.pagopa.pn.datavault.middleware.wsclient.SelfcarePGClient;
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
    SelfcarePGClient selfcarePGClient;

    @Mock
    PnDatavaultConfig pnDatavaultConfig;


    @Test
    void ensureRecipientByExternalIdPF() {
        //Given
        String uid = "425e4567-e89b-12d3-a456-426655449631";
        String expecteduid = RecipientType.PF.getValue()+"-" + uid;

        when(pnDatavaultConfig.getCacheExpireAfterMinutes()).thenReturn(0);
        when(client.ensureRecipientByExternalId(Mockito.any())).thenReturn(Mono.just(expecteduid));
        privateService = new RecipientService(client, userClient, selfcarePGClient, pnDatavaultConfig);

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
        when(client.ensureRecipientByExternalId(Mockito.any())).thenReturn(Mono.just(expecteduid));
        privateService = new RecipientService(client, userClient, selfcarePGClient, pnDatavaultConfig);

        //When
        String result = privateService.ensureRecipientByExternalId(RecipientType.PF, "RSSMRA85T10A562S").block(d);

        //Then
        assertNotNull(result);
        assertEquals(expecteduid, result);
    }

    @Test
    void getRecipientDenominationByInternalId_PF() {
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
        privateService = new RecipientService(client, userClient, selfcarePGClient, pnDatavaultConfig);

        //When
        List<BaseRecipientDto> result = privateService.getRecipientDenominationByInternalId(ids).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }


    @Test
    void getRecipientDenominationByInternalId_PG() {
        //Given
        String uid = "425e4567-e89b-12d3-a456-426655449631";
        String expecteduid = RecipientType.PG.getValue()+"-" + uid;
        List<String> ids = List.of(expecteduid);
        BaseRecipientDto brd = new BaseRecipientDto();
        brd.setDenomination("mario rossi SRL");
        brd.setInternalId(expecteduid);
        List<BaseRecipientDto> res = Arrays.asList(brd);

        when(pnDatavaultConfig.getCacheExpireAfterMinutes()).thenReturn(0);
        when(selfcarePGClient.retrieveInstitutionByIdUsingGET (Mockito.any())).thenReturn(Flux.fromIterable(res));
        privateService = new RecipientService(client, userClient, selfcarePGClient, pnDatavaultConfig);

        //When
        List<BaseRecipientDto> result = privateService.getRecipientDenominationByInternalId(ids).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }



    @Test
    void getRecipientDenominationByInternalId_PF_PG() {
        //Given
        String uid = "425e4567-e89b-12d3-a456-426655449631";
        String expecteduid = RecipientType.PG.getValue()+"-" + uid;
        BaseRecipientDto brd = new BaseRecipientDto();
        brd.setDenomination("mario rossi SRL");
        brd.setInternalId(expecteduid);
        brd.setRecipientType(RecipientType.PG);
        brd.setTaxId("20517490320");
        List<BaseRecipientDto> res = Arrays.asList(brd);

        //Given
        String uidpf = "425e4567-e89b-12d3-a456-426655449632";
        String expecteduidpf = RecipientType.PF.getValue()+"-" + uidpf;
        BaseRecipientDto brdpf = new BaseRecipientDto();
        brdpf.setDenomination("mario rossi");
        brdpf.setInternalId(expecteduidpf);
        brdpf.setRecipientType(RecipientType.PF);
        brdpf.setTaxId("CSRGGL44L13H501E");
        List<BaseRecipientDto> respf = Arrays.asList(brdpf);

        List<String> ids = List.of(expecteduid, expecteduidpf);


        when(pnDatavaultConfig.getCacheExpireAfterMinutes()).thenReturn(0);
        when(userClient.getRecipientDenominationByInternalId(Mockito.any())).thenReturn(Flux.fromIterable(respf));
        when(selfcarePGClient.retrieveInstitutionByIdUsingGET (Mockito.any())).thenReturn(Flux.fromIterable(res));
        privateService = new RecipientService(client, userClient, selfcarePGClient, pnDatavaultConfig);

        //When
        List<BaseRecipientDto> result = privateService.getRecipientDenominationByInternalId(ids).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(2, result.size());
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
        privateService = new RecipientService(client, userClient, selfcarePGClient, pnDatavaultConfig);

        //When
        List<BaseRecipientDto> result = privateService.getRecipientDenominationByInternalId(ids).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}