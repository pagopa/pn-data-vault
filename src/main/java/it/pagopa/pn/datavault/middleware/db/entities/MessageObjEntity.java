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
    public static final String COL_BODY = "body";
    public static final String COL_ABSTRACT = "abstract";
    public static final String COL_LANGUAGE = "language";

    public static final String LANGUAGE_REGEX = "^(IT|DE|SI|FR)$";

    @ToString.Exclude @Getter(onMethod = @__({@DynamoDbAttribute(COL_SUBJECT)})) private String subject;
    @ToString.Exclude @Getter(onMethod = @__({@DynamoDbAttribute(COL_BODY)})) private String body;
    @ToString.Exclude @Getter(onMethod = @__({@DynamoDbAttribute(COL_ABSTRACT)})) private String abstractText;
    @Getter(onMethod = @__({@DynamoDbAttribute(COL_LANGUAGE)})) private String language;
}

