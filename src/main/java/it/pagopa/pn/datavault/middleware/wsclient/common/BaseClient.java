package it.pagopa.pn.datavault.middleware.wsclient.common;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

public abstract class BaseClient extends CommonBaseClient {

    private static final String HEADER_API_KEY = "x-api-key";

    protected  BaseClient( ){
    }

    protected WebClient initWebClient(WebClient.Builder builder, String apiKey){

        return super.enrichBuilder(builder)
                .defaultHeader(HEADER_API_KEY, apiKey)
                .build();
    }


    protected UUID getUUIDFromInternalId(String internalId)
    {
        internalId = internalId.substring(3);
        return UUID.fromString(internalId);
    }

    protected RecipientType getRecipientTypeFromInternalId(String internalId)
    {
        if (internalId.startsWith(RecipientType.PF.getValue()))
            return RecipientType.PF;
        else
            return RecipientType.PG;
    }
}
