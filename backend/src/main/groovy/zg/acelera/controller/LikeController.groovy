package zg.acelera.controller

import zg.acelera.dto.like.LikeCreateDTO
import zg.acelera.dto.match.MatchResponseDTO
import zg.acelera.service.CompanyLikeService
import zg.acelera.service.JobLikeService
import zg.acelera.service.RecruiterJobLikeService

class LikeController {
    private final JobLikeService jobLikeService
    private final CompanyLikeService companyLikeService

    LikeController(JobLikeService jobLikeService, CompanyLikeService companyLikeService) {
        this.jobLikeService = jobLikeService
        this.companyLikeService = companyLikeService
    }

    MatchResponseDTO likeJob(LikeCreateDTO likeDTO) {
        return jobLikeService.likeJob(likeDTO.entityLikedId(), likeDTO.userId())
    }

    void unlikeJob(LikeCreateDTO likeDTO) {
        jobLikeService.unlikeJob(likeDTO.entityLikedId(), likeDTO.userId())
    }

    List<MatchResponseDTO> likeCandidate(LikeCreateDTO likeDTO) {
        return companyLikeService.likeCandidate(likeDTO.userId(), likeDTO.entityLikedId())
    }

    void unlikeCandidate(LikeCreateDTO likeDTO) {
        companyLikeService.unlikeCandidate(likeDTO.userId(), likeDTO.entityLikedId())
    }

    zg.acelera.dto.recruiter.RecruiterLikeResultDTO likeCandidateAsRecruiter(
            RecruiterJobLikeService recruiterLikeService, UUID recruiterId, UUID jobId, UUID candidateId) {
        recruiterLikeService.like(recruiterId, jobId, candidateId)
    }

    zg.acelera.dto.recruiter.RecruiterLikeResultDTO unlikeCandidateAsRecruiter(
            RecruiterJobLikeService recruiterLikeService, UUID recruiterId, UUID jobId, UUID candidateId) {
        recruiterLikeService.unlike(recruiterId, jobId, candidateId)
    }
}
