package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/**
 * Entity Address
 */
@DynamoDbBean
@Data
public class NotificationEntity {

    public static final String ADDRESS_PREFIX = "NOTIFY#";
    public static final String NORMALIZED_ADDRESS_PREFIX = "NORMALIZED_NOTIFY#";

    public static final String COL_PK = "hashKey";
    public static final String COL_SK = "sortKey";
    public static final String COL_DENOMINATION = "denomination";
    public static final String COL_DIGITAL_ADDRESS = "digitalAddress";
    public static final String COL_PHYSICAL_ADDRESS = "physicalAddress";
    public static final String COL_NORMALIZED_ADDRESS = "normalizedAddress";

    public NotificationEntity(){}

    public NotificationEntity(String uid, String recipientIndex, Boolean normalized){
        this.setNormalizedAddress(normalized);
        this.setInternalId(uid);
        this.setRecipientIndex(recipientIndex);
    }

    @DynamoDbIgnore
    public String getInternalId(){
        String prefix = getPrefixPk();
        return this.pk.replace(prefix, "");
    }

    @DynamoDbIgnore
    public void setInternalId(String uid){
        String prefix = getPrefixPk();
        this.pk = prefix + uid;
    }

    @DynamoDbIgnore
    public String getPrefixPk() {
        return Boolean.TRUE.equals(getNormalizedAddress()) ? NORMALIZED_ADDRESS_PREFIX : ADDRESS_PREFIX;
    }

    public void setNormalizedAddress(Boolean normalizedAddress) {
        this.normalizedAddress = Boolean.TRUE.equals(normalizedAddress);
    }

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)})) private String pk;
    @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))  private String recipientIndex;

    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_DENOMINATION)}))  private String denomination;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_DIGITAL_ADDRESS)})) private String digitalAddress;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_PHYSICAL_ADDRESS)})) private PhysicalAddress physicalAddress;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_NORMALIZED_ADDRESS)})) private Boolean normalizedAddress;
}
