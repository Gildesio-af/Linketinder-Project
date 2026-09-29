package user_interface

import spock.lang.Specification
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.service.CandidateService
import zg.acelera.user_interface.CandidateJobUI
import zg.acelera.user_interface.CandidateUI
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.utils.reader.SkillConsoleReader

import java.time.LocalDate

class CandidateUISpec extends Specification {

    CandidateService serviceMock
    CandidateJobUI jobUIMock
    InputReader inputMock
    AddressConsoleReader addressReaderMock
    SkillConsoleReader skillReaderMock
    CandidateUI ui

    UUID candidateId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()
    UUID countryId = UUID.randomUUID()

    def setup() {
        serviceMock = Mock(CandidateService)
        jobUIMock = Mock(CandidateJobUI)
        inputMock = Mock(InputReader)
        addressReaderMock = Mock(AddressConsoleReader)
        skillReaderMock = Mock(SkillConsoleReader)
        ui = new CandidateUI(serviceMock, jobUIMock, inputMock, addressReaderMock, skillReaderMock)
    }

    private CandidateResponseDTO createCandidateResponse() {
        return new CandidateResponseDTO(
                id: candidateId, name: "John", lastName: "Doe",
                email: "john@email.com", description: "Developer",
                cpf: "12345678900", birthDate: LocalDate.of(1995, 5, 15),
                addresses: [] as Set,
                skills: [new SkillResponseDTO(id: skillId, name: "Java")] as Set
        )
    }

    def "registerCandidate should read inputs and call service when data is valid"() {
        given:
        inputMock.readString("CPF (11 digits): ") >> "12345678900"
        inputMock.readString("Name: ") >> "John"
        inputMock.readString("Last Name: ") >> "Doe"
        inputMock.readString("E-mail: ") >> "john@email.com"
        inputMock.readString("Password (min 6 characters): ") >> "secret123"
        inputMock.readString("Description/Bio: ") >> "Developer"
        inputMock.readDate("Birth Date (dd/MM/yyyy): ") >> LocalDate.of(1995, 5, 15)

        skillReaderMock.getSkillsFromUser() >> (["Java"] as Set)
        serviceMock.resolveSkillNamesToIds(["Java"] as Set) >> ([skillId.toString()] as Set)

        AddressCreateDTO addressDTO = new AddressCreateDTO(
                cep: "12345678", street: "Main St", number: "100",
                city: "São Paulo", state: "SP", complement: null, neighborhood: "Downtown",
                countryId: countryId.toString()
        )

        addressReaderMock.readAddressData() >> addressDTO

        serviceMock.registerCandidate(_, addressDTO) >> createCandidateResponse()

        when:
        ui.registerCandidate()

        then:
        1 * serviceMock.registerCandidate({ CandidateDTO dto ->
            dto.cpf() == "12345678900" &&
                    dto.name() == "John" &&
                    dto.lastName() == "Doe" &&
                    dto.email() == "john@email.com"
        }, addressDTO)
    }

    def "registerCandidate should handle validation error gracefully"() {
        given:
        inputMock.readString("CPF (11 digits): ") >> "123"
        inputMock.readString("Name: ") >> "John"
        inputMock.readString("Last Name: ") >> "Doe"
        inputMock.readString("E-mail: ") >> "john@email.com"
        inputMock.readString("Password (min 6 characters): ") >> "secret123"
        inputMock.readString("Description/Bio: ") >> "Developer"
        inputMock.readDate("Birth Date (dd/MM/yyyy): ") >> LocalDate.of(1995, 5, 15)

        skillReaderMock.getSkillsFromUser() >> (["Java"] as Set)
        serviceMock.resolveSkillNamesToIds(["Java"] as Set) >> ([skillId.toString()] as Set)

        when:
        ui.registerCandidate()

        then:
        0 * serviceMock.registerCandidate(_, _)
    }

    def "listAllCandidates should call service and print candidates"() {
        given:
        serviceMock.listAllCandidates() >> [createCandidateResponse()]

        when:
        ui.listAllCandidates()

        then:
        1 * serviceMock.listAllCandidates()
    }

    def "listAllCandidates should handle empty list"() {
        given:
        serviceMock.listAllCandidates() >> []

        when:
        ui.listAllCandidates()

        then:
        1 * serviceMock.listAllCandidates()
    }

    def "searchCandidateByCpf should call service with the provided CPF"() {
        given:
        inputMock.readString("Enter the CPF: ") >> "12345678900"
        serviceMock.listCandidateByCpf("12345678900") >> createCandidateResponse()

        when:
        ui.searchCandidateByCpf()

        then:
        1 * serviceMock.listCandidateByCpf("12345678900")
    }

    def "searchCandidatesBySkill should call service with the provided skill"() {
        given:
        inputMock.readString("Enter the name of the skill (e.g. JAVA): ") >> "Java"
        serviceMock.listCandidatesBySkill("Java") >> [createCandidateResponse()]

        when:
        ui.searchCandidatesBySkill()

        then:
        1 * serviceMock.listCandidatesBySkill("Java")
    }

    def "deleteCandidate should call service with the provided CPF"() {
        given:
        inputMock.readString("Enter the CPF of the candidate you want to delete: ") >> "12345678900"

        when:
        ui.deleteCandidate()

        then:
        1 * serviceMock.deleteCandidate("12345678900")
    }

    def "updateCandidate should read inputs and call service"() {
        given:
        inputMock.readString("Enter the CPF of the candidate you want to update: ") >> "12345678900"
        inputMock.readString("New Name: ", false) >> "John Updated"
        inputMock.readString("New Last Name: ", false) >> null
        inputMock.readString("New E-mail: ", false) >> null
        inputMock.readString("New Password: ", false) >> null
        inputMock.readString("New Description: ", false) >> null
        inputMock.readDate("New Birth Date (dd/MM/yyyy): ", false) >> null

        serviceMock.updateCandidate(_) >> createCandidateResponse()

        when:
        ui.updateCandidate()

        then:
        1 * serviceMock.updateCandidate({ CandidateUpdateDTO dto ->
            dto.cpf() == "12345678900" && dto.name() == "John Updated"
        })
    }
}
