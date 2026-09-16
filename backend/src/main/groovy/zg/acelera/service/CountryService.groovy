package zg.acelera.service

import zg.acelera.domain.Country
import zg.acelera.dto.country.CountryDTO
import zg.acelera.repository.ICountryRepository
import zg.acelera.utils.exception.EntityNotFoundException

class CountryService {
    private final ICountryRepository countryRepository

    CountryService(ICountryRepository countryRepository) {
        this.countryRepository = countryRepository
    }

    CountryDTO getCountryById(UUID id) {
        Country country
        try {
            country = countryRepository.findById(id)
        } catch (EntityNotFoundException e) {
            e.printStackTrace()
            return null
        }

        return new CountryDTO(
            id: country.id,
            name: country.name,
            code: country.code
        )
    }

    List<CountryDTO> getAllCountries() {
        List<Country> countries = countryRepository.findAll()
        return countries.collect { country ->
            new CountryDTO(
                id: country.id,
                name: country.name,
                code: country.code
            )
        }
    }
}