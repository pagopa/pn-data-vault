package it.pagopa.pn.datavault.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "pn.data-vault")
public class PnDatavaultConfig {

    private String dynamodbTableName;
    private String clientTokenizerBasepath;
    private String clientUserregistryBasepath;
}
