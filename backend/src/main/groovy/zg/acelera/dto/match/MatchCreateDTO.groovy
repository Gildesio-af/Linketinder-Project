package zg.acelera.dto.match

import zg.acelera.domain.Match

import java.time.LocalDateTime

record MatchCreateDTO(
        UUID candidateId,
        UUID companyId,
        UUID jobId,
        LocalDateTime createdAt
) {
    public MatchCreateDTO {
        if (!candidateId) throw new IllegalArgumentException("Candidate id must be provided to create a match")
        if (!companyId) throw new IllegalArgumentException("Company id must be provided to create a match")
        if (!jobId) throw new IllegalArgumentException("Job id must be provided to create a match")
    }

    Match toDomain() {
        return new Match(
                candidateId: candidateId,
                companyId: companyId,
                jobId: jobId,
                createdAt: createdAt
        )
    }
}