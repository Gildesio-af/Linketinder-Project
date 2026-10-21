package zg.acelera.repository.recruiter

import zg.acelera.domain.RecruiterJobLike

interface RecruiterJobLikeRepository {
    RecruiterJobLike like(UUID recruiterId, UUID jobId, UUID candidateId)
    RecruiterJobLike unlike(UUID recruiterId, UUID jobId, UUID candidateId)
    boolean existsActive(UUID recruiterId, UUID jobId, UUID candidateId)
    boolean hasActiveLike(UUID jobId, UUID candidateId)
    boolean hasAcceptedAssignment(UUID recruiterId, UUID jobId)
    UUID findCompanyIdByJobId(UUID jobId)
}
