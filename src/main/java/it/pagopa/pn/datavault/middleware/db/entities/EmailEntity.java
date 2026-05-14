package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Email object for NotificationEntity
 */
@DynamoDbBean
@Data
public class EmailEntity {
    private static final String COL_VALUE = "value";
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_VALUE)}))  private String value;
}
