package it.pagopa.pn.datavault.exceptions;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.Problem;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

@Slf4j
public class ExceptionHelper {

    public static final String MDC_TRACE_ID_KEY = "trace_id";

    private ExceptionHelper(){}
    
    public static Problem handleException(Throwable ex, HttpStatus statusError){
        // gestione exception e generazione fault
        Problem res = new Problem();
        res.setStatus(statusError.value());
        try {
            res.setTraceId(MDC.get(MDC_TRACE_ID_KEY));
        } catch (Exception e) {
            log.warn("Cannot get traceid", e);
        }

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
