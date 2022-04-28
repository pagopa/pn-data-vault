package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.exceptions.InvalidInputException;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "aws.region-code=us-east-1",
        "aws.profile-name=${PN_AWS_PROFILE_NAME:default}",
        "aws.endpoint-url=http://localhost:4566"
})
@SpringBootTest
class MandateDaoTestIT {

    @Autowired
    private MandateDao mandateDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    TestDao testDao;

    @BeforeEach
    void setup( @Value("${pn.data-vault.dynamodb_table-name}") String table) {
        testDao = new TestDao( dynamoDbEnhancedAsyncClient, table);
    }

    @Test
    void listMandatesByIds() {
        //Given
        MandateEntity mandateToInsert = newMandate(false);
        MandateEntity mandateToInsert1 = newMandate(false);
        mandateToInsert1.setPk(mandateToInsert1.getPk() + "_1");
        MandateEntity mandateToInsert2 = newMandate(false);
        mandateToInsert2.setPk(mandateToInsert2.getPk() + "_2");
        mandateToInsert2.setMandateId(mandateToInsert2.getMandateId() + "_2");
        MandateEntity mandateToInsert3 = newMandate(false);
        mandateToInsert3.setPk(mandateToInsert3.getPk() + "_3");
        mandateToInsert3.setMandateId(mandateToInsert3.getMandateId() + "_3");
        List<String> ids = new ArrayList<>();
        ids.add(mandateToInsert.getMandateId());
        ids.add(mandateToInsert1.getMandateId());
        ids.add(mandateToInsert2.getMandateId());
        ids.add(mandateToInsert3.getMandateId());


        try {
            testDao.delete(mandateToInsert.getPk(), mandateToInsert.getSk());
            mandateDao.updateMandate(mandateToInsert).block(Duration.ofMillis(3000));
            testDao.delete(mandateToInsert1.getPk(), mandateToInsert1.getSk());
            mandateDao.updateMandate(mandateToInsert1).block(Duration.ofMillis(3000));
            testDao.delete(mandateToInsert2.getPk(), mandateToInsert2.getSk());
            mandateDao.updateMandate(mandateToInsert2).block(Duration.ofMillis(3000));
            testDao.delete(mandateToInsert3.getPk(), mandateToInsert3.getSk());
            mandateDao.updateMandate(mandateToInsert3).block(Duration.ofMillis(3000));
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        List<MandateEntity> results = mandateDao.listMandatesByIds(ids).collectList().block(Duration.ofMillis(3000));

        //Then
        try {
            Assertions.assertNotNull(results);
            Assertions.assertEquals(4, results.size());
            Assertions.assertTrue(results.contains(mandateToInsert));
            Assertions.assertTrue(results.contains(mandateToInsert1));
            Assertions.assertTrue(results.contains(mandateToInsert2));
            Assertions.assertTrue(results.contains(mandateToInsert3));
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                testDao.delete(mandateToInsert.getPk(), mandateToInsert.getSk());
                testDao.delete(mandateToInsert1.getPk(), mandateToInsert1.getSk());
                testDao.delete(mandateToInsert2.getPk(), mandateToInsert2.getSk());
                testDao.delete(mandateToInsert3.getPk(), mandateToInsert3.getSk());
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


    private MandateEntity newMandate(boolean pf) {
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
        mandateDao.deleteMandateId(mandateToInsert.getMandateId());

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
}