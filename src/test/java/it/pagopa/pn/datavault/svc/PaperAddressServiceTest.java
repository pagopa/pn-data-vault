package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddress;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddresses;
import it.pagopa.pn.datavault.mapper.PaperAddressEntityPaperAddressMapper;
import it.pagopa.pn.datavault.middleware.db.PaperAddressDao;
import it.pagopa.pn.datavault.middleware.db.entities.PaperAddressEntity;
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
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class PaperAddressServiceTest {

    Duration d = Duration.ofMillis(3000);

    @InjectMocks
    private PaperAddressService paperAddressService;

    @Mock
    PaperAddressDao paperAddressDao;

    @Mock
    PaperAddressEntityPaperAddressMapper mappingsDao;

    @Test
    void getPaperAddressesByPaperRequestId() {
        // Arrange
        String paperRequestId = "test-paper-request-id";
        PaperAddressEntity paperAddressEntity = new PaperAddressEntity();
        List<PaperAddressEntity> list = new ArrayList<>();
        list.add(paperAddressEntity);

        when(paperAddressDao.getPaperAddressesByPaperRequestId(Mockito.any())).thenReturn(Flux.fromIterable(list));
        when(mappingsDao.toDto(Mockito.any())).thenReturn(new PaperAddress());

        // Act
        PaperAddresses result = paperAddressService.getPaperAddressesByPaperRequestId(paperRequestId).block(d);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAddresses());
        assertEquals(1, result.getAddresses().size());
        verify(paperAddressDao).getPaperAddressesByPaperRequestId(paperRequestId);
        verify(mappingsDao).toDto(paperAddressEntity);
    }

    @Test
    void getPaperAddressByIds() {
        //Given
        String paperRequestId = "test-paper-request-id";
        String addressId = "test-address-id";
        PaperAddressEntity paperAddressEntity = new PaperAddressEntity();
        PaperAddress expectedDto = new PaperAddress();

        when(paperAddressDao.getPaperAddressByIds(Mockito.any(), Mockito.any())).thenReturn(Mono.just(paperAddressEntity));
        when(mappingsDao.toDto(Mockito.any())).thenReturn(expectedDto);

        //When
        PaperAddress result = paperAddressService.getPaperAddressByIds(paperRequestId, addressId).block(d);

        //Then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(paperAddressDao).getPaperAddressByIds(paperRequestId, addressId);
        verify(mappingsDao).toDto(paperAddressEntity);
    }

    @Test
    void updatePaperAddress() {
        //Given
        String paperRequestId = "test-paper-request-id";
        String addressId = "test-address-id";
        PaperAddress addressDto = new PaperAddress();
        PaperAddressEntity paperAddressEntity = new PaperAddressEntity();

        when(mappingsDao.toEntity(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(paperAddressEntity);
        when(paperAddressDao.updatePaperAddress(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(paperAddressEntity));

        //When
        assertDoesNotThrow(() -> {
            paperAddressService.updatePaperAddress(paperRequestId, addressId, addressDto).block(d);
        });

        //Then
        verify(mappingsDao).toEntity(paperRequestId, addressId, addressDto);
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
        when(mappingsDao.toDto(Mockito.any())).thenReturn(expectedDto);

        //When
        PaperAddress result = paperAddressService.deletePaperAddress(paperRequestId, addressId).block(d);

        //Then
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(paperAddressDao).deletePaperAddress(paperRequestId, addressId);
        verify(mappingsDao).toDto(paperAddressEntity);
    }
}