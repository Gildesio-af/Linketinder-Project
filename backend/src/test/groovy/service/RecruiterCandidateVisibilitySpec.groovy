package service

import spock.lang.Specification
import zg.acelera.dto.candidate.AnonymousCandidateResponseDTO
import zg.acelera.dto.job.RecruiterJobResponseDTO
import zg.acelera.dto.skill.SkillResponseDTO
import zg.acelera.repository.candidate.CandidateRepository
import zg.acelera.service.AddressService
import zg.acelera.service.CandidateService
import zg.acelera.service.SkillService

class RecruiterCandidateVisibilitySpec extends Specification {

    CandidateRepository repository
    CandidateService service
    UUID recruiterId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()

    def setup() {
        repository = Mock(CandidateRepository)
        service = new CandidateService(repository, Mock(AddressService), Mock(SkillService))
    }

    def "lists only accepted jobs using the anonymous job projection"() {
        given:
        def job = new RecruiterJobResponseDTO(
                jobId, "Backend Developer", "Build APIs",
                [new SkillResponseDTO(UUID.randomUUID(), "Groovy")] as Set,
                "São Paulo", "SP", "Brazil")
        repository.findAcceptedJobsByRecruiterId(recruiterId) >> [job]

        when:
        List<RecruiterJobResponseDTO> result = service.listAcceptedJobsForRecruiter(recruiterId)

        then:
        result == [job]
        result[0].name() == "Backend Developer"
        result[0].city() == "São Paulo"
        0 * repository.findAnonymousCandidatesByJobId(_)
    }

    def "rejects candidate visibility when the recruiter has no accepted link"() {
        given:
        repository.hasAcceptedJob(recruiterId, jobId) >> false

        when:
        service.listAnonymousCandidatesForRecruiterJob(recruiterId, jobId)

        then:
        thrown(SecurityException)
        0 * repository.findAnonymousCandidatesByJobId(_)
    }

    def "returns only description and skills for an authorized job"() {
        given:
        def candidate = new AnonymousCandidateResponseDTO(
                "Anonymous experience", [new SkillResponseDTO(UUID.randomUUID(), "Java")] as Set)
        repository.hasAcceptedJob(recruiterId, jobId) >> true
        repository.findAnonymousCandidatesByJobId(jobId) >> [candidate]

        when:
        List<AnonymousCandidateResponseDTO> result =
                service.listAnonymousCandidatesForRecruiterJob(recruiterId, jobId)

        then:
        result == [candidate]
        result[0].description() == "Anonymous experience"
        result[0].skills()*.name() == ["Java"]
        !result[0].properties.containsKey("name")
        !result[0].properties.containsKey("email")
        !result[0].properties.containsKey("cpf")
        !result[0].properties.containsKey("birthDate")
        !result[0].properties.containsKey("addresses")
    }

    def "rejects missing recruiter or job before querying"() {
        when:
        service.listAnonymousCandidatesForRecruiterJob(recruiterId, null)

        then:
        thrown(IllegalArgumentException)
        0 * repository._

        when:
        service.listAcceptedJobsForRecruiter(null)

        then:
        thrown(SecurityException)
        0 * repository._
    }
}
