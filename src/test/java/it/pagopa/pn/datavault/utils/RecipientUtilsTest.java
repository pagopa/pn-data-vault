package it.pagopa.pn.datavault.utils;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.BaseRecipientDto;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecipientUtilsTest {

    @Test
    void reverseStringTest() {
        String reverseString = RecipientUtils.reverseString("prova");
        assertThat(reverseString).isEqualTo("avorp");

    }

    @Test
    void getRecipientDenominationByInternalIdMockForPFTest() {
        List<BaseRecipientDto> recipientDenominations = RecipientUtils.getRecipientDenominationByInternalIdMock(List.of(
                "PF-provapf",
                "PG-provapg"
        )).collectList().block();

        assertThat(recipientDenominations).hasSize(2);
        assertThat(recipientDenominations.get(0).getDenomination()).isEqualTo("Nome cognomePF-provapf");
        assertThat(recipientDenominations.get(0).getTaxId()).isEqualTo("fpavorp");
        assertThat(recipientDenominations.get(1).getDenomination()).isEqualTo("ragionesocialePG-provapg");
        assertThat(recipientDenominations.get(1).getTaxId()).isEqualTo("gpavorp");
    }

    @Test
    void encapsulateRecipientTypeTest() {
        String uuidPF = UUID.randomUUID().toString();
        String uuidPG = UUID.randomUUID().toString();
        String internalIdPF = RecipientUtils.encapsulateRecipientType(RecipientType.PF, uuidPF);
        String internalIdPG = RecipientUtils.encapsulateRecipientType(RecipientType.PG, uuidPG);

        assertThat(internalIdPF).isEqualTo(RecipientType.PF + "-" + uuidPF);
        assertThat(internalIdPG).isEqualTo(RecipientType.PG + "-" + uuidPG);
    }

    @Test
    void mapToInternalIdTest() {
        UUID uuidPF = UUID.randomUUID();
        UUID uuidPG = UUID.randomUUID();
        String internalIdPF = RecipientType.PF + "-" + uuidPF;
        String internalIdPG = RecipientType.PG + "-" + uuidPG;

        List<InternalId> internalIds = RecipientUtils.mapToInternalId(List.of(internalIdPF, internalIdPG));

        assertThat(internalIds).hasSize(2);

        assertThat(internalIds.get(0).internalId()).isEqualTo(uuidPF);
        assertThat(internalIds.get(0).recipientType()).isEqualTo(RecipientType.PF);
        assertThat(internalIds.get(0).internalIdWithRecipientType()).isEqualTo(internalIdPF);

        assertThat(internalIds.get(1).internalId()).isEqualTo(uuidPG);
        assertThat(internalIds.get(1).recipientType()).isEqualTo(RecipientType.PG);
        assertThat(internalIds.get(1).internalIdWithRecipientType()).isEqualTo(internalIdPG);

    }
}
