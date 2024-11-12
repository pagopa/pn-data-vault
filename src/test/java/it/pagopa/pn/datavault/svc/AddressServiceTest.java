package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.exceptions.PnInvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientAddressesDto;
import it.pagopa.pn.datavault.mapper.AddressEntityAddressDtoMapper;
import it.pagopa.pn.datavault.middleware.db.AddressDao;
import it.pagopa.pn.datavault.middleware.db.entities.AddressEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class AddressServiceTest {

    Duration d = Duration.ofMillis(3000);

    @InjectMocks
    private AddressService privateService;

    @Mock
    AddressDao objDao;

    @Mock
    AddressEntityAddressDtoMapper mapper;

    @Test
    void getAddressByInternalId() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();
        List<AddressEntity> list = new ArrayList<>();
        list.add(addressEntity);

        when(objDao.listAddressesById(Mockito.any())).thenReturn(Flux.fromIterable(list));
        when(mapper.toDto(Mockito.any())).thenReturn(new AddressDto());

        //When
        RecipientAddressesDto result = privateService.getAddressByInternalId("").block(d);

        //Then
        assertNotNull(result);
        assertEquals(1, result.getAddresses().size());
    }

    @Test
    void updateAddressByInternalId() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue("test@test.it");

        when(objDao.updateAddress(Mockito.any())).thenReturn(Mono.just(addressEntity));

        //When
        assertDoesNotThrow(() -> {
            privateService.updateAddressByInternalId(addressEntity.getInternalId(), addressEntity.getAddressId(), dto, null).block(d);
        });

        //Then
        // nothing
    }

    @Test
    void updateAddressByInternalIdWithTtl() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue("test@test.it");

        ArgumentCaptor<AddressEntity> argCaptor = ArgumentCaptor.forClass(AddressEntity.class);
        when(objDao.updateAddress(argCaptor.capture())).thenReturn(Mono.just(addressEntity));

        //When
        assertDoesNotThrow(() -> {
            privateService.updateAddressByInternalId(addressEntity.getInternalId(), addressEntity.getAddressId(), dto, BigDecimal.TEN).block(d);
        });

        //Then
        Assertions.assertNotNull(argCaptor.getValue().getExpiration());
    }

    @Test
    void updateAddressByInternalIdInvalid1() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue(null);

        //When
        String iun = addressEntity.getInternalId();
        String addr = addressEntity.getAddressId();
        assertThrows(PnInvalidInputException.class, () ->privateService.updateAddressByInternalId(iun, addr, dto, null));

        //Then
        // nothing
    }

    @Test
    void updateAddressByInternalIdInvalid2() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue("");

        //When
        String iun = addressEntity.getInternalId();
        String addr = addressEntity.getAddressId();
        assertThrows(PnInvalidInputException.class, () -> privateService.updateAddressByInternalId(iun, addr, dto, null));

        //Then
        // nothing
    }

    @Test
    void updateAddressByInternalIdInvalid3() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();

        //When
        String iun = addressEntity.getInternalId();
        String addr = addressEntity.getAddressId();
        assertThrows(PnInvalidInputException.class, () -> privateService.updateAddressByInternalId(iun, addr, null, null));

        //Then
        // nothing
    }

    @Test
    void updateAddressByInternalIdInvalid4() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue("test@test.it");

        //When
        String addr = addressEntity.getAddressId();
        assertThrows(PnInvalidInputException.class, () -> privateService.updateAddressByInternalId(null, addr, dto, null));

        //Then
        // nothing
    }

    @Test
    void updateAddressByInternalIdInvalid5() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue("test@test.it");

        //When
        String iun = addressEntity.getInternalId();
        assertThrows(PnInvalidInputException.class, () -> privateService.updateAddressByInternalId(iun, null, dto, null));

        //Then
        // nothing
    }

    @Test
    void deleteAddressByInternalId() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();

        when(objDao.deleteAddressId(Mockito.any(), Mockito.any())).thenReturn(Mono.just(addressEntity));

        //When
        assertDoesNotThrow(() -> {
            privateService.deleteAddressByInternalId(addressEntity.getInternalId(), addressEntity.getAddressId()).block(d);
        });

        //Then
        // nothing
    }

    @Test
    void deleteAddressByInternalId1() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();

        //When
        String add = addressEntity.getAddressId();
        assertThrows(PnInvalidInputException.class, () ->privateService.deleteAddressByInternalId(null, add));

        //Then
        // nothing
    }

    @Test
    void deleteAddressByInternalId2() {
        //Given
        AddressEntity addressEntity = TestUtils.newAddress();

        //When
        String iun = addressEntity.getInternalId();
        assertThrows(PnInvalidInputException.class, () -> privateService.deleteAddressByInternalId(iun, null));

        //Then
        // nothing
    }
}