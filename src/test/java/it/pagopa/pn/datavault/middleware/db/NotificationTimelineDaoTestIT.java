package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.LocalStackTestConfig;
import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.ConfidentialTimelineElementId;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest
@Import(LocalStackTestConfig.class)
class NotificationTimelineDaoTestIT {

    @Autowired
    private NotificationTimelineDao notificationDao;

    @Test
    void updateNotification() {
        //Given
        NotificationTimelineEntity addresToInsert = new NotificationTimelineEntity("IUN2", "el1");
        //When
        notificationDao.updateNotification(addresToInsert).block(Duration.ofMillis(3000));

        //Then
        try {
            NotificationTimelineEntity elementFromDb = notificationDao.getNotificationTimelineByIunAndTimelineElementId(addresToInsert.getInternalId(), addresToInsert.getTimelineElementId()).block();

            Assertions.assertNotNull( elementFromDb);
            Assertions.assertEquals(addresToInsert, elementFromDb);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void getNotificationTimelineByIunAndTimelineElementId() {
        String iun = "IUN1";
        String elementId = "el1";

        NotificationTimelineEntity result = notificationDao.getNotificationTimelineByIunAndTimelineElementId(iun, elementId).block(Duration.ofMillis(3000));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(iun, result.getInternalId());
        Assertions.assertEquals(elementId, result.getTimelineElementId());

    }

    @Test
    void getNotificationTimelineByIun() {

        String iun = "IUN1";

        List<NotificationTimelineEntity> results = notificationDao.getNotificationTimelineByIun(iun).collectList().block(Duration.ofMillis(3000));

        Assertions.assertNotNull(results);
        Assertions.assertEquals(3, results.size());

    }

    @Test
    void getNotificationTimelines() {
        ConfidentialTimelineElementId c1 = new ConfidentialTimelineElementId();
        c1.setIun("IUN1");
        c1.setTimelineElementId("el1");
        ConfidentialTimelineElementId c2 = new ConfidentialTimelineElementId();
        c2.setIun("IUN1");
        c2.setTimelineElementId("el2");
        List<ConfidentialTimelineElementId> confidentialTimelineElementIds = List.of(c1, c2);

        Flux<NotificationTimelineEntity> result = notificationDao.getNotificationTimelines(Flux.fromIterable(confidentialTimelineElementIds));
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

}