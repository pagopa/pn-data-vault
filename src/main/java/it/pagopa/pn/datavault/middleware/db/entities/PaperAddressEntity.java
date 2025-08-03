package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/**
 * Entity PaperAddress
 * <p>
 * Usata dal per anonimizzare gli indirizzi del flusso analogico.
 */
@DynamoDbBean
@Data
public class PaperAddressEntity {

    public static final String PAPER_ADDRESS_PREFIX = "PAPER_ADDR#";

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

    public PaperAddressEntity(){}

    public PaperAddressEntity(String paperRequestId, String addressId){
        this.setPaperRequestId(paperRequestId);
        this.setAddressId(addressId);
    }

    @DynamoDbIgnore
    public static String buildPk(String paperRequestId){
        return PAPER_ADDRESS_PREFIX + paperRequestId;
    }

    @DynamoDbIgnore
    public String getPaperRequestId(){
        return this.pk.replace(PAPER_ADDRESS_PREFIX, "");
    }

    @DynamoDbIgnore
    public void setPaperRequestId(String id){
        this.pk = buildPk(id);
    }

    @DynamoDbIgnore
    public String getAddressId(){
        return this.sk;
    }

    @DynamoDbIgnore
    public void setAddressId(String id){
        this.sk = id;
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
