package zg.acelera.dto.recruiter

import zg.acelera.domain.RecruiterJobLike
import zg.acelera.dto.match.MatchResponseDTO

import java.time.LocalDateTime

record RecruiterLikeResultDTO(
        UUID recruiterId,
        UUID jobId,
        UUID candidateId,
        RecruiterJobLike.Status status,
        LocalDateTime removedAt,
        MatchResponseDTO match
) {
    static RecruiterLikeResultDTO fromDomain(RecruiterJobLike like, MatchResponseDTO match = null) {
        new RecruiterLikeResultDTO(
                like.recruiterId, like.jobId, like.candidateId,
                like.status, like.removedAt, match
        )
    }
}
