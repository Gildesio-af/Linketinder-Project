package zg.acelera.dto.recruiter

import zg.acelera.domain.RecruiterJobInvitation

import java.time.LocalDateTime

record RecruiterInvitationDTO(
        UUID id,
        UUID recruiterId,
        UUID companyId,
        UUID jobId,
        RecruiterJobInvitation.Status status,
        LocalDateTime createdAt,
        LocalDateTime respondedAt,
        LocalDateTime revokedAt
) {
    static RecruiterInvitationDTO fromDomain(RecruiterJobInvitation invitation) {
        new RecruiterInvitationDTO(
                invitation.id,
                invitation.recruiterId,
                invitation.companyId,
                invitation.jobId,
                invitation.status,
                invitation.createdAt,
                invitation.respondedAt,
                invitation.revokedAt
        )
    }
}
