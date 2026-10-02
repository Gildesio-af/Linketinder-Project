package service

import spock.lang.Specification
import zg.acelera.domain.Company
import zg.acelera.domain.Skill
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.company.CompanyCreateDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.company.CompanyRepository
import zg.acelera.service.AddressService
import zg.acelera.service.CompanyService
import zg.acelera.service.SkillService
import zg.acelera.utils.exception.EntityNotFoundException

class CompanyServiceSpec extends Specification {

    CompanyRepository repositoryMock
    AddressService addressServiceMock
    SkillService skillServiceMock
    CompanyService service

    UUID companyId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()
    UUID addressId = UUID.randomUUID()
    UUID countryId = UUID.randomUUID()

    def setup() {
        repositoryMock = Mock(CompanyRepository)
        addressServiceMock = Mock(AddressService)
        skillServiceMock = Mock(SkillService)
        service = new CompanyService(repositoryMock, addressServiceMock, skillServiceMock)
    }

    private Company createCompany() {
        return new Company(
                id: companyId, name: "Tech Corp", email: "tech@corp.com",
                password: "secret123", description: "A tech company",
                cnpj: "12345678000199",
                skills: [new Skill(id: skillId, name: "Java")] as Set
        )
    }

    private AddressResponseDTO createAddressResponse() {
        return new AddressResponseDTO(
                id: addressId, cep: "12345678", street: "Main St", number: "100",
                city: "São Paulo", state: "SP", complement: null, neighborhood: "Downtown",
                country: new CountryDTO(id: countryId, name: "Brazil", code: "BR"),
                userId: companyId
        )
    }

    def "listAllCompanies should return a list of company response DTOs"() {
        given:
        Company company = createCompany()
        repositoryMock.findAll() >> [company]
        addressServiceMock.getAddressesByUserId(companyId) >> ([createAddressResponse()] as Set)

        when:
        List<CompanyResponseDTO> result = service.listAllCompanies()

        then:
        result.size() == 1
        result[0].name() == "Tech Corp"
        result[0].cnpj() == "12345678000199"
    }

    def "listAllCompanies should return empty list when no companies exist"() {
        given:
        repositoryMock.findAll() >> []

        when:
        List<CompanyResponseDTO> result = service.listAllCompanies()

        then:
        result.isEmpty()
    }

    def "listCompanyByCnpj should return a company response DTO"() {
        given:
        Company company = createCompany()
        repositoryMock.findByCnpj("12345678000199") >> company
        addressServiceMock.getAddressesByUserId(companyId) >> ([createAddressResponse()] as Set)

        when:
        CompanyResponseDTO result = service.listCompanyByCnpj("12345678000199")

        then:
        result.cnpj() == "12345678000199"
        result.name() == "Tech Corp"
    }

    def "listCompaniesBySkill should return companies matching the skill"() {
        given:
        Company company = createCompany()
        repositoryMock.findBySkill("Java") >> [company]
        addressServiceMock.getAddressesByUserId(companyId) >> ([createAddressResponse()] as Set)

        when:
        List<CompanyResponseDTO> result = service.listCompaniesBySkill("Java")

        then:
        result.size() == 1
        result[0].name() == "Tech Corp"
    }

    def "registerCompany should save company with address and return response DTO"() {
        given:
        CompanyCreateDTO dto = new CompanyCreateDTO(
                "12345678000199", "Tech Corp", "tech@corp.com",
                "secret123", "A tech company", [skillId.toString()] as Set
        )

        AddressCreateDTO addressDTO = new AddressCreateDTO(null, "12345678", "Main St", "100", "São Paulo", "SP", null, "Downtown", countryId.toString(), null)
        Company savedCompany = createCompany()
        AddressResponseDTO addressResponse = createAddressResponse()

        repositoryMock.save(_) >> savedCompany
        addressServiceMock.createAddress(addressDTO, companyId) >> addressResponse
        addressServiceMock.getAddressesByUserId(companyId) >> ([addressResponse] as Set)

        when:
        CompanyResponseDTO result = service.registerCompany(dto, addressDTO)

        then:
        result.name() == "Tech Corp"
        result.cnpj() == "12345678000199"
    }

    def "registerCompany without address should save company and return response DTO"() {
        given:
        CompanyCreateDTO dto = new CompanyCreateDTO(
                "12345678000199", "Tech Corp", "tech@corp.com",
                "secret123", "A tech company", [skillId.toString()] as Set
        )
        Company savedCompany = createCompany()

        repositoryMock.save(_) >> savedCompany
        addressServiceMock.getAddressesByUserId(companyId) >> ([] as Set)

        when:
        CompanyResponseDTO result = service.registerCompany(dto, null)

        then:
        result.name() == "Tech Corp"
        0 * addressServiceMock.createAddress(_, _)
    }

    def "resolveSkillNamesToIds should return set of skill IDs"() {
        given:
        skillServiceMock.getSkillByName("Java") >> new SkillResponseDTO(id: skillId, name: "Java")

        when:
        Set<String> result = service.resolveSkillNamesToIds(["Java"] as Set)

        then:
        result.size() == 1
        result.first() == skillId.toString()
    }

    def "resolveSkillNamesToIds should throw when skill not found"() {
        given:
        skillServiceMock.getSkillByName("NonExistent") >> null

        when:
        service.resolveSkillNamesToIds(["NonExistent"] as Set)

        then:
        thrown(EntityNotFoundException)
    }

    def "updateCompany with UUID should update and return response DTO"() {
        given:
        CompanyUpdateDTO updateDTO = new CompanyUpdateDTO(
                "12345678000199", "Tech Corp Updated", null, null, null
        )
        Company updatedCompany = createCompany()
        updatedCompany.name = "Tech Corp Updated"

        repositoryMock.update(_, companyId) >> updatedCompany
        addressServiceMock.getAddressesByUserId(companyId) >> ([createAddressResponse()] as Set)

        when:
        CompanyResponseDTO result = service.updateCompany(updateDTO, companyId)

        then:
        result.name() == "Tech Corp Updated"
    }

    def "updateCompany with CNPJ should find company and update"() {
        given:
        Company existingCompany = createCompany()
        CompanyUpdateDTO updateDTO = new CompanyUpdateDTO(
                "12345678000199", "Tech Corp Updated", null, null, null
        )
        Company updatedCompany = createCompany()
        updatedCompany.name = "Tech Corp Updated"

        repositoryMock.findByCnpj("12345678000199") >> existingCompany
        repositoryMock.update(_, companyId) >> updatedCompany
        addressServiceMock.getAddressesByUserId(companyId) >> ([createAddressResponse()] as Set)

        when:
        CompanyResponseDTO result = service.updateCompany(updateDTO)

        then:
        result.name() == "Tech Corp Updated"
    }

    def "deleteCompany by UUID should call repository delete"() {
        when:
        service.deleteCompany(companyId)

        then:
        1 * repositoryMock.delete(companyId)
    }

    def "deleteCompany by CNPJ should find company and delete"() {
        given:
        Company company = createCompany()
        repositoryMock.findByCnpj("12345678000199") >> company

        when:
        service.deleteCompany("12345678000199")

        then:
        1 * repositoryMock.delete(companyId)
    }

    def "getCompanyByJobId should return company response DTO"() {
        given:
        UUID jobId = UUID.randomUUID()
        Company company = createCompany()
        repositoryMock.findByJobId(jobId) >> company
        addressServiceMock.getAddressesByUserId(companyId) >> ([createAddressResponse()] as Set)

        when:
        CompanyResponseDTO result = service.getCompanyByJobId(jobId)

        then:
        result.name() == "Tech Corp"
    }
}
