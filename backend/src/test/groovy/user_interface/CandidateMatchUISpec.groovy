package user_interface

import spock.lang.Specification
import zg.acelera.controller.JobController
import zg.acelera.controller.LikeController
import zg.acelera.controller.MatchController
import zg.acelera.domain.Skill
import zg.acelera.dto.candidate.CandidateResponseDTO
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.job.JobMatchResponseDTO
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.like.LikeCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.user_interface.CandidateMatchUI
import zg.acelera.utils.reader.InputReader

import java.time.LocalDateTime

class CandidateMatchUISpec extends Specification {

    static final String MENU_PROMPT = "Choose an option: "
    static final String LIKE_PROMPT = "Enter the job ID you want to like: "
    static final String UNLIKE_PROMPT = "Enter the job ID you want to unlike: "

    JobController jobControllerMock
    LikeController likeControllerMock
    MatchController matchControllerMock
    InputReader inputMock
    CandidateMatchUI ui

    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()
    UUID matchId = UUID.randomUUID()

    CandidateResponseDTO candidate

    PrintStream originalOut
    ByteArrayOutputStream output

    def setup() {
        jobControllerMock = Mock(JobController)
        likeControllerMock = Mock(LikeController)
        matchControllerMock = Mock(MatchController)
        inputMock = Mock(InputReader)
        ui = new CandidateMatchUI(jobControllerMock, likeControllerMock, matchControllerMock, inputMock)

        candidate = new CandidateResponseDTO(
                id: candidateId, name: "John", lastName: "Doe", email: "john@email.com",
                description: "Java dev", cpf: "12345678901", birthDate: null,
                addresses: [] as Set, skills: [] as Set
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

    private JobResponseDTO createJobResponse() {
        return new JobResponseDTO(
                id: jobId, name: "Java Developer", description: "Develop Java apps",
                skills: [new Skill(id: UUID.randomUUID(), name: "JAVA")] as Set,
                addressResponseDTO: null
        )
    }

    private JobMatchResponseDTO createJobMatchResponse() {
        CompanyResponseDTO company = new CompanyResponseDTO(
                id: companyId, name: "Acme", email: "contact@acme.com", description: "Tech",
                cnpj: "12345678000199", addresses: [] as Set, skills: [] as Set
        )
        return new JobMatchResponseDTO(
                id: jobId, name: "Java Developer", description: "Develop Java apps",
                skills: [] as Set, addressResponseDTO: null, companyResponseDTO: company
        )
    }

    private MatchResponseDTO createMatchResponse() {
        return new MatchResponseDTO(matchId, candidateId, companyId, jobId, LocalDateTime.of(2026, 1, 1, 10, 0))
    }

    def "start should greet the candidate and logout when option 0 is selected"() {
        given:
        inputMock.readString(MENU_PROMPT) >> "0"

        when:
        ui.start(candidate)

        then:
        printed().contains("Welcome, John!")
        printed().contains("Logging out...")
        0 * jobControllerMock._
        0 * likeControllerMock._
        0 * matchControllerMock._
    }

    def "should print invalid option message for unknown option"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["9", "0"]

        when:
        ui.start(candidate)

        then:
        printed().contains("Invalid option. Please try again.")
    }

    def "option 1 should list available jobs with skills"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["1", "0"]

        when:
        ui.start(candidate)

        then:
        1 * jobControllerMock.getAllJobs() >> ([createJobResponse()] as Set)
        printed().contains("[ID: ${jobId}] - Java Developer | Skills: JAVA | Description: Develop Java apps")
    }

    def "option 1 should inform when there are no jobs"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["1", "0"]

        when:
        ui.start(candidate)

        then:
        1 * jobControllerMock.getAllJobs() >> ([] as Set)
        printed().contains("No jobs available at the moment.")
    }

    def "option 2 should like the job and announce a match when one is created"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["2", "0"]
        inputMock.readUUID(LIKE_PROMPT) >> jobId

        when:
        ui.start(candidate)

        then:
        1 * likeControllerMock.likeJob({ LikeCreateDTO dto ->
            dto.entityLikedId() == jobId && dto.userId() == candidateId
        }) >> createMatchResponse()
        printed().contains("Job liked successfully.")
        printed().contains("IT'S A MATCH!")
    }

    def "option 2 should like the job without announcing a match when none is created"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["2", "0"]
        inputMock.readUUID(LIKE_PROMPT) >> jobId

        when:
        ui.start(candidate)

        then:
        1 * likeControllerMock.likeJob(_ as LikeCreateDTO) >> null
        printed().contains("Job liked successfully.")
        !printed().contains("IT'S A MATCH!")
    }

    def "option 2 should do nothing when no job id is provided"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["2", "0"]
        inputMock.readUUID(LIKE_PROMPT) >> null

        when:
        ui.start(candidate)

        then:
        0 * likeControllerMock.likeJob(_)
    }

    def "option 2 should print action failed when like throws IllegalArgumentException"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["2", "0"]
        inputMock.readUUID(LIKE_PROMPT) >> jobId
        likeControllerMock.likeJob(_ as LikeCreateDTO) >> { throw new IllegalArgumentException("Job already liked") }

        when:
        ui.start(candidate)

        then:
        printed().contains("Action failed: Job already liked")
        printed().contains("Logging out...")
    }

    def "option 3 should unlike the job"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["3", "0"]
        inputMock.readUUID(UNLIKE_PROMPT) >> jobId

        when:
        ui.start(candidate)

        then:
        1 * likeControllerMock.unlikeJob({ LikeCreateDTO dto ->
            dto.entityLikedId() == jobId && dto.userId() == candidateId
        })
        printed().contains("Job unliked successfully.")
    }

    def "option 3 should do nothing when no job id is provided"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["3", "0"]
        inputMock.readUUID(UNLIKE_PROMPT) >> null

        when:
        ui.start(candidate)

        then:
        0 * likeControllerMock.unlikeJob(_)
    }

    def "option 4 should show the candidate matches with company info"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["4", "0"]

        when:
        ui.start(candidate)

        then:
        1 * matchControllerMock.getMatchesByCandidate(candidateId) >> [createMatchResponse()]
        1 * jobControllerMock.getJobByIdToMatch(jobId) >> createJobMatchResponse()
        printed().contains("Job: Java Developer | Company: Acme (contact@acme.com)")
    }

    def "option 4 should inform when the candidate has no matches"() {
        given:
        inputMock.readString(MENU_PROMPT) >>> ["4", "0"]

        when:
        ui.start(candidate)

        then:
        1 * matchControllerMock.getMatchesByCandidate(candidateId) >> []
        0 * jobControllerMock.getJobByIdToMatch(_)
        printed().contains("You don't have any matches yet.")
    }
}
