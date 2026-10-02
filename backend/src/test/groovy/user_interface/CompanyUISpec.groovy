package user_interface

import spock.lang.Specification
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.company.CompanyCreateDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.company.CompanyUpdateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.service.CompanyService
import zg.acelera.user_interface.CompanyJobUI
import zg.acelera.user_interface.CompanyUI
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.utils.reader.SkillConsoleReader

class CompanyUISpec extends Specification {

    CompanyService serviceMock
    CompanyJobUI jobUIMock
    InputReader inputMock
    AddressConsoleReader addressReaderMock
    SkillConsoleReader skillReaderMock
    CompanyUI ui

    UUID companyId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()

    def setup() {
        serviceMock = Mock(CompanyService)
        jobUIMock = Mock(CompanyJobUI)
        inputMock = Mock(InputReader)
        addressReaderMock = Mock(AddressConsoleReader)
        skillReaderMock = Mock(SkillConsoleReader)
        ui = new CompanyUI(serviceMock, jobUIMock, inputMock, addressReaderMock, skillReaderMock)
    }

    private CompanyResponseDTO createCompanyResponse() {
        return new CompanyResponseDTO(
                id: companyId, name: "Tech Corp", email: "tech@corp.com",
                description: "A tech company", cnpj: "12345678000199",
                addresses: [] as Set,
                skills: [new SkillResponseDTO(id: skillId, name: "Java")] as Set
        )
    }

    def "registerCompany should read inputs and call service when data is valid"() {
        given:
        inputMock.readString("CNPJ (14 digits, without punctuation): ") >> "12345678000199"
        inputMock.readString("Name: ") >> "Tech Corp"
        inputMock.readString("Corporate E-mail: ") >> "tech@corp.com"
        inputMock.readString("Password (min 6 characters): ") >> "secret123"
        inputMock.readString("Description/Bio: ") >> "A tech company"

        skillReaderMock.getSkillsFromUser() >> (["Java"] as Set)
        serviceMock.resolveSkillNamesToIds(["Java"] as Set) >> ([skillId.toString()] as Set)

        AddressCreateDTO addressDTO = new AddressCreateDTO(
                cep: "12345678", street: "Main St", number: "100",
                city: "São Paulo", state: "SP", complement: null, neighborhood: "Downtown",
                countryId: UUID.randomUUID()
        )
        addressReaderMock.readAddressData() >> addressDTO

        serviceMock.registerCompany(_, addressDTO) >> createCompanyResponse()

        when:
        ui.registerCompany()

        then:
        1 * serviceMock.registerCompany({ CompanyCreateDTO dto ->
            dto.cnpj() == "12345678000199" &&
                    dto.name() == "Tech Corp" &&
                    dto.email() == "tech@corp.com"
        }, addressDTO)
    }

    def "registerCompany should handle validation error for CNPJ gracefully"() {
        given:
        inputMock.readString("CNPJ (14 digits, without punctuation): ") >> "123"
        inputMock.readString("Name: ") >> "Tech Corp"
        inputMock.readString("Corporate E-mail: ") >> "tech@corp.com"
        inputMock.readString("Password (min 6 characters): ") >> "secret123"
        inputMock.readString("Description/Bio: ") >> "A tech company"

        skillReaderMock.getSkillsFromUser() >> (["Java"] as Set)
        serviceMock.resolveSkillNamesToIds(["Java"] as Set) >> ([skillId.toString()] as Set)

        when:
        ui.registerCompany()

        then:
        0 * serviceMock.registerCompany(_, _)
    }

    def "registerCompany should handle empty skills gracefully"() {
        given:
        inputMock.readString("CNPJ (14 digits, without punctuation): ") >> "12345678000199"
        inputMock.readString("Name: ") >> "Tech Corp"
        inputMock.readString("Corporate E-mail: ") >> "tech@corp.com"
        inputMock.readString("Password (min 6 characters): ") >> "secret123"
        inputMock.readString("Description/Bio: ") >> "A tech company"

        skillReaderMock.getSkillsFromUser() >> ([] as Set)

        when:
        ui.registerCompany()

        then:
        0 * serviceMock.registerCompany(_, _)
    }

    def "listAllCompanies should call service and print companies"() {
        given:
        serviceMock.listAllCompanies() >> [createCompanyResponse()]

        when:
        ui.listAllCompanies()

        then:
        1 * serviceMock.listAllCompanies()
    }

    def "listAllCompanies should handle empty list"() {
        given:
        serviceMock.listAllCompanies() >> []

        when:
        ui.listAllCompanies()

        then:
        1 * serviceMock.listAllCompanies()
    }

    def "searchCompanyByCnpj should call service with the provided CNPJ"() {
        given:
        inputMock.readString("Enter the CNPJ: ") >> "12345678000199"
        serviceMock.listCompanyByCnpj("12345678000199") >> createCompanyResponse()

        when:
        ui.searchCompanyByCnpj()

        then:
        1 * serviceMock.listCompanyByCnpj("12345678000199")
    }

    def "searchCompaniesBySkill should call service with the provided skill"() {
        given:
        inputMock.readString("Enter the name of the skill (e.g. JAVA): ") >> "Java"
        serviceMock.listCompaniesBySkill("Java") >> [createCompanyResponse()]

        when:
        ui.searchCompaniesBySkill()

        then:
        1 * serviceMock.listCompaniesBySkill("Java")
    }

    def "deleteCompany should call service with the provided CNPJ"() {
        given:
        inputMock.readString("Enter the CNPJ of the company you want to delete: ") >> "12345678000199"

        when:
        ui.deleteCompany()

        then:
        1 * serviceMock.deleteCompany("12345678000199")
    }

    def "updateCompany should read inputs and call service"() {
        given:
        inputMock.readString("Enter the CNPJ of the company you want to update: ") >> "12345678000199"
        inputMock.readString("New Name: ", false) >> "Tech Corp Updated"
        inputMock.readString("New Corporate E-mail: ", false) >> null
        inputMock.readString("New Password: ", false) >> null
        inputMock.readString("New Description: ", false) >> null

        serviceMock.updateCompany(_) >> createCompanyResponse()

        when:
        ui.updateCompany()

        then:
        1 * serviceMock.updateCompany({ CompanyUpdateDTO dto ->
            dto.cnpj() == "12345678000199" && dto.name() == "Tech Corp Updated"
        })
    }
}
