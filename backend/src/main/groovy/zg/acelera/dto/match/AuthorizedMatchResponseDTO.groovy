package zg.acelera.dto.match

import zg.acelera.domain.Match
import zg.acelera.domain.Recruiter

import java.time.LocalDateTime

record AuthorizedMatchResponseDTO(
        UUID id,
        UUID candidateId,
        UUID companyId,
        UUID jobId,
        LocalDateTime createdAt,
        String candidateName,
        String candidateEmail,
        String candidateDescription,
        String companyName,
        String companyEmail,
        String companyDescription,
        UUID recruiterId,
        UUID recruitmentAreaId,
        String recruiterDescription
) {
    static AuthorizedMatchResponseDTO fromMatch(
            Match match,
            String candidateName,
            String candidateEmail,
            String candidateDescription,
            String companyName,
            String companyEmail,
            String companyDescription,
            Recruiter recruiter
    ) {
        new AuthorizedMatchResponseDTO(
                match.id,
                match.candidateId,
                match.companyId,
                match.jobId,
                match.createdAt,
                candidateName,
                candidateEmail,
                candidateDescription,
                companyName,
                companyEmail,
                companyDescription,
                recruiter?.id,
                recruiter?.recruitmentAreaId,
                recruiter?.description
        )
    }
}
