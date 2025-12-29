package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddress;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddressRequest;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddresses;
import it.pagopa.pn.datavault.mapper.PaperAddressEntityPaperAddressMapper;
import it.pagopa.pn.datavault.middleware.db.PaperAddressDao;
import it.pagopa.pn.datavault.middleware.db.entities.PaperAddressEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class PaperAddressServiceTest {

    Duration d = Duration.ofMillis(3000);

    private PaperAddressService paperAddressService;

    @Mock
    PaperAddressDao paperAddressDao;

    @Autowired
    PaperAddressEntityPaperAddressMapper mappingsDao;

    @BeforeEach
    void setUp() {
        Mockito.reset(paperAddressDao);
        paperAddressService = new PaperAddressService(paperAddressDao, mappingsDao);
    }

    @Test
    void getPaperAddressesByPaperRequestId() {
        // Arrange
        String paperRequestId = "test-paper-request-id";
        PaperAddressEntity paperAddressEntity = new PaperAddressEntity();
        List<PaperAddressEntity> list = new ArrayList<>();
        list.add(paperAddressEntity);

        when(paperAddressDao.getPaperAddressesByPaperRequestId(Mockito.any())).thenReturn(Flux.fromIterable(list));

        // Act
        PaperAddresses result = paperAddressService.getPaperAddressesByPaperRequestId(paperRequestId).block(d);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAddresses());
        assertEquals(1, result.getAddresses().size());
        verify(paperAddressDao).getPaperAddressesByPaperRequestId(paperRequestId);
    }

    @Test
    void getPaperAddressesByPaperRequestId_emptyList() {
        // Arrange
        String paperRequestId = "test-paper-request-id";
        List<PaperAddressEntity> list = new ArrayList<>();

        when(paperAddressDao.getPaperAddressesByPaperRequestId(Mockito.any())).thenReturn(Flux.fromIterable(list));

        // Act
        PaperAddresses result = paperAddressService.getPaperAddressesByPaperRequestId(paperRequestId).block(d);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAddresses());
        assertEquals(0, result.getAddresses().size());
        verify(paperAddressDao).getPaperAddressesByPaperRequestId(paperRequestId);
    }

    @Test
    void getPaperAddressByIds() {
        //Given
        String paperRequestId = "test-paper-request-id";
        String addressId = "test-address-id";
        PaperAddressEntity paperAddressEntity = new PaperAddressEntity();
        PaperAddress expectedDto = new PaperAddress();

        when(paperAddressDao.getPaperAddressByIds(Mockito.any(), Mockito.any())).thenReturn(Mono.just(paperAddressEntity));

        //When
        PaperAddress result = paperAddressService.getPaperAddressByIds(paperRequestId, addressId).block(d);

        //Then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(paperAddressDao).getPaperAddressByIds(paperRequestId, addressId);
    }

    @Test
    void updatePaperAddress() {
        //Given
        String paperRequestId = "test-paper-request-id";
        String addressId = "test-address-id";
        PaperAddress addressDto = new PaperAddress();
        PaperAddressEntity paperAddressEntity = new PaperAddressEntity();
        paperAddressEntity.setPaperRequestId(paperRequestId);
        paperAddressEntity.setAddressId(addressId);

        when(paperAddressDao.updatePaperAddress(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(paperAddressEntity));

        //When
        assertDoesNotThrow(() -> {
            paperAddressService.updatePaperAddress(paperRequestId, addressId, addressDto).block(d);
        });

        //Then
        verify(paperAddressDao).updatePaperAddress(paperRequestId, addressId, paperAddressEntity);
    }

    @Test
    void deletePaperAddress() {
        //Given
        String paperRequestId = "test-paper-request-id";
        String addressId = "test-address-id";
        PaperAddressEntity paperAddressEntity = new PaperAddressEntity();
        PaperAddress expectedDto = new PaperAddress();

        when(paperAddressDao.deletePaperAddress(Mockito.any(), Mockito.any())).thenReturn(Mono.just(paperAddressEntity));

        //When
        PaperAddress result = paperAddressService.deletePaperAddress(paperRequestId, addressId).block(d);

        //Then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(paperAddressDao).deletePaperAddress(paperRequestId, addressId);
    }

    @Test
    void createPaperAddress() {
        // Arrange
        String iun = "test-iun";
        String addressId = "00000000-0000-0000-0000-000000000001";
        PaperAddressRequest paperAddressRequest = new PaperAddressRequest();
        PaperAddress paperAddressDto = new PaperAddress();
        paperAddressDto.setAddress("Via Test 1");
        paperAddressDto.setCity("Roma");
        paperAddressDto.setName("Mario Rossi");
        paperAddressRequest.setPaperAddress(paperAddressDto);
        paperAddressRequest.setAttempt(1);
        paperAddressRequest.setRecIndex(0);
        paperAddressRequest.setPcRetry(0);
        paperAddressRequest.setNormalized(true);
        paperAddressRequest.setAddressType("RESIDENCE");

        PaperAddressEntity entity = new PaperAddressEntity(iun, addressId);
        entity.setAddress("Via Test 1");
        entity.setCity("Roma");
        entity.setName("Mario Rossi");
        entity.setAttempt(1);
        entity.setRecIndex(0);
        entity.setPcRetry(0);
        entity.setNormalized(true);
        entity.setAddressType("RESIDENCE");

        when(paperAddressDao.updatePaperAddress(any(), any(), any()))
                .thenReturn(Mono.just(entity));

        // Act
        PaperAddressEntity result = paperAddressService.createPaperAddress(iun, paperAddressRequest).block(d);

        // Assert
        assertNotNull(result);
        assertEquals("Via Test 1", result.getAddress());
        assertEquals("Roma", result.getCity());
        assertEquals("Mario Rossi", result.getName());
        assertEquals(1, result.getAttempt());
        assertEquals(0, result.getRecIndex());
        assertEquals(0, result.getPcRetry());
        assertTrue(result.getNormalized());
        assertEquals("RESIDENCE", result.getAddressType());
    }
}