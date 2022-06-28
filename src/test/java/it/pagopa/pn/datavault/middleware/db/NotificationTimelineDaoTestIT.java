package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import it.pagopa.pn.datavault.middleware.db.entities.PhysicalAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.fail;


@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "aws.region-code=us-east-1",
        "aws.profile-name=${PN_AWS_PROFILE_NAME:default}",
        "aws.endpoint-url=http://localhost:4566"
})
@SpringBootTest
public class NotificationTimelineDaoTestIT {

    @Autowired
    private NotificationTimelineDao notificationDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    TestDao<NotificationTimelineEntity> testDao;

    @BeforeEach
    void setup( @Value("${pn.data-vault.dynamodb_table-name}") String table) {
        testDao = new TestDao<NotificationTimelineEntity>( dynamoDbEnhancedAsyncClient, table, NotificationTimelineEntity.class);
    }

    @Test
    void updateNotification() {
        //Given
        NotificationTimelineEntity addresToInsert = newNotification();

        try {
            testDao.delete(addresToInsert.getPk(), addresToInsert.getTimelineElementId());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        notificationDao.updateNotification(addresToInsert).block(Duration.ofMillis(3000));

        //Then
        try {
            NotificationTimelineEntity elementFromDb = testDao.get(addresToInsert.getPk(), addresToInsert.getTimelineElementId());

            Assertions.assertNotNull( elementFromDb);
            Assertions.assertEquals(addresToInsert, elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(addresToInsert.getPk(), addresToInsert.getTimelineElementId());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void getNotificationTimelineByIunAndTimelineElementId() {
        //Given
        List<NotificationTimelineEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationTimelineEntity ae = newNotification();
            ae.setTimelineElementId(ae.getTimelineElementId() + "_" + i);
            notificationsEntities.add(ae);
        }


        try {
            notificationsEntities.forEach(m -> {
                try {
                    testDao.delete(m.getPk(), m.getTimelineElementId());
                } catch (ExecutionException e) {
                    System.out.println("Nothing to remove");
                } catch (InterruptedException e) {
                    System.out.println("Nothing to remove");
                    Thread.currentThread().interrupt();
                }
                notificationDao.updateNotification(m).block(Duration.ofMillis(3000));

            });
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        NotificationTimelineEntity result = notificationDao.getNotificationTimelineByIunAndTimelineElementId(notificationsEntities.get(0).getInternalId(), notificationsEntities.get(0).getTimelineElementId()).block(Duration.ofMillis(3000));

        //Then
        try {
            Assertions.assertNotNull(result);
            Assertions.assertEquals(notificationsEntities.get(0), result);

        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                notificationsEntities.forEach(m -> {
                    try {
                        testDao.delete(m.getPk(), m.getTimelineElementId());
                    } catch (ExecutionException e) {
                        System.out.println("Nothing to remove");
                    } catch (InterruptedException e) {
                        System.out.println("Nothing to remove");
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void getNotificationTimelineByIun() {
        //Given
        List<NotificationTimelineEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationTimelineEntity ae = newNotification();
            ae.setTimelineElementId(ae.getTimelineElementId() + "_" + i);
            notificationsEntities.add(ae);
        }


        try {
            notificationsEntities.forEach(m -> {
                try {
                    testDao.delete(m.getPk(), m.getTimelineElementId());
                } catch (ExecutionException e) {
                    System.out.println("Nothing to remove");
                } catch (InterruptedException e) {
                    System.out.println("Nothing to remove");
                    Thread.currentThread().interrupt();
                }
                notificationDao.updateNotification(m).block(Duration.ofMillis(3000));

            });
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        List<NotificationTimelineEntity> results = notificationDao.getNotificationTimelineByIun(notificationsEntities.get(0).getInternalId()).collectList().block(Duration.ofMillis(3000));

        //Then
        try {
            Assertions.assertNotNull(results);
            Assertions.assertEquals(N, results.size());
            for(int i = 0;i<N;i++)
            {
                Assertions.assertTrue(results.contains(notificationsEntities.get(i)));
            }
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                notificationsEntities.forEach(m -> {
                    try {
                        testDao.delete(m.getPk(), m.getTimelineElementId());
                    } catch (ExecutionException e) {
                        System.out.println("Nothing to remove");
                    } catch (InterruptedException e) {
                        System.out.println("Nothing to remove");
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }


    public static NotificationTimelineEntity newNotification(){
        NotificationTimelineEntity ne = new NotificationTimelineEntity("425e4567-e89b-12d3-a456-426655449631", "mario rossi");
        ne.setDigitalAddress("mario.rossi@test.it");
        PhysicalAddress pa = new PhysicalAddress();
        pa.setAddress("via casa sua");
        pa.setAt("via");
        pa.setAddressDetails("interno 2");
        pa.setMunicipality("Venezia");
        pa.setMunicipalityDetails("zattere");
        pa.setCap("30000");
        pa.setProvince("VE");
        pa.setState("Italia");
        ne.setPhysicalAddress(pa);
        return ne;
    }
}