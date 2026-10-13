package zg.acelera.service

import zg.acelera.dto.job.JobResponseDTO
import zg.acelera.dto.match.MatchCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.repository.like.LikeRepository

class CompanyLikeService {
    private final LikeRepository likeRepository
    private final LikeRepository jobLikeRepository
    private final JobService jobService
    private final MatchService matchService

    CompanyLikeService(LikeRepository likeRepository, LikeRepository jobLikeRepository,
                       JobService jobService, MatchService matchService) {
        this.likeRepository = likeRepository
        this.jobLikeRepository = jobLikeRepository
        this.jobService = jobService
        this.matchService = matchService
    }

    List<MatchResponseDTO> likeCandidate(UUID companyId, UUID candidateId) {
        if (!likeRepository.existsLike(candidateId, companyId)) {
            likeRepository.like(candidateId, companyId)
        }

        Set<JobResponseDTO> companyJobs = jobService.getJobsByPublisher(companyId)

        return companyJobs
                .findAll { job -> jobLikeRepository.existsLike(job.id(), candidateId) }
                .collect { job -> matchService.createMatch(new MatchCreateDTO(candidateId, companyId, job.id(), null)) }
                .findAll { it != null } as List<MatchResponseDTO>
    }

    void unlikeCandidate(UUID companyId, UUID candidateId) {
        likeRepository.unlike(candidateId, companyId)
        matchService.deleteMatchesByCandidateAndCompany(candidateId, companyId)
    }
}
