package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.middleware.db.entities.AddressEntity;
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
public
class AddressDaoTestIT {


    @Autowired
    private AddressDao addressDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    TestDao<AddressEntity> testDao;

    @BeforeEach
    void setup( @Value("${pn.data-vault.dynamodb_table-name}") String table) {
        testDao = new TestDao<AddressEntity>( dynamoDbEnhancedAsyncClient, table, AddressEntity.class);
    }

    @Test
    void listAddressesById() {
        //Given
        List<AddressEntity> addressesEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            AddressEntity ae = newAddress();
            ae.setAddressId(ae.getAddressId() + "_" + i);
            addressesEntities.add(ae);
        }



        try {
            addressesEntities.forEach(m -> {
                try {
                    testDao.delete(m.getPk(), m.getAddressId());
                } catch (ExecutionException e) {
                    System.out.println("Nothing to remove");
                } catch (InterruptedException e) {
                    System.out.println("Nothing to remove");
                    Thread.currentThread().interrupt();
                }
                addressDao.updateAddress(m).block(Duration.ofMillis(3000));
            });
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        // basta cercare per un internalid qualsiasi
        List<AddressEntity> results = addressDao.listAddressesById(addressesEntities.get(0).getInternalId()).collectList().block(Duration.ofMillis(3000));

        //Then
        try {
            Assertions.assertNotNull(results);
            Assertions.assertEquals(N, results.size());
            for(int i = 0;i<N;i++)
            {
                Assertions.assertTrue(results.contains(addressesEntities.get(i)));
            }
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                addressesEntities.forEach(m -> {
                    try {
                        testDao.delete(m.getPk(), m.getAddressId());
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
    void updateAddress() {
        //Given
        AddressEntity addresToInsert = newAddress();

        try {
            testDao.delete(addresToInsert.getPk(), addresToInsert.getAddressId());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        addressDao.updateAddress(addresToInsert).block(Duration.ofMillis(3000));

        //Then
        try {
            AddressEntity elementFromDb = testDao.get(addresToInsert.getPk(), addresToInsert.getAddressId());

            Assertions.assertNotNull( elementFromDb);
            Assertions.assertEquals( addresToInsert, elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(addresToInsert.getPk(), addresToInsert.getAddressId());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }


    @Test
    void deleteAddressId() {
        //Given
        AddressEntity addresToInsert = newAddress();

        try {
            testDao.delete(addresToInsert.getPk(), addresToInsert.getAddressId());
            addressDao.updateAddress(addresToInsert).block(Duration.ofMillis(3000));
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        addressDao.deleteAddressId(addresToInsert.getInternalId(), addresToInsert.getAddressId()).block(Duration.ofMillis(3000));

        //Then
        try {
            AddressEntity elementFromDb = testDao.get(addresToInsert.getPk(), addresToInsert.getAddressId());

            Assertions.assertNull( elementFromDb);
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(addresToInsert.getPk(), addresToInsert.getAddressId());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }


    public static AddressEntity newAddress() {
        AddressEntity ae = new AddressEntity("425e4567-e89b-12d3-a456-426655449631", "DD_c_f205_1");
        ae.setValue("test@test.it");
        return  ae;
    }
}