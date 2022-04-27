package it.pagopa.pn.datavault.mapper;

public interface BaseMapperInterface<T,S> {
    S toEntity(T source);
    T toDto(S source);
}
