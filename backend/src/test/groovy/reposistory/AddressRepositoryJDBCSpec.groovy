package reposistory

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Specification
import zg.acelera.domain.Address
import zg.acelera.domain.Country
import zg.acelera.repository.address.AddressRepositoryJDBC
import zg.acelera.utils.exception.EntityNotFoundException

class AddressRepositoryJDBCSpec extends Specification {

    Sql sqlMock
    AddressRepositoryJDBC repository

    UUID addressId = UUID.randomUUID()
    UUID userId = UUID.randomUUID()
    UUID countryId = UUID.randomUUID()

    def setup() {
        sqlMock = Mock(Sql)
        repository = new AddressRepositoryJDBC(sqlMock)
    }

    private GroovyRowResult createAddressRow() {
        return [
                id          : addressId,
                user_id     : userId,
                cep         : "12345678",
                street      : "Main Street",
                number      : "100",
                complement  : "Apt 5",
                neighborhood: "Downtown",
                city        : "São Paulo",
                state       : "SP",
                country_id  : countryId
        ] as GroovyRowResult
    }

    def "findById should return an address when found"() {
        given:
        sqlMock.firstRow(_, [addressId]) >> createAddressRow()

        when:
        Address result = repository.findById(addressId)

        then:
        result.id == addressId
        result.cep == "12345678"
        result.street == "Main Street"
        result.city == "São Paulo"
        result.country.id == countryId
    }

    def "findById should throw EntityNotFoundException when address not found"() {
        given:
        sqlMock.firstRow(_, [addressId]) >> null

        when:
        repository.findById(addressId)

        then:
        thrown(EntityNotFoundException)
    }

    def "findByUserId should return addresses for a given user"() {
        given:
        sqlMock.rows(_, [userId]) >> [createAddressRow()]

        when:
        Set<Address> result = repository.findByUserId(userId)

        then:
        result.size() == 1
        result.first().userId == userId
    }

    def "findByUserId should return empty set when no addresses found"() {
        given:
        sqlMock.rows(_, [userId]) >> []

        when:
        Set<Address> result = repository.findByUserId(userId)

        then:
        result.isEmpty()
    }

    def "findByJobId should return an address when found"() {
        given:
        UUID jobId = UUID.randomUUID()
        sqlMock.firstRow(_, [jobId]) >> createAddressRow()

        when:
        Address result = repository.findByJobId(jobId)

        then:
        result.street == "Main Street"
    }

    def "findByJobId should throw EntityNotFoundException when not found"() {
        given:
        UUID jobId = UUID.randomUUID()
        sqlMock.firstRow(_, [jobId]) >> null

        when:
        repository.findByJobId(jobId)

        then:
        thrown(EntityNotFoundException)
    }

    def "create should insert an address and return it"() {
        given:
        Address address = new Address(
                cep: "12345678", street: "Main Street", number: "100",
                complement: "Apt 5", neighborhood: "Downtown",
                city: "São Paulo", state: "SP",
                country: new Country(id: countryId)
        )

        sqlMock.firstRow(_, _) >> createAddressRow()

        when:
        Address result = repository.create(address, userId)

        then:
        result.id == addressId
        result.street == "Main Street"
    }

    def "delete should call executeUpdate and succeed when address exists"() {
        given:
        sqlMock.executeUpdate(_, [addressId]) >> 1

        when:
        repository.delete(addressId)

        then:
        notThrown(EntityNotFoundException)
    }

    def "delete should throw EntityNotFoundException when address does not exist"() {
        given:
        sqlMock.executeUpdate(_, [addressId]) >> 0

        when:
        repository.delete(addressId)

        then:
        thrown(EntityNotFoundException)
    }
}
