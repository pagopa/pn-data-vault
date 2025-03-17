package it.pagopa.pn.datavault.exceptions;

import it.pagopa.pn.commons.exceptions.PnExceptionsCodes;

public class PnDatavaultExceptionCodes extends PnExceptionsCodes {

    // raccolgo qui tutti i codici di errore di datavault
    public static final String ERROR_CODE_DATAVAULT_RECIPIENT_NOT_FOUND = "PN_DATAVAULT_RECIPIENTNOTFOUND";
    public static final String ERROR_CODE_DATAVAULT_MALFORMED_INPUT = "PN_DATAVAULT_MALFORMED_INPUT";
    public static final String ERROR_MESSAGE_DATAVAULT_MALFORMED_BODY_UPDATEADDRESSES = "recIndex must be specified for each address or none.";
}
