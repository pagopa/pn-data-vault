package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.LocalStackTestConfig;
import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationEntity;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class NotificationDaoTestIT {

    private final Duration d = Duration.ofMillis(3000);
    
    @Autowired
    private NotificationDao notificationDao;

    @Autowired
    private NotificationTimelineDao notificationTimelineDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    TestDao<NotificationEntity> testDao;
    TestDao<NotificationTimelineEntity> testNotificationDao;

    @BeforeEach
    void setup( @Value("${pn.data-vault.dynamodb_table-name}") String table) {
        testDao = new TestDao<>(dynamoDbEnhancedAsyncClient, table, NotificationEntity.class);
        testNotificationDao = new TestDao<>(dynamoDbEnhancedAsyncClient, table, NotificationTimelineEntity.class);
    }

    @Test
    void updateNotificationsNormalized() {
        //Given
        NotificationEntity addressToInsert = TestUtils.newNotification(true);
        List<NotificationEntity> l = new ArrayList<>();
        l.add(addressToInsert);

        try {
            testDao.delete(addressToInsert.getPk(), addressToInsert.getRecipientIndex());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        notificationDao.updateNotifications(l).block(d);

        //Then
        try {
            NotificationEntity elementFromDb = testDao.get(addressToInsert.getPk(), addressToInsert.getRecipientIndex());

            Assertions.assertNotNull( elementFromDb);
            Assertions.assertEquals(addressToInsert, elementFromDb);
            Assertions.assertEquals(Boolean.TRUE, elementFromDb.getNormalizedAddress());
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(addressToInsert.getPk(), addressToInsert.getRecipientIndex());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void updateNotificationsNotNormalized() {
        //Given
        NotificationEntity addressToInsert = TestUtils.newNotification(false);
        List<NotificationEntity> l = new ArrayList<>();
        l.add(addressToInsert);

        try {
            testDao.delete(addressToInsert.getPk(), addressToInsert.getRecipientIndex());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        notificationDao.updateNotifications(l).block(d);

        //Then
        try {
            NotificationEntity elementFromDb = testDao.get(addressToInsert.getPk(), addressToInsert.getRecipientIndex());

            Assertions.assertNotNull( elementFromDb);
            Assertions.assertEquals(addressToInsert, elementFromDb);
            Assertions.assertEquals(Boolean.FALSE, elementFromDb.getNormalizedAddress());
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(addressToInsert.getPk(), addressToInsert.getRecipientIndex());
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
            NotificationEntity ae = TestUtils.newNotification();
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
        notificationDao.updateNotifications(notificationsEntities).block(d);

        //Then
        try {
            notificationsEntities.forEach(a -> {
                NotificationEntity elementFromDb = null;
                try {
                    elementFromDb = testDao.get(a.getPk(), a.getRecipientIndex());
                } catch (Exception e) {
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
        NotificationEntity addresToInsert = TestUtils.newNotification();
        List<NotificationEntity> l = new ArrayList<>();
        l.add(addresToInsert);

        try {
            testDao.delete(addresToInsert.getPk(), addresToInsert.getRecipientIndex());
            notificationDao.updateNotifications(l).block(d);
            NotificationEntity elementFromDb = testDao.get(addresToInsert.getPk(), addresToInsert.getRecipientIndex());
            Assertions.assertNotNull( elementFromDb);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        String internalId = addresToInsert.getInternalId();
        notificationDao.deleteNotificationByIun(internalId).block(d);

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
    void deleteNotificationByIunMultipleNotNormalizedAndOneNormalized() {
        //Given
        List<NotificationEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationEntity ae = TestUtils.newNotification(false);
            ae.setRecipientIndex(ae.getRecipientIndex() + "_" + i);
            notificationsEntities.add(ae);
        }

        // aggiunto per lo stesso IUN un oggetto con indirizzo fisico normalizzato
        NotificationEntity entityWithNormalizedAddress = new NotificationEntity(notificationsEntities.get(0).getInternalId(), "000_4", true);
        entityWithNormalizedAddress.setDigitalAddress(notificationsEntities.get(0).getDigitalAddress());
        entityWithNormalizedAddress.setPhysicalAddress(notificationsEntities.get(0).getPhysicalAddress());
        notificationsEntities.add(entityWithNormalizedAddress);


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
            notificationDao.updateNotifications(notificationsEntities).block(d);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        notificationDao.deleteNotificationByIun(notificationsEntities.get(0).getInternalId()).block(d);

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
    void deleteNotificationByIunMultipleNormalizedAndOneNotNormalized() {
        //Given
        List<NotificationEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationEntity ae = TestUtils.newNotification(true);
            ae.setRecipientIndex(ae.getRecipientIndex() + "_" + i);
            notificationsEntities.add(ae);
        }

        // aggiunto per lo stesso IUN un oggetto con indirizzo fisico NON normalizzato
        NotificationEntity entityWithNotNormalizedAddress = new NotificationEntity(notificationsEntities.get(0).getInternalId(), "000_4", false);
        entityWithNotNormalizedAddress.setDigitalAddress(notificationsEntities.get(0).getDigitalAddress());
        entityWithNotNormalizedAddress.setPhysicalAddress(notificationsEntities.get(0).getPhysicalAddress());
        notificationsEntities.add(entityWithNotNormalizedAddress);


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
            notificationDao.updateNotifications(notificationsEntities).block(d);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        notificationDao.deleteNotificationByIun(notificationsEntities.get(0).getInternalId()).block(d);

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
    void deleteNotificationByIunMultipleWithTimeline() {
        //Given
        List<NotificationEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationEntity ae = TestUtils.newNotification();
            ae.setRecipientIndex(ae.getRecipientIndex() + "_" + i);
            notificationsEntities.add(ae);
        }
        List<NotificationTimelineEntity> notificationsTimelineEntities = new ArrayList<>();
        for(int i = 0;i<N;i++)
        {
            NotificationTimelineEntity ae = TestUtils.newNotificationTimeline();
            ae.setTimelineElementId(ae.getTimelineElementId() + "_" + i);
            notificationsTimelineEntities.add(ae);
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
            notificationDao.updateNotifications(notificationsEntities).block(d);
            notificationsTimelineEntities.forEach(m -> {
                try {
                    testDao.delete(m.getPk(), m.getTimelineElementId());
                    notificationTimelineDao.updateNotification(m).block(d);
                } catch (ExecutionException e) {
                    System.out.println("Nothing to remove");
                } catch (InterruptedException e) {
                    System.out.println("Nothing to remove");
                    Thread.currentThread().interrupt();
                }
            });

            // controllo che siano stati creati
            NotificationEntity elementFromDb = testDao.get(notificationsEntities.get(0).getPk(), notificationsEntities.get(0).getRecipientIndex());
            Assertions.assertNotNull( elementFromDb);

            NotificationTimelineEntity elementFromDb1 = testNotificationDao.get(notificationsTimelineEntities.get(0).getPk(), notificationsTimelineEntities.get(0).getTimelineElementId());
            Assertions.assertNotNull( elementFromDb1);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        notificationDao.deleteNotificationByIun(notificationsEntities.get(0).getInternalId()).block(d);

        //Then
        try {
            NotificationEntity elementFromDb = testDao.get(notificationsEntities.get(0).getPk(), notificationsEntities.get(0).getRecipientIndex());

            Assertions.assertNull( elementFromDb);

            NotificationTimelineEntity elementFromDb1 = testNotificationDao.get(notificationsTimelineEntities.get(0).getPk(), notificationsTimelineEntities.get(0).getTimelineElementId());

            Assertions.assertNull( elementFromDb1);
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
                notificationsTimelineEntities.forEach(m -> {
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
    void listNotificationRecipientnotificationsDtoByIdWithNormalizedNull() {
        //Given
        List<NotificationEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationEntity ae = TestUtils.newNotification(null);
            assertThat(ae.getPrefixPk()).isEqualTo(NotificationEntity.ADDRESS_PREFIX);
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
            notificationDao.updateNotifications(notificationsEntities).block(d);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        List<NotificationEntity> results = notificationDao.listNotificationRecipientAddressesDtoById(notificationsEntities.get(0).getInternalId(), null).collectList().block(d);

        //Then
        try {
            Assertions.assertNotNull(results);
            Assertions.assertEquals(N, results.size());
            for(int i = 0;i<N;i++)
            {
                assertThat(results).contains(notificationsEntities.get(i));
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

    @Test
    void listNotificationRecipientnotificationsDtoByIdWithNormalizedFalse() {
        //Given
        List<NotificationEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationEntity ae = TestUtils.newNotification(false);
            assertThat(ae.getPrefixPk()).isEqualTo(NotificationEntity.ADDRESS_PREFIX);
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
            notificationDao.updateNotifications(notificationsEntities).block(d);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        List<NotificationEntity> results = notificationDao.listNotificationRecipientAddressesDtoById(notificationsEntities.get(0).getInternalId(), false).collectList().block(d);

        //Then
        try {
            Assertions.assertNotNull(results);
            Assertions.assertEquals(N, results.size());
            for(int i = 0;i<N;i++)
            {
                assertThat(results).contains(notificationsEntities.get(i));
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

    @Test
    void listNotificationRecipientnotificationsDtoByIdWithNormalizedTrue() {
        //Given
        List<NotificationEntity> notificationsEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            NotificationEntity ae = TestUtils.newNotification(true);
            assertThat(ae.getPrefixPk()).isEqualTo(NotificationEntity.NORMALIZED_ADDRESS_PREFIX);
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
            notificationDao.updateNotifications(notificationsEntities).block(d);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        List<NotificationEntity> results = notificationDao.listNotificationRecipientAddressesDtoById(notificationsEntities.get(0).getInternalId(), true).collectList().block(d);

        //Then
        try {
            Assertions.assertNotNull(results);
            Assertions.assertEquals(N, results.size());
            for(int i = 0;i<N;i++)
            {
                assertThat(results).contains(notificationsEntities.get(i));
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
}