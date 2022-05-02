package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.exceptions.InvalidInputException;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientAddressesDto;
import it.pagopa.pn.datavault.mapper.AddressEntityAddressDtoMapper;
import it.pagopa.pn.datavault.middleware.db.AddressDao;
import it.pagopa.pn.datavault.middleware.db.AddressDaoTestIT;
import it.pagopa.pn.datavault.middleware.db.entities.AddressEntity;
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
        AddressEntity addressEntity = AddressDaoTestIT.newAddress();
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
        AddressEntity addressEntity = AddressDaoTestIT.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue("test@test.it");

        when(objDao.updateAddress(Mockito.any())).thenReturn(Mono.just(addressEntity));

        //When
        assertDoesNotThrow(() -> {
            privateService.updateAddressByInternalId(addressEntity.getInternalId(), addressEntity.getAddressId(), dto).block(d);
        });

        //Then
        // nothing
    }

    @Test
    void updateAddressByInternalIdInvalid1() {
        //Given
        AddressEntity addressEntity = AddressDaoTestIT.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue(null);

        //When
        String iun = addressEntity.getInternalId();
        String addr = addressEntity.getAddressId();
        assertThrows(InvalidInputException.class, () ->privateService.updateAddressByInternalId(iun, addr, dto));

        //Then
        // nothing
    }

    @Test
    void updateAddressByInternalIdInvalid2() {
        //Given
        AddressEntity addressEntity = AddressDaoTestIT.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue("");

        //When
        String iun = addressEntity.getInternalId();
        String addr = addressEntity.getAddressId();
        assertThrows(InvalidInputException.class, () -> privateService.updateAddressByInternalId(iun, addr, dto));

        //Then
        // nothing
    }

    @Test
    void updateAddressByInternalIdInvalid3() {
        //Given
        AddressEntity addressEntity = AddressDaoTestIT.newAddress();

        //When
        String iun = addressEntity.getInternalId();
        String addr = addressEntity.getAddressId();
        assertThrows(InvalidInputException.class, () -> privateService.updateAddressByInternalId(iun, addr, null));

        //Then
        // nothing
    }

    @Test
    void updateAddressByInternalIdInvalid4() {
        //Given
        AddressEntity addressEntity = AddressDaoTestIT.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue("test@test.it");

        //When
        String addr = addressEntity.getAddressId();
        assertThrows(InvalidInputException.class, () -> privateService.updateAddressByInternalId(null, addr, dto));

        //Then
        // nothing
    }

    @Test
    void updateAddressByInternalIdInvalid5() {
        //Given
        AddressEntity addressEntity = AddressDaoTestIT.newAddress();
        AddressDto dto = new AddressDto();
        dto.setValue("test@test.it");

        //When
        String iun = addressEntity.getInternalId();
        assertThrows(InvalidInputException.class, () -> privateService.updateAddressByInternalId(iun, null, dto));

        //Then
        // nothing
    }

    @Test
    void deleteAddressByInternalId() {
        //Given
        AddressEntity addressEntity = AddressDaoTestIT.newAddress();

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
        AddressEntity addressEntity = AddressDaoTestIT.newAddress();

        //When
        String add = addressEntity.getAddressId();
        assertThrows(InvalidInputException.class, () ->privateService.deleteAddressByInternalId(null, add));

        //Then
        // nothing
    }

    @Test
    void deleteAddressByInternalId2() {
        //Given
        AddressEntity addressEntity = AddressDaoTestIT.newAddress();

        //When
        String iun = addressEntity.getInternalId();
        assertThrows(InvalidInputException.class, () -> privateService.deleteAddressByInternalId(iun, null));

        //Then
        // nothing
    }
}