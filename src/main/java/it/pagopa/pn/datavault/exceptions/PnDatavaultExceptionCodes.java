package it.pagopa.pn.datavault.exceptions;

import it.pagopa.pn.commons.exceptions.PnExceptionsCodes;

public class PnDatavaultExceptionCodes extends PnExceptionsCodes {

    // raccolgo qui tutti i codici di errore delle deleghe
    public static final String ERROR_CODE_DATAVAULT_RECIPIENT_NOT_FOUND = "PN_DATAVAULT_RECIPIENTNOTFOUND";
    public static final String ERROR_CODE_MANDATE_ALREADY_EXISTS = "PN_MANDATE_ALREADYEXISTS";
    public static final String ERROR_CODE_MANDATE_NOTACCEPTABLE = "PN_MANDATE_NOTACCEPTABLE";
    public static final String ERROR_CODE_MANDATE_DELEGATE_HIMSELF = "PN_MANDATE_DELEGATEHIMSELF";
    public static final String ERROR_CODE_INVALID_VERIFICATION_CODE = "PN_MANDATE_INVALIDVERIFICATIONCODE";
}
