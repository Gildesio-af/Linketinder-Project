package zg.acelera.dto.address

import zg.acelera.domain.Address
import zg.acelera.domain.Country

record AddressCreateDTO(
        String cep,
        String street,
        String number,
        String city,
        String state,
        String complement,
        String neighborhood,
        String countryId,
        UUID userId
) {
    AddressCreateDTO() {
        if (!cep || cep.trim().length() <= 8)
            throw new IllegalArgumentException("CEP must be provided and have at least 8 characters")
        if (!street || street.trim().isEmpty())
            throw new IllegalArgumentException("Street must be provided and cannot be empty")
        if (!number || number.trim().isEmpty())
            throw new IllegalArgumentException("Number must be provided and cannot be empty")
        if (!city || city.trim().isEmpty())
            throw new IllegalArgumentException("City must be provided and cannot be empty")
        if (!state || state.trim().isEmpty())
            throw new IllegalArgumentException("State must be provided and cannot be empty")
        if (complement != null && complement.trim().isEmpty())
            throw new IllegalArgumentException("Complement must be provided and cannot be empty")
        if (!neighborhood || neighborhood.trim().isEmpty())
            throw new IllegalArgumentException("Neighborhood must be provided and cannot be empty")
        if (!countryId || countryId.trim().isEmpty())
            throw new IllegalArgumentException("Country must be provided and cannot be empty")
    }

    Address toDomain() {
        return new Address(
                cep: this.cep,
                street: this.street,
                number: this.number,
                city: this.city,
                state: this.state,
                country: new Country(id: UUID.fromString(this.countryId)),
                complement: this.complement ? this.complement : null,
                neighborhood: this.neighborhood,
                userId: this.userId
        )
    }

    static AddressCreateDTO fromMap(Map map) {
        return new AddressCreateDTO(
                cep: map.cep as String,
                street: map.street as String,
                number: map.number as String,
                city: map.city as String,
                state: map.state as String,
                complement: map.complement as String,
                neighborhood: map.neighborhood as String,
                countryId: map.countryId as String,
                userId: map.userId as UUID
        )
    }
}
