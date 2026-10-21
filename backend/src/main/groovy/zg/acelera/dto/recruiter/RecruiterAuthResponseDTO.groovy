package zg.acelera.dto.recruiter

import zg.acelera.domain.Recruiter

record RecruiterAuthResponseDTO(
        UUID id,
        String role,
        RecruiterResponseDTO recruiter
) {
    static RecruiterAuthResponseDTO fromDomain(Recruiter recruiter) {
        new RecruiterAuthResponseDTO(
                recruiter.id,
                'RECRUITER',
                RecruiterResponseDTO.fromDomain(recruiter)
        )
    }
}
