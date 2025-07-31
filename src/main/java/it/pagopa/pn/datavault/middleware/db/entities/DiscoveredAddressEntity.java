package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.math.BigDecimal;

/**
 * Entity DiscoveredAddress
 * <p>
 * Usata dal ms pn-paper-tracker per anonimizzare i discoveredAddress restituiti
 * dal consolidatore.
 */
@DynamoDbBean
@Data
public class DiscoveredAddressEntity {

    public static final String DISCOVERED_ADDRESS_PREFIX = "DISCOVERED_ADDR#";
    public static final String DISCOVERED_ADDRESS_SORTKEY = "N/A";

    public static final String COL_PK = "hashKey";
    public static final String COL_SK = "sortKey";

    public static final String COL_NAME = "name";
    public static final String COL_NAME_ROW2 = "nameRow2";
    public static final String COL_ADDRESS = "address";
    public static final String COL_ADDRESS_ROW2 = "addressRow2";
    public static final String COL_CAP = "cap";
    public static final String COL_CITY = "city";
    public static final String COL_CITY2 = "city2";
    public static final String COL_PR = "pr";
    public static final String COL_COUNTRY = "country";

    public DiscoveredAddressEntity(){}

    public DiscoveredAddressEntity(String addressId){
        this.setAddressId(addressId);
        this.setSk(DISCOVERED_ADDRESS_SORTKEY);
    }

    @DynamoDbIgnore
    public String getAddressId(){
        return this.pk.replace(DISCOVERED_ADDRESS_PREFIX, "");
    }

    @DynamoDbIgnore
    public void setAddressId(String id){
        this.pk = DISCOVERED_ADDRESS_PREFIX + id;
    }

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)}))
    private String pk;
    @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))
    private String sk;

    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_NAME)}))
    private String name;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_NAME_ROW2)}))
    private String nameRow2;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_ADDRESS)}))
    private String address;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_ADDRESS_ROW2)}))
    private String addressRow2;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_CAP)}))
    private String cap;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_CITY)}))
    private String city;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_CITY2)}))
    private String city2;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_PR)}))
    private String pr;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_COUNTRY)}))
    private String country;
}
