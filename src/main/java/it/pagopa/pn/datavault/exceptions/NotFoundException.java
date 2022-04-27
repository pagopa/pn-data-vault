package it.pagopa.pn.datavault.exceptions;

public class NotFoundException extends PnException {


    public NotFoundException() {
        super("Oggetto non presente", "Non è stata trovata nessuna referenza", 404);
    }

}
