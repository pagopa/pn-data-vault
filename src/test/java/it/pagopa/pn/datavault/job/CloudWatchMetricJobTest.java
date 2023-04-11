package it.pagopa.pn.datavault.job;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import it.pagopa.pn.datavault.config.PnDatavaultConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudWatchMetricJobTest {

    @Mock
    private RateLimiterRegistry rateLimiterRegistry;

    @Mock
    private CloudWatchAsyncClient cloudWatchAsyncClient;

    @Mock
    private PnDatavaultConfig pnDatavaultConfig;

    @InjectMocks
    private CloudWatchMetricJob cloudWatchMetricJob;

    @Test
    void testSendMetricToCloudWatch() {
        RateLimiter rateLimiterPDV = RateLimiter.of("pdv-rate-limiter", RateLimiterConfig.custom().build());
        RateLimiter rateLimiterSELC = RateLimiter.of("selc-rate-limiter", RateLimiterConfig.custom().build());
        when(rateLimiterRegistry.rateLimiter("pdv-rate-limiter")).thenReturn(rateLimiterPDV);
        when(rateLimiterRegistry.rateLimiter("selc-rate-limiter")).thenReturn(rateLimiterSELC);
        when(pnDatavaultConfig.getEnvRuntime()).thenReturn("test");
        PutMetricDataResponse putMetricDataResponse = PutMetricDataResponse.builder().build();
        when(cloudWatchAsyncClient.putMetricData(any(PutMetricDataRequest.class))).thenReturn(CompletableFuture.completedFuture(putMetricDataResponse));

        Assertions.assertDoesNotThrow(() -> cloudWatchMetricJob.sendMetricToCloudWatch());
    }
}
