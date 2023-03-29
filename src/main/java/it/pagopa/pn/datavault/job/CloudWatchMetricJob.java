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

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CloudWatchMetricJob {

    public static final String PDV_RATE_LIMITER = "pdv-rate-limiter";

    private static final UUID UUID_FOR_CLOUDWATCH_METRIC = UUID.randomUUID();

    private final RateLimiterRegistry rateLimiterRegistry;
    private final CloudWatchAsyncClient cloudWatchAsyncClient;
    private final PnDatavaultConfig pnDatavaultConfig;


    @Scheduled(cron = "${pn.data-vault.cloudwatch-metric-cron}")
    public void sendMetricToCloudWatch() {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(PDV_RATE_LIMITER);
        final String NAMESPACE = "pn-data-vault-" + UUID_FOR_CLOUDWATCH_METRIC;

        int availablePermissions = rateLimiter.getMetrics().getAvailablePermissions();
        int numberOfWaitingRequests = availablePermissions >= 0 ? 0 : Math.abs(availablePermissions);
        log.info("[{}] AvailablePermissions: {} - NumberOfWaitingRequest: {}", NAMESPACE, availablePermissions, numberOfWaitingRequests);
        if(numberOfWaitingRequests > 0) {
            log.warn("[{}] PDVNumberOfWaitingRequests: {}", NAMESPACE, numberOfWaitingRequests);
        }

        MetricDatum metricDatum = MetricDatum.builder()
                .metricName("PDVNumberOfWaitingRequests")
                .value((double) numberOfWaitingRequests)
                .unit(StandardUnit.COUNT)
                .dimensions(Collections.singletonList(Dimension.builder()
                        .name("Environment")
                        .value(pnDatavaultConfig.getEnvRuntime())
                        .build()))
                .timestamp(Instant.now())
                .build();

        PutMetricDataRequest putMetricDataRequest = PutMetricDataRequest.builder()
                .namespace(NAMESPACE)
                .metricData(Collections.singletonList(metricDatum))
                .build();


        Mono.fromFuture(cloudWatchAsyncClient.putMetricData(putMetricDataRequest))
                .subscribe(putMetricDataResponse -> log.trace("PutMetricDataResponse: {}", putMetricDataResponse),
                        throwable -> log.warn("Error sending metric", throwable));


    }
}
