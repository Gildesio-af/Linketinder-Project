package zg.acelera.service

import zg.acelera.dto.company.CompanyResponseDTO
import zg.acelera.dto.match.MatchCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.repository.like.LikeRepository
import zg.acelera.repository.recruiter.RecruiterJobLikeRepository

class JobLikeService {
    private final LikeRepository likeRepository
    private final LikeRepository companyLikeRepository
    private final CompanyService companyService
    private final MatchService matchService
    private final RecruiterJobLikeRepository recruiterLikeRepository

    JobLikeService(LikeRepository likeRepository, LikeRepository companyLikeRepository,
                   CompanyService companyService, MatchService matchService) {
        this(likeRepository, companyLikeRepository, companyService, matchService, null)
    }

    JobLikeService(LikeRepository likeRepository, LikeRepository companyLikeRepository,
                   CompanyService companyService, MatchService matchService,
                   RecruiterJobLikeRepository recruiterLikeRepository) {
        this.likeRepository = likeRepository
        this.companyLikeRepository = companyLikeRepository
        this.companyService = companyService
        this.matchService = matchService
        this.recruiterLikeRepository = recruiterLikeRepository
    }

    MatchResponseDTO likeJob(UUID jobId, UUID candidateId) {
        if (!likeRepository.existsLike(jobId, candidateId)) {
            likeRepository.like(jobId, candidateId)
        }

        CompanyResponseDTO publisher = companyService.getCompanyByJobId(jobId)
        boolean recruiterLikedCandidate = recruiterLikeRepository?.hasActiveLike(jobId, candidateId) ?: false
        boolean companyLikedCandidate = recruiterLikeRepository == null &&
                companyLikeRepository.existsLike(candidateId, publisher.id())
        if (!recruiterLikedCandidate && !companyLikedCandidate) return null

        return matchService.createMatch(new MatchCreateDTO(candidateId, publisher.id(), jobId, null))
    }

    void unlikeJob(UUID jobId, UUID candidateId) {
        likeRepository.unlike(jobId, candidateId)
        matchService.deleteMatchByCandidateAndJob(candidateId, jobId)
    }
}
