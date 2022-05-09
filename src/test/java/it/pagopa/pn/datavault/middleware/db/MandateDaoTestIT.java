package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.middleware.db.entities.MandateEntity;
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
public class MandateDaoTestIT {

    @Autowired
    private MandateDao mandateDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    TestDao<MandateEntity> testDao;

    @BeforeEach
    void setup( @Value("${pn.data-vault.dynamodb_table-name}") String table) {
        testDao = new TestDao<MandateEntity>( dynamoDbEnhancedAsyncClient, table, MandateEntity.class);
    }

    @Test
    void listMandatesByIds() {
        //Given
        List<String> ids = new ArrayList<>();
        List<MandateEntity> mandateEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            MandateEntity mandateToInsert = newMandate(false);
            mandateToInsert.setPk(mandateToInsert.getPk() + "_"+i);
            ids.add(mandateToInsert.getMandateId());
            mandateEntities.add(mandateToInsert);
        }



        try {
            mandateEntities.forEach(m -> {
                try {
                    testDao.delete(m.getPk(), m.getSk());
                } catch (ExecutionException e) {
                    System.out.println("Nothing to remove");
                } catch (InterruptedException e) {
                    System.out.println("Nothing to remove");
                    Thread.currentThread().interrupt();
                }
                mandateDao.updateMandate(m).block(Duration.ofMillis(3000));
            });
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        List<MandateEntity> results = mandateDao.listMandatesByIds(ids).collectList().block(Duration.ofMillis(3000));

        //Then
        try {
            Assertions.assertNotNull(results);
            Assertions.assertEquals(N, results.size());
            for(int i = 0;i<N;i++)
            {
                Assertions.assertTrue(results.contains(mandateEntities.get(i)));
            }
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                mandateEntities.forEach(m -> {
                    try {
                        testDao.delete(m.getPk(), m.getSk());
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
    void updateMandatePF() {
        //Given
        MandateEntity mandateToInsert = newMandate(true);

        try {
            testDao.delete(mandateToInsert.getPk(), mandateToInsert.getSk());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        mandateDao.updateMandate(mandateToInsert).block(Duration.ofMillis(3000));

        //Then
        try {
            MandateEntity elementFromDb = testDao.get(mandateToInsert.getPk(), mandateToInsert.getSk());

            Assertions.assertNotNull( elementFromDb);
            Assertions.assertEquals( mandateToInsert, elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(mandateToInsert.getPk(), mandateToInsert.getSk());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }

    @Test
    void updateMandatePG() {
        //Give
        MandateEntity mandateToInsert = newMandate(false);

        try {
            testDao.delete(mandateToInsert.getPk(), mandateToInsert.getSk());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        mandateDao.updateMandate(mandateToInsert).block(Duration.ofMillis(3000));

        //Then
        try {
            MandateEntity elementFromDb = testDao.get(mandateToInsert.getPk(), mandateToInsert.getSk());

            Assertions.assertNotNull( elementFromDb);
            Assertions.assertEquals( mandateToInsert, elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(mandateToInsert.getPk(), mandateToInsert.getSk());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }


    @Test
    void deleteMandateId() {
        //Given
        MandateEntity mandateToInsert = newMandate(true);

        try {
            testDao.delete(mandateToInsert.getPk(), mandateToInsert.getSk());
            mandateDao.updateMandate(mandateToInsert).block(Duration.ofMillis(3000));
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        mandateDao.deleteMandateId(mandateToInsert.getMandateId()).block(Duration.ofMillis(3000));

        //Then
        try {
            MandateEntity elementFromDb = testDao.get(mandateToInsert.getPk(), mandateToInsert.getSk());

            Assertions.assertNull( elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(mandateToInsert.getPk(), mandateToInsert.getSk());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }


    public static MandateEntity newMandate(boolean pf) {
        MandateEntity me = new MandateEntity("425e4567-e89b-12d3-a456-426655449631");
        if (pf)
        {
            me.setName("mario");
            me.setSurname("rossi");
        }
        else
            me.setBusinessName("ragione sociale");

        return me;
    }

}