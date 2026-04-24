package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Embedded entity representing a message object (primary or additional language)
 */
@DynamoDbBean
@Data
public class MessageObjEntity {

    public static final String COL_SUBJECT = "subject";
    public static final String COL_LONG_BODY = "longBody";
    public static final String COL_SHORT_BODY = "shortBody";
    public static final String COL_LANGUAGE = "language";

    @ToString.Exclude @Getter(onMethod = @__({@DynamoDbAttribute(COL_SUBJECT)})) private String subject;
    @ToString.Exclude @Getter(onMethod = @__({@DynamoDbAttribute(COL_LONG_BODY)})) private String longBody;
    @ToString.Exclude @Getter(onMethod = @__({@DynamoDbAttribute(COL_SHORT_BODY)})) private String shortBody;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_LANGUAGE)})) private String language;
}

