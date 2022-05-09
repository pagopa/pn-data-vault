package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.middleware.db.entities.NotificationEntity;
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
public class NotificationDaoTestIT {

    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    TestDao<NotificationEntity> testDao;

    @BeforeEach
    void setup( @Value("${pn.data-vault.dynamodb_table-name}") String table) {
        testDao = new TestDao<NotificationEntity>( dynamoDbEnhancedAsyncClient, table, NotificationEntity.class);
    }

    @Test
    void updateNotifications() {
        //Given
        NotificationEntity addresToInsert = newNotification();
        List<NotificationEntity> l = new ArrayList<>();
        l.add(addresToInsert);

        try {
            testDao.delete(addresToInsert.getPk(), addresToInsert.getRecipientIndex());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        notificationDao.updateNotifications(l).block(Duration.ofMillis(3000));

        //Then
        try {
            NotificationEntity elementFromDb = testDao.get(addresToInsert.getPk(), addresToInsert.getRecipientIndex());

            Assertions.assertNotNull( elementFromDb);
            Assertions.assertEquals(addresToInsert, elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(addresToInsert.getPk(), addresToInsert.getRecipientIndex());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void updateNotificationsMultiple() {
        //Given
        List<NotificationEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationEntity ae = newNotification();
            ae.setRecipientIndex(ae.getRecipientIndex() + "_" + i);
            notificationsEntities.add(ae);
        }


        try {
            notificationsEntities.forEach(m -> {
                try {
                    testDao.delete(m.getPk(), m.getRecipientIndex());
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

        //When
        notificationDao.updateNotifications(notificationsEntities).block(Duration.ofMillis(3000));

        //Then
        try {
            notificationsEntities.forEach(a -> {
                NotificationEntity elementFromDb = null;
                try {
                    elementFromDb = testDao.get(a.getPk(), a.getRecipientIndex());
                } catch (ExecutionException e) {
                    fail("get exception");
                } catch (InterruptedException e) {
                    fail("get exception");
                }

                Assertions.assertNotNull(elementFromDb);
                Assertions.assertEquals(a, elementFromDb);
            });

        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                notificationsEntities.forEach(m -> {
                    try {
                        testDao.delete(m.getPk(), m.getRecipientIndex());
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
    void deleteNotificationByIun() {
        //Given
        NotificationEntity addresToInsert = newNotification();
        List<NotificationEntity> l = new ArrayList<>();
        l.add(addresToInsert);

        try {
            testDao.delete(addresToInsert.getPk(), addresToInsert.getRecipientIndex());
            notificationDao.updateNotifications(l).block(Duration.ofMillis(3000));
            NotificationEntity elementFromDb = testDao.get(addresToInsert.getPk(), addresToInsert.getRecipientIndex());
            Assertions.assertNotNull( elementFromDb);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        notificationDao.deleteNotificationByIun(addresToInsert.getInternalId()).block(Duration.ofMillis(3000));

        //Then
        try {
            NotificationEntity elementFromDb = testDao.get(addresToInsert.getPk(), addresToInsert.getRecipientIndex());

            Assertions.assertNull( elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(addresToInsert.getPk(), addresToInsert.getRecipientIndex());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void deleteNotificationByIunMultiple() {
        //Given
        List<NotificationEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationEntity ae = newNotification();
            ae.setRecipientIndex(ae.getRecipientIndex() + "_" + i);
            notificationsEntities.add(ae);
        }


        try {
            notificationsEntities.forEach(m -> {
                try {
                    testDao.delete(m.getPk(), m.getRecipientIndex());
                } catch (ExecutionException e) {
                    System.out.println("Nothing to remove");
                } catch (InterruptedException e) {
                    System.out.println("Nothing to remove");
                    Thread.currentThread().interrupt();
                }

            });
            notificationDao.updateNotifications(notificationsEntities).block(Duration.ofMillis(3000));
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        notificationDao.deleteNotificationByIun(notificationsEntities.get(0).getInternalId()).block(Duration.ofMillis(3000));

        //Then
        try {
            NotificationEntity elementFromDb = testDao.get(notificationsEntities.get(0).getPk(), notificationsEntities.get(0).getRecipientIndex());

            Assertions.assertNull( elementFromDb);
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                notificationsEntities.forEach(m -> {
                    try {
                        testDao.delete(m.getPk(), m.getRecipientIndex());
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
    void listNotificationRecipientnotificationsDtoById() {
        //Given
        List<NotificationEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationEntity ae = newNotification();
            ae.setRecipientIndex(ae.getRecipientIndex() + "_" + i);
            notificationsEntities.add(ae);
        }


        try {
            notificationsEntities.forEach(m -> {
                try {
                    testDao.delete(m.getPk(), m.getRecipientIndex());
                } catch (ExecutionException e) {
                    System.out.println("Nothing to remove");
                } catch (InterruptedException e) {
                    System.out.println("Nothing to remove");
                    Thread.currentThread().interrupt();
                }

            });
            notificationDao.updateNotifications(notificationsEntities).block(Duration.ofMillis(3000));
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        List<NotificationEntity> results = notificationDao.listNotificationRecipientAddressesDtoById(notificationsEntities.get(0).getInternalId()).collectList().block(Duration.ofMillis(3000));

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
                        testDao.delete(m.getPk(), m.getRecipientIndex());
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

    public static NotificationEntity newNotification(){
        NotificationEntity ne = new NotificationEntity("425e4567-e89b-12d3-a456-426655449631", "mario rossi");
        ne.setDigitalAddress("mario.rossi@test.it");
        PhysicalAddress pa = new PhysicalAddress();
        pa.setAddress("via casa sua");
        pa.setAt("via");
        pa.setAddressDetails("interno 2");
        pa.setMunicipality("Venezia");
        pa.setCap("30000");
        pa.setProvince("VE");
        pa.setState("Italia");
        ne.setPhysicalAddress(pa);
        return ne;
    }
}