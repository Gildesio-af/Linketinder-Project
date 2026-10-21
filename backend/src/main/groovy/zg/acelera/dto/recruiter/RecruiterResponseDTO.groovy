package zg.acelera.dto.recruiter

import zg.acelera.domain.Recruiter
import zg.acelera.domain.RecruiterStatus

record RecruiterResponseDTO(
        UUID id,
        String name,
        UUID recruitmentAreaId,
        String description,
        String email,
        RecruiterStatus status
) {
    static RecruiterResponseDTO fromDomain(Recruiter recruiter) {
        new RecruiterResponseDTO(
                recruiter.id,
                recruiter.name,
                recruiter.recruitmentAreaId,
                recruiter.description,
                recruiter.email,
                recruiter.status
        )
    }
}
