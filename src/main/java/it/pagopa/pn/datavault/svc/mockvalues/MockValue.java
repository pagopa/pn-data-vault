package it.pagopa.pn.datavault.svc.mockvalues;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import lombok.Data;

@Data
public class MockValue {

    private String taxId;
    private RecipientType recipientType;
    private String denomination;
}
