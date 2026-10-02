package service

import spock.lang.Specification
import zg.acelera.domain.Address
import zg.acelera.domain.Country
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.address.AddressUpdateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.repository.address.AddressRepository
import zg.acelera.service.AddressService
import zg.acelera.service.CountryService

    class AddressServiceSpec extends Specification {

        AddressRepository repositoryMock
        CountryService countryServiceMock
        AddressService service

        UUID addressId = UUID.randomUUID()
        UUID userId = UUID.randomUUID()
        UUID countryId = UUID.randomUUID()

        def setup() {
            repositoryMock = Mock(AddressRepository)
            countryServiceMock = Mock(CountryService)
            service = new AddressService(repositoryMock, countryServiceMock)
        }

        private Address createAddress() {
            return new Address(
                    id: addressId, userId: userId, cep: "12345678",
                    street: "Main St", number: "100", complement: "Apt 1",
                    neighborhood: "Downtown", city: "São Paulo", state: "SP",
                    country: new Country(id: countryId)
            )
        }

        private CountryDTO createCountryDTO() {
            return new CountryDTO(id: countryId, name: "Brazil", code: "BR")
        }

        def "getAddressById should return address response DTO"() {
            given:
            Address address = createAddress()
            repositoryMock.findById(addressId) >> address
            countryServiceMock.getCountryById(countryId) >> createCountryDTO()

            when:
            AddressResponseDTO result = service.getAddressById(addressId)

            then:
            result.id() == addressId
            result.street() == "Main St"
            result.country().name() == "Brazil"
        }

        def "getAddressesByUserId should return set of address response DTOs"() {
            given:
            Address address = createAddress()
            repositoryMock.findByUserId(userId) >> ([address] as Set)
            countryServiceMock.getCountryById(countryId) >> createCountryDTO()

            when:
            Set<AddressResponseDTO> result = service.getAddressesByUserId(userId)

            then:
            result.size() == 1
        }

        def "getAddressesByUserId should return empty set when no addresses found"() {
            given:
            repositoryMock.findByUserId(userId) >> ([] as Set)

            when:
            Set<AddressResponseDTO> result = service.getAddressesByUserId(userId)

            then:
            result.isEmpty()
        }

        def "getAddressByJobId should return address response DTO"() {
            given:
            UUID jobId = UUID.randomUUID()
            Address address = createAddress()
            repositoryMock.findByJobId(jobId) >> address
            countryServiceMock.getCountryById(countryId) >> createCountryDTO()

            when:
            AddressResponseDTO result = service.getAddressByJobId(jobId)

            then:
            result.street() == "Main St"
        }

        def "createAddress should create and return address response DTO"() {
            given:
            Address domainAddress = createAddress()

            repositoryMock.create(_, userId) >> domainAddress
            countryServiceMock.getCountryById(countryId) >> createCountryDTO()

            AddressCreateDTO dto = new AddressCreateDTO(null, "12345678", "Main St", "100", "São Paulo", "SP", "Apt 1", "Downtown", countryId.toString(), userId)

            when:
            AddressResponseDTO result = service.createAddress(dto, userId)

            then:
            result.street() == "Main St"
        }

        def "updateAddress should update and return address response DTO"() {
            given:
            Address domainAddress = createAddress()

            repositoryMock.update(_, addressId) >> domainAddress
            countryServiceMock.getCountryById(countryId) >> createCountryDTO()

            AddressUpdateDTO dto = new AddressUpdateDTO("12345678", "Main St", "100", "São Paulo", "Apt 1", "Downtown", "SP", countryId.toString())

            when:
            AddressResponseDTO result = service.updateAddress(dto, addressId)

            then:
            result.street() == "Main St"
        }

        def "deleteAddress should call repository delete"() {
            when:
            service.deleteAddress(addressId)

            then:
            1 * repositoryMock.delete(addressId)
        }
    }
