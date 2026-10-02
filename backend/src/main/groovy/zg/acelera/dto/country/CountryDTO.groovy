package zg.acelera.dto.country

import zg.acelera.domain.Country

record CountryDTO(
        UUID id,
        String name,
        String code
) {

    static CountryDTO fromDomain(Country country) {
        return new CountryDTO(
                id: country.id,
                name: country.name,
                code: country.code
        )
    }
}
