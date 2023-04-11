package it.pagopa.pn.datavault.utils;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.datavault.svc.entities.InternalId;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecipientUtils {

    private RecipientUtils(){}

    /**
     * Ritorna un internal id "modificato" che contiene anche l'informazione di PF/PG
     * In questo modo, il token che gira all'interno di PN è parlante (perchè devo sapere per quale namespace vado a risolvere
     * il token in userregistry)
     *
     * @param recipientType il tipo di utente
     * @param internalId internal id
     * @return internal id modificato
     */
    public static String encapsulateRecipientType(RecipientType recipientType, String internalId)
    {
        return recipientType.getValue() + "-" + internalId;
    }

    public static List<InternalId> mapToInternalId(List<String> internalIdsWithRecipientType) {
        if (CollectionUtils.isEmpty(internalIdsWithRecipientType))
            return new ArrayList<>();

        return internalIdsWithRecipientType
                .stream()
                .map(intId -> new InternalId(intId, getRecipientTypeFromInternalId(intId), getUUIDFromInternalId(intId)))
                .toList();

    }

    protected static UUID getUUIDFromInternalId(String internalId)
    {
        internalId = internalId.substring(3);
        return UUID.fromString(internalId);
    }

    /**
     * Ritorna il recipentType in base all'internalId
     *
     * @param internalId internal id
     * @return RecipientType ricavato dall'internalId
     */
    protected static RecipientType getRecipientTypeFromInternalId(String internalId)
    {
        if (internalId.startsWith(RecipientType.PF.getValue()))
            return RecipientType.PF;
        else
            return RecipientType.PG;
    }


    public static String reverseString(String inputvalue) {
        byte[] strAsByteArray = inputvalue.getBytes();
        byte[] resultoutput = new byte[strAsByteArray.length];
        for (int i = 0; i < strAsByteArray.length; i++)
            resultoutput[i] = strAsByteArray[strAsByteArray.length - i - 1];

        return new String(resultoutput);
    }

}
