package it.pagopa.pn.datavault.config;

import it.pagopa.pn.datavault.generated.openapi.msclient.selfcarepg.v1.ApiClient;
import it.pagopa.pn.datavault.generated.openapi.msclient.selfcarepg.v1.api.InstitutionsApi;
import it.pagopa.pn.datavault.generated.openapi.msclient.selfcarepg.v1.api.InstitutionsPnpgApi;
import it.pagopa.pn.datavault.generated.openapi.msclient.tokenizer.v1.api.TokenApi;
import it.pagopa.pn.datavault.generated.openapi.msclient.userregistry.v1.api.UserApi;
import it.pagopa.pn.datavault.middleware.wsclient.common.BaseClient;
import it.pagopa.pn.datavault.middleware.wsclient.common.OcpBaseClient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    static class BaseClients extends BaseClient {

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

    }



}
