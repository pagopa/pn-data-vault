package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.UUID;

/**
 * Entity Message
 */
@DynamoDbBean
@Data
public class MessageEntity {

    public static final String MESSAGE_PREFIX = "MSG#";

    public static final String COL_PK = "hashKey";
    public static final String COL_SK = "sortKey";
    public static final String COL_PRIMARY_MESSAGE = "primaryMessage";
    public static final String COL_ADDITIONAL_MESSAGE = "additionalMessage";
    public static final String COL_CREATED_AT = "createdAt";
    public static final String COL_EXPIRATION = "expiration";

    public MessageEntity() {}

    public MessageEntity(String senderId) {
        this.setMessageId(UUID.randomUUID().toString());
        this.setSenderId(senderId);
    }

    @DynamoDbIgnore
    public static String buildPk(String messageId) {
        return MESSAGE_PREFIX + messageId;
    }

    @DynamoDbIgnore
    public String getMessageId() {
        return this.pk.replace(MESSAGE_PREFIX, "");
    }

    @DynamoDbIgnore
    public void setMessageId(String messageId) {
        this.pk = buildPk(messageId);
    }

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)})) private String pk;
    @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)})) private String senderId;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_PRIMARY_MESSAGE)})) private MessageObjEntity primaryMessage;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_ADDITIONAL_MESSAGE)})) private MessageObjEntity additionalMessage;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_CREATED_AT)})) private String createdAt;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_EXPIRATION)})) private Long expiration;
}
