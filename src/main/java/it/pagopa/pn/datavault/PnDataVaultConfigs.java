package it.pagopa.pn.datavault;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.time.Duration;

@Configuration
@ConfigurationProperties( prefix = "pn.data-vault")
@Data
public class PnDataVaultConfigs {

}
