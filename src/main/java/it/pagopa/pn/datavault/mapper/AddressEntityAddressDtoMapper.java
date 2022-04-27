package it.pagopa.pn.datavault.mapper;


import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.middleware.db.entities.AddressEntity;
import org.springframework.stereotype.Component;

@Component
public class AddressEntityAddressDtoMapper implements BaseMapperInterface<AddressDto, AddressEntity> {


    private AddressEntityAddressDtoMapper(){
        super();
    }     

    @Override
    public AddressEntity toEntity(AddressDto dto) {throw  new UnsupportedOperationException();}

    @Override
    public AddressDto toDto(AddressEntity entity) {
        final AddressDto target = new AddressDto();
        target.setValue(entity.getValue());
        return target;
    }
}
