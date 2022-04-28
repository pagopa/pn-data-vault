package it.pagopa.pn.datavault.middleware.db.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.datavault.exceptions.InvalidDataException;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.EnhancedAttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class PhysicalAddressTypeConverter implements AttributeConverter<PhysicalAddress> {

    private final ObjectMapper jsonMapper = new ObjectMapper();


    @Override
    public AttributeValue transformFrom(PhysicalAddress input) {
        String jsonValue;
        try {
            jsonValue = jsonMapper.writeValueAsString( input );
        } catch (JsonProcessingException e) {
            InvalidDataException exc = new InvalidDataException();
            exc.addSuppressed(e);
            throw exc;
        }
        return EnhancedAttributeValue.fromString(jsonValue).toAttributeValue();
    }

    @Override
    public PhysicalAddress transformTo(AttributeValue input) {
        if( input.s() != null ) {
            try {
                return jsonMapper.readValue( input.s(), PhysicalAddress.class );
            } catch (JsonProcessingException e) {
                InvalidDataException exc = new InvalidDataException();
                exc.addSuppressed(e);
                throw exc;
            }
        }

        return null;
    }

    @Override
    public EnhancedType<PhysicalAddress> type() {
        return EnhancedType.of(PhysicalAddress.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
