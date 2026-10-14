package service

import spock.lang.Specification
import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.match.MatchCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.repository.like.LikeRepository
import zg.acelera.service.CompanyLikeService
import zg.acelera.service.JobService
import zg.acelera.service.MatchService

class CompanyLikeServiceSpec extends Specification {

    LikeRepository companyLikeRepositoryMock
    LikeRepository jobLikeRepositoryMock
    JobService jobServiceMock
    MatchService matchServiceMock
    CompanyLikeService service

    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()
    UUID likedJobId = UUID.randomUUID()
    UUID notLikedJobId = UUID.randomUUID()

    def setup() {
        companyLikeRepositoryMock = Mock(LikeRepository)
        jobLikeRepositoryMock = Mock(LikeRepository)
        jobServiceMock = Mock(JobService)
        matchServiceMock = Mock(MatchService)
        service = new CompanyLikeService(companyLikeRepositoryMock, jobLikeRepositoryMock, jobServiceMock, matchServiceMock)
    }

    private JobResponseDTO createJobResponse(UUID id) {
        return new JobResponseDTO(id: id, name: "Job", description: "Desc", skills: [] as Set, addressResponseDTO: null)
    }

    def "likeCandidate should create matches only for jobs the candidate liked"() {
        given:
        MatchResponseDTO match = new MatchResponseDTO(UUID.randomUUID(), candidateId, companyId, likedJobId, null)
        jobServiceMock.getJobsByPublisher(companyId) >> ([createJobResponse(likedJobId), createJobResponse(notLikedJobId)] as Set)
        jobLikeRepositoryMock.existsLike(likedJobId, candidateId) >> true
        jobLikeRepositoryMock.existsLike(notLikedJobId, candidateId) >> false

        when:
        List<MatchResponseDTO> result = service.likeCandidate(companyId, candidateId)

        then:
        1 * companyLikeRepositoryMock.existsLike(candidateId, companyId) >> false
        1 * companyLikeRepositoryMock.like(candidateId, companyId)
        1 * matchServiceMock.createMatch({ MatchCreateDTO dto ->
            dto.candidateId() == candidateId && dto.companyId() == companyId && dto.jobId() == likedJobId
        }) >> match
        0 * matchServiceMock.createMatch({ MatchCreateDTO dto -> dto.jobId() == notLikedJobId })
        result == [match]
    }

    def "likeCandidate should return empty list when the candidate did not like any job"() {
        given:
        companyLikeRepositoryMock.existsLike(candidateId, companyId) >> false
        jobServiceMock.getJobsByPublisher(companyId) >> ([createJobResponse(likedJobId)] as Set)
        jobLikeRepositoryMock.existsLike(likedJobId, candidateId) >> false

        when:
        List<MatchResponseDTO> result = service.likeCandidate(companyId, candidateId)

        then:
        0 * matchServiceMock.createMatch(_)
        result.isEmpty()
    }

    def "unlikeCandidate should remove the like and all matches between company and candidate"() {
        when:
        service.unlikeCandidate(companyId, candidateId)

        then:
        1 * companyLikeRepositoryMock.unlike(candidateId, companyId)
        1 * matchServiceMock.deleteMatchesByCandidateAndCompany(candidateId, companyId)
    }
}
