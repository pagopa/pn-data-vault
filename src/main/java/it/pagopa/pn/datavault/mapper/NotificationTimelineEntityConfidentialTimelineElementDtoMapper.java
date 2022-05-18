package it.pagopa.pn.datavault.mapper;


import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementDto;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationTimelineEntityConfidentialTimelineElementDtoMapper  extends PhysicalAddressAnalogDomicileMapper
        implements BaseMapperInterface<ConfidentialTimelineElementDto, NotificationTimelineEntity> {


    private NotificationTimelineEntityConfidentialTimelineElementDtoMapper(){
        super();
    }     

    @Override
    public NotificationTimelineEntity toEntity(ConfidentialTimelineElementDto dto) {
        final NotificationTimelineEntity target = new NotificationTimelineEntity();
        target.setTimelineElementId(dto.getTimelineElementId());
        target.setDigitalAddress(dto.getDigitalAddress().getValue());
        target.setPhysicalAddress(toPhysicalAddress(dto.getPhysicalAddress()));
        target.setNewPhysicalAddress(toPhysicalAddress(dto.getNewPhysicalAddress()));
        return target;
    }

    @Override
    public ConfidentialTimelineElementDto toDto(NotificationTimelineEntity entity) {
        final ConfidentialTimelineElementDto target = new ConfidentialTimelineElementDto();
        target.setTimelineElementId(entity.getTimelineElementId());
        AddressDto addressDto = new AddressDto();
        addressDto.setValue(entity.getDigitalAddress());
        target.setDigitalAddress(addressDto);
        target.setPhysicalAddress(toAnalogDomicile(entity.getPhysicalAddress()));
        target.setNewPhysicalAddress(toAnalogDomicile(entity.getNewPhysicalAddress()));
        return target;
    }
}
