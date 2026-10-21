package service

import spock.lang.Specification
import zg.acelera.domain.RecruiterJobLike
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.repository.like.LikeRepository
import zg.acelera.repository.recruiter.RecruiterJobLikeRepository
import zg.acelera.service.MatchService
import zg.acelera.service.RecruiterJobLikeService

class RecruiterJobLikeServiceSpec extends Specification {
    RecruiterJobLikeRepository recruiterLikeRepository = Mock()
    LikeRepository candidateLikeRepository = Mock()
    MatchService matchService = Mock()
    RecruiterJobLikeService service

    UUID recruiterId = UUID.randomUUID()
    UUID jobId = UUID.randomUUID()
    UUID candidateId = UUID.randomUUID()
    UUID companyId = UUID.randomUUID()

    def setup() {
        service = new RecruiterJobLikeService(recruiterLikeRepository, candidateLikeRepository, matchService)
    }

    private RecruiterJobLike activeLike() {
        new RecruiterJobLike(recruiterId: recruiterId, jobId: jobId, candidateId: candidateId)
    }

    def "rejects likes without an accepted assignment"() {
        when:
        service.like(recruiterId, jobId, candidateId)

        then:
        1 * recruiterLikeRepository.hasAcceptedAssignment(recruiterId, jobId) >> false
        0 * recruiterLikeRepository.like(_, _, _)
        thrown(SecurityException)
    }

    def "is idempotent and creates one match for reciprocal interest"() {
        given:
        recruiterLikeRepository.hasAcceptedAssignment(recruiterId, jobId) >> true
        recruiterLikeRepository.like(recruiterId, jobId, candidateId) >> activeLike()
        candidateLikeRepository.existsLike(jobId, candidateId) >> true
        recruiterLikeRepository.findCompanyIdByJobId(jobId) >> companyId
        MatchResponseDTO match = new MatchResponseDTO(UUID.randomUUID(), candidateId, companyId, jobId, null)

        when:
        def result = service.like(recruiterId, jobId, candidateId)

        then:
        result.status() == RecruiterJobLike.Status.ACTIVE
        result.match() == match
        1 * matchService.createMatch({ it.candidateId() == candidateId && it.companyId() == companyId }) >> match
    }

    def "keeps the match while another recruiter still has an active like"() {
        given:
        recruiterLikeRepository.hasAcceptedAssignment(recruiterId, jobId) >> true
        recruiterLikeRepository.unlike(recruiterId, jobId, candidateId) >> new RecruiterJobLike(
                recruiterId: recruiterId, jobId: jobId, candidateId: candidateId,
                removedAt: java.time.LocalDateTime.now())
        candidateLikeRepository.existsLike(jobId, candidateId) >> true
        recruiterLikeRepository.hasActiveLike(jobId, candidateId) >> true

        when:
        def result = service.unlike(recruiterId, jobId, candidateId)

        then:
        result.status() == RecruiterJobLike.Status.REMOVED
        1 * matchService.deleteMatchIfNoActiveRecruiterLike(candidateId, jobId)
    }

    def "removes the match after the last recruiter like is removed"() {
        given:
        recruiterLikeRepository.hasAcceptedAssignment(recruiterId, jobId) >> true
        recruiterLikeRepository.unlike(recruiterId, jobId, candidateId) >> new RecruiterJobLike(
                recruiterId: recruiterId, jobId: jobId, candidateId: candidateId,
                removedAt: java.time.LocalDateTime.now())
        candidateLikeRepository.existsLike(jobId, candidateId) >> true

        when:
        service.unlike(recruiterId, jobId, candidateId)

        then:
        1 * matchService.deleteMatchIfNoActiveRecruiterLike(candidateId, jobId)
    }
}
