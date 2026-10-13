package zg.acelera.dto.match

import zg.acelera.domain.Match

import java.time.LocalDateTime

record MatchResponseDTO(
        UUID id,
        UUID candidateId,
        UUID companyId,
        UUID jobId,
        LocalDateTime createdAt
) {
    static MatchResponseDTO fromDomain(Match match) {
        return new MatchResponseDTO(
                id: match.id,
                candidateId: match.candidateId,
                companyId: match.companyId,
                jobId: match.jobId,
                createdAt: match.createdAt
        )
    }
}
