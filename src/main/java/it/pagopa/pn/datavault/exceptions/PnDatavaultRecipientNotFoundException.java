package it.pagopa.pn.datavault.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import org.springframework.http.HttpStatus;

import static it.pagopa.pn.datavault.exceptions.PnDatavaultExceptionCodes.ERROR_CODE_DATAVAULT_RECIPIENT_NOT_FOUND;


public class PnDatavaultRecipientNotFoundException extends PnRuntimeException {

    public PnDatavaultRecipientNotFoundException() {
        super("Oggetto non presente", "Non è stata trovata nessuna referenza", HttpStatus.NOT_FOUND.value(), ERROR_CODE_DATAVAULT_RECIPIENT_NOT_FOUND, null, null);
    }

}
