package user_interface

import spock.lang.Specification
import zg.acelera.controller.CandidateController
import zg.acelera.controller.JobController
import zg.acelera.controller.LikeController
import zg.acelera.controller.MatchController
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.job.JobMatchResponseDTO
import zg.acelera.dto.like.LikeCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.user_interface.CompanyMatchUI
import zg.acelera.utils.reader.InputReader

import java.time.LocalDateTime

class CompanyMatchUISpec extends Specification {

    static final String MENU_PROMPT = "Choose an option: "
    static final String LIKE_PROMPT = "Enter the CPF of the candidate you want to like: "
    static final String UNLIKE_PROMPT = "Enter the CPF of the candidate you want to unlike: "

    CandidateController candidateControllerMock
    JobController jobControllerMock
    LikeController likeControllerMock
    MatchController matchControllerMock
    InputReader inputMock
    CompanyMatchUI ui

    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()
    String cpf = "12345678901"

    CompanyResponseDTO company
    CandidateResponseDTO candidate

    PrintStream originalOut
    ByteArrayOutputStream output

    def setup() {
        candidateControllerMock = Mock(CandidateController)
        jobControllerMock = Mock(JobController)
        likeControllerMock = Mock(LikeController)
        matchControllerMock = Mock(MatchController)
        inputMock = Mock(InputReader)
        ui = new CompanyMatchUI(candidateControllerMock, jobControllerMock, likeControllerMock, matchControllerMock, inputMock)

        company = new CompanyResponseDTO(
                id: companyId, name: "Acme", email: "contact@acme.com", description: "Tech",
                cnpj: "12345678000199", addresses: [] as Set, skills: [] as Set
        )
        candidate = new CandidateResponseDTO(
                id: candidateId, name: "John", lastName: "Doe", email: "john@email.com",
                description: "Java dev", cpf: cpf, birthDate: null, addresses: [] as Set,
                skills: [new SkillResponseDTO(UUID.randomUUID(), "JAVA")] as Set
        )

        originalOut = System.out
        output = new ByteArrayOutputStream()
        System.out = new PrintStream(output, true)
    }

    def cleanup() {
        System.out = originalOut
    }

    private String printed() {
        return output.toString()
    }

    private MatchResponseDTO createMatchResponse() {
        return new MatchResponseDTO(UUID.randomUUID(), candidateId, companyId, jobId, LocalDateTime.of(2026, 1, 1, 10, 0))
    }

    private JobMatchResponseDTO createJobMatchResponse() {
        return new JobMatchResponseDTO(
                id: jobId, name: "Java Developer", description: "Develop Java apps",
                skills: [] as Set, addressResponseDTO: null, companyResponseDTO: company
        )
    }

    def "start should greet the company and logout when option 0 is selected"() {
        given:
        inputMock.readString(MENU_PROMPT) >> "0"

        when:
        ui.start(company)

        then:
        printed().contains("Welcome, Acme!")
        printed().contains("Logging out...")
        0 * candidateControllerMock._
        0 * likeControllerMock._
        0 * matchControllerMock._
    }

    def "should print invalid option message for unknown option"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["x", "0"]

        when:
        ui.start(company)

        then:
        printed().contains("Invalid option. Please try again.")
    }

    def "option 1 should list candidates anonymously with skills"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["1", "0"]

        when:
        ui.start(company)

        then:
        1 * candidateControllerMock.listAll() >> [candidate]
        printed().contains("[CPF: ${cpf}] - Skills: JAVA | Description: Java dev")
        !printed().contains("John")
    }

    def "option 1 should inform when there are no candidates"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["1", "0"]

        when:
        ui.start(company)

        then:
        1 * candidateControllerMock.listAll() >> []
        printed().contains("No candidates available at the moment.")
    }

    def "option 2 should like the candidate and announce matches"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["2", "0"]
        inputMock.readString(LIKE_PROMPT) >> cpf
        candidateControllerMock.findByCpf(cpf) >> candidate

        when:
        ui.start(company)

        then:
        1 * likeControllerMock.likeCandidate({ LikeCreateDTO dto ->
            dto.entityLikedId() == candidateId && dto.userId() == companyId
        }) >> [createMatchResponse(), createMatchResponse()]
        printed().contains("Candidate liked successfully.")
        printed().contains("IT'S A MATCH! 2 new match(es) with your jobs.")
    }

    def "option 2 should like the candidate without announcing when there are no matches"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["2", "0"]
        inputMock.readString(LIKE_PROMPT) >> cpf
        candidateControllerMock.findByCpf(cpf) >> candidate

        when:
        ui.start(company)

        then:
        1 * likeControllerMock.likeCandidate(_ as LikeCreateDTO) >> matches
        printed().contains("Candidate liked successfully.")
        !printed().contains("IT'S A MATCH!")

        where:
        matches << [[], null]
    }

    def "option 2 should print action failed when like throws IllegalArgumentException"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["2", "0"]
        inputMock.readString(LIKE_PROMPT) >> cpf
        candidateControllerMock.findByCpf(cpf) >> candidate
        likeControllerMock.likeCandidate(_ as LikeCreateDTO) >> { throw new IllegalArgumentException("Already liked") }

        when:
        ui.start(company)

        then:
        printed().contains("Action failed: Already liked")
        printed().contains("Logging out...")
    }

    def "option 3 should unlike the candidate"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["3", "0"]
        inputMock.readString(UNLIKE_PROMPT) >> cpf
        candidateControllerMock.findByCpf(cpf) >> candidate

        when:
        ui.start(company)

        then:
        1 * likeControllerMock.unlikeCandidate({ LikeCreateDTO dto ->
            dto.entityLikedId() == candidateId && dto.userId() == companyId
        })
        printed().contains("Candidate unliked successfully.")
    }

    def "option 4 should show company matches with candidate and job info"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["4", "0"]

        when:
        ui.start(company)

        then:
        1 * matchControllerMock.getMatchesByCompany(companyId) >> [createMatchResponse()]
        1 * candidateControllerMock.findById(candidateId) >> candidate
        1 * jobControllerMock.getJobByIdToMatch(jobId) >> createJobMatchResponse()
        printed().contains("Candidate: John Doe (john@email.com) | Job: Java Developer")
    }

    def "option 4 should inform when the company has no matches"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["4", "0"]

        when:
        ui.start(company)

        then:
        1 * matchControllerMock.getMatchesByCompany(companyId) >> []
        0 * candidateControllerMock.findById(_)
        printed().contains("You don't have any matches yet.")
    }
}
