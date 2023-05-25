package it.pagopa.pn.datavault.utils;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.AddressDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.DenominationDto;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtils {

    private static final String INTERNAL_ID = "InternalId";
    private static final String ADDRESS_ID = "AddressId";
    private static final String MANDATE_ID = "MandateId";
    private static final String DENOMINATION = "Denomination";
    private static final String DIGITAL_ADDRESS = "DigitalAddress";
    private static final String DENOMINATION_DTO = "DenominationDto";


    public static boolean checkInternalId(String internalId) {
        return checkString(internalId, INTERNAL_ID);
    }

    public static boolean checkAddressId(String addressId) {
        return checkString(addressId, ADDRESS_ID);
    }

    public static boolean checkAddressDto(AddressDto addressDto) {
        log.logChecking("AddressDto");
        if (addressDto == null || !StringUtils.hasText(addressDto.getValue())) return false;
        log.logCheckingOutcome("AddressDto", true);
        return true;
    }

    public static boolean checkMandateId(String mandateId) {
        return checkString(mandateId, MANDATE_ID);
    }

    public static boolean checkDenominationDto(DenominationDto denominationDto) {
        log.logChecking(DENOMINATION_DTO);
        if (denominationDto == null)
        {
            log.logCheckingOutcome(DENOMINATION_DTO, false, "Is null");
            return false;
        }
        if( !StringUtils.hasText(denominationDto.getDestName())
                && !StringUtils.hasText(denominationDto.getDestSurname())
                && !StringUtils.hasText(denominationDto.getDestBusinessName()) ) {

            log.logCheckingOutcome(DENOMINATION_DTO, false, "DestName, DestSurname and DestBusinessName have no text");
            return false;
        }
        if(!StringUtils.hasText(denominationDto.getDestName())
                && StringUtils.hasText(denominationDto.getDestSurname())) {

            log.logCheckingOutcome(DENOMINATION_DTO, false, "DestName has no text and DestSurname has text.");
            return false;

        }

        if(!StringUtils.hasText(denominationDto.getDestSurname())
                && StringUtils.hasText(denominationDto.getDestName())) {

            log.logCheckingOutcome(DENOMINATION_DTO, false, "DestSurname has no text and DestName has text.");
            return false;

        }
        log.logCheckingOutcome(DENOMINATION_DTO, true);
        return true;
    }

    public static boolean checkDenomination(String denomination) {
        return checkString(denomination, DENOMINATION);
    }

    public static boolean checkDigitalAddress(AddressDto digitalAddress) {
        log.logChecking(DIGITAL_ADDRESS);
        if (digitalAddress != null && !StringUtils.hasText(digitalAddress.getValue())) {
            log.logCheckingOutcome(DIGITAL_ADDRESS, false, "Has no text");
            return false;
        }
        log.logCheckingOutcome(DIGITAL_ADDRESS, true);
        return true;
    }

    private static boolean checkString(String fieldValue, String fieldName) {
        log.logChecking(fieldName);
        if (!StringUtils.hasText(fieldValue)) {
            log.logCheckingOutcome(fieldName, false, "Has no text");
            return false;
        }
        log.logCheckingOutcome(fieldName, true);
        return true;
    }
}
