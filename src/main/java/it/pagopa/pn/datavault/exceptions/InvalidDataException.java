package it.pagopa.pn.datavault.exceptions;

public class InvalidDataException extends PnException {


    public InvalidDataException() {
        super("Dati non validi", "Alcuni dati non sono validi");
    }

}
