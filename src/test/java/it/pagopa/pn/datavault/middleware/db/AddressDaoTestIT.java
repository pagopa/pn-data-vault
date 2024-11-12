package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.LocalStackTestConfig;
import it.pagopa.pn.datavault.TestUtils;
import it.pagopa.pn.datavault.middleware.db.entities.AddressEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Import(LocalStackTestConfig.class)
class AddressDaoTestIT {


    @Autowired
    private AddressDao addressDao;

    @Autowired
    DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    TestDao<AddressEntity> testDao;

    @BeforeEach
    void setup( @Value("${pn.data-vault.dynamodb_table-name}") String table) {
        testDao = new TestDao<>( dynamoDbEnhancedAsyncClient, table, AddressEntity.class);
    }

    @Test
    void listAddressesById() {
        //Given
        List<AddressEntity> addressesEntities = new ArrayList<>();
        int N = 4;
        for(int i = 0;i<N;i++)
        {
            AddressEntity ae = TestUtils.newAddress();
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
        AddressEntity addresToInsert = TestUtils.newAddress();

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
            Assertions.assertNull( elementFromDb.getExpiration() );
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
    void updateAddressWithTtl() {
        //Given
        AddressEntity addressToInsert = TestUtils.newAddress();
        addressToInsert.setExpiration(BigDecimal.TEN);

        try {
            testDao.delete(addressToInsert.getPk(), addressToInsert.getAddressId());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }

        //When
        addressDao.updateAddress(addressToInsert).block(Duration.ofMillis(3000));

        //Then
        try {
            AddressEntity elementFromDb = testDao.get(addressToInsert.getPk(), addressToInsert.getAddressId());

            Assertions.assertNotNull( elementFromDb);
            Assertions.assertEquals( addressToInsert, elementFromDb);
            Assertions.assertNotNull( elementFromDb.getExpiration() );
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(addressToInsert.getPk(), addressToInsert.getAddressId());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
            fail("Red phase");
        }
    }

    // Caso in cui passo un'expiration, ma l'address per il recipient esiste già con ttl nullo -> il record non viene aggiornato.
    @Test
    void updateAddressWithTtl_AlreadyExisting() {
        //Given
        AddressEntity addressToInsert = TestUtils.newAddress();

        try {
            testDao.delete(addressToInsert.getPk(), addressToInsert.getAddressId());
        } catch (Exception e) {
            System.out.println("Nothing to remove");
        }
        addressDao.updateAddress(addressToInsert).block();

        //When
        AddressEntity addressToUpdate = TestUtils.newAddress();
        String newValue = "newtest@test.it";
        addressToUpdate.setValue(newValue);
        addressToUpdate.setExpiration(BigDecimal.TEN);
        addressDao.updateAddress(addressToUpdate).block(Duration.ofMillis(3000));

        //Then
        try {
            AddressEntity elementFromDb = testDao.get(addressToInsert.getPk(), addressToInsert.getAddressId());

            Assertions.assertNotNull( elementFromDb);
            Assertions.assertEquals( addressToInsert, elementFromDb);
            Assertions.assertNull( elementFromDb.getExpiration() );
        } catch (Exception e) {
            fail(e);
        } finally {
            try {
                testDao.delete(addressToInsert.getPk(), addressToInsert.getAddressId());
            } catch (Exception e) {
                System.out.println("Nothing to remove");
            }
        }
    }


    @Test
    void deleteAddressId() {
        //Given
        AddressEntity addresToInsert = TestUtils.newAddress();

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
}