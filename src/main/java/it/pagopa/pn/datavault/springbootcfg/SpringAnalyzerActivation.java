package it.pagopa.pn.datavault.springbootcfg;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import it.pagopa.pn.commons.utils.metrics.SpringAnalyzer;
import it.pagopa.pn.commons.utils.metrics.cloudwatch.CloudWatchMetricHandler;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;

@Component
@CustomLog
@Profile("!test")
@Import(CloudWatchMetricHandler.class)
public class SpringAnalyzerActivation extends SpringAnalyzer {

    public static final String PDV_RATE_LIMITER = "pdv-rate-limiter";
    public static final String SELC_RATE_LIMITER = "selc-rate-limiter";
    public static final String PDV_WAITING_REQUESTS = "PDVNumberOfWaitingRequests";
    public static final String SELC_WAITING_REQUESTS = "SELCNumberOfWaitingRequests";
    MeterRegistry meterRegistry;
    CloudWatchMetricHandler cloudWatchMetricHandler;
    private final RateLimiterRegistry rateLimiterRegistry;
    @Value("${spring.application.name}")
    private String applicationName;
    @Value("#{'${pn.ecs.uri}'.split('[/]')[4].split('[-]')[0]}")
    private String taskId;

    public SpringAnalyzerActivation(CloudWatchMetricHandler cloudWatchMetricHandler, MetricsEndpoint metricsEndpoint, MeterRegistry meterRegistry, RateLimiterRegistry rateLimiterRegistry) {
        super(cloudWatchMetricHandler, metricsEndpoint);
        this.cloudWatchMetricHandler = cloudWatchMetricHandler;
        this.meterRegistry = meterRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Scheduled(cron = "${pn.data-vault.cloudwatch-metric-cron}")
    public void sendMetricToCloudWatch() {
        String namespace = "SpringAnalyzer-" + this.applicationName;
        createAndSendMetric(PDV_RATE_LIMITER, namespace, PDV_WAITING_REQUESTS);
        createAndSendMetric(SELC_RATE_LIMITER, namespace, SELC_WAITING_REQUESTS);
    }

    private void createAndSendMetric(String rateLimiterName, String namespace, String metricName) {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterName);

        int availablePermissions = rateLimiter.getMetrics().getAvailablePermissions();
        int numberOfWaitingRequests = availablePermissions >= 0 ? 0 : Math.abs(availablePermissions);
        log.trace("[{}] AvailablePermissions: {} - NumberOfWaitingRequest: {}", namespace, availablePermissions, numberOfWaitingRequests);
        if(numberOfWaitingRequests > 0) {
            log.warn("[{}] {}: {}", namespace, metricName, numberOfWaitingRequests);
        }
        Dimension dimension = Dimension.builder().name("ApplicationName_TaskId").value(this.applicationName + "_" + this.taskId).build();

        this.cloudWatchMetricHandler.sendMetric(namespace, dimension, metricName, numberOfWaitingRequests);
    }
}
