package it.pagopa.pn.datavault.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.datavault.dao.DynamoDbUtils.*;

@Component
public class DynamoItem2ObjectDtoMapper {

    private final DynamoDbAsyncClient dynamo;
    private final TableDefinition tableDef;

    private final ObjectMapper jsonMapper = new ObjectMapper();

    public DynamoItem2ObjectDtoMapper(DynamoDbAsyncClient dynamo, TableDefinition tableDef) {
        this.dynamo = dynamo;
        this.tableDef = tableDef;
    }

    public <T> Map<String, AttributeValue> dto2dynamoItem(String hashKeyValue, String fieldId, T valueObj) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put( tableDef.getHashKeyAttributeName(), AttributeValue.builder().s( hashKeyValue ).build());
            item.put( tableDef.getSortKeyAttributeName(), AttributeValue.builder().s( fieldId ).build());

            String jsonValue = jsonMapper.writeValueAsString( valueObj );
            item.put( tableDef.getValueAttributeName(), AttributeValue.builder().s( jsonValue ).build());
            return item;
        }
        catch ( JsonProcessingException exc) {
            throw new RuntimeException( exc );
        }
    }

    public <T> Map<String, T> dynamoItem2dto(List<Map<String, AttributeValue>> items, Class<T> objType) {
        Map<String, T> results = new HashMap<>();

        items.forEach( item -> {
            KeyValuePair<T> entry = this.oneItemToObject( item, objType );
            results.put( entry.getKey(), entry.getValue() );
        });
        return results;
    }

    private <T> KeyValuePair<T> oneItemToObject(Map<String, AttributeValue> item, Class<T> objType) {
        try {
            String key = nullSafeGetAttributeValue( item, tableDef.getSortKeyAttributeName() );
            String jsonValue = nullSafeGetAttributeValue( item, tableDef.getValueAttributeName() );
            T value;
            if( jsonValue != null ) {
                value = jsonMapper.readValue( jsonValue, objType );
            }
            else {
                value = null;
            }

            return new KeyValuePair<>( key, value);
        }
        catch ( JsonProcessingException exc) {
            throw new RuntimeException( exc );
        }
    }

    @Value
    private static class KeyValuePair<T> {
        String key;
        T value;
    }
}
