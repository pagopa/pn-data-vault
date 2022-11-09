package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.exceptions.PnInvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DenominationDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.MandateDto;
import it.pagopa.pn.datavault.mapper.MandateEntityMandateDtoMapper;
import it.pagopa.pn.datavault.middleware.db.MandateDao;
import it.pagopa.pn.datavault.middleware.db.entities.MandateEntity;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class MandateServiceTest {

    Duration d = Duration.ofMillis(3000);

    @InjectMocks
    private MandateService privateService;

    @Mock
    MandateDao objDao;

    @Mock
    MandateEntityMandateDtoMapper mapper;


    @Test
    void getMandatesByInternalIds() {
        //Given
        MandateEntity mandateEntity = TestUtils.newMandate(true);
        MandateEntity mandateEntity1 = TestUtils.newMandate(false);
        List<MandateEntity> list = new ArrayList<>();
        List<String> listids = new ArrayList<>();
        list.add(mandateEntity);
        list.add(mandateEntity1);
        listids.add(mandateEntity.getMandateId());
        listids.add(mandateEntity1.getMandateId());

        when(objDao.listMandatesByIds(Mockito.any())).thenReturn(Flux.fromIterable(list));
        when(mapper.toDto(Mockito.any())).thenReturn(new MandateDto());

        //When
        List<MandateDto> result = privateService.getMandatesByInternalIds(listids).collectList().block(d);

        //Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void updateMandateByInternalId() {
        //Given
        MandateEntity mandateEntity = TestUtils.newMandate(true);
        DenominationDto dto = new DenominationDto();
        dto.setDestName(mandateEntity.getName());
        dto.setDestSurname(mandateEntity.getSurname());


        when(objDao.updateMandate(Mockito.any())).thenReturn(Mono.just(mandateEntity));

        //When
        assertDoesNotThrow(() -> {
            privateService.updateMandateByInternalId(mandateEntity.getMandateId(), dto).block(d);
        });

        //Then
        // nothing
    }

    @Test
    void updateMandateByInternalIdNullMandateId() {
        //Given
        MandateEntity mandateEntity = TestUtils.newMandate(true);
        DenominationDto dto = new DenominationDto();
        dto.setDestName(mandateEntity.getName());
        dto.setDestSurname(mandateEntity.getSurname());


        //When
        assertThrows(PnInvalidInputException.class, () -> privateService.updateMandateByInternalId(null, dto));

        //Then
        // nothing
    }

    @Test
    void updateMandateByInternalIdNullDto() {
        //Given
        MandateEntity mandateEntity = TestUtils.newMandate(true);

        //When
        String mid = mandateEntity.getMandateId();
        assertThrows(PnInvalidInputException.class, () -> privateService.updateMandateByInternalId(mid, null));

        //Then
        // nothing
    }

    @Test
    void updateMandateByInternalIdInvalidDto() {
        //Given
        MandateEntity mandateEntity = TestUtils.newMandate(true);
        DenominationDto dto = new DenominationDto();
        dto.setDestName(mandateEntity.getName());


        //When
        assertThrows(PnInvalidInputException.class, () -> privateService.updateMandateByInternalId(null, dto));

        //Then
        // nothing
    }

    @Test
    void updateMandateByInternalIdInvalidDto1() {
        //Given
        MandateEntity mandateEntity = TestUtils.newMandate(true);
        DenominationDto dto = new DenominationDto();
        dto.setDestSurname(mandateEntity.getSurname());


        //When
        assertThrows(PnInvalidInputException.class, () -> privateService.updateMandateByInternalId(null, dto));

        //Then
        // nothing
    }

    @Test
    void updateMandateByInternalIdInvalidDto2() {
        //Given
        DenominationDto dto = new DenominationDto();

        //When
        assertThrows(PnInvalidInputException.class, () -> privateService.updateMandateByInternalId(null, dto));

        //Then
        // nothing
    }

    @Test
    void deleteMandateByInternalId() {
        //Given
        MandateEntity mandateEntity = TestUtils.newMandate(true);


        when(objDao.deleteMandateId(Mockito.any())).thenReturn(Mono.just(mandateEntity));

        //When
        assertDoesNotThrow(() -> {
            privateService.deleteMandateByInternalId(mandateEntity.getMandateId()).block(d);
        });

        //Then
        // nothing
    }

    @Test
    void deleteMandateByInternalIdNullMandateId() {
        //Given

        //When
        assertThrows(PnInvalidInputException.class, () -> privateService.deleteMandateByInternalId(null));

        //Then
        // nothing
    }
}