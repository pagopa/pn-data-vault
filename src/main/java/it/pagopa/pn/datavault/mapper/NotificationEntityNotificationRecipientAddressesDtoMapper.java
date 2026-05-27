package it.pagopa.pn.datavault.mapper;


import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.EmailDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.NotificationRecipientAddressesDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PhoneNumberDto;
import it.pagopa.pn.datavault.middleware.db.entities.EmailEntity;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationEntity;
import it.pagopa.pn.datavault.middleware.db.entities.PhoneNumberEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
        target.setEmails(mapEmailDtoToEntity(dto.getEmails()));
        target.setPhoneNumbers(mapPhoneNumberDtoToEntity(dto.getPhoneNumbers()));
        return target;
    }

    private static List<EmailEntity> mapEmailDtoToEntity(List<EmailDto> emailsDto){
        if(emailsDto == null || emailsDto.isEmpty()){
            return null;
        }

        List<EmailEntity> list = new ArrayList<>();
        for(EmailDto emailDto : emailsDto){
            EmailEntity emailEntity = new EmailEntity();
            emailEntity.setValue(emailDto.getValue());
            list.add(emailEntity);
        }
        return list;
    }

    private static List<PhoneNumberEntity> mapPhoneNumberDtoToEntity(List<PhoneNumberDto> phoneNumbersDto){
        if(phoneNumbersDto == null || phoneNumbersDto.isEmpty()){
            return null;
        }

        List<PhoneNumberEntity> list = new ArrayList<>();
        for(PhoneNumberDto phoneNumberDto : phoneNumbersDto){
            PhoneNumberEntity phoneNumberEntity = new PhoneNumberEntity();
            phoneNumberEntity.setValue(phoneNumberDto.getValue());
            list.add(phoneNumberEntity);
        }
        return list;
    }

    private static List<EmailDto> mapEmailEntityToDto(List<EmailEntity> emails){
        if(emails == null || emails.isEmpty()){
            return null;
        }

        List<EmailDto> list = new ArrayList<>();
        for(EmailEntity email : emails){
            EmailDto emailDto = new EmailDto();
            emailDto.setValue(email.getValue());
            list.add(emailDto);
        }
        return list;
    }

    private static List<PhoneNumberDto> mapPhoneNumberEntityToDto(List<PhoneNumberEntity> phoneNumbers){
        if(phoneNumbers == null || phoneNumbers.isEmpty()){
            return null;
        }

        List<PhoneNumberDto> list = new ArrayList<>();
        for(PhoneNumberEntity phoneNumberEntity : phoneNumbers){
            PhoneNumberDto phoneNumberDto = new PhoneNumberDto();
            phoneNumberDto.setValue(phoneNumberEntity.getValue());
            list.add(phoneNumberDto);
        }
        return list;
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
        target.setRecIndex(Integer.valueOf(entity.getRecipientIndex()));
        target.setEmails(mapEmailEntityToDto(entity.getEmails()));
        target.setPhoneNumbers(mapPhoneNumberEntityToDto(entity.getPhoneNumbers()));
        return target;
    }
}
