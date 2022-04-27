package it.pagopa.pn.datavault.middleware.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Component
@Slf4j
public class ConfidentialObjectDao {

    private final DynamoDbAsyncClient dynamo = null;
    private final TableDefinition tableDef = null;
    private final DynamoItem2ObjectDtoMapper entity2dto = null;

    public ConfidentialObjectDao() {
    }

    public <T> Mono<Map<String, T>> getByInternalId(String namespace, String internalId, Class<T> objType) {
        log.info( "Retrieve object namespace={}, internalId={}", namespace, internalId);

        String hashKeyValue = buildHashKeyValue( namespace, internalId );

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName( tableDef.getTableName() )
                .keyConditions(Collections.singletonMap( tableDef.getHashKeyAttributeName(), Condition.builder()
                        .comparisonOperator( ComparisonOperator.EQ )
                        .attributeValueList( AttributeValue.builder().s( hashKeyValue ).build())
                        .build()
                ))
                .build();

        return Mono.fromFuture(dynamo.query( queryRequest ))
                .map( queryResponse -> {
                    Map<String, T> results;
                    if( queryResponse.hasItems() ) {
                        log.info( "Retrieve object namespace={}, internalId={} result=FOUND", namespace, internalId);
                        List<Map<String, AttributeValue>> items = queryResponse.items();
                        log.debug( "Retrieve object namespace={}, internalId={} resultItem={}", namespace, internalId, items);
                        results = entity2dto.dynamoItem2dto( items, objType );
                        log.debug( "Retrieve object namespace={}, internalId={} resultDto={}", namespace, internalId, results);
                    }
                    else {
                        log.info( "Retrieve object namespace={}, internalId={} result=NOT_FOUND", namespace, internalId);
                        results = Collections.emptyMap();
                    }
                    return results;
                });
    }

    public <T> Mono<String> updateFieldByInternalId(String namespace, String internalId, String fieldId, T valueObj) {
        String hashKeyValue = buildHashKeyValue( namespace, internalId );

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName( tableDef.getTableName() )
                .item( entity2dto.dto2dynamoItem( hashKeyValue, fieldId, valueObj ) )
                .build();

        return Mono.fromFuture( dynamo.putItem( putItemRequest ))
                .map( putItemResponse -> internalId );
    }

    public Mono<String> deleteFieldByInternalId(String namespace, String internalId, String fieldId) {
        String hashKeyValue = buildHashKeyValue( namespace, internalId );

        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName( tableDef.getTableName() )
                .key( entity2dto.dto2dynamoItemKey( hashKeyValue, fieldId ) )
                .build();

        return Mono.fromFuture( dynamo.deleteItem( deleteItemRequest ))
                .map( deleteItemResponse -> internalId );
    }



    private String buildHashKeyValue(String namespace, String internalId) {
        return "OBJECT-" + namespace + "#" + internalId;
    }

    public Mono<String> deleteByInternalId(String namespace, String internalId) {
        return getByInternalId(namespace, internalId, HashMap.class)
                .flatMap( map ->
                        Flux.fromIterable( map.keySet() )
                            .flatMap( key -> deleteFieldByInternalId( namespace, internalId, key) )
                            .reduce( (a, b) -> a+b)
                );
    }

}
