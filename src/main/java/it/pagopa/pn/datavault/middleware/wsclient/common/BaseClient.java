package it.pagopa.pn.datavault.middleware.wsclient.common;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

public abstract class BaseClient {

    //FIXME: sistemare i valori corretti qui sotto
    private static final String HEADER_API_KEY = "x-api-key";
    private static final String HEADER_API_KEY_VALUE_PF = "pf";
    private static final String HEADER_API_KEY_VALUE_PG = "pg";

    protected  BaseClient( ){
    }

    protected WebClient initWebClient(WebClient.Builder builder, boolean isPF){

        HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS)));

        return builder.clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HEADER_API_KEY, isPF?HEADER_API_KEY_VALUE_PF:HEADER_API_KEY_VALUE_PG)
                .build();
    }
}
