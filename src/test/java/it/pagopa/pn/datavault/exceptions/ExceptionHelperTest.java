package it.pagopa.pn.datavault.exceptions;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.Problem;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHelperTest {


    @Test
    void handlePnException() {

        //When
        Problem res = ExceptionHelper.handleException(new NotFoundException(), HttpStatus.NOT_FOUND);

        //Then
        assertNotNull(res);
        assertEquals("Oggetto non presente", res.getTitle());
        assertEquals(404, res.getStatus());
    }


    @Test
    void handleException() {

        //When
        Problem res = ExceptionHelper.handleException(new NullPointerException(), HttpStatus.BAD_REQUEST);

        //Then
        assertNotNull(res);
        assertEquals(HttpStatus.BAD_REQUEST.value(), res.getStatus());
    }
}