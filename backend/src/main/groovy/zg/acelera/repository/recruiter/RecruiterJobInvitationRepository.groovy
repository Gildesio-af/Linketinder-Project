package zg.acelera.repository.recruiter

import zg.acelera.domain.RecruiterJobInvitation

interface RecruiterJobInvitationRepository {
    RecruiterJobInvitation create(RecruiterJobInvitation invitation)
    RecruiterJobInvitation findById(UUID invitationId)
    List<RecruiterJobInvitation> findByRecruiterId(UUID recruiterId)
    List<RecruiterJobInvitation> findByCompanyId(UUID companyId)
    RecruiterJobInvitation accept(UUID invitationId, UUID recruiterId)
    RecruiterJobInvitation decline(UUID invitationId, UUID recruiterId)
    RecruiterJobInvitation cancel(UUID invitationId, UUID companyId)
    RecruiterJobInvitation revoke(UUID invitationId, UUID companyId)
}
