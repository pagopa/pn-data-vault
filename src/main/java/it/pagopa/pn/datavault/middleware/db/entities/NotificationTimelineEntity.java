package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/**
 * Entity Address
 */
@DynamoDbBean
@Data
public class NotificationTimelineEntity {

    public static final String ADDRESS_PREFIX = "TIMELINE#";

    public static final String COL_PK = "hashKey";
    public static final String COL_SK = "sortKey";
    public static final String COL_TAXID = "taxId";
    public static final String COL_DENOMINATION = "denomination";
    public static final String COL_DIGITAL_ADDRESS = "digitalAddress";
    public static final String COL_PHYSICAL_ADDRESS = "physicalAddress";
    public static final String COL_NEW_PHYSICAL_ADDRESS = "newPhysicalAddress";

    public NotificationTimelineEntity(){}

    public NotificationTimelineEntity(String uid, String timelineElementId){
        this.setInternalId(uid);
        this.setTimelineElementId(timelineElementId);
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
    @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))  private String timelineElementId;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_TAXID)})) private String taxId;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DENOMINATION)})) private String denomination;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DIGITAL_ADDRESS)})) private String digitalAddress;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_PHYSICAL_ADDRESS)})) private PhysicalAddress physicalAddress;
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_NEW_PHYSICAL_ADDRESS)})) private PhysicalAddress newPhysicalAddress;

}
