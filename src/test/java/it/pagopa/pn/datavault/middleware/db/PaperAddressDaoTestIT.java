package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.LocalStackTestConfig;
import it.pagopa.pn.datavault.middleware.db.entities.PaperAddressEntity;
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

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class PaperAddressDaoTestIT {

    @Autowired
    private PaperAddressDao paperAddressDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    TestDao<PaperAddressEntity> testDao;

    @BeforeEach
    void setup(@Value("${pn.data-vault.dynamodb_table-name}") String table) {
        testDao = new TestDao<>(dynamoDbEnhancedAsyncClient, table, PaperAddressEntity.class);
    }

    @Test
    void getPaperAddressesByPaperRequestId() {
        // Arrange
        String paperRequestId = "paperReq_" + System.currentTimeMillis();
        List<PaperAddressEntity> paperAddressEntities = new ArrayList<>();
        int N = 3;

        for(int i = 0; i < N; i++) {
            PaperAddressEntity entity = new PaperAddressEntity(paperRequestId, "addr_" + i);
            paperAddressEntities.add(entity);
        }

        try {
            // Cleanup before test
            paperAddressEntities.forEach(entity -> {
                try {
                    testDao.delete(entity.getPaperRequestId(), entity.getAddressId());
                } catch (ExecutionException e) {
                    System.out.println("Nothing to remove");
                } catch (InterruptedException e) {
                    System.out.println("Nothing to remove");
                    Thread.currentThread().interrupt();
                }
            });

            // Insert test data
            paperAddressEntities.forEach(entity -> {
                paperAddressDao.updatePaperAddress(entity.getPaperRequestId(), entity.getAddressId(), entity)
                        .block(Duration.ofMillis(3000));
            });
        } catch (Exception e) {
            System.out.println("Setup error: " + e.getMessage());
        }

        // Act
        List<PaperAddressEntity> results = paperAddressDao.getPaperAddressesByPaperRequestId(paperRequestId)
                .collectList().block(Duration.ofMillis(3000));

        // Assert
        try {
            System.out.println(results);
            Assertions.assertNotNull(results);
            Assertions.assertEquals(N, results.size());
            for(int i = 0; i < N; i++) {
                Assertions.assertTrue(results.contains(paperAddressEntities.get(i)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // Cleanup after test
            try {
                paperAddressEntities.forEach(entity -> {
                    try {
                        testDao.delete(entity.getPaperRequestId(), entity.getAddressId());
                    } catch (ExecutionException e) {
                        System.out.println("Nothing to remove");
                    } catch (InterruptedException e) {
                        System.out.println("Nothing to remove");
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (Exception e) {
                System.out.println("Cleanup error: " + e.getMessage());
            }
        }
    }

    @Test
    void getPaperAddressByIds() {
        // Arrange
        String paperRequestId = "paperReq_" + System.currentTimeMillis();
        String addressId = "addr_" + System.currentTimeMillis();
        PaperAddressEntity paperAddressToInsert = new PaperAddressEntity(paperRequestId, addressId);

        try {
            testDao.delete(paperRequestId, addressId);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        // Insert test data
        paperAddressDao.updatePaperAddress(paperRequestId, addressId, paperAddressToInsert)
                .block(Duration.ofMillis(3000));

        // Act
        PaperAddressEntity result = paperAddressDao.getPaperAddressByIds(paperRequestId, addressId)
                .block(Duration.ofMillis(3000));

        // Assert
        try {
            Assertions.assertNotNull(result);
            Assertions.assertEquals(paperAddressToInsert.getPaperRequestId(), result.getPaperRequestId());
            Assertions.assertEquals(paperAddressToInsert.getAddressId(), result.getAddressId());
            Assertions.assertEquals(paperAddressToInsert, result);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(paperRequestId, addressId);
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void getPaperAddressByIds_NotFound() {
        // Arrange
        String paperRequestId = "nonExistent_" + System.currentTimeMillis();
        String addressId = "nonExistent_" + System.currentTimeMillis();

        // Act
        PaperAddressEntity result = paperAddressDao.getPaperAddressByIds(paperRequestId, addressId)
                .block(Duration.ofMillis(3000));

        // Assert
        Assertions.assertNull(result);
    }

    @Test
    void updatePaperAddress() {
        // Arrange
        String paperRequestId = "paperReq_" + System.currentTimeMillis();
        String addressId = "addr_" + System.currentTimeMillis();
        PaperAddressEntity paperAddressToInsert = new PaperAddressEntity(paperRequestId, addressId);

        try {
            testDao.delete(paperRequestId, addressId);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        // Act
        PaperAddressEntity result = paperAddressDao.updatePaperAddress(paperRequestId, addressId, paperAddressToInsert)
                .block(Duration.ofMillis(3000));

        // Assert
        try {
            Assertions.assertNotNull(result);
            Assertions.assertEquals(paperRequestId, result.getPaperRequestId());
            Assertions.assertEquals(addressId, result.getAddressId());

            // Verify data is actually in DB
            PaperAddressEntity elementFromDb = testDao.get(PaperAddressEntity.buildPk(paperRequestId), addressId);
            Assertions.assertNotNull(elementFromDb);
            Assertions.assertEquals(paperRequestId, elementFromDb.getPaperRequestId());
            Assertions.assertEquals(addressId, elementFromDb.getAddressId());
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(paperRequestId, addressId);
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void updatePaperAddress_UpdateExisting() {
        //Given
        String paperRequestId = "paperReq_" + System.currentTimeMillis();
        String addressId = "addr_" + System.currentTimeMillis();
        PaperAddressEntity originalEntity = new PaperAddressEntity();

        try {
            testDao.delete(paperRequestId, addressId);
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        // Insert original
        paperAddressDao.updatePaperAddress(paperRequestId, addressId, originalEntity)
                .block(Duration.ofMillis(3000));

        // Prepare updated entity
        PaperAddressEntity updatedEntity = new PaperAddressEntity();
        String newValue = "updated_value_" + System.currentTimeMillis();
        updatedEntity.setAddress(newValue);

        //When
        PaperAddressEntity result = paperAddressDao.updatePaperAddress(paperRequestId, addressId, updatedEntity)
                .block(Duration.ofMillis(3000));

        //Then
        try {
            Assertions.assertNotNull(result);
            Assertions.assertEquals(paperRequestId, result.getPaperRequestId());
            Assertions.assertEquals(addressId, result.getAddressId());

            // Verify updated data is in DB
            PaperAddressEntity elementFromDb = testDao.get(PaperAddressEntity.buildPk(paperRequestId), addressId);
            Assertions.assertNotNull(elementFromDb);
            Assertions.assertEquals(updatedEntity, elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(paperRequestId, addressId);
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void deletePaperAddress() {
        //Given
        String paperRequestId = "paperReq_" + System.currentTimeMillis();
        String addressId = "addr_" + System.currentTimeMillis();
        PaperAddressEntity paperAddressToInsert = new PaperAddressEntity();

        try {
            testDao.delete(paperRequestId, addressId);
            paperAddressDao.updatePaperAddress(paperRequestId, addressId, paperAddressToInsert)
                    .block(Duration.ofMillis(3000));
        } catch (Exception e) {
            System.out.println("Setup error: " + e.getMessage());
        }

        //When
        PaperAddressEntity deletedEntity = paperAddressDao.deletePaperAddress(paperRequestId, addressId)
                .block(Duration.ofMillis(3000));

        //Then
        try {
            Assertions.assertNotNull(deletedEntity);

            // Verify entity is deleted from DB
            PaperAddressEntity elementFromDb = testDao.get(paperRequestId, addressId);
            Assertions.assertNull(elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(paperRequestId, addressId);
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void deletePaperAddress_NotExisting() {
        //Given
        String paperRequestId = "nonExistent_" + System.currentTimeMillis();
        String addressId = "nonExistent_" + System.currentTimeMillis();

        //When
        PaperAddressEntity deletedEntity = paperAddressDao.deletePaperAddress(paperRequestId, addressId)
                .block(Duration.ofMillis(3000));

        //Then
        // DynamoDB deleteItem returns null if item doesn't exist
        Assertions.assertNull(deletedEntity);
    }

    @Test
    void getPaperAddressesByPaperRequestId_EmptyResult() {
        //Given
        String nonExistentPaperRequestId = "nonExistent_" + System.currentTimeMillis();

        //When
        List<PaperAddressEntity> results = paperAddressDao.getPaperAddressesByPaperRequestId(nonExistentPaperRequestId)
                .collectList().block(Duration.ofMillis(3000));

        //Then
        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }
}