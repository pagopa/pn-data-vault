package it.pagopa.pn.datavault.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static  it.pagopa.pn.datavault.dao.DynamoDbUtils.*;

@Component
public class ExternalToInternalIdMappingDao {

    public static final AttributeValue FIXED_SORT_KEY = AttributeValue.builder().s("N/A").build();
    private final DynamoDbAsyncClient dynamo;
    private final TableDefinition tableDef;

    public ExternalToInternalIdMappingDao(DynamoDbAsyncClient dynamo, TableDefinition tableDef) {
        this.dynamo = dynamo;
        this.tableDef = tableDef;
    }

    public CompletableFuture<Optional<String>> getObjectMapping(String externalId ) {
        Map<String, AttributeValue> key = new HashMap<>();

        AttributeValue hashKeyAttribute = buildKeyAttribute( externalId, null , false );
        key.put( tableDef.getHashKeyAttributeName(), hashKeyAttribute );
        key.put( tableDef.getSortKeyAttributeName(), FIXED_SORT_KEY );

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName( tableDef.getTableName() )
                .key( key )
                .build();

        return dynamo.getItem( getRequest )
                .thenApply( getResponse -> {
                    Optional<String> result;
                    if( getResponse.hasItem() ) {

                        Map<String, AttributeValue> item = getResponse.item();
                        String internalId = nullSafeGetAttributeValue(item, tableDef.getValueAttributeName() );
                        result = Optional.of( internalId );
                    }
                    else {
                        result = Optional.empty();
                    }
                    return result;
                });
    }

    public CompletableFuture<String> createObjectMapping( String externalId ) {
        UUID uuid = UUID.randomUUID();
        String internalId = uuid.toString();

        Map<String, AttributeValue> int2extItem = prepareItem( externalId, internalId, true);
        Map<String, AttributeValue> ext2intItem = prepareItem( externalId, internalId, false);


        Put intToExtPut = Put.builder()
                .tableName( tableDef.getTableName() )
                .item( int2extItem )
                .conditionExpression("attribute_not_exists(" + tableDef.getHashKeyAttributeName() + ")")
                .build();

        Put extToIntPut = Put.builder()
                .tableName( tableDef.getTableName() )
                .item( ext2intItem )
                .conditionExpression("attribute_not_exists(" + tableDef.getHashKeyAttributeName() + ")")
                .build();

        TransactWriteItemsRequest writeRequest = TransactWriteItemsRequest.builder()
                .transactItems(
                    TransactWriteItem.builder().put( intToExtPut ).build(),
                        TransactWriteItem.builder().put( extToIntPut ).build()
                ).build();

        return dynamo.transactWriteItems( writeRequest )
                .thenApply( writeResponse -> internalId );
    }

    private Map<String, AttributeValue> prepareItem(String externalId, String internalId, boolean internal2external) {
        AttributeValue keyAttribute = buildKeyAttribute(externalId, internalId, internal2external);
        String value = internal2external ? externalId : internalId;

        Map<String, AttributeValue> item = new HashMap<>();
        item.put(
                tableDef.getHashKeyAttributeName(),
                keyAttribute
            );
        item.put(
                tableDef.getSortKeyAttributeName(),
                FIXED_SORT_KEY
            );
        item.put(
                tableDef.getValueAttributeName(),
                AttributeValue.builder().s( value ).build()
            );
        return item;
    }

    @NotNull
    private AttributeValue buildKeyAttribute(String externalId, String internalId, boolean internal2external) {
        String keyValue = internal2external ? "INT2EXT::" + internalId : "EXT2INT::" + externalId;
        return AttributeValue.builder().s( keyValue ).build();
    }
}
