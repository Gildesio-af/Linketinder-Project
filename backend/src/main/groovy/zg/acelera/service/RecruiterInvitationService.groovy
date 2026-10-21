package zg.acelera.service

import zg.acelera.domain.Job
import zg.acelera.domain.RecruiterJobInvitation
import zg.acelera.dto.recruiter.RecruiterInvitationDTO
import zg.acelera.repository.job.JobRepository
import zg.acelera.repository.recruiter.RecruiterJobInvitationRepository
import zg.acelera.repository.recruiter.RecruiterRepository
import zg.acelera.utils.exception.EntityNotFoundException

class RecruiterInvitationService {
    private final RecruiterJobInvitationRepository invitationRepository
    private final JobRepository jobRepository
    private final RecruiterRepository recruiterRepository

    RecruiterInvitationService(RecruiterJobInvitationRepository invitationRepository,
                               JobRepository jobRepository,
                               RecruiterRepository recruiterRepository) {
        this.invitationRepository = invitationRepository
        this.jobRepository = jobRepository
        this.recruiterRepository = recruiterRepository
    }

    RecruiterInvitationDTO invite(UUID authenticatedCompanyId, UUID recruiterId, UUID jobId) {
        requireId(authenticatedCompanyId, 'Company')
        requireId(recruiterId, 'Recruiter')
        requireId(jobId, 'Job')
        Job job = jobRepository.findById(jobId)
        if (job.publisher?.id != authenticatedCompanyId)
            throw new SecurityException('Only the company that owns the job may send invitations.')
        recruiterRepository.findById(recruiterId)
        RecruiterInvitationDTO.fromDomain(invitationRepository.create(new RecruiterJobInvitation(
                recruiterId: recruiterId, companyId: authenticatedCompanyId, jobId: jobId)))
    }

    List<RecruiterInvitationDTO> listForRecruiter(UUID authenticatedRecruiterId) {
        requireId(authenticatedRecruiterId, 'Recruiter')
        invitationRepository.findByRecruiterId(authenticatedRecruiterId)
                .collect { RecruiterInvitationDTO.fromDomain(it) }
    }

    List<RecruiterInvitationDTO> listForCompany(UUID authenticatedCompanyId) {
        requireId(authenticatedCompanyId, 'Company')
        invitationRepository.findByCompanyId(authenticatedCompanyId)
                .collect { RecruiterInvitationDTO.fromDomain(it) }
    }

    RecruiterInvitationDTO accept(UUID authenticatedRecruiterId, UUID invitationId) {
        requireId(authenticatedRecruiterId, 'Recruiter')
        validateTransition(invitationId, authenticatedRecruiterId, RecruiterJobInvitation.Status.PENDING, true)
        RecruiterInvitationDTO.fromDomain(invitationRepository.accept(invitationId, authenticatedRecruiterId))
    }

    RecruiterInvitationDTO decline(UUID authenticatedRecruiterId, UUID invitationId) {
        requireId(authenticatedRecruiterId, 'Recruiter')
        validateTransition(invitationId, authenticatedRecruiterId, RecruiterJobInvitation.Status.PENDING, true)
        RecruiterInvitationDTO.fromDomain(invitationRepository.decline(invitationId, authenticatedRecruiterId))
    }

    RecruiterInvitationDTO cancel(UUID authenticatedCompanyId, UUID invitationId) {
        requireId(authenticatedCompanyId, 'Company')
        validateTransition(invitationId, authenticatedCompanyId, RecruiterJobInvitation.Status.PENDING, false)
        RecruiterInvitationDTO.fromDomain(invitationRepository.cancel(invitationId, authenticatedCompanyId))
    }

    RecruiterInvitationDTO revoke(UUID authenticatedCompanyId, UUID invitationId) {
        requireId(authenticatedCompanyId, 'Company')
        validateTransition(invitationId, authenticatedCompanyId, RecruiterJobInvitation.Status.ACCEPTED, false)
        RecruiterInvitationDTO.fromDomain(invitationRepository.revoke(invitationId, authenticatedCompanyId))
    }

    private void validateTransition(UUID invitationId, UUID authenticatedUserId,
                                    RecruiterJobInvitation.Status expectedStatus, boolean recruiterAction) {
        if (invitationId == null)
            throw new IllegalArgumentException('Invitation ID is required.')
        RecruiterJobInvitation invitation = invitationRepository.findById(invitationId)
        UUID ownerId = recruiterAction ? invitation.recruiterId : invitation.companyId
        if (ownerId != authenticatedUserId)
            throw new SecurityException('The authenticated user is not authorized for this invitation.')
        if (invitation.status != expectedStatus)
            throw new IllegalStateException("Invitation must be ${expectedStatus.name().toLowerCase()} to perform this action.")
    }

    private static void requireId(UUID id, String type) {
        if (id == null)
            throw new SecurityException("${type} authentication is required.")
    }
}
