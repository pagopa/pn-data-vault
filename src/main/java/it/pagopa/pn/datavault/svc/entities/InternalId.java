package it.pagopa.pn.datavault.svc.entities;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;

import java.util.UUID;

public record InternalId(String internalIdWithRecipientType, RecipientType recipientType, UUID internalId) {
}
