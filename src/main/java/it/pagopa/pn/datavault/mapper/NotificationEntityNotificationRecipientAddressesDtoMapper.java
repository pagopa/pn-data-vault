package it.pagopa.pn.datavault.mapper;


import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.NotificationRecipientAddressesDto;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NotificationEntityNotificationRecipientAddressesDtoMapper extends PhysicalAddressAnalogDomicileMapper
        implements BaseMapperInterface<NotificationRecipientAddressesDto, NotificationEntity>  {


    private NotificationEntityNotificationRecipientAddressesDtoMapper(){
        super();
    }     

    @Override
    public NotificationEntity toEntity(NotificationRecipientAddressesDto dto) {
        final NotificationEntity target = new NotificationEntity();
        target.setDenomination( dto.getDenomination() );
        target.setDigitalAddress(dto.getDigitalAddress() == null ? null : dto.getDigitalAddress().getValue());
        target.setPhysicalAddress(toPhysicalAddress(dto.getPhysicalAddress()));
        return target;
    }

    @Override
    public NotificationRecipientAddressesDto toDto(NotificationEntity entity) {
        final NotificationRecipientAddressesDto target = new NotificationRecipientAddressesDto();
        target.setDenomination(entity.getDenomination());
        if (StringUtils.hasText(entity.getDigitalAddress())) {
            AddressDto addressDto = new AddressDto();
            addressDto.setValue(entity.getDigitalAddress());
            target.setDigitalAddress(addressDto);
        }
        target.setPhysicalAddress(toAnalogDomicile(entity.getPhysicalAddress()));
        return target;
    }
}
