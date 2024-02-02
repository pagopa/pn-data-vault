package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.LocalStackTestConfig;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementId;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
@Profile("test")
@Import(LocalStackTestConfig.class)
class NotificationTimelinesDaoTestIT {

    @Autowired
    private NotificationTimelineDao notificationTimelinesDao;

    private NotificationTimelineEntity entity;

    @Test
    void getNotificationTimelines() {
        ConfidentialTimelineElementId c1 = new ConfidentialTimelineElementId();
        c1.setIun("IUN1");
        c1.setTimelineElementId("el1");
        ConfidentialTimelineElementId c2 = new ConfidentialTimelineElementId();
        c2.setIun("IUN1");
        c2.setTimelineElementId("el2");
        List<ConfidentialTimelineElementId> confidentialTimelineElementIds = List.of(c1, c2);

        Flux<NotificationTimelineEntity> result = notificationTimelinesDao.getNotificationTimelines(Flux.fromIterable(confidentialTimelineElementIds));
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }
}