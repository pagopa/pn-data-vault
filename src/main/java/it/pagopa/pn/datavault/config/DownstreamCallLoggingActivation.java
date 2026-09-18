
package it.pagopa.pn.datavault.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.commons.pnclients.filters.DownstreamCallLoggingFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class DownstreamCallLoggingActivation extends DownstreamCallLoggingFilterFactory {

    public DownstreamCallLoggingActivation(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}