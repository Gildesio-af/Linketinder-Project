package user_interface

import spock.lang.Specification
import zg.acelera.controller.CandidateController
import zg.acelera.dto.address.AddressCreateDTO
import zg.acelera.dto.address.AddressResponseDTO
import zg.acelera.dto.candidate.CandidateDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.candidate.CandidateUpdateDTO
import zg.acelera.dto.country.CountryDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.user_interface.CandidateJobUI
import zg.acelera.user_interface.CandidateUI
import zg.acelera.utils.reader.AddressConsoleReader
import zg.acelera.utils.reader.InputReader
import zg.acelera.utils.reader.SkillConsoleReader

import java.time.LocalDate

class CandidateUISpec extends Specification {

    CandidateController controllerMock
    CandidateJobUI jobUIMock
    InputReader inputMock
    AddressConsoleReader addressReaderMock
    SkillConsoleReader skillReaderMock
    CandidateUI ui

    UUID candidateId = UUID.randomUUID()
    UUID skillId = UUID.randomUUID()
    UUID countryId = UUID.randomUUID()

    def setup() {
        controllerMock = Mock(CandidateController)
        jobUIMock = Mock(CandidateJobUI)
        inputMock = Mock(InputReader)
        addressReaderMock = Mock(AddressConsoleReader)
        skillReaderMock = Mock(SkillConsoleReader)
        ui = new CandidateUI(controllerMock, jobUIMock, inputMock, addressReaderMock, skillReaderMock)
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

    def "registerCandidate should read inputs and call controller when data is valid"() {
        given:
        inputMock.readString("CPF (11 digits): ") >> "12345678900"
        inputMock.readString("Name: ") >> "John"
        inputMock.readString("Last Name: ") >> "Doe"
        inputMock.readString("E-mail: ") >> "john@email.com"
        inputMock.readString("Password (min 6 characters): ") >> "secret123"
        inputMock.readString("Description/Bio: ") >> "Developer"
        inputMock.readDate("Birth Date (dd/MM/yyyy): ") >> LocalDate.of(1995, 5, 15)

        skillReaderMock.getSkillsFromUser() >> (["Java"] as Set)
        controllerMock.resolveSkillNamesToIds(["Java"] as Set) >> ([skillId.toString()] as Set)

        AddressCreateDTO addressDTO = new AddressCreateDTO(
                cep: "12345678", street: "Main St", number: "100",
                city: "São Paulo", state: "SP", complement: null, neighborhood: "Downtown",
                countryId: countryId.toString()
        )

        addressReaderMock.readAddressData() >> addressDTO

        controllerMock.register(_, addressDTO) >> createCandidateResponse()

        when:
        ui.registerCandidate()

        then:
        1 * controllerMock.register({ CandidateDTO dto ->
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
        controllerMock.resolveSkillNamesToIds(["Java"] as Set) >> ([skillId.toString()] as Set)

        when:
        ui.registerCandidate()

        then:
        0 * controllerMock.register(_, _)
    }

    def "listAllCandidates should call controller and print candidates"() {
        given:
        controllerMock.listAll() >> [createCandidateResponse()]

        when:
        ui.listAllCandidates()

        then:
        1 * controllerMock.listAll()
    }

    def "listAllCandidates should handle empty list"() {
        given:
        controllerMock.listAll() >> []

        when:
        ui.listAllCandidates()

        then:
        1 * controllerMock.listAll()
    }

    def "searchCandidateByCpf should call controller with the provided CPF"() {
        given:
        inputMock.readString("Enter the CPF: ") >> "12345678900"
        controllerMock.findByCpf("12345678900") >> createCandidateResponse()

        when:
        ui.searchCandidateByCpf()

        then:
        1 * controllerMock.findByCpf("12345678900")
    }

    def "searchCandidatesBySkill should call controller with the provided skill"() {
        given:
        inputMock.readString("Enter the name of the skill (e.g. JAVA): ") >> "Java"
        controllerMock.findBySkill("Java") >> [createCandidateResponse()]

        when:
        ui.searchCandidatesBySkill()

        then:
        1 * controllerMock.findBySkill("Java")
    }

    def "deleteCandidate should call controller with the provided CPF"() {
        given:
        inputMock.readString("Enter the CPF of the candidate you want to delete: ") >> "12345678900"

        when:
        ui.deleteCandidate()

        then:
        1 * controllerMock.delete("12345678900")
    }

    def "updateCandidate should read inputs and call controller"() {
        given:
        inputMock.readString("Enter the CPF of the candidate you want to update: ") >> "12345678900"
        inputMock.readString("New Name: ", false) >> "John Updated"
        inputMock.readString("New Last Name: ", false) >> null
        inputMock.readString("New E-mail: ", false) >> null
        inputMock.readString("New Password: ", false) >> null
        inputMock.readString("New Description: ", false) >> null
        inputMock.readDate("New Birth Date (dd/MM/yyyy): ", false) >> null

        controllerMock.update(_) >> createCandidateResponse()

        when:
        ui.updateCandidate()

        then:
        1 * controllerMock.update({ CandidateUpdateDTO dto ->
            dto.cpf() == "12345678900" && dto.name() == "John Updated"
        })
    }
}
