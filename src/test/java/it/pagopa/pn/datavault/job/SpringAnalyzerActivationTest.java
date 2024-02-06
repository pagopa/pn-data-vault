package it.pagopa.pn.datavault.job;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import it.pagopa.pn.commons.utils.metrics.cloudwatch.CloudWatchMetricHandler;
import it.pagopa.pn.datavault.springbootcfg.SpringAnalyzerActivation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringAnalyzerActivationTest {

    @Mock
    private RateLimiterRegistry rateLimiterRegistry;

    @InjectMocks
    private SpringAnalyzerActivation springAnalyzerActivation;

    @Mock
    private CloudWatchMetricHandler cloudWatchMetricHandler;

    @Test
    void testSendMetricToCloudWatch() {
        RateLimiter rateLimiterPDV = RateLimiter.of("pdv-rate-limiter", RateLimiterConfig.custom().build());
        RateLimiter rateLimiterSELC = RateLimiter.of("selc-rate-limiter", RateLimiterConfig.custom().build());
        when(rateLimiterRegistry.rateLimiter("pdv-rate-limiter")).thenReturn(rateLimiterPDV);
        when(rateLimiterRegistry.rateLimiter("selc-rate-limiter")).thenReturn(rateLimiterSELC);
        when(cloudWatchMetricHandler.sendMetric(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyDouble())).thenReturn(null);

        Assertions.assertDoesNotThrow(() -> springAnalyzerActivation.sendMetricToCloudWatch());
    }
}
