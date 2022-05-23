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
        if(dto.getPhysicalAddress() != null){
            target.setPhysicalAddress(toPhysicalAddress(dto.getPhysicalAddress()));
        }
        if(dto.getNewPhysicalAddress() != null){
            target.setNewPhysicalAddress(toPhysicalAddress(dto.getNewPhysicalAddress()));
        }
        return target;
    }

    @Override
    public ConfidentialTimelineElementDto toDto(NotificationTimelineEntity entity) {
        final ConfidentialTimelineElementDto target = new ConfidentialTimelineElementDto();
        target.setTimelineElementId(entity.getTimelineElementId());
        AddressDto addressDto = new AddressDto();
        addressDto.setValue(entity.getDigitalAddress());
        target.setDigitalAddress(addressDto);
        if(entity.getPhysicalAddress() != null){
            target.setPhysicalAddress(toAnalogDomicile(entity.getPhysicalAddress()));
        }
        if(entity.getNewPhysicalAddress() != null){
            target.setNewPhysicalAddress(toAnalogDomicile(entity.getNewPhysicalAddress()));
        }
        return target;
    }
}
