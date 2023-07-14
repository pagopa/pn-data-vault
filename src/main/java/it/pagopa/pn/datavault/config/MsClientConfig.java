package it.pagopa.pn.datavault.config;

import io.netty.handler.logging.LogLevel;
import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.datavault.generated.openapi.msclient.selfcarepg.v1.ApiClient;
import it.pagopa.pn.datavault.generated.openapi.msclient.selfcarepg.v1.api.InstitutionsApi;
import it.pagopa.pn.datavault.generated.openapi.msclient.selfcarepg.v1.api.InstitutionsPnpgApi;
import it.pagopa.pn.datavault.generated.openapi.msclient.tokenizer.v1.api.TokenApi;
import it.pagopa.pn.datavault.generated.openapi.msclient.userregistry.v1.api.UserApi;
import it.pagopa.pn.datavault.middleware.wsclient.common.OcpBaseClient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MsClientConfig {

    @Configuration
    static class SelfcareApi extends OcpBaseClient {

        @Bean
        InstitutionsApi institutionsApi(PnDatavaultConfig pnDatavaultConfig) {
            var apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), pnDatavaultConfig.getSelfcarepgApiKeyPg()).build());
            apiClient.setBasePath(pnDatavaultConfig.getClientSelfcarepgBasepath());
            return new InstitutionsApi(apiClient);
        }

        @Bean
        InstitutionsPnpgApi institutionsPnpgApi(PnDatavaultConfig pnDatavaultConfig) {
            var apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder(), pnDatavaultConfig.getSelfcarepgApiKeyPg()).build());
            apiClient.setBasePath(pnDatavaultConfig.getClientSelfcarepgBasepath());
            return new InstitutionsPnpgApi(apiClient);
        }
    }

    @Configuration
    static class BaseClients extends CommonBaseClient {

        @Autowired
        private PnDatavaultConfig dataVaultConfiguration;

        @Bean
        UserApi userClientPF(PnDatavaultConfig pnDatavaultConfig) {
            var apiClient = new it.pagopa.pn.datavault.generated.openapi.msclient.userregistry.v1.ApiClient(initWebClient(it.pagopa.pn.datavault.generated.openapi.msclient.userregistry.v1.ApiClient.buildWebClientBuilder(), pnDatavaultConfig.getUserregistryApiKeyPf()));
            apiClient.setBasePath(pnDatavaultConfig.getClientUserregistryBasepath());
            return new UserApi(apiClient);
        }

        @Bean
        TokenApi tokenApiPF(PnDatavaultConfig pnDatavaultConfig) {
            var apiClient = new it.pagopa.pn.datavault.generated.openapi.msclient.tokenizer.v1.ApiClient(initWebClient(it.pagopa.pn.datavault.generated.openapi.msclient.tokenizer.v1.ApiClient.buildWebClientBuilder(), pnDatavaultConfig.getTokenizerApiKeyPf()));
            apiClient.setBasePath(pnDatavaultConfig.getClientTokenizerBasepath());
            return new TokenApi(apiClient);
        }

        private static final String HEADER_API_KEY = "x-api-key";

        protected WebClient initWebClient(WebClient.Builder builder, String apiKey){

            return super.enrichWithDefaultProps( builder )
            //return super.enrichBuilder(builder)
                    .defaultHeader(HEADER_API_KEY, apiKey)
                    .build();
        }

        @Override
        protected HttpClient buildHttpClient() {
            HttpClient httpClient = super.buildHttpClient();
            if( dataVaultConfiguration.isWiretapEnabled() ) {
                httpClient = httpClient.wiretap( true );
                httpClient.wiretap("reactor.netty.http.client.HttpClient", LogLevel.TRACE, AdvancedByteBufFormat.TEXTUAL);
            }
            return httpClient;
        }
    }
}
