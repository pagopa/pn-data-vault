package it.pagopa.pn.datavault.exceptions;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.Problem;
import org.springframework.http.HttpStatus;

public class ExceptionHelper {

    private ExceptionHelper(){}
    
    public static Problem handleException(Throwable ex, HttpStatus statusError){
        // gestione exception e generazione fault
        Problem res = new Problem();
        res.setStatus(statusError.value());
        //res.setTraceId();
        if (ex instanceof PnException)
        {
            res.setTitle(ex.getMessage());
            res.setDetail(((PnException)ex).getDescription());
            res.setStatus(((PnException) ex).getStatus());
        }
        else
        {
            // nascondo all'utente l'errore
            res.title("Errore generico");
            res.detail("Qualcosa è andato storto, ritenta più tardi");
        }

        return res;
    }
}
