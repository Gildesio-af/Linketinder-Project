package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.domain.Country
import zg.acelera.repository.country.CountryRepositoryJDBC
import zg.acelera.utils.exception.EntityNotFoundException

class CountryRepositoryJDBCSpec extends Specification {

    Sql sqlMock
    CountryRepositoryJDBC repository

    UUID countryId = UUID.randomUUID()

    def setup() {
        sqlMock = Mock(Sql)
        repository = new CountryRepositoryJDBC(sqlMock)
    }

    private GroovyRowResult createCountryRow(UUID id = countryId) {
        return [
                id  : id,
                name: "Brazil",
                code: "BR"
        ] as GroovyRowResult
    }

    def "findById should return a country when found"() {
        given:
        sqlMock.firstRow(_, [countryId]) >> createCountryRow()

        when:
        Country result = repository.findById(countryId)

        then:
        result.id == countryId
        result.name == "Brazil"
        result.code == "BR"
    }

    def "findById should throw EntityNotFoundException when country not found"() {
        given:
        sqlMock.firstRow(_, [countryId]) >> null

        when:
        repository.findById(countryId)

        then:
        thrown(EntityNotFoundException)
    }

    def "findAll should return list of countries"() {
        given:
        UUID id2 = UUID.randomUUID()
        sqlMock.rows(_) >> [
                createCountryRow(),
                [id: id2, name: "United States", code: "US"] as GroovyRowResult
        ]

        when:
        List<Country> result = repository.findAll()

        then:
        result.size() == 2
        result[0].name == "Brazil"
        result[1].name == "United States"
    }

    def "findAll should return empty list when no countries exist"() {
        given:
        sqlMock.rows(_) >> []

        when:
        List<Country> result = repository.findAll()

        then:
        result.isEmpty()
    }
}
