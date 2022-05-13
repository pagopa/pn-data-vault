package it.pagopa.pn.datavault.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "pn.data-vault")
@Slf4j
public class PnDatavaultConfig {

    private String dynamodbTableName;
    private String clientTokenizerBasepath;
    private String clientUserregistryBasepath;

    private String tokenizerApiKeyPf;
    private String tokenizerApiKeyPg;

    private String userregistryApiKeyPf;
    private String userregistryApiKeyPg;

    @Value("${pn.env.runtime}")
    private String envRuntime;

    public boolean isDevelopment(){
        return envRuntime!=null && envRuntime.equals("DEVELOPMENT");
    }

    @PostConstruct
    public void init(){
        if (isDevelopment())
            log.warn("DEVELOPMENT IS ACTIVE!");
    }
}
