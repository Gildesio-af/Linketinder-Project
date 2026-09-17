package zg.acelera.dto.address

import zg.acelera.domain.Address
import zg.acelera.domain.Country

record AddressUpdateDTO(
        String cep,
        String street,
        String number,
        String city,
        String complement,
        String neighborhood,
        String state,
        String country)
{
    AddressUpdateDTO() {
        if (cep != null && cep.trim().length() < 8)
            throw new IllegalArgumentException("CEP must have at least 8 characters if provided")
        if (street != null && street.trim().isEmpty())
            throw new IllegalArgumentException("Street cannot be empty if provided")
        if (number != null && number.trim().isEmpty())
            throw new IllegalArgumentException("Number cannot be empty if provided")
        if (city != null && city.trim().isEmpty())
            throw new IllegalArgumentException("City cannot be empty if provided")
        if (state != null && state.trim().isEmpty())
            throw new IllegalArgumentException("State cannot be empty if provided")
        if (complement != null && complement.trim().isEmpty())
            throw new IllegalArgumentException("Complement cannot be empty if provided")
        if (neighborhood != null && neighborhood.trim().isEmpty())
            throw new IllegalArgumentException("Neighborhood cannot be empty if provided")
        if (country != null && country.trim().isEmpty())
            throw new IllegalArgumentException("Country cannot be empty if provided")
    }

    Address toDomain() {
        return new Address(
                cep: this.cep ? this.cep : null,
                street: this.street ? this.street : null,
                number: this.number ? this.number : null,
                city: this.city ? this.city : null,
                state: this.state ? this.state : null,
                country: this.country ?
                        new Country(id: UUID.fromString(this.country)) : null,
                complement: this.complement ? this.complement : null,
                neighborhood: this.neighborhood ? this.neighborhood : null
        )
    }
}
