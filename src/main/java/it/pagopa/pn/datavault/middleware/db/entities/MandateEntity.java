package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/**
 * Entity Mandate
 */
@DynamoDbBean
@Data
public class MandateEntity {

    public static final String MANDATE_PREFIX = "MAND#";
    public static final String MANDATE_SORTKEY = "N/A";

    public static final String COL_PK = "hashKey";
    public static final String COL_SK = "sortKey";
    public static final String COL_NAME = "name";
    public static final String COL_SURNAME = "surname";
    public static final String COL_BUSINESSNAME = "businessName";

    public MandateEntity(){}

    public MandateEntity(String mandateId){
        this.setMandateId(mandateId);
        this.setSk(MANDATE_SORTKEY);
    }

    @DynamoDbIgnore
    public String getMandateId(){
        return this.pk.replace(MANDATE_PREFIX, "");
    }

    @DynamoDbIgnore
    public void setMandateId(String mandateId){
        this.pk = MANDATE_PREFIX + mandateId;
    }

    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_PK)})) private String pk;
    @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_SK)}))  private String sk;

    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_NAME)})) private String name;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_SURNAME)})) private String surname;
    @ToString.Exclude @Getter(onMethod=@__({@DynamoDbAttribute(COL_BUSINESSNAME)})) private String businessName;
}
