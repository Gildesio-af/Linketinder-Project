package zg.acelera.service

import zg.acelera.domain.Country
import zg.acelera.dto.country.CountryDTO
import zg.acelera.repository.country.CountryRepository
import zg.acelera.utils.exception.EntityNotFoundException

class CountryService {
    private final CountryRepository countryRepository

    CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository
    }

    CountryDTO getCountryById(UUID id) {
        Country country = countryRepository.findById(id)

        return CountryDTO.fromDomain(country)
    }

    List<CountryDTO> getAllCountries() {
        List<Country> countries = countryRepository.findAll()
        return countries.collect { country -> CountryDTO.fromDomain(country)}
    }
}