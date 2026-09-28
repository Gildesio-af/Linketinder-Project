package service

import spock.lang.Specification
import zg.acelera.domain.Country
import zg.acelera.dto.country.CountryDTO
import zg.acelera.repository.country.CountryRepository
import zg.acelera.service.CountryService

class CountryServiceSpec extends Specification {

    CountryRepository repositoryMock
    CountryService service

    UUID countryId = UUID.randomUUID()

    def setup() {
        repositoryMock = Mock(CountryRepository)
        service = new CountryService(repositoryMock)
    }

    def "getCountryById should return a CountryDTO"() {
        given:
        Country country = new Country(id: countryId, name: "Brazil", code: "BR")
        repositoryMock.findById(countryId) >> country

        when:
        CountryDTO result = service.getCountryById(countryId)

        then:
        result.id() == countryId
        result.name() == "Brazil"
        result.code() == "BR"
    }

    def "getAllCountries should return a list of CountryDTOs"() {
        given:
        UUID id2 = UUID.randomUUID()
        repositoryMock.findAll() >> [
                new Country(id: countryId, name: "Brazil", code: "BR"),
                new Country(id: id2, name: "United States", code: "US")
        ]

        when:
        List<CountryDTO> result = service.getAllCountries()

        then:
        result.size() == 2
        result[0].name() == "Brazil"
        result[1].name() == "United States"
    }

    def "getAllCountries should return empty list when no countries exist"() {
        given:
        repositoryMock.findAll() >> []

        when:
        List<CountryDTO> result = service.getAllCountries()

        then:
        result.isEmpty()
    }
}
