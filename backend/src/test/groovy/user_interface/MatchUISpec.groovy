package user_interface

import spock.lang.Specification
import zg.acelera.controller.CandidateController
import zg.acelera.controller.CompanyController
import zg.acelera.dto.auth.AuthRequestDTO
import zg.acelera.dto.auth.AuthResponseDTO
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.service.AuthService
import zg.acelera.user_interface.CandidateMatchUI
import zg.acelera.user_interface.CompanyMatchUI
import zg.acelera.user_interface.MatchUI
import zg.acelera.utils.reader.InputReader

class MatchUISpec extends Specification {

    AuthService authServiceMock
    CandidateController candidateControllerMock
    CompanyController companyControllerMock
    CandidateMatchUI candidateUIMock
    CompanyMatchUI companyUIMock
    InputReader inputMock
    MatchUI ui

    String cpf = "12345678901"
    String cnpj = "12345678000199"

    PrintStream originalOut
    PrintStream originalErr
    ByteArrayOutputStream output

    def setup() {
        authServiceMock = Mock(AuthService)
        candidateControllerMock = Mock(CandidateController)
        companyControllerMock = Mock(CompanyController)
        candidateUIMock = Mock(CandidateMatchUI)
        companyUIMock = Mock(CompanyMatchUI)
        inputMock = Mock(InputReader)
        ui = new MatchUI(authServiceMock, candidateControllerMock, companyControllerMock,
                candidateUIMock, companyUIMock, inputMock)

        originalOut = System.out
        originalErr = System.err
        output = new ByteArrayOutputStream()
        System.out = new PrintStream(output, true)
        System.err = new PrintStream(new ByteArrayOutputStream(), true)
    }

    def cleanup() {
        System.out = originalOut
        System.err = originalErr
    }

    private String printed() {
        return output.toString()
    }

    def "showMenu should start candidate UI when authenticated user is a candidate"() {
        given:
        AuthRequestDTO authDTO = new AuthRequestDTO(cpf, "123")
        CandidateResponseDTO candidate = new CandidateResponseDTO(
                id: UUID.randomUUID(), name: "John", lastName: "Doe", email: "john@email.com",
                description: null, cpf: cpf, birthDate: null, addresses: [] as Set, skills: [] as Set
        )
        inputMock.readAuthDTO() >> authDTO

        when:
        ui.showMenu()

        then:
        1 * authServiceMock.authenticate(authDTO) >> new AuthResponseDTO(candidate.id(), "CANDIDATE")
        1 * candidateControllerMock.findByCpf(cpf) >> candidate
        1 * candidateUIMock.start(candidate)
        0 * companyControllerMock._
        0 * companyUIMock._
    }

    def "showMenu should start company UI when authenticated user is a company"() {
        given:
        AuthRequestDTO authDTO = new AuthRequestDTO(cnpj, "123")
        CompanyResponseDTO company = new CompanyResponseDTO(
                id: UUID.randomUUID(), name: "Acme", email: "contact@acme.com", description: null,
                cnpj: cnpj, addresses: [] as Set, skills: [] as Set
        )
        inputMock.readAuthDTO() >> authDTO

        when:
        ui.showMenu()

        then:
        1 * authServiceMock.authenticate(authDTO) >> new AuthResponseDTO(company.id(), "COMPANY")
        1 * companyControllerMock.findByCnpj(cnpj) >> company
        1 * companyUIMock.start(company)
        0 * candidateControllerMock._
        0 * candidateUIMock._
    }

    def "showMenu should print invalid role message when role is unknown"() {
        given:
        AuthRequestDTO authDTO = new AuthRequestDTO(cpf, "123")
        inputMock.readAuthDTO() >> authDTO
        authServiceMock.authenticate(authDTO) >> new AuthResponseDTO(UUID.randomUUID(), "ADMIN")

        when:
        ui.showMenu()

        then:
        printed().contains("Invalid format or role.")
        0 * candidateUIMock.start(_)
        0 * companyUIMock.start(_)
    }

    def "showMenu should print validation error when authentication fails"() {
        given:
        AuthRequestDTO authDTO = new AuthRequestDTO(cpf, "wrong")
        inputMock.readAuthDTO() >> authDTO
        authServiceMock.authenticate(authDTO) >> { throw new IllegalArgumentException("Invalid username or password") }

        when:
        ui.showMenu()

        then:
        notThrown(Exception)
        printed().contains("Validation Error: Invalid username or password")
        0 * candidateUIMock.start(_)
        0 * companyUIMock.start(_)
    }

    def "showMenu should handle unexpected exceptions without propagating them"() {
        given:
        AuthRequestDTO authDTO = new AuthRequestDTO(cpf, "123")
        inputMock.readAuthDTO() >> authDTO
        authServiceMock.authenticate(authDTO) >> new AuthResponseDTO(UUID.randomUUID(), "CANDIDATE")
        candidateControllerMock.findByCpf(cpf) >> { throw new RuntimeException("DB down") }

        when:
        ui.showMenu()

        then:
        notThrown(Exception)
        printed().contains("Unexpected Error occurred:")
        0 * candidateUIMock.start(_)
    }
}
