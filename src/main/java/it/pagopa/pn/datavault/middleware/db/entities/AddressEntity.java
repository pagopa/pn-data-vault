package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/**
 * Entity Address
 */
@DynamoDbBean
@Data
public class AddressEntity {

    public static final String ADDRESS_PREFIX = "ADDR#";

    public static final String COL_PK = "hashKey";
    public static final String COL_SK = "sortKey";
    public static final String COL_VALUE = "value";

    public AddressEntity(){}

    public AddressEntity(String uid, String addressid){
        this.setInternalId(uid);
        this.setSk(addressid);
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
    @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))  private String sk;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_VALUE)})) private String value;
}
