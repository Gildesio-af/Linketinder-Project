package service

import spock.lang.Specification
import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.match.MatchCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.repository.like.LikeRepository
import zg.acelera.service.CompanyService
import zg.acelera.service.JobLikeService
import zg.acelera.service.MatchService

class JobLikeServiceSpec extends Specification {

    LikeRepository jobLikeRepositoryMock
    LikeRepository companyLikeRepositoryMock
    CompanyService companyServiceMock
    MatchService matchServiceMock
    JobLikeService service

    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()

    def setup() {
        jobLikeRepositoryMock = Mock(LikeRepository)
        companyLikeRepositoryMock = Mock(LikeRepository)
        companyServiceMock = Mock(CompanyService)
        matchServiceMock = Mock(MatchService)
        service = new JobLikeService(jobLikeRepositoryMock, companyLikeRepositoryMock, companyServiceMock, matchServiceMock)
    }

    private CompanyResponseDTO createCompanyResponse() {
        return new CompanyResponseDTO(
                id: companyId, name: "Tech Corp", email: "tech@corp.com",
                description: "A tech company", cnpj: "12345678000199",
                addresses: [] as Set, skills: [] as Set
        )
    }

    def "likeJob should create a match when the company already liked the candidate"() {
        given:
        MatchResponseDTO match = new MatchResponseDTO(UUID.randomUUID(), candidateId, companyId, jobId, null)
        companyServiceMock.getCompanyByJobId(jobId) >> createCompanyResponse()

        when:
        MatchResponseDTO result = service.likeJob(jobId, candidateId)

        then:
        1 * jobLikeRepositoryMock.existsLike(jobId, candidateId) >> false
        1 * jobLikeRepositoryMock.like(jobId, candidateId)
        1 * companyLikeRepositoryMock.existsLike(candidateId, companyId) >> true
        1 * matchServiceMock.createMatch({ MatchCreateDTO dto ->
            dto.candidateId() == candidateId && dto.companyId() == companyId && dto.jobId() == jobId
        }) >> match
        result == match
    }

    def "likeJob should not create a match when the company did not like the candidate"() {
        given:
        companyServiceMock.getCompanyByJobId(jobId) >> createCompanyResponse()

        when:
        MatchResponseDTO result = service.likeJob(jobId, candidateId)

        then:
        1 * jobLikeRepositoryMock.existsLike(jobId, candidateId) >> false
        1 * jobLikeRepositoryMock.like(jobId, candidateId)
        1 * companyLikeRepositoryMock.existsLike(candidateId, companyId) >> false
        0 * matchServiceMock.createMatch(_)
        result == null
    }

    def "likeJob should not insert a duplicated like"() {
        given:
        companyServiceMock.getCompanyByJobId(jobId) >> createCompanyResponse()
        companyLikeRepositoryMock.existsLike(candidateId, companyId) >> false

        when:
        service.likeJob(jobId, candidateId)

        then:
        1 * jobLikeRepositoryMock.existsLike(jobId, candidateId) >> true
        0 * jobLikeRepositoryMock.like(_, _)
    }

    def "unlikeJob should remove the like and the match"() {
        when:
        service.unlikeJob(jobId, candidateId)

        then:
        1 * jobLikeRepositoryMock.unlike(jobId, candidateId)
        1 * matchServiceMock.deleteMatchByCandidateAndJob(candidateId, jobId)
    }
}
