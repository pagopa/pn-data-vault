package it.pagopa.pn.datavault.middleware.wsclient.common;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class BaseClient extends CommonBaseClient {

    private static final String HEADER_API_KEY = "x-api-key";

    protected  BaseClient( ){
    }

    protected WebClient initWebClient(WebClient.Builder builder, String apiKey){

        return super.enrichBuilder(builder)
                .defaultHeader(HEADER_API_KEY, apiKey)
                .build();
    }

}
