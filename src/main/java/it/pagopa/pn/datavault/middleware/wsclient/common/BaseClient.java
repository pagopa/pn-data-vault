package it.pagopa.pn.datavault.middleware.wsclient.common;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class BaseClient extends CommonBaseClient {

    private static final String HEADER_API_KEY = "x-api-key";

    protected  BaseClient( ){
    }

    protected WebClient initWebClient(WebClient.Builder builder, String apiKey){
        ConnectionProvider provider = ConnectionProvider.builder("fixed")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120)).build();

        HttpClient httpClient = HttpClient.create(provider).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS)));

        return super.enrichBuilder(builder)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
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
