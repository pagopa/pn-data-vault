package it.pagopa.pn.datavault.middleware.db.entities;

import lombok.Data;

@Data
public class PhysicalAddress {
    // campo 'presso' per specificare meglio
    private String at;

    // via, piazza, ….. e numero civico
    private String address;

    // scala o interno
    private String addressDetails;

    // Codice avviamento postale italiano o internazionale
    private String cap;

    // frazione o località
    private String municipality;

    // provincia
    private String province;

    // stato estero
    private String state;
}
