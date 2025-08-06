package it.pagopa.pn.datavault.svc;

import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddress;
import it.pagopa.pn.datavault.generated.openapi.server.v1.dto.PaperAddresses;
import it.pagopa.pn.datavault.mapper.PaperAddressEntityPaperAddressMapper;
import it.pagopa.pn.datavault.middleware.db.PaperAddressDao;
import it.pagopa.pn.datavault.middleware.db.entities.PaperAddressEntity;
import lombok.CustomLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@CustomLog
@Slf4j
public class PaperAddressService {

    private final PaperAddressDao paperAddressDao;
    private final PaperAddressEntityPaperAddressMapper mappingsDao;

    public PaperAddressService(PaperAddressDao paperAddressDao, PaperAddressEntityPaperAddressMapper mappingsDao) {
        this.paperAddressDao = paperAddressDao;
        this.mappingsDao = mappingsDao;
    }

    /**
     * Recupera tutti gli indirizzi analogici per un dato paperRequestId.
     *
     * @param paperRequestId id della richiesta analogica
     * @return {@link Mono} contenente la lista degli indirizzi
     */
    public Mono<PaperAddresses> getPaperAddressesByPaperRequestId(String paperRequestId) {
        log.debug("Getting paper addresses for paperRequestId: {}", paperRequestId);

        return paperAddressDao.getPaperAddressesByPaperRequestId(paperRequestId)
                .map(mappingsDao::toDto)
                .collectList()
                .map(addresses -> {
                    PaperAddresses result = new PaperAddresses();
                    result.setAddresses(addresses);
                    return result;
                });
    }

    /**
     * Recupera un indirizzo analogico specifico per paperRequestId e addressId.
     *
     * @param paperRequestId id della richiesta analogica
     * @param addressId id dell'indirizzo
     * @return {@link Mono} contenente l'indirizzo
     */
    public Mono<PaperAddress> getPaperAddressByIds(String paperRequestId, String addressId) {
        log.debug("Getting paper address for paperRequestId: {} and addressId: {}", paperRequestId, addressId);

        return paperAddressDao.getPaperAddressByIds(paperRequestId, addressId)
                .map(mappingsDao::toDto);
    }


    /**
     * Salva o aggiorna un indirizzo analogico.
     *
     * @param paperRequestId id della richiesta paper
     * @param addressId id dell'indirizzo
     * @param addressDto dati dell'indirizzo da salvare
     * @return {@link Mono}
     */
    public Mono<Void> updatePaperAddress(String paperRequestId, String addressId, PaperAddress addressDto) {
        log.debug("Saving/updating paper address for paperRequestId: {} and addressId: {}", paperRequestId, addressId);

        PaperAddressEntity entity = mappingsDao.toEntity(paperRequestId, addressId, addressDto);

        return paperAddressDao.updatePaperAddress(paperRequestId, addressId, entity)
                .then();
    }

    /**
     * Elimina un indirizzo analogico.
     *
     * @param paperRequestId id della richiesta paper
     * @param addressId id dell'indirizzo da eliminare
     * @return {@link Mono}
     */
    public Mono<PaperAddress> deletePaperAddress(String paperRequestId, String addressId) {
        log.debug("Deleting paper address for paperRequestId: {} and addressId: {}", paperRequestId, addressId);

        return paperAddressDao.deletePaperAddress(paperRequestId, addressId)
                .map(mappingsDao::toDto);
    }
}
