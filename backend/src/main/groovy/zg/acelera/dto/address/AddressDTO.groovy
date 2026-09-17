package zg.acelera.dto.address

import zg.acelera.domain.Address
import zg.acelera.domain.Country

record AddressDTO (
        UUID id,
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
    AddressDTO() {
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
                id: this.id ? this.id : null,
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
}
