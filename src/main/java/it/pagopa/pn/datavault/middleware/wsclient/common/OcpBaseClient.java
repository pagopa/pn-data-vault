package it.pagopa.pn.datavault.middleware.wsclient.common;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public abstract class OcpBaseClient extends CommonBaseClient {
    private static final String HEADER_API_KEY = "Ocp-Apim-Subscription-Key";

    protected WebClient.Builder initWebClient(WebClient.Builder builder, String apiKey){

        return super.enrichBuilder(builder)
                .defaultHeader(HEADER_API_KEY, apiKey);
    }

}
