package zg.acelera.service

import zg.acelera.dto.recruiter.RecruiterLikeResultDTO
import zg.acelera.repository.like.LikeRepository
import zg.acelera.repository.recruiter.RecruiterJobLikeRepository

class RecruiterJobLikeService {
    private final RecruiterJobLikeRepository recruiterLikeRepository
    private final LikeRepository candidateLikeRepository
    private final MatchService matchService

    RecruiterJobLikeService(RecruiterJobLikeRepository recruiterLikeRepository,
                             LikeRepository candidateLikeRepository,
                             MatchService matchService) {
        this.recruiterLikeRepository = recruiterLikeRepository
        this.candidateLikeRepository = candidateLikeRepository
        this.matchService = matchService
    }

    RecruiterLikeResultDTO like(UUID recruiterId, UUID jobId, UUID candidateId) {
        requireIds(recruiterId, jobId, candidateId)
        requireAcceptedAssignment(recruiterId, jobId)

        def like = recruiterLikeRepository.like(recruiterId, jobId, candidateId)
        def match = null
        if (candidateLikeRepository.existsLike(jobId, candidateId)) {
            match = matchService.createMatch(new zg.acelera.dto.match.MatchCreateDTO(
                    candidateId, recruiterLikeRepository.findCompanyIdByJobId(jobId), jobId, null))
        }
        RecruiterLikeResultDTO.fromDomain(like, match)
    }

    RecruiterLikeResultDTO unlike(UUID recruiterId, UUID jobId, UUID candidateId) {
        requireIds(recruiterId, jobId, candidateId)
        requireAcceptedAssignment(recruiterId, jobId)
        def like = recruiterLikeRepository.unlike(recruiterId, jobId, candidateId)
        if (candidateLikeRepository.existsLike(jobId, candidateId))
            matchService.deleteMatchIfNoActiveRecruiterLike(candidateId, jobId)
        RecruiterLikeResultDTO.fromDomain(like)
    }

    private void requireAcceptedAssignment(UUID recruiterId, UUID jobId) {
        if (!recruiterLikeRepository.hasAcceptedAssignment(recruiterId, jobId))
            throw new SecurityException('The recruiter has no active assignment for this job.')
    }

    private static void requireIds(UUID recruiterId, UUID jobId, UUID candidateId) {
        if (!recruiterId || !jobId || !candidateId)
            throw new IllegalArgumentException('Recruiter, job and candidate IDs are required.')
    }
}
