package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/**
 * Entity Address
 */
@DynamoDbBean
@Data
public class NotificationEntity {

    public static final String ADDRESS_PREFIX = "NOTIFY#";

    public static final String COL_PK = "hashKey";
    public static final String COL_SK = "sortKey";
    public static final String COL_DENOMINATION = "denomination";
    public static final String COL_DIGITAL_ADDRESS = "digitalAddress";
    public static final String COL_PHYSICAL_ADDRESS = "physicalAddress";

    public NotificationEntity(){}

    public NotificationEntity(String uid, String recipientIndex){
        this.setInternalId(uid);
        this.setRecipientIndex(recipientIndex);
    }

    @DynamoDbIgnore
    public String getInternalId(){
        return this.pk.replace(ADDRESS_PREFIX, "");
    }

    @DynamoDbIgnore
    public void setInternalId(String uid){
        this.pk = ADDRESS_PREFIX + uid;
    }

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)})) private String pk;
    @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))  private String recipientIndex;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DENOMINATION)}))  private String denomination;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DIGITAL_ADDRESS)})) private String digitalAddress;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_PHYSICAL_ADDRESS), @DynamoDbConvertedBy(PhysicalAddressTypeConverter.class)})) private PhysicalAddress physicalAddress;
}
