package it.pagopa.pn.datavault.middleware.db;

import it.pagopa.pn.datavault.middleware.db.entities.AddressEntity;
import it.pagopa.pn.datavault.middleware.db.entities.MandateEntity;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationEntity;
import it.pagopa.pn.datavault.middleware.db.entities.NotificationTimelineEntity;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;

import java.util.concurrent.ExecutionException;


@SpringBootTest
class TestDao extends BaseDao {

        DynamoDbAsyncTable<MandateEntity> mandateTable;
        DynamoDbAsyncTable<NotificationEntity> notificationTable;
        DynamoDbAsyncTable<NotificationTimelineEntity> timelineTable;
        DynamoDbAsyncTable<AddressEntity> addressTable;

        public TestDao(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient, String table)
        {
            this.mandateTable = dynamoDbEnhancedAsyncClient.table(table, TableSchema.fromBean(MandateEntity.class));
            this.notificationTable = dynamoDbEnhancedAsyncClient.table(table, TableSchema.fromBean(NotificationEntity.class));
            this.timelineTable = dynamoDbEnhancedAsyncClient.table(table, TableSchema.fromBean(NotificationTimelineEntity.class));
            this.addressTable = dynamoDbEnhancedAsyncClient.table(table, TableSchema.fromBean(AddressEntity.class));
        }

        public MandateEntity get(String pk, String sk) throws ExecutionException, InterruptedException {

            GetItemEnhancedRequest req = GetItemEnhancedRequest.builder()
                    .key(getKeyBuild(pk, sk))
                    .build();

            return mandateTable.getItem(req).get();
        }

        public void delete(String pk, String sk) throws ExecutionException, InterruptedException {

            DeleteItemEnhancedRequest req = DeleteItemEnhancedRequest.builder()
                    .key(getKeyBuild(pk, sk))
                    .build();

            mandateTable.deleteItem(req).get();
        }

        public NotificationEntity getNotification(String pk, String sk) throws ExecutionException, InterruptedException {

            GetItemEnhancedRequest req = GetItemEnhancedRequest.builder()
                    .key(getKeyBuild(pk, sk))
                    .build();

            return notificationTable.getItem(req).get();
        }

        public void deleteNotification(String pk, String sk) throws ExecutionException, InterruptedException {

            DeleteItemEnhancedRequest req = DeleteItemEnhancedRequest.builder()
                    .key(getKeyBuild(pk, sk))
                    .build();

            notificationTable.deleteItem(req).get();
        }

        public NotificationTimelineEntity getTimeline(String pk, String sk) throws ExecutionException, InterruptedException {

            GetItemEnhancedRequest req = GetItemEnhancedRequest.builder()
                    .key(getKeyBuild(pk, sk))
                    .build();

            return timelineTable.getItem(req).get();
        }

        public void deleteTimeline(String pk, String sk) throws ExecutionException, InterruptedException {

            DeleteItemEnhancedRequest req = DeleteItemEnhancedRequest.builder()
                    .key(getKeyBuild(pk, sk))
                    .build();

            timelineTable.deleteItem(req).get();
        }

    public AddressEntity getAddress(String pk, String sk) throws ExecutionException, InterruptedException {

        GetItemEnhancedRequest req = GetItemEnhancedRequest.builder()
                .key(getKeyBuild(pk, sk))
                .build();

        return addressTable.getItem(req).get();
    }

    public void deleteAddress(String pk, String sk) throws ExecutionException, InterruptedException {

        DeleteItemEnhancedRequest req = DeleteItemEnhancedRequest.builder()
                .key(getKeyBuild(pk, sk))
                .build();

        addressTable.deleteItem(req).get();
    }

}