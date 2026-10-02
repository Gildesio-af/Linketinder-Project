package zg.acelera.dto.address

import groovy.transform.ImmutableOptions
import zg.acelera.domain.Address
import zg.acelera.dto.country.CountryDTO

@ImmutableOptions(knownImmutableClasses = [CountryDTO])
record AddressResponseDTO(
        UUID id,
        String cep,
        String street,
        String number,
        String city,
        String state,
        String complement,
        String neighborhood,
        CountryDTO country,
        UUID userId
){
    static AddressResponseDTO fromDomain(Address address, CountryDTO countryDTO ) {
        return new AddressResponseDTO(
                id: address.id,
                cep: address.cep,
                street: address.street,
                number: address.number,
                city: address.city,
                state: address.state,
                complement: address.complement,
                neighborhood: address.neighborhood,
                country: countryDTO,
                userId: address.userId
        )
    }
}
