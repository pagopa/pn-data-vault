package it.pagopa.pn.datavault.dao;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class DynamoDbUtils {

    public static String nullSafeGetAttributeValue(Map<String, AttributeValue> item, String attributeName ) {
        String result;

        AttributeValue attribute = item.get( attributeName );
        if( attribute != null ) {
            result = attribute.s();
        }
        else {
            result = null;
        }
        return result;
    }
}
