package it.pagopa.pn.datavault.job;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CloudWatchMetricJob {

    public static final String PDV_RATE_LIMITER = "pdv-rate-limiter";
    public static final String SELC_RATE_LIMITER = "selc-rate-limiter";

    private static final UUID UUID_FOR_CLOUDWATCH_METRIC = UUID.randomUUID();
    private static final String NAMESPACE_CW_PDV = "pn-data-vault-" + UUID_FOR_CLOUDWATCH_METRIC;
    private static final String NAMESPACE_CW_SELC = "selc-" + UUID_FOR_CLOUDWATCH_METRIC;

    private final RateLimiterRegistry rateLimiterRegistry;
    private final CloudWatchAsyncClient cloudWatchAsyncClient;
    private final PnDatavaultConfig pnDatavaultConfig;

    @PostConstruct
    public void init() {
        log.info("UUID in Namespace for CloudWatchMetricJob is: {}", UUID_FOR_CLOUDWATCH_METRIC);
    }


    @Scheduled(cron = "${pn.data-vault.cloudwatch-metric-cron}")
    public void sendMetricToCloudWatch() {

        createAndSendMetric(PDV_RATE_LIMITER, NAMESPACE_CW_PDV, "PDVNumberOfWaitingRequests");
        createAndSendMetric(SELC_RATE_LIMITER, NAMESPACE_CW_SELC, "SELCNumberOfWaitingRequests");

    }

    private void createAndSendMetric(String rateLimiterName, String namespace, String metricName) {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterName);

        int availablePermissions = rateLimiter.getMetrics().getAvailablePermissions();
        int numberOfWaitingRequests = availablePermissions >= 0 ? 0 : Math.abs(availablePermissions);
        log.trace("[{}] AvailablePermissions: {} - NumberOfWaitingRequest: {}", namespace, availablePermissions, numberOfWaitingRequests);
        if(numberOfWaitingRequests > 0) {
            log.warn("[{}] {}: {}", namespace, metricName, numberOfWaitingRequests);
        }

        MetricDatum metricDatum = MetricDatum.builder()
                .metricName(metricName)
                .value((double) numberOfWaitingRequests)
                .unit(StandardUnit.COUNT)
                .dimensions(Collections.singletonList(Dimension.builder()
                        .name("Environment")
                        .value(pnDatavaultConfig.getEnvRuntime())
                        .build()))
                .timestamp(Instant.now())
                .build();

        PutMetricDataRequest metricDataRequest = PutMetricDataRequest.builder()
                .namespace(namespace)
                .metricData(Collections.singletonList(metricDatum))
                .build();

        Mono.fromFuture(cloudWatchAsyncClient.putMetricData(metricDataRequest))
                .subscribe(putMetricDataResponse -> log.trace("[{}] PutMetricDataResponse: {}", namespace, putMetricDataResponse),
                        throwable -> log.warn(String.format("[%s] Error sending metric", namespace), throwable));

    }
}
