package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
@Data
public class PhysicalAddress {

    private static final String COL_AT = "at";
    private static final String COL_ADDRESS = "address";
    private static final String COL_ADDRESS_DETAILS = "addressDetails";
    private static final String COL_CAP = "cap";
    private static final String COL_MUNICIPALITY = "municipality";
    private static final String COL_MUNICIPALITY_DETAILS = "municipalityDetails";
    private static final String COL_PROVINCE = "province";
    private static final String COL_STATE = "state";


    // campo 'presso' per specificare meglio
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_AT)}))  private String at;

    // via, piazza, ….. e numero civico
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_ADDRESS)}))  private String address;

    // scala o interno
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_ADDRESS_DETAILS)}))  private String addressDetails;

    // Codice avviamento postale italiano o internazionale
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_CAP)}))  private String cap;

    // comune
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_MUNICIPALITY)}))  private String municipality;

    // frazione o località
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_MUNICIPALITY_DETAILS)}))  private String municipalityDetails;

    // provincia
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_PROVINCE)}))  private String province;

    // stato estero
    @Getter(onMethod=@__({@DynamoDbAttribute(COL_STATE)}))  private String state;
}
