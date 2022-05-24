package it.pagopa.pn.datavault.exceptions;

public class InvalidInputException extends PnException {


    public InvalidInputException(String message) {
        super("Parametri non validi", "Alcuni parametri non sono validi: " + message);
    }

}
