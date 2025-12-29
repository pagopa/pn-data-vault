package it.pagopa.pn.datavault.mapper;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddressResponse;
import it.pagopa.pn.datavault.middleware.db.entities.PaperAddressEntity;

public class ControllerMapper {
    public static PaperAddressResponse createPaperAddressResponse(PaperAddressEntity entity) {
        return new PaperAddressResponse()
                .paperAddressId(entity.getAddressId());
    }
}
